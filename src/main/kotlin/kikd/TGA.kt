// author: Piotr Andrzejewski
package kikd

import ImageFormat
import java.io.FileInputStream
import java.io.FileOutputStream

@ExperimentalUnsignedTypes
class TGA : ImageFormat {
    override fun decode(stream: FileInputStream, fromBottom: Boolean): ImageMatrix {
        stream.use {
            it.skip(12)
            val widthBytes = stream.readNBytes(2)
            val heightBytes = stream.readNBytes(2)
            val width = byteArrayToInt(widthBytes.asUByteArray(), littleEndian = true)
            val height = byteArrayToInt(heightBytes.asUByteArray(), littleEndian = true)
            it.skip(2)

            if (fromBottom) {
                // due to TGA format, we start from bottom left pixel
                val pixels: Array<Array<Pixel>> = Array(height) { i ->
                    Array(width) { j ->
                        Pixel(i, j, Color(0u, 0u, 0u))
                    }
                }
                for (i in height - 1 downTo 0) {
                    for (j in 0 until width) {
                        pixels[i][j].color = readPixel(it)
                    }
                }
                it.skip(26)
                return ImageMatrix(height, width, transform2DArrayToList(pixels))
            } else {
                val pixels = mutableListOf<Pixel>()
                repeat(height * width) { i ->
                    val row = i / width
                    val column = i % width
                    val pixel = Pixel(row, column, readPixel(it))
                    pixels.add(pixel)
                }
                it.skip(26)
                return ImageMatrix(height, width, pixels)
            }
        }

    }


    override fun encode(stream: FileOutputStream, image: ImageMatrix) {
        stream.use {
            stream.write(generateHeader(image.width, image.height))
            image.pixels.forEach { pixel ->
                writePixel(it, pixel.color)
            }
            stream.write(generateFooter())
        }
    }

    private fun generateHeader(width: Int, height: Int): ByteArray {
        val widthBytes = intToTwoBytes(width, true)
        val heightBytes = intToTwoBytes(height, true)
        val header = ByteArray(18) { 0 }
        val descriptor: Byte = 0b00100000 // first pixel is top left
        header[2] = 2 // image type
        widthBytes.copyInto(header, 12)
        heightBytes.copyInto(header, 14)
        header[16] = 24 // pixel depth
        header[17] = descriptor
        return header
    }

    private fun generateFooter(): ByteArray {
        val footer = ByteArray(26) { 0 }
        return footer
    }

    private fun writePixel(stream: FileOutputStream, color: Color) {
        stream.write(color.b.toInt())
        stream.write(color.g.toInt())
        stream.write(color.r.toInt())
    }

    private fun readPixel(stream: FileInputStream): Color {
        // little endian (b, g, r) instead of (r, g, b)
        val blue = stream.read().toUByte()
        val green = stream.read().toUByte()
        val red = stream.read().toUByte()
        return Color(red, green, blue)
    }

    private fun <T> transform2DArrayToList(array2D: Array<Array<T>>): MutableList<T> {
        // TODO: zamienić na przechodzenie jak snake
        val list1D = mutableListOf<T>()
        for (row in array2D) {
            list1D.addAll(row)
        }
        return list1D
    }
}
