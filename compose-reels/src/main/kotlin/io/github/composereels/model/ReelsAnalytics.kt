package io.github.composereels.model

/**
 * Analytics callbacks for tracking video playback events.
 *
 * @param onVideoStart Called when a video starts playing at the given index
 * @param onVideoPaused Called when a video is paused, with the total watch time in milliseconds
 * @param onVideoCompleted Called when a video finishes playing (reaches the end)
 */
data class ReelsAnalytics(
    val onVideoStart: ((index: Int) -> Unit)? = null,
    val onVideoPaused: ((index: Int, watchTimeMs: Long) -> Unit)? = null,
    val onVideoCompleted: ((index: Int) -> Unit)? = null
)
