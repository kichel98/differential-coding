// author: Piotr Andrzejewski
import kikd.ImageMatrix
import java.io.FileInputStream
import java.io.FileOutputStream

@ExperimentalUnsignedTypes
interface ImageFormat {
    fun decode(stream: FileInputStream, fromBottom: Boolean = true): ImageMatrix
    fun encode(stream: FileOutputStream, image: ImageMatrix)
}