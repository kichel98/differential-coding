package kikd

import kotlin.math.pow

@ExperimentalUnsignedTypes
fun calculateAndPrintMSEAndSNR(before: ImageMatrix, after: ImageMatrix) {
    var redErrorSum = 0.0
    var greenErrorSum = 0.0
    var blueErrorSum = 0.0
    var allErrorSum = 0.0
    var redNoiseSum = 0.0
    var greenNoiseSum = 0.0
    var blueNoiseSum = 0.0
    var allNoiseSum = 0.0
    before.pixels.zip(after.pixels) {beforePixel, afterPixel ->
        val redError = (beforePixel.color.r.toInt() - afterPixel.color.r.toInt()).toDouble().pow(2)
        val greenError = (beforePixel.color.g.toInt() - afterPixel.color.g.toInt()).toDouble().pow(2)
        val blueError = (beforePixel.color.b.toInt() - afterPixel.color.b.toInt()).toDouble().pow(2)
        redErrorSum += redError
        greenErrorSum += greenError
        blueErrorSum += blueError
        allErrorSum += redError + greenError + blueError
        val redNoise = beforePixel.color.r.toDouble().pow(2)
        val greenNoise = beforePixel.color.g.toDouble().pow(2)
        val blueNoise = beforePixel.color.b.toDouble().pow(2)
        redNoiseSum += redNoise
        greenNoiseSum += greenNoise
        blueNoiseSum += blueNoise
        allNoiseSum += redNoise + greenNoise + blueNoise
    }
    val redMSE = redErrorSum / before.pixels.size
    val greenMSE = greenErrorSum / before.pixels.size
    val blueMSE = blueErrorSum / before.pixels.size
    val allMSE = allErrorSum / before.pixels.size
    println("mse = $allMSE")
    println("mse(r) = $redMSE")
    println("mse(g) = $greenMSE")
    println("mse(b) = $blueMSE")
    println("SNR = ${allNoiseSum / before.pixels.size / allMSE}")
    println("SNR(r) = ${redNoiseSum / before.pixels.size / redMSE}")
    println("SNR(g) = ${greenNoiseSum / before.pixels.size / greenMSE}")
    println("SNR(b) = ${blueNoiseSum / before.pixels.size / blueMSE}")
}