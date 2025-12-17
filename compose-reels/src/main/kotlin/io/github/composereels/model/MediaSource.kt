package io.github.composereels.model

/**
 * Represents a media source for ComposeReels.
 * Can be either a video or an image.
 */
sealed class MediaSource {
    /**
     * Video media source.
     *
     * @param url The URL of the video (supports MP4, HLS, DASH)
     * @param thumbnailUrl Optional thumbnail URL to show while loading
     */
    data class Video(
        val url: String,
        val thumbnailUrl: String? = null
    ) : MediaSource()

    /**
     * Image media source.
     *
     * @param url The URL of the image
     * @param contentDescription Accessibility description for the image
     */
    data class Image(
        val url: String,
        val contentDescription: String? = null
    ) : MediaSource()

    companion object {
        /**
         * Automatically detect media type from URL extension.
         * Defaults to Video if unable to determine.
         */
        fun fromUrl(url: String, thumbnailUrl: String? = null): MediaSource {
            val extension = url.substringAfterLast('.', "").lowercase()
            return when (extension) {
                "jpg", "jpeg", "png", "gif", "webp", "bmp" -> Image(url)
                else -> Video(url, thumbnailUrl)
            }
        }
    }
}
