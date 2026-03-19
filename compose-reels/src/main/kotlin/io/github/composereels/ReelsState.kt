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

private const val INFINITE_SCROLL_PAGE_COUNT = Int.MAX_VALUE / 2

/**
 * State holder for ComposeReels component.
 * Manages the current page, playback state, and zoom state.
 */
@Stable
class ReelsState internal constructor(
    val pagerState: PagerState,
    initialMuted: Boolean,
    private val itemCount: () -> Int,
    private val infiniteScroll: Boolean
) {
    internal val startPage = if (infiniteScroll) INFINITE_SCROLL_PAGE_COUNT / 2 else 0

    /**
     * Current page index (mapped to actual item index for infinite scroll).
     */
    val currentPage: Int
        get() {
            if (!infiniteScroll) return pagerState.currentPage
            val count = itemCount()
            if (count == 0) return 0
            return ((pagerState.currentPage - startPage) % count + count) % count
        }

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
     * Current playback speed multiplier (1.0f = normal speed).
     */
    var playbackSpeed: Float by mutableFloatStateOf(1f)
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
     * Update playback speed multiplier.
     */
    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = speed
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
        if (infiniteScroll) {
            pagerState.scrollToPage(startPage + page)
        } else {
            pagerState.scrollToPage(page)
        }
    }

    /**
     * Animate scroll to a specific page.
     */
    suspend fun animateScrollToPage(page: Int) {
        if (infiniteScroll) {
            pagerState.animateScrollToPage(startPage + page)
        } else {
            pagerState.animateScrollToPage(page)
        }
    }

    /**
     * Map a virtual pager page to an actual item index.
     */
    internal fun getActualIndex(page: Int): Int {
        if (!infiniteScroll) return page
        val count = itemCount()
        if (count == 0) return 0
        return ((page - startPage) % count + count) % count
    }
}

/**
 * Remember a [ReelsState] for use with [ComposeReels].
 *
 * @param initialPage The initial page to display
 * @param pageCount The total number of pages
 * @param config Configuration for the reels component
 * @param initialMuted Whether to start with audio muted
 */
@Composable
fun rememberReelsState(
    initialPage: Int = 0,
    pageCount: () -> Int,
    config: ReelsConfig = ReelsConfig(),
    initialMuted: Boolean = false
): ReelsState {
    val actualPageCount = if (config.infiniteScroll) INFINITE_SCROLL_PAGE_COUNT else pageCount()
    val startPage = if (config.infiniteScroll) INFINITE_SCROLL_PAGE_COUNT / 2 + initialPage else initialPage

    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { actualPageCount }
    )
    return remember(pagerState) {
        ReelsState(pagerState, initialMuted, pageCount, config.infiniteScroll)
    }
}
