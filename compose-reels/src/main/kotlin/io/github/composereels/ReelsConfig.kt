package io.github.composereels

/**
 * Configuration for ComposeReels component.
 *
 * @param autoPlay Whether to automatically play videos when they become visible
 * @param isZoomEnabled Whether pinch-to-zoom gesture is enabled
 * @param isMuted Whether videos should start muted
 * @param showProgressBar Whether to show the progress bar for videos
 * @param preloadCount Number of items to preload ahead of current position (in both directions)
 * @param playerPoolSize Maximum number of ExoPlayer instances in the pool.
 *        Must be at least (preloadCount * 2) + 1 to support preloading in both directions plus current page.
 * @param infiniteScroll Whether to loop back to the first item after reaching the end
 * @param longPressFastPlaybackEnabled Whether long-press to temporarily boost playback speed is enabled
 * @param longPressFastPlaybackSpeed Playback speed multiplier to use while long press is active
 */
data class ReelsConfig(
    val autoPlay: Boolean = true,
    val isZoomEnabled: Boolean = true,
    val isMuted: Boolean = false,
    val showProgressBar: Boolean = true,
    val preloadCount: Int = 2,
    val playerPoolSize: Int = 5,
    val infiniteScroll: Boolean = false,
    val longPressFastPlaybackEnabled: Boolean = true,
    val longPressFastPlaybackSpeed: Float = 2f
) {
    init {
        val minPoolSize = (preloadCount * 2) + 1
        require(playerPoolSize >= minPoolSize) {
            "playerPoolSize ($playerPoolSize) must be at least (preloadCount * 2) + 1 = $minPoolSize " +
                "to support preloading $preloadCount items in both directions plus the current page."
        }
        require(longPressFastPlaybackSpeed > 0f) {
            "longPressFastPlaybackSpeed must be greater than 0."
        }
    }

    companion object {
        /**
         * Calculate the minimum required pool size for a given preload count.
         */
        fun minPoolSizeFor(preloadCount: Int): Int = (preloadCount * 2) + 1
    }
}
