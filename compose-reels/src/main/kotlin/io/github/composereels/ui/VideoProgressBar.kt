package io.github.composereels.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.mandatorySystemGestures
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val POSITION_POLL_PLAYING_MS = 50L
private const val POSITION_POLL_PAUSED_MS = 250L

/**
 * Shorts-style progress bar: a thin line along the bottom that thickens while the
 * user drags it to seek. Horizontal drags are consumed here, so they never reach
 * the pager or the tap/long-press gesture layer underneath.
 */
@Composable
internal fun VideoProgressBar(
    player: ExoPlayer,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    var positionMs by remember(player) { mutableLongStateOf(0L) }
    var durationMs by remember(player) { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekFraction by remember { mutableFloatStateOf(0f) }

    // ExoPlayer has no position callback, so poll — faster while playing.
    LaunchedEffect(player, isPlaying) {
        while (isActive) {
            durationMs = player.duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: 0L
            positionMs = player.currentPosition
            delay(if (isPlaying) POSITION_POLL_PLAYING_MS else POSITION_POLL_PAUSED_MS)
        }
    }

    val fraction = when {
        isSeeking -> seekFraction
        durationMs > 0 -> (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
        else -> 0f
    }
    val trackHeight by animateDpAsState(if (isSeeking) 6.dp else 2.dp, label = "trackHeight")

    fun seekTo(f: Float) {
        if (durationMs <= 0) return
        val target = (f * durationMs).toLong()
        player.seekTo(target)
        positionMs = target
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            // Stay clear of the bottom system gesture zone (home / quick-switch),
            // which is taller than the nav bar inset and would swallow horizontal drags.
            .windowInsetsPadding(WindowInsets.mandatorySystemGestures.only(WindowInsetsSides.Bottom)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(visible = isSeeking, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = "${formatTime((seekFraction * durationMs).toLong())} / ${formatTime(durationMs)}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        // Touch target is taller than the visible track so it's easy to grab.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .pointerInput(player) {
                    detectTapGestures { offset ->
                        seekTo((offset.x / size.width).coerceIn(0f, 1f))
                    }
                }
                .pointerInput(player) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isSeeking = true
                            seekFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            seekTo(seekFraction)
                            isSeeking = false
                        },
                        onDragCancel = { isSeeking = false },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            seekFraction = (seekFraction + dragAmount / size.width).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
            ) {
                val radius = CornerRadius(size.height / 2)
                drawRoundRect(color = Color.White.copy(alpha = 0.3f), cornerRadius = radius)
                drawRoundRect(
                    color = Color.White,
                    size = Size(size.width * fraction, size.height),
                    cornerRadius = radius
                )
                if (isSeeking) {
                    drawCircle(
                        color = Color.White,
                        radius = size.height * 1.2f,
                        center = Offset(size.width * fraction, size.height / 2)
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
