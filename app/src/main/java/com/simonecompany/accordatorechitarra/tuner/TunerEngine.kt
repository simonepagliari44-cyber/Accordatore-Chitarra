package com.simonecompany.accordatorechitarra.tuner

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

data class TunerUiState(
    val isListening: Boolean = false,
    val hasSignal: Boolean = false,
    val frequency: Float = 0f,
    val amplitude: Float = 0f,
    val probability: Float = 0f,
    val noteName: String = "",
    val noteIndex: Int = 0,
    val octave: Int = 0,
    val cents: Float = 0f,
    val stringNumber: Int = 0
)

class TunerEngine {

    private val detector = YinPitchDetector(SAMPLE_RATE)
    private val _state = MutableStateFlow(TunerUiState())
    val state: StateFlow<TunerUiState> = _state.asStateFlow()

    private var worker: Thread? = null

    @Volatile
    private var running = false

    private val history = ArrayDeque<Float>()
    private var hits = 0
    private var misses = 0

    fun start() {
        if (running) return
        running = true
        _state.value = _state.value.copy(isListening = true)
        worker = Thread({ loop() }, "TunerAudio").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        running = false
        worker?.let { t ->
            try {
                t.join(1000)
            } catch (_: InterruptedException) {
            }
        }
        worker = null
        _state.value = _state.value.copy(isListening = false, hasSignal = false)
    }

    @SuppressLint("MissingPermission")
    private fun loop() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        var rec: AudioRecord? = null
        try {
            val minBuf = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            rec = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minBuf, FRAME_SIZE * 4)
            )
            if (rec.state != AudioRecord.STATE_INITIALIZED) {
                rec.release()
                rec = null
                return
            }
            rec.startRecording()
            val shorts = ShortArray(FRAME_SIZE)
            val floats = FloatArray(FRAME_SIZE)
            while (running && !Thread.currentThread().isInterrupted) {
                val n = rec.read(shorts, 0, FRAME_SIZE)
                if (n > 0) analyze(shorts, floats, n)
            }
        } catch (_: Exception) {
        } finally {
            try {
                rec?.stop()
            } catch (_: Exception) {
            }
            try {
                rec?.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun analyze(shorts: ShortArray, floats: FloatArray, n: Int) {
        var acc = 0.0
        for (i in 0 until n) {
            val v = shorts[i] / 32768f
            floats[i] = v
            acc += v.toDouble() * v
        }
        val rms = sqrt(acc / n)
        val level = ((20.0 * log10(rms.coerceAtLeast(1e-6)) + 60.0) / 60.0).coerceIn(0.0, 1.0).toFloat()

        val result = if (rms > NOISE_GATE) detector.detect(floats, n) else null
        val current = _state.value

        if (result != null) {
            hits++
            misses = 0
            history.addLast(result.frequency)
            while (history.size > HISTORY) history.removeFirst()
            if (hits < 2) return
            val f = median(history)
            val midiValue = Notes.midi(f)
            val ref = Notes.referenceFrequency(midiValue)
            val centsValue = Notes.cents(f, ref).toFloat()
            val nearest = GUITAR_STRINGS.minByOrNull { abs(Notes.cents(f, it.frequency)) }
            _state.value = current.copy(
                hasSignal = true,
                frequency = f,
                amplitude = level,
                probability = result.probability,
                noteName = Notes.international(Notes.nameIndex(midiValue)),
                noteIndex = Notes.nameIndex(midiValue),
                octave = Notes.octave(midiValue),
                cents = centsValue,
                stringNumber = nearest?.number ?: 0
            )
        } else {
            hits = 0
            history.clear()
            misses++
            if (misses > MISSES_BEFORE_IDLE) {
                if (current.hasSignal || current.amplitude != level) {
                    _state.value = current.copy(hasSignal = false, amplitude = level)
                }
            } else if (current.amplitude != level) {
                _state.value = current.copy(amplitude = level)
            }
        }
    }

    private fun median(values: Collection<Float>): Float {
        val sorted = values.sorted()
        return sorted[sorted.size / 2]
    }

    companion object {
        const val SAMPLE_RATE = 44100
        const val FRAME_SIZE = 4096
        const val HISTORY = 5
        const val NOISE_GATE = 0.0035f
        const val MISSES_BEFORE_IDLE = 8
    }
}
