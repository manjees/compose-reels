package io.github.composereels.ui.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.awaitPointerEvent
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration

/**
 * Modifier that detects a long press and triggers temporary fast playback callbacks
 * while the press is active.
 */
fun Modifier.longPressFastPlaybackGesture(
    enabled: Boolean = true,
    onFastPlaybackStart: () -> Unit,
    onFastPlaybackEnd: () -> Unit
): Modifier = composed {
    val viewConfiguration = LocalViewConfiguration.current
    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
    val touchSlop = viewConfiguration.touchSlop

    this.pointerInput(enabled, longPressTimeout, touchSlop) {
        if (!enabled) return@pointerInput

        awaitEachGesture {
            val down = awaitFirstDown(pass = PointerEventPass.Initial)
            val pointerId = down.id
            val downPosition = down.position
            val downUptime = down.uptimeMillis
            var fastPlaybackActive = false

            while (true) {
                val event = awaitPointerEvent(pass = PointerEventPass.Main)
                val change = event.changes.firstOrNull { it.id == pointerId }

                if (change == null) {
                    break
                }

                val elapsed = change.uptimeMillis - downUptime
                if (!fastPlaybackActive && elapsed >= longPressTimeout && change.pressed) {
                    fastPlaybackActive = true
                    onFastPlaybackStart()
                }

                if (!fastPlaybackActive) {
                    val distance = (change.position - downPosition).getDistance()
                    if (distance > touchSlop) {
                        break
                    }
                }

                if (!change.pressed || change.changedToUpIgnoreConsumed()) {
                    break
                }
            }

            if (fastPlaybackActive) {
                onFastPlaybackEnd()
            }
        }
    }
}
