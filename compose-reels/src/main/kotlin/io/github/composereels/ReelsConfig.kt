package io.github.composereels

/**
 * Configuration for ComposeReels component.
 *
 * @param autoPlay Whether to automatically play videos when they become visible
 * @param isZoomEnabled Whether pinch-to-zoom gesture is enabled
 * @param isMuted Whether videos should start muted
 * @param showProgressBar Whether to show the progress bar for videos
 * @param cacheSizeMb Maximum cache size for video in megabytes
 * @param preloadCount Number of items to preload ahead of current position
 * @param playerPoolSize Maximum number of ExoPlayer instances in the pool
 * @param infiniteScroll Whether to loop back to the first item after reaching the end
 */
data class ReelsConfig(
    val autoPlay: Boolean = true,
    val isZoomEnabled: Boolean = true,
    val isMuted: Boolean = false,
    val showProgressBar: Boolean = true,
    val cacheSizeMb: Int = 100,
    val preloadCount: Int = 2,
    val playerPoolSize: Int = 3,
    val infiniteScroll: Boolean = false
)
