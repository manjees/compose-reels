package io.github.composereels.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.github.composereels.ReelsConfig
import io.github.composereels.ReelsState
import io.github.composereels.model.MediaSource
import io.github.composereels.player.rememberReelsPlayerController
import io.github.composereels.ui.gesture.pinchToZoom
import io.github.composereels.ui.gesture.reelsTapGesture
import io.github.composereels.ui.gesture.rememberZoomState
import kotlinx.coroutines.flow.distinctUntilChanged

private const val INFINITE_SCROLL_PAGE_COUNT = Int.MAX_VALUE / 2

/**
 * Internal implementation of the reels pager.
 */
@Composable
internal fun <T> ReelsPagerImpl(
    items: List<T>,
    reelsState: ReelsState,
    config: ReelsConfig,
    mediaSource: (T) -> MediaSource,
    onPageChanged: (Int, T) -> Unit,
    onDoubleTap: ((Int, T) -> Unit)?,
    onSingleTap: ((Int, T) -> Unit)?,
    modifier: Modifier = Modifier,
    overlayContent: @Composable (BoxScope.(T) -> Unit)?
) {
    if (items.isEmpty()) return

    val context = LocalContext.current
    val playerController = rememberReelsPlayerController(context, config)

    val actualPageCount = if (config.infiniteScroll) INFINITE_SCROLL_PAGE_COUNT else items.size
    val startPage = if (config.infiniteScroll) INFINITE_SCROLL_PAGE_COUNT / 2 else 0

    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { actualPageCount }
    )

    fun getActualIndex(page: Int): Int {
        return if (config.infiniteScroll) {
            ((page - startPage) % items.size + items.size) % items.size
        } else {
            page
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val actualIndex = getActualIndex(page)
                if (actualIndex in items.indices) {
                    onPageChanged(actualIndex, items[actualIndex])
                    playerController.releaseDistantPlayers(page, config.preloadCount)

                    if (config.autoPlay) {
                        val source = mediaSource(items[actualIndex])
                        if (source is MediaSource.Video) {
                            playerController.play(page)
                        }
                    }
                }
            }
    }

    LaunchedEffect(reelsState.isMuted) {
        playerController.setMuted(reelsState.isMuted)
    }

    LaunchedEffect(reelsState.isPlaying, pagerState.currentPage) {
        val currentPage = pagerState.currentPage
        if (reelsState.isPlaying) {
            playerController.play(currentPage)
        } else {
            playerController.pause(currentPage)
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = true,
        beyondViewportPageCount = config.preloadCount
    ) { page ->
        val actualIndex = getActualIndex(page)
        val item = items[actualIndex]
        val source = mediaSource(item)
        val zoomState = rememberZoomState()
        val isCurrentPage = page == pagerState.currentPage

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Media content with zoom and tap gestures
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .reelsTapGesture(
                        enabled = onDoubleTap != null || onSingleTap != null,
                        onDoubleTap = onDoubleTap?.let { callback -> { _ -> callback(actualIndex, item) } },
                        onSingleTap = onSingleTap?.let { callback -> { callback(actualIndex, item) } }
                    )
                    .then(
                        if (config.isZoomEnabled) {
                            Modifier.pinchToZoom(
                                enabled = true,
                                zoomState = zoomState,
                                onZoomChanged = { scale ->
                                    reelsState.zoomScale = scale
                                }
                            )
                        } else {
                            Modifier
                        }
                    )
            ) {
                when (source) {
                    is MediaSource.Video -> {
                        val player = playerController.getPlayer(page, source.url)
                        VideoPlayer(
                            player = player,
                            isPlaying = isCurrentPage && reelsState.isPlaying,
                            isMuted = reelsState.isMuted,
                            thumbnailUrl = source.thumbnailUrl
                        )
                    }

                    is MediaSource.Image -> {
                        ImageViewer(
                            imageUrl = source.url,
                            contentDescription = source.contentDescription
                        )
                    }
                }
            }

            // Playback controls (play button, mute button) - only for videos
            if (source is MediaSource.Video && isCurrentPage) {
                PlaybackControls(
                    isPlaying = reelsState.isPlaying,
                    isMuted = reelsState.isMuted,
                    onPlayClick = { reelsState.togglePlayPause() },
                    onMuteClick = { reelsState.toggleMute() }
                )
            }

            // User's overlay UI
            overlayContent?.invoke(this, item)
        }
    }
}
