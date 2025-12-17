package io.github.composereels

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * State holder for ComposeReels component.
 * Manages the current page, playback state, and zoom state.
 */
@Stable
class ReelsState internal constructor(
    val pagerState: PagerState,
    initialMuted: Boolean
) {
    /**
     * Current page index.
     */
    val currentPage: Int
        get() = pagerState.currentPage

    /**
     * Whether the current video is playing.
     */
    var isPlaying: Boolean by mutableStateOf(true)
        internal set

    /**
     * Whether the audio is muted.
     */
    var isMuted: Boolean by mutableStateOf(initialMuted)
        internal set

    /**
     * Current zoom scale (1.0f = original size).
     */
    var zoomScale: Float by mutableFloatStateOf(1f)
        internal set

    /**
     * Whether zoom is currently active (scale > 1.0).
     */
    val isZoomed: Boolean
        get() = zoomScale > 1f

    /**
     * Toggle play/pause state.
     */
    fun togglePlayPause() {
        isPlaying = !isPlaying
    }

    /**
     * Toggle mute state.
     */
    fun toggleMute() {
        isMuted = !isMuted
    }

    /**
     * Pause playback.
     */
    fun pause() {
        isPlaying = false
    }

    /**
     * Resume playback.
     */
    fun play() {
        isPlaying = true
    }

    /**
     * Scroll to a specific page.
     */
    suspend fun scrollToPage(page: Int) {
        pagerState.scrollToPage(page)
    }

    /**
     * Animate scroll to a specific page.
     */
    suspend fun animateScrollToPage(page: Int) {
        pagerState.animateScrollToPage(page)
    }
}

/**
 * Remember a [ReelsState] for use with [ComposeReels].
 *
 * @param initialPage The initial page to display
 * @param pageCount The total number of pages
 * @param initialMuted Whether to start with audio muted
 */
@Composable
fun rememberReelsState(
    initialPage: Int = 0,
    pageCount: () -> Int,
    initialMuted: Boolean = false
): ReelsState {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = pageCount
    )
    return remember(pagerState, initialMuted) {
        ReelsState(pagerState, initialMuted)
    }
}
