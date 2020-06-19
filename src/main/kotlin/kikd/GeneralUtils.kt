package kikd

import kotlin.math.roundToInt

fun Int.toPaddedBinString(length: Int = 8): String {
    return this.toString(2).padStart(length, '0')
}

@ExperimentalUnsignedTypes
fun UByte.toPaddedBinString(length: Int = 8): String {
    return this.toString(2).padStart(length, '0')
}

/**
 * Returns a list of lists, each built from elements of all lists with the same indexes.
 * Output has length of shortest input list.
 */
fun <T> zip(vararg lists: List<T>): List<List<T>> {
    return zip(*lists, transform = { it })
}

/**
 * Returns a list of values built from elements of all lists with same indexes using provided [transform].
 * Output has length of shortest input list.
 */
inline fun <T, V> zip(vararg lists: List<T>, transform: (List<T>) -> V): List<V> {
    val minSize = lists.map(List<T>::size).min() ?: return emptyList()
    val list = ArrayList<V>(minSize)

    val iterators = lists.map { it.iterator() }
    var i = 0
    while (i < minSize) {
        list.add(transform(iterators.map { it.next() }))
        i++
    }

    return list
}

// like linspace in Python Numpy
fun linspace(list: MutableList<Int>, start: Int, stop: Int, n: Int) {
    val step = (stop - start).toDouble() / (n - 1)
    for (i in 0..n - 2) {
        list.add((start + i * step).roundToInt())
    }
    list.add(stop)
}

@ExperimentalUnsignedTypes
fun intToTwoBytes(x: Int, littleEndian: Boolean = false): ByteArray {
    val paddedBinaryString = x.toPaddedBinString(2 * 8)
    return getBytesByString(paddedBinaryString, littleEndian)
}

@ExperimentalUnsignedTypes
fun byteArrayToInt(byteArray: UByteArray, littleEndian: Boolean = false): Int {
    val array = if (littleEndian) byteArray.reversed() else byteArray
    return array
        .joinToString(separator = "") {
            it.toPaddedBinString()
        }.toInt(2)
}

fun getBytesByString(text: String, littleEndian: Boolean = false): ByteArray {
    val splitSize = 8
    val resultByteArray = ByteArray(text.length / splitSize)
    text.chunked(splitSize).forEachIndexed { idx, chunk ->
        if (idx < resultByteArray.size) {
            resultByteArray[idx] = chunk.toInt(2).toByte()
        }
    }
    return if (littleEndian) resultByteArray.reversed().toByteArray() else resultByteArray
}