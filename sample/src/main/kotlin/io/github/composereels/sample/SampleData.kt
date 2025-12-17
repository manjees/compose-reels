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
        // Videos
        SampleReel(
            id = "1",
            mediaSource = MediaSource.Video(
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg"
            ),
            username = "nature_lover",
            description = "Big Buck Bunny - A short open movie project",
            likes = 12500
        ),
        // Image
        SampleReel(
            id = "2",
            mediaSource = MediaSource.Image(
                url = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1080",
                contentDescription = "Mountain landscape"
            ),
            username = "travel_photos",
            description = "Beautiful mountain sunrise view",
            likes = 8430
        ),
        // Video
        SampleReel(
            id = "3",
            mediaSource = MediaSource.Video(
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg"
            ),
            username = "action_reels",
            description = "For Bigger Blazes - Google Chrome ad",
            likes = 5200
        ),
        // Image
        SampleReel(
            id = "4",
            mediaSource = MediaSource.Image(
                url = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1080",
                contentDescription = "Starry night sky"
            ),
            username = "night_sky",
            description = "Milky way over the mountains",
            likes = 15600
        ),
        // Video
        SampleReel(
            id = "5",
            mediaSource = MediaSource.Video(
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerEscapes.jpg"
            ),
            username = "adventure_time",
            description = "For Bigger Escapes - Exciting chase scene",
            likes = 9870
        ),
        // Image
        SampleReel(
            id = "6",
            mediaSource = MediaSource.Image(
                url = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=1080",
                contentDescription = "Ocean waves"
            ),
            username = "ocean_vibes",
            description = "Peaceful ocean waves at sunset",
            likes = 7320
        ),
        // Video
        SampleReel(
            id = "7",
            mediaSource = MediaSource.Video(
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerFun.jpg"
            ),
            username = "fun_videos",
            description = "For Bigger Fun - Entertainment at its best",
            likes = 11200
        )
    )
}
