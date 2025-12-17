package io.github.composereels.ui.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

private const val DOUBLE_TAP_TIMEOUT_MS = 300L
private const val TAP_SLOP = 20f

/**
 * Modifier that detects single and double tap gestures without interfering with scroll.
 */
fun Modifier.reelsTapGesture(
    enabled: Boolean = true,
    onDoubleTap: ((Offset) -> Unit)? = null,
    onSingleTap: (() -> Unit)? = null
): Modifier = composed {
    val scope = rememberCoroutineScope()
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var lastTapPosition by remember { mutableStateOf(Offset.Zero) }

    this.pointerInput(enabled, onDoubleTap, onSingleTap) {
        if (!enabled) return@pointerInput

        awaitEachGesture {
            val down = awaitFirstDown(pass = PointerEventPass.Initial)
            val downTime = System.currentTimeMillis()
            val downPosition = down.position

            val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)

            if (up != null) {
                val upPosition = up.position
                val distance = (upPosition - downPosition).getDistance()

                if (distance < TAP_SLOP) {
                    val timeSinceLastTap = downTime - lastTapTime
                    val distanceFromLastTap = (downPosition - lastTapPosition).getDistance()

                    if (timeSinceLastTap < DOUBLE_TAP_TIMEOUT_MS && distanceFromLastTap < TAP_SLOP * 2) {
                        onDoubleTap?.invoke(downPosition)
                        lastTapTime = 0L
                    } else {
                        lastTapTime = downTime
                        lastTapPosition = downPosition

                        if (onSingleTap != null) {
                            scope.launch {
                                kotlinx.coroutines.delay(DOUBLE_TAP_TIMEOUT_MS)
                                if (lastTapTime == downTime) {
                                    onSingleTap()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
