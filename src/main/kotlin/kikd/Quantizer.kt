package kikd

interface Quantizer {
    // this function return index of element in list, not value
    // to get value, use getQuantizedValue
    fun quantize(value: Int): Int
    fun getQuantizedValue(index: Int): Int
    fun minValue(): Int
    fun maxValue(): Int
}