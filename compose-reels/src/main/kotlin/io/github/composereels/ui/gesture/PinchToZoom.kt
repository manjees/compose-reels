package io.github.composereels.ui.gesture

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.abs

/**
 * State holder for pinch-to-zoom gesture.
 */
class ZoomState {
    var scale by mutableFloatStateOf(1f)
        internal set

    var offset by mutableStateOf(Offset.Zero)
        internal set

    var isZooming by mutableStateOf(false)
        internal set

    val isZoomed: Boolean
        get() = scale > 1f

    fun reset() {
        scale = 1f
        offset = Offset.Zero
        isZooming = false
    }
}

@Composable
fun rememberZoomState(): ZoomState = remember { ZoomState() }

/**
 * Modifier that enables pinch-to-zoom gesture with spring animation reset.
 * Only activates with two or more fingers, allowing single-finger scroll to pass through.
 */
@Composable
fun Modifier.pinchToZoom(
    enabled: Boolean = true,
    zoomState: ZoomState = rememberZoomState(),
    minScale: Float = 1f,
    maxScale: Float = 3f,
    onZoomChanged: (Float) -> Unit = {}
): Modifier {
    val animatedScale by animateFloatAsState(
        targetValue = zoomState.scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "zoom_scale"
    )

    val animatedOffsetX by animateFloatAsState(
        targetValue = zoomState.offset.x,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "zoom_offset_x"
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = zoomState.offset.y,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "zoom_offset_y"
    )

    return this
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput

            awaitEachGesture {
                // Wait for first finger
                awaitFirstDown(requireUnconsumed = false)

                do {
                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                    val pointerCount = event.changes.count { it.pressed }

                    // Only handle multi-touch (pinch gesture)
                    if (pointerCount >= 2) {
                        val zoomChange = event.calculateZoom()
                        val centroid = event.calculateCentroid(useCurrent = true)

                        if (zoomChange != 1f) {
                            zoomState.isZooming = true
                            val newScale = (zoomState.scale * zoomChange).coerceIn(minScale, maxScale)
                            zoomState.scale = newScale
                            onZoomChanged(newScale)

                            // Consume the events to prevent scroll during pinch
                            event.changes.forEach { change ->
                                if (change.positionChanged()) {
                                    change.consume()
                                }
                            }
                        }
                    }

                    // Check if all fingers lifted
                    if (event.changes.all { !it.pressed }) {
                        if (zoomState.isZooming) {
                            // Reset zoom when fingers lifted
                            zoomState.isZooming = false
                            zoomState.scale = 1f
                            zoomState.offset = Offset.Zero
                            onZoomChanged(1f)
                        }
                    }
                } while (event.changes.any { it.pressed })
            }
        }
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
            translationX = animatedOffsetX
            translationY = animatedOffsetY
        }
}
