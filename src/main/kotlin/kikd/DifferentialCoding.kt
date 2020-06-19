// author: Piotr Andrzejewski
package kikd

import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

@ExperimentalUnsignedTypes
class DifferentialCoding(private val k: Int) {

    fun encode(
        image: ImageMatrix,
        stream: FileOutputStream,
        redQuantizer: Quantizer,
        greenQuantizer: Quantizer,
        blueQuantizer: Quantizer
    ) {
        stream.use {
            val widthBytes = intToTwoBytes(image.width)
            val heightBytes = intToTwoBytes(image.height)
            it.write(widthBytes)
            it.write(heightBytes)
            saveQuantizersBoundaries(it, redQuantizer, greenQuantizer, blueQuantizer)
            val diffIndices: List<Int> =
                calculateIndicesOfQuantizedDiffs(image.pixels, redQuantizer, greenQuantizer, blueQuantizer)
            saveDiffsIndicesOnKBytes(diffIndices, it)
        }
    }

    fun decode(stream: FileInputStream): ImageMatrix {
        stream.use {
            val width = byteArrayToInt(it.readNBytes(2).toUByteArray())
            val height = byteArrayToInt(it.readNBytes(2).toUByteArray())
            val quantizers = createQuantizersFromStream(it)
            val redQuantizer = quantizers[0]
            val greenQuantizer = quantizers[1]
            val blueQuantizer = quantizers[2]
            val diffsIndices = readDiffsIndices(it, height * width)
            return createImageFromDiffsIndices(diffsIndices, redQuantizer, greenQuantizer, blueQuantizer, width, height)
        }
    }

    private fun saveQuantizersBoundaries(
        stream: FileOutputStream,
        redQuantizer: Quantizer,
        greenQuantizer: Quantizer,
        blueQuantizer: Quantizer
    ) {
        // we save 6 * 9 bits + 2 offset = 56 bits = 7 bytes
        val boundaries = listOf(
            redQuantizer.minValue(),
            redQuantizer.maxValue(),
            greenQuantizer.minValue(),
            greenQuantizer.maxValue(),
            blueQuantizer.minValue(),
            blueQuantizer.maxValue()
        )
        var output = ""
        boundaries.forEach {
            val signBit: Char = if (it >= 0) '0' else '1'
            val value = abs(it).toPaddedBinString()
            output += "$signBit$value"
        }
        output += "00" // offset to full bytes
        val outputBytes = getBytesByString(output)
        stream.write(outputBytes)
    }

    private fun calculateIndicesOfQuantizedDiffs(
        pixels: List<Pixel>,
        redQuantizer: Quantizer,
        greenQuantizer: Quantizer,
        blueQuantizer: Quantizer
    ): List<Int> {
        val reds = calculateIndicesOfQuantizedDiffsForColor(pixels.map { it.color.r }, redQuantizer)
        val greens = calculateIndicesOfQuantizedDiffsForColor(pixels.map { it.color.g }, greenQuantizer)
        val blues = calculateIndicesOfQuantizedDiffsForColor(pixels.map { it.color.b }, blueQuantizer)
        return zip(reds, greens, blues).flatten()
    }

    private fun calculateIndicesOfQuantizedDiffsForColor(colors: List<UByte>, quantizer: Quantizer): List<Int> {
        var previousDiff = colors[0].toInt()
        var previousQuantizedValue = quantizer.getQuantizedValue(quantizer.quantize(previousDiff))
        val quantizedDiffIndices = mutableListOf(quantizer.quantize(previousDiff))
        for (i in colors.indices.drop(1)) {
            previousDiff = colors[i].toInt() - previousQuantizedValue
            quantizedDiffIndices.add(quantizer.quantize(previousDiff))
            previousQuantizedValue += quantizer.getQuantizedValue(quantizedDiffIndices[i])
        }
        return quantizedDiffIndices
    }

    private fun saveDiffsIndicesOnKBytes(diffs: List<Int>, stream: FileOutputStream) {
        val offset = (diffs.size * k) % 8
        val binaryDiffs = mutableListOf<String>()
        diffs.forEach {
            binaryDiffs.add(it.toPaddedBinString(k))
        }
        binaryDiffs.add("0".repeat(offset))
        stream.write(getBytesByString(binaryDiffs.joinToString("")))
    }

    private fun createQuantizersFromStream(stream: FileInputStream): List<Quantizer> {
        val boundaries = readQuantizerBoundaries(stream)
        val quantizers = mutableListOf<Quantizer>()
        quantizers.add(UniformQuantizer(k, boundaries[0], boundaries[1]))
        quantizers.add(UniformQuantizer(k, boundaries[2], boundaries[3]))
        quantizers.add(UniformQuantizer(k, boundaries[4], boundaries[5]))
        return quantizers
    }

    private fun readQuantizerBoundaries(stream: FileInputStream): List<Int> {
        val boundaries = mutableListOf<Int>()
        val boundariesBinaryString = stream.readNBytes(7)
            .toUByteArray()
            .joinToString("") {
                it.toPaddedBinString()
            }
        boundariesBinaryString.chunked(9).dropLast(1).forEach {
            val signBit = it.take(1).toInt(2)
            val value = it.drop(1).toInt(2)
            val boundary = if (signBit == 0) value else -value
            boundaries.add(boundary)
        }
        return boundaries
    }

    private fun readDiffsIndices(stream: FileInputStream, pixelsNumber: Int): List<Int> {
        val numberOfBytes = ceil((pixelsNumber * 3 * k).toDouble() / 8).toInt()
        val diffsBinaryString = stream.readNBytes(numberOfBytes)
            .toUByteArray()
            .joinToString("") {
                it.toPaddedBinString()
            }
        var diffsChunks = diffsBinaryString.chunked(k)
        if (diffsChunks.last().length < k) {
            diffsChunks = diffsChunks.dropLast(1)
        }
        val diffsIndices = mutableListOf<Int>()
        diffsChunks.forEach {
            diffsIndices.add(it.toInt(2))
        }
        return diffsIndices
    }

    private fun createImageFromDiffsIndices(
        diffs: List<Int>,
        redQuantizer: Quantizer,
        greenQuantizer: Quantizer,
        blueQuantizer: Quantizer,
        width: Int,
        height: Int
    ): ImageMatrix {
        val redDiffsIndices = diffs.filterIndexed { index, _ -> index % 3 == 0 }
        val greenDiffsIndices = diffs.filterIndexed { index, _ -> index % 3 == 1 }
        val blueDiffsIndices = diffs.filterIndexed { index, _ -> index % 3 == 2 }

        val redDiffs = redDiffsIndices.map { redQuantizer.getQuantizedValue(it) }
        val greenDiffs = greenDiffsIndices.map { greenQuantizer.getQuantizedValue(it) }
        val blueDiffs = blueDiffsIndices.map { blueQuantizer.getQuantizedValue(it) }

        val redValues = mutableListOf(intToColorComponent(redDiffs[0]))
        val greenValues = mutableListOf(intToColorComponent(greenDiffs[0]))
        val blueValues = mutableListOf(intToColorComponent(blueDiffs[0]))

        val pixels = mutableListOf<Pixel>()
        for (i in redDiffs.indices.drop(1)) {
            redValues.add(intToColorComponent(redDiffs[i] + redValues[i - 1].toInt()))
            greenValues.add(intToColorComponent(greenDiffs[i] + greenValues[i - 1].toInt()))
            blueValues.add(intToColorComponent(blueDiffs[i] + blueValues[i - 1].toInt()))
        }
        for (i in redValues.indices) {
            val color = Color(redValues[i], greenValues[i], blueValues[i])
            val row = i / width
            val column = i % width
            pixels.add(Pixel(row, column, color))
        }
        return ImageMatrix(height, width, pixels)
    }

    private fun intToColorComponent(x: Int): UByte = min(255, max(0, x)).toUByte()
}