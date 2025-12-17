package io.github.composereels.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import java.util.LinkedList

/**
 * Pool of ExoPlayer instances to avoid creating too many players.
 * Reuses players that are no longer in use.
 */
@OptIn(UnstableApi::class)
internal class PlayerPool(
    private val context: Context,
    private val maxSize: Int = 3
) {
    private val availablePlayers = LinkedList<ExoPlayer>()
    private val inUsePlayers = mutableSetOf<ExoPlayer>()

    /**
     * Acquire a player from the pool.
     * Creates a new one if the pool is empty and we haven't reached max size.
     */
    @Synchronized
    fun acquire(): ExoPlayer {
        val player = if (availablePlayers.isNotEmpty()) {
            availablePlayers.removeFirst()
        } else {
            createPlayer()
        }
        inUsePlayers.add(player)
        return player
    }

    /**
     * Release a player back to the pool.
     * If pool is at max capacity, releases the player completely.
     */
    @Synchronized
    fun release(player: ExoPlayer) {
        if (!inUsePlayers.remove(player)) return

        player.stop()
        player.clearMediaItems()

        if (availablePlayers.size < maxSize) {
            availablePlayers.add(player)
        } else {
            player.release()
        }
    }

    /**
     * Release all players in the pool.
     */
    @Synchronized
    fun releaseAll() {
        availablePlayers.forEach { it.release() }
        availablePlayers.clear()

        inUsePlayers.forEach { it.release() }
        inUsePlayers.clear()
    }

    private fun createPlayer(): ExoPlayer {
        return ExoPlayer.Builder(context)
            .build()
            .apply {
                playWhenReady = false
                repeatMode = ExoPlayer.REPEAT_MODE_ONE
            }
    }
}
