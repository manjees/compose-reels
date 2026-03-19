package io.github.composereels.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.PlaybackException
import io.github.composereels.ReelsConfig
import io.github.composereels.ReelsState
import io.github.composereels.model.MediaSource
import io.github.composereels.model.ReelsAnalytics
import io.github.composereels.player.rememberReelsPlayerController
import io.github.composereels.ui.gesture.pinchToZoom
import io.github.composereels.ui.gesture.reelsGestures
import io.github.composereels.ui.gesture.rememberZoomState
import kotlinx.coroutines.flow.distinctUntilChanged

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
    onError: ((Int, T, PlaybackException) -> Unit)?,
    errorContent: @Composable (BoxScope.(T, PlaybackException) -> Unit)?,
    analytics: ReelsAnalytics?,
    modifier: Modifier = Modifier,
    overlayContent: @Composable (BoxScope.(T) -> Unit)?
) {
    if (items.isEmpty()) return

    val context = LocalContext.current
    val playerController = rememberReelsPlayerController(context, config)
    val pagerState = reelsState.pagerState

    // Track settled page to avoid recomposition during scroll
    var settledPage by remember { mutableIntStateOf(pagerState.settledPage) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                settledPage = page
                // Reset playback speed when page changes
                reelsState.setPlaybackSpeed(1f)
                
                val actualIndex = reelsState.getActualIndex(page)
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

    LaunchedEffect(reelsState.isPlaying, settledPage) {
        if (reelsState.isPlaying) {
            playerController.play(settledPage)
        } else {
            playerController.pause(settledPage)
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = true,
        beyondViewportPageCount = config.preloadCount
    ) { page ->
        val actualIndex = reelsState.getActualIndex(page)
        val item = items[actualIndex]
        val source = mediaSource(item)
        val zoomState = rememberZoomState()
        val isCurrentPage = page == settledPage

        var videoError by remember(page) { mutableStateOf<PlaybackException?>(null) }
        var videoRetryKey by remember(page) { mutableIntStateOf(0) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Media content with zoom and gestures (tap, double tap, long press)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .reelsGestures(
                        enabled = isCurrentPage,
                        onDoubleTap = onDoubleTap?.let { callback -> { _ -> callback(actualIndex, item) } },
                        onSingleTap = onSingleTap?.let { callback -> { callback(actualIndex, item) } },
                        onFastPlaybackStart = {
                            if (config.longPressFastPlaybackEnabled) {
                                reelsState.setPlaybackSpeed(config.longPressFastPlaybackSpeed)
                            }
                        },
                        onFastPlaybackEnd = {
                            reelsState.setPlaybackSpeed(1f)
                        }
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
                        val player = remember(page, videoRetryKey) {
                            playerController.getPlayer(page, source.url)
                        }

                        if (player != null) {
                            VideoPlayer(
                                player = player,
                                isPlaying = isCurrentPage && reelsState.isPlaying,
                                isMuted = reelsState.isMuted,
                                playbackSpeed = if (isCurrentPage) reelsState.playbackSpeed else 1f,
                                thumbnailUrl = source.thumbnailUrl,
                                onError = { error ->
                                    videoError = error
                                    onError?.invoke(actualIndex, item, error)
                                },
                                onVideoStart = analytics?.onVideoStart?.let { cb -> { cb(actualIndex) } },
                                onVideoPaused = analytics?.onVideoPaused?.let { cb -> { watchTimeMs -> cb(actualIndex, watchTimeMs) } },
                                onVideoCompleted = analytics?.onVideoCompleted?.let { cb -> { cb(actualIndex) } }
                            )
                        }
                    }

                    is MediaSource.Image -> {
                        ImageViewer(
                            imageUrl = source.url,
                            contentDescription = source.contentDescription
                        )
                    }
                }
            }

            // Error overlay - placed above gesture layer for proper interaction
            if (source is MediaSource.Video && videoError != null) {
                if (errorContent != null) {
                    errorContent.invoke(this, item, videoError!!)
                } else {
                    ErrorView(
                        onRetry = {
                            videoError = null
                            playerController.releasePlayer(page)
                            videoRetryKey++
                        }
                    )
                }
            }

            // Playback controls (play button, mute button) - only for videos
            if (source is MediaSource.Video && isCurrentPage && videoError == null) {
                PlaybackControls(
                    isPlaying = reelsState.isPlaying,
                    isMuted = reelsState.isMuted,
                    onPlayClick = { reelsState.togglePlayPause() },
                    onMuteClick = { reelsState.toggleMute() }
                )
            }

            // User's overlay UI
            overlayContent?.invoke(this, item)

            // Fast playback speed indicator (TikTok style)
            if (source is MediaSource.Video && isCurrentPage) {
                AnimatedVisibility(
                    visible = reelsState.playbackSpeed > 1f,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FastForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${reelsState.playbackSpeed.toInt()}x Speed",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
