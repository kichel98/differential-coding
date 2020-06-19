// author: Piotr Andrzejewski
package kikd


@ExperimentalUnsignedTypes
class Pixel(val row: Int, val column: Int, var color: Color)

@ExperimentalUnsignedTypes
class ImageMatrix(val height: Int, val width: Int, val pixels: MutableList<Pixel>) {

    fun minComponentDiff(differences: List<Int>): Int {
        return differences.min()!!
    }

    fun maxComponentDiff(differences: List<Int>): Int {
        return differences.max()!!
    }

    fun getColorDifferencesList(colorType: ColorType): List<Int> {
        val colorProperty = when (colorType) {
            ColorType.RED -> Color::r
            ColorType.GREEN -> Color::g
            ColorType.BLUE -> Color::b
        }
        val colorList = pixels.map { colorProperty.get(it.color) }

        val diff = colorList.indices
            .drop(1)
            .map {
                colorList[it].toInt() - colorList[it - 1].toInt()
            }.toMutableList()
        diff.add(0, colorList[0].toInt())
        return diff
    }
}