package com.example.guitartuner.tuner

import kotlin.math.ceil
import kotlin.math.floor

class YinPitchDetector(
    private val sampleRate: Int,
    private val threshold: Float = 0.12f,
    minFrequency: Double = 65.0,
    maxFrequency: Double = 1320.0
) {
    private val tauMin = floor(sampleRate / maxFrequency).toInt().coerceAtLeast(2)
    private val tauMax = ceil(sampleRate / minFrequency).toInt()

    fun detect(buffer: FloatArray, size: Int): PitchResult? {
        val maxTau = minOf(tauMax, size - 1)
        if (maxTau <= tauMin) return null

        val diff = FloatArray(maxTau + 1)
        for (tau in 1..maxTau) {
            var sum = 0f
            val limit = size - tau
            for (i in 0 until limit) {
                val d = buffer[i] - buffer[i + tau]
                sum += d * d
            }
            diff[tau] = sum
        }

        val cmnd = FloatArray(maxTau + 1)
        cmnd[0] = 1f
        var running = 0f
        for (tau in 1..maxTau) {
            running += diff[tau]
            cmnd[tau] = if (running <= 0f) 1f else diff[tau] * tau / running
        }

        var tau = tauMin
        while (tau <= maxTau && cmnd[tau] >= threshold) tau++
        if (tau > maxTau) return null
        while (tau + 1 <= maxTau && cmnd[tau + 1] < cmnd[tau]) tau++

        val refined = interpolate(cmnd, tau)
        val frequency = (sampleRate / refined).toFloat()
        if (frequency < 60f || frequency > 1500f) return null
        return PitchResult(frequency, (1f - cmnd[tau]).coerceIn(0f, 1f))
    }

    private fun interpolate(buf: FloatArray, tau: Int): Double {
        if (tau < 1 || tau + 1 >= buf.size) return tau.toDouble()
        val s0 = buf[tau - 1].toDouble()
        val s1 = buf[tau].toDouble()
        val s2 = buf[tau + 1].toDouble()
        val denom = s0 - 2.0 * s1 + s2
        if (denom == 0.0) return tau.toDouble()
        return tau + (s0 - s2) / (2.0 * denom)
    }
}
