package com.simonecompany.accordatorechitarra.tuner

import kotlin.math.log2
import kotlin.math.roundToInt

data class PitchResult(val frequency: Float, val probability: Float)

data class GuitarString(val number: Int, val italian: String, val frequency: Double) {
    private val midiValue: Double = Notes.midi(frequency)
    val international: String = Notes.international(Notes.nameIndex(midiValue))
    val octave: Int = Notes.octave(midiValue)
}

val GUITAR_STRINGS = listOf(
    GuitarString(1, "Mi", 329.63),
    GuitarString(2, "Si", 246.94),
    GuitarString(3, "Sol", 196.00),
    GuitarString(4, "Re", 146.83),
    GuitarString(5, "La", 110.00),
    GuitarString(6, "Mi", 82.41)
)

object Notes {
    val SHARP = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val ITALIAN = listOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")

    fun midi(frequency: Double): Double = 69.0 + 12.0 * log2(frequency / 440.0)

    fun midi(frequency: Float): Double = midi(frequency.toDouble())

    fun nameIndex(midiValue: Double): Int {
        val m = midiValue.roundToInt()
        return ((m % 12) + 12) % 12
    }

    fun international(index: Int): String = SHARP[index]

    fun italian(index: Int): String = ITALIAN[index]

    fun octave(midiValue: Double): Int {
        val m = midiValue.roundToInt()
        return m / 12 - 1
    }

    fun referenceFrequency(midiValue: Double): Double =
        440.0 * Math.pow(2.0, (midiValue.roundToInt() - 69) / 12.0)

    fun cents(frequency: Float, reference: Double): Double =
        1200.0 * log2(frequency / reference)
}
