package com.example.guitartuner.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.guitartuner.R
import com.example.guitartuner.tuner.GUITAR_STRINGS
import com.example.guitartuner.tuner.GuitarString
import com.example.guitartuner.tuner.Notes
import com.example.guitartuner.tuner.TunerEngine
import com.example.guitartuner.tuner.TunerUiState
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class AppMode(@StringRes val title: Int) {
    Tuner(R.string.mode_tuner),
    Detector(R.string.mode_detector)
}

private val Green = Color(0xFF43A047)
private val Orange = Color(0xFFFB8C00)
private val Red = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunerApp(engine: TunerEngine) {
    val state by engine.state.collectAsStateWithLifecycle()
    var mode by rememberSaveable { mutableStateOf(AppMode.Tuner) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(mode.title), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        mode = if (mode == AppMode.Tuner) AppMode.Detector else AppMode.Tuner
                    }) {
                        Icon(
                            imageVector = if (mode == AppMode.Tuner) Icons.Filled.GraphicEq else Icons.Filled.Tune,
                            contentDescription = stringResource(
                                if (mode == AppMode.Tuner) R.string.switch_to_detector else R.string.switch_to_tuner
                            )
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (mode == AppMode.Tuner) TunerScreen(state) else DetectorScreen(state)
        }
    }
}

@Composable
private fun noteNameOf(index: Int): String =
    LocalContext.current.resources.getStringArray(R.array.note_names)[index]

@Composable
private fun TunerScreen(state: TunerUiState) {
    var manual by rememberSaveable { mutableIntStateOf(0) }
    val autoString = GUITAR_STRINGS.firstOrNull { it.number == state.stringNumber } ?: GUITAR_STRINGS.first()
    val target = if (manual != 0) GUITAR_STRINGS.first { it.number == manual } else autoString
    val cents = if (state.hasSignal) Notes.cents(state.frequency, target.frequency).toFloat() else 0f
    val inTune = state.hasSignal && abs(cents) <= 5f

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.strings_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.weight(1f))
            if (manual != 0) TextButton(onClick = { manual = 0 }) { Text(stringResource(R.string.auto)) }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            GUITAR_STRINGS.forEach { s ->
                StringChip(
                    string = s,
                    noteLabel = noteNameOf(Notes.nameIndex(Notes.midi(s.frequency))),
                    selected = target.number == s.number,
                    onClick = { manual = if (manual == s.number) 0 else s.number }
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (state.hasSignal) {
            Text(
                text = noteNameOf(state.noteIndex),
                fontSize = 88.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 96.sp
            )
            Text(
                text = "${state.noteName}${state.octave} · ${"%.1f".format(state.frequency)} Hz",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.play_a_string),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(Modifier.height(12.dp))
        CentsGauge(
            active = state.hasSignal,
            cents = cents,
            inTune = inTune,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            needleColor = when {
                !state.hasSignal -> MaterialTheme.colorScheme.outline
                inTune -> Green
                else -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.fillMaxWidth().height(170.dp)
        )
        Spacer(Modifier.height(8.dp))
        val statusColor = when {
            !state.hasSignal -> MaterialTheme.colorScheme.outline
            inTune -> Green
            cents < 0 -> Orange
            else -> Red
        }
        val statusText = when {
            !state.hasSignal -> stringResource(R.string.waiting_signal)
            inTune -> stringResource(R.string.in_tune)
            cents < 0 -> stringResource(R.string.too_low)
            else -> stringResource(R.string.too_high)
        }
        Text(
            text = statusText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = statusColor,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.cents_value, cents.toInt()),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(
                R.string.string_info,
                target.number,
                noteNameOf(Notes.nameIndex(Notes.midi(target.frequency))),
                target.international,
                target.octave,
                target.frequency
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun StringChip(string: GuitarString, noteLabel: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "${string.number}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = noteLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CentsGauge(
    active: Boolean,
    cents: Float,
    inTune: Boolean,
    trackColor: Color,
    needleColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = 18.dp.toPx()
        val cy = h - 14.dp.toPx()
        val cx = w / 2f
        val r = minOf(w / 2f - stroke, cy) - 4.dp.toPx()

        drawArc(
            color = trackColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        val zoneSweep = 36f
        drawArc(
            color = Green.copy(alpha = 0.85f),
            startAngle = 270f - zoneSweep / 2f,
            sweepAngle = zoneSweep,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )

        val t = ((cents + 50f) / 100f).coerceIn(0f, 1f)
        val angleDeg = if (active) 180f + t * 180f else 270f
        val rad = Math.toRadians(angleDeg.toDouble())
        val tipLen = r - stroke * 1.7f
        val tip = Offset(cx + (tipLen * cos(rad)).toFloat(), cy + (tipLen * sin(rad)).toFloat())
        drawLine(
            color = needleColor,
            start = Offset(cx, cy),
            end = tip,
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(color = needleColor, radius = 7.dp.toPx(), center = Offset(cx, cy))
    }
}

@Composable
private fun DetectorScreen(state: TunerUiState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        if (!state.hasSignal) {
            Text(
                text = stringResource(R.string.play_any_note),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            Text(
                text = noteNameOf(state.noteIndex),
                fontSize = 110.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 118.sp
            )
            Text(
                text = "${state.noteName}${state.octave}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "%.1f Hz".format(state.frequency),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "%+.0f ¢".format(state.cents),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.weight(1f))
        LinearProgressIndicator(
            progress = { state.amplitude },
            modifier = Modifier.fillMaxWidth().height(10.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.signal_level),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
    }
}
