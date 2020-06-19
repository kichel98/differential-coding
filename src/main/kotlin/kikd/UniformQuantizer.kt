package kikd

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class UniformQuantizer(bits: Int, minValue: Int, maxValue: Int) : Quantizer {
    private val outputValues = mutableListOf<Int>()

    init {
        initOutputValues(bits, minValue, maxValue)
    }

    override fun quantize(value: Int): Int {
        var insertionPoint = outputValues.binarySearch(value)
        if (insertionPoint >= 0) {
            return insertionPoint
        } else {
            insertionPoint = -insertionPoint - 1
            val leftIndex = max(0, insertionPoint - 1)
            val rightIndex = min(0, outputValues.size - 1)

            val distanceToLeft = abs(outputValues[leftIndex] - value)
            val distanceToRight = abs(outputValues[rightIndex] - value)
            return if (distanceToLeft < distanceToRight) {
                leftIndex
            } else {
                rightIndex
            }
        }
    }

    override fun getQuantizedValue(index: Int) = outputValues[index]

    override fun minValue(): Int = outputValues.first()

    override fun maxValue(): Int = outputValues.last()

    private fun initOutputValues(bits: Int, minValue: Int, maxValue: Int) {
        val numberOfValues = 2.toDouble().pow(bits).toInt()
        linspace(outputValues, minValue, maxValue, numberOfValues)
    }

}