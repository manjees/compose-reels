package io.github.composereels

/**
 * Configuration for ComposeReels component.
 *
 * @param autoPlay Whether to automatically play videos when they become visible
 * @param isZoomEnabled Whether pinch-to-zoom gesture is enabled
 * @param isMuted Whether videos should start muted
 * @param showProgressBar Whether to show the progress bar for videos
 * @param cacheSizeMb Maximum cache size for video in megabytes
 * @param preloadCount Number of items to preload ahead of current position (in both directions)
 * @param playerPoolSize Maximum number of ExoPlayer instances in the pool.
 *        Must be at least (preloadCount * 2) + 1 to support preloading in both directions plus current page.
 * @param infiniteScroll Whether to loop back to the first item after reaching the end
 */
data class ReelsConfig(
    val autoPlay: Boolean = true,
    val isZoomEnabled: Boolean = true,
    val isMuted: Boolean = false,
    val showProgressBar: Boolean = true,
    val cacheSizeMb: Int = 100,
    val preloadCount: Int = 2,
    val playerPoolSize: Int = 5,
    val infiniteScroll: Boolean = false
) {
    init {
        val minPoolSize = (preloadCount * 2) + 1
        require(playerPoolSize >= minPoolSize) {
            "playerPoolSize ($playerPoolSize) must be at least (preloadCount * 2) + 1 = $minPoolSize " +
                "to support preloading $preloadCount items in both directions plus the current page."
        }
    }

    companion object {
        /**
         * Calculate the minimum required pool size for a given preload count.
         */
        fun minPoolSizeFor(preloadCount: Int): Int = (preloadCount * 2) + 1
    }
}
