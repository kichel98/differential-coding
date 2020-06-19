// author: Piotr Andrzejewski
package kikd

import ImageFormat
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import java.io.File

class Lista6 : CliktCommand() {
    override fun run() = Unit
}

@ExperimentalUnsignedTypes
class Encode : CliktCommand(help = "Encode TGA using differential coding") {
    val k: Int by argument(help = "Number of bits of quantizer").int().restrictTo(1, 7)
    val inputTgaPath: String by argument(name = "input", help = "Filepath of input TGA")
    val outputEncodedPath: String by argument(name = "output", help = "Filepath of output encoded file")
    override fun run() {
        val inputStream = File(inputTgaPath).inputStream()
        val outputStream = File(outputEncodedPath).outputStream()
        val tga: ImageFormat = TGA()
        val image: ImageMatrix = tga.decode(inputStream)
        var componentDiffs = image.getColorDifferencesList(ColorType.RED)
        val redQuantizer: Quantizer = UniformQuantizer(
            k,
            image.minComponentDiff(componentDiffs),
            image.maxComponentDiff(componentDiffs)
        )
        componentDiffs = image.getColorDifferencesList(ColorType.GREEN)
        val greenQuantizer: Quantizer = UniformQuantizer(
            k,
            image.minComponentDiff(componentDiffs),
            image.maxComponentDiff(componentDiffs)
        )
        componentDiffs = image.getColorDifferencesList(ColorType.BLUE)
        val blueQuantizer: Quantizer = UniformQuantizer(
            k,
            image.minComponentDiff(componentDiffs),
            image.maxComponentDiff(componentDiffs)
        )
        val coding = DifferentialCoding(k)
        coding.encode(image, outputStream, redQuantizer, greenQuantizer, blueQuantizer)
    }
}

@ExperimentalUnsignedTypes
class Decode : CliktCommand(help = "Decode file into TGA") {
    val k: Int by argument(help = "Number of bits of quantizer").int().restrictTo(1, 7)
    val inputEncodedPath: String by argument(name = "input", help = "Filepath of input encoded file")
    val outputTgaPath: String by argument(name = "output", help = "Filepath of output TGA")
    override fun run() {
        val inputStream = File(inputEncodedPath).inputStream()
        val outputStream = File(outputTgaPath).outputStream()
        val coding = DifferentialCoding(k)
        val image = coding.decode(inputStream)
        val tga: ImageFormat = TGA()
        tga.encode(outputStream, image)

    }
}

@ExperimentalUnsignedTypes
class Stats : CliktCommand(help = "Calculate stats") {
    val inputTgaPath: String by argument(name = "input", help = "Filepath of input TGA")
    val outputTgaPath: String by argument(name = "output", help = "Filepath of output TGA")
    override fun run() {
        val tga: ImageFormat = TGA()
        val beforeImage = tga.decode(File(inputTgaPath).inputStream())
        val afterImage = tga.decode(File(outputTgaPath).inputStream())
        calculateAndPrintMSEAndSNR(beforeImage, afterImage)

    }
}

@ExperimentalUnsignedTypes
fun main(args: Array<String>) = Lista6()
    .subcommands(Encode(), Decode(), Stats())
    .main(args)

