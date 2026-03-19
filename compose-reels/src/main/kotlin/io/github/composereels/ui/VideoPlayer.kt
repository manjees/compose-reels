package io.github.composereels.ui

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

/**
 * Video player composable that displays video using ExoPlayer.
 */
@Composable
internal fun VideoPlayer(
    player: ExoPlayer,
    isPlaying: Boolean,
    isMuted: Boolean,
    playbackSpeed: Float = 1f,
    thumbnailUrl: String?,
    modifier: Modifier = Modifier,
    onError: ((PlaybackException) -> Unit)? = null,
    onVideoStart: (() -> Unit)? = null,
    onVideoPaused: ((watchTimeMs: Long) -> Unit)? = null,
    onVideoCompleted: (() -> Unit)? = null
) {
    var isBuffering by remember { mutableStateOf(true) }
    val currentOnError by rememberUpdatedState(onError)
    val currentOnVideoStart by rememberUpdatedState(onVideoStart)
    val currentOnVideoPaused by rememberUpdatedState(onVideoPaused)
    val currentOnVideoCompleted by rememberUpdatedState(onVideoCompleted)

    // Listen to player state
    DisposableEffect(player) {
        isBuffering = true
        var hasStarted = false
        var watchStartTimeMs = 0L

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_ENDED) {
                    currentOnVideoCompleted?.invoke()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    watchStartTimeMs = System.currentTimeMillis()
                    if (!hasStarted) {
                        hasStarted = true
                        currentOnVideoStart?.invoke()
                    }
                } else if (hasStarted && watchStartTimeMs > 0) {
                    val watchTimeMs = System.currentTimeMillis() - watchStartTimeMs
                    currentOnVideoPaused?.invoke(watchTimeMs)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                currentOnError?.invoke(error)
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }

    // Control playback
    LaunchedEffect(isPlaying) {
        player.playWhenReady = isPlaying
    }

    // Control volume
    LaunchedEffect(isMuted) {
        player.volume = if (isMuted) 0f else 1f
    }

    // Control playback speed
    LaunchedEffect(playbackSpeed) {
        player.setPlaybackSpeed(playbackSpeed)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // ExoPlayer view
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = player
            },
            modifier = Modifier.fillMaxSize()
        )

        // Thumbnail while buffering
        if (isBuffering && thumbnailUrl != null) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}
