package io.github.composereels.ui.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val DOUBLE_TAP_TIMEOUT_MS = 300L

/**
 * Integrated gesture modifier for ComposeReels.
 * Handles single tap, double tap, and long press for fast playback in a single pointer input
 * to ensure maximum stability and prevent gesture conflicts.
 */
fun Modifier.reelsGestures(
    enabled: Boolean = true,
    onDoubleTap: ((Offset) -> Unit)? = null,
    onSingleTap: (() -> Unit)? = null,
    onFastPlaybackStart: () -> Unit = {},
    onFastPlaybackEnd: () -> Unit = {}
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val viewConfiguration = LocalViewConfiguration.current
    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
    val touchSlop = viewConfiguration.touchSlop

    // Track state to manage interaction between tap and long press
    val gestureState = remember { object {
        var lastTapTime: Long = 0L
        var lastTapPosition: Offset = Offset.Zero
        var singleTapJob: Job? = null
    } }

    this.pointerInput(enabled, onDoubleTap, onSingleTap) {
        if (!enabled) return@pointerInput

        awaitEachGesture {
            val down = awaitFirstDown(pass = PointerEventPass.Main)
            val downTime = System.currentTimeMillis()
            val downPosition = down.position
            val pointerId = down.id
            var isLongPressActive = false

            // 1. Wait for long press or up/move
            val longPressResult = withTimeoutOrNull(longPressTimeout) {
                while (true) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Main)
                    val change = event.changes.firstOrNull { it.id == pointerId }

                    if (change == null || !change.pressed || change.isConsumed) {
                        return@withTimeoutOrNull false // Cancelled
                    }

                    val distance = (change.position - downPosition).getDistance()
                    if (distance > touchSlop) {
                        return@withTimeoutOrNull false // Moved too much
                    }
                }
            }

            // longPressResult is null if timeout reached (Long press triggered)
            if (longPressResult == null) {
                isLongPressActive = true
                gestureState.singleTapJob?.cancel() // Cancel pending single tap
                onFastPlaybackStart()

                // Keep tracking until lift
                while (true) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Main)
                    val change = event.changes.firstOrNull { it.id == pointerId }
                    if (change == null || !change.pressed || change.changedToUpIgnoreConsumed()) {
                        break
                    }
                }
                onFastPlaybackEnd()
            } else {
                // It was a short press (Tap or Double Tap)
                // Wait for up event if not already up
                val up = if (currentEvent.changes.any { it.id == pointerId && !it.pressed }) {
                    currentEvent.changes.first { it.id == pointerId }
                } else {
                    var upEvent = withTimeoutOrNull(100) { // Small timeout for safety
                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Main)
                            val change = event.changes.firstOrNull { it.id == pointerId }
                            if (change != null && !change.pressed) return@withTimeoutOrNull change
                            if (change == null || change.isConsumed) return@withTimeoutOrNull null
                        }
                    }
                    upEvent
                }

                if (up != null) {
                    val timeSinceLastTap = downTime - gestureState.lastTapTime
                    val distanceFromLastTap = (downPosition - gestureState.lastTapPosition).getDistance()

                    if (timeSinceLastTap < DOUBLE_TAP_TIMEOUT_MS && distanceFromLastTap < touchSlop * 2) {
                        // Double Tap
                        gestureState.singleTapJob?.cancel()
                        onDoubleTap?.invoke(downPosition)
                        gestureState.lastTapTime = 0L
                    } else {
                        // Potential Single Tap
                        gestureState.lastTapTime = downTime
                        gestureState.lastTapPosition = downPosition

                        if (onSingleTap != null) {
                            gestureState.singleTapJob = scope.launch {
                                delay(DOUBLE_TAP_TIMEOUT_MS)
                                onSingleTap()
                            }
                        }
                    }
                }
            }
        }
    }
}
