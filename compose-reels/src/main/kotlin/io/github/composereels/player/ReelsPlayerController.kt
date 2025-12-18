package io.github.composereels.player

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import io.github.composereels.ReelsConfig

/**
 * Controller for managing ExoPlayer instances in ComposeReels.
 */
internal class ReelsPlayerController(
    context: Context,
    config: ReelsConfig
) {
    private val playerPool = PlayerPool(context, config.playerPoolSize)
    private val activePlayersMap = mutableMapOf<Int, ExoPlayer>()

    /**
     * Get or create a player for the given page index.
     * Returns null if no player is available from the pool.
     */
    fun getPlayer(pageIndex: Int, videoUrl: String): ExoPlayer? {
        activePlayersMap[pageIndex]?.let { return it }

        val player = playerPool.acquire() ?: return null
        player.setMediaItem(MediaItem.fromUri(videoUrl))
        player.prepare()
        activePlayersMap[pageIndex] = player
        return player
    }

    /**
     * Play the video at the given page index.
     */
    fun play(pageIndex: Int) {
        activePlayersMap[pageIndex]?.playWhenReady = true
    }

    /**
     * Pause the video at the given page index.
     */
    fun pause(pageIndex: Int) {
        activePlayersMap[pageIndex]?.playWhenReady = false
    }

    /**
     * Pause all players.
     */
    fun pauseAll() {
        activePlayersMap.values.forEach { it.playWhenReady = false }
    }

    /**
     * Set mute state for all players.
     */
    fun setMuted(muted: Boolean) {
        val volume = if (muted) 0f else 1f
        activePlayersMap.values.forEach { it.volume = volume }
    }

    /**
     * Release player for the given page index.
     */
    fun releasePlayer(pageIndex: Int) {
        activePlayersMap.remove(pageIndex)?.let { player ->
            playerPool.release(player)
        }
    }

    /**
     * Release players that are far from the current page.
     */
    fun releaseDistantPlayers(currentPage: Int, keepRange: Int = 1) {
        val pagesToRemove = activePlayersMap.keys.filter { page ->
            kotlin.math.abs(page - currentPage) > keepRange
        }
        pagesToRemove.forEach { releasePlayer(it) }
    }

    /**
     * Release all players and clean up.
     */
    fun release() {
        activePlayersMap.clear()
        playerPool.releaseAll()
    }
}

/**
 * Remember a [ReelsPlayerController] that is lifecycle-aware.
 */
@Composable
internal fun rememberReelsPlayerController(
    context: Context,
    config: ReelsConfig
): ReelsPlayerController {
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember(context, config) {
        ReelsPlayerController(context, config)
    }

    DisposableEffect(lifecycleOwner, controller) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> controller.pauseAll()
                Lifecycle.Event.ON_DESTROY -> controller.release()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.release()
        }
    }

    return controller
}
