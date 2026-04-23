package io.github.composereels.sample

import io.github.composereels.model.MediaSource

/**
 * Sample reel item for demonstration.
 */
data class SampleReel(
    val id: String,
    val mediaSource: MediaSource,
    val username: String,
    val description: String,
    val likes: Int,
    val isLiked: Boolean = false
)

/**
 * Sample data with public domain videos and images.
 */
object SampleData {
    val reels = listOf(
        // Videos (bundled in sample/src/main/assets)
        SampleReel(
            id = "1",
            mediaSource = MediaSource.Video(
                url = "asset:///sample_1.mp4"
            ),
            username = "nature_lover",
            description = "Bundled sample clip #1",
            likes = 12500
        ),
        // Image
        SampleReel(
            id = "2",
            mediaSource = MediaSource.Image(
                url = "file:///android_asset/image_sample_1.jpg",
                contentDescription = "Lake with clouds and greenery"
            ),
            username = "travel_photos",
            description = "Peaceful lake reflections",
            likes = 8430
        ),
        // Video
        SampleReel(
            id = "3",
            mediaSource = MediaSource.Video(
                url = "asset:///sample_2.mp4"
            ),
            username = "action_reels",
            description = "Bundled sample clip #2",
            likes = 5200
        ),
        // Image
        SampleReel(
            id = "4",
            mediaSource = MediaSource.Image(
                url = "file:///android_asset/image_sample_2.jpg",
                contentDescription = "Rocky pier by the sea"
            ),
            username = "night_sky",
            description = "Rocky outcrop over calm water",
            likes = 15600
        ),
        // Video
        SampleReel(
            id = "5",
            mediaSource = MediaSource.Video(
                url = "asset:///sample_3.mp4"
            ),
            username = "adventure_time",
            description = "Bundled sample clip #3",
            likes = 9870
        ),
        // Image
        SampleReel(
            id = "6",
            mediaSource = MediaSource.Image(
                url = "file:///android_asset/image_sample_3.jpg",
                contentDescription = "Green field under a cloudy sky"
            ),
            username = "ocean_vibes",
            description = "Open meadow on a bright day",
            likes = 7320
        ),
        // Video
        SampleReel(
            id = "7",
            mediaSource = MediaSource.Video(
                url = "asset:///sample_4.mp4"
            ),
            username = "fun_videos",
            description = "Bundled sample clip #4",
            likes = 11200
        ),
        // Image
        SampleReel(
            id = "8",
            mediaSource = MediaSource.Image(
                url = "file:///android_asset/image_sample_4.jpg",
                contentDescription = "Mountains seen through leaves"
            ),
            username = "mountain_view",
            description = "Distant ridges through the foliage",
            likes = 6100
        )
    )
}
