package io.github.composereels

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.composereels.model.MediaSource
import io.github.composereels.ui.ReelsPagerImpl

/**
 * ComposeReels - A short-form video feed component for Jetpack Compose.
 *
 * Provides Instagram Reels / YouTube Shorts style vertical scrolling video feed
 * with support for both video and image content.
 *
 * Features:
 * - Vertical snap scrolling (one item per swipe)
 * - Auto-play videos when visible
 * - Pinch-to-zoom gesture
 * - Double-tap like animation
 * - Player pooling for memory efficiency
 * - Lifecycle-aware playback management
 *
 * @param T The type of items in the feed
 * @param items List of items to display
 * @param state The [ReelsState] to control and observe the feed state
 * @param config Configuration options for the feed
 * @param mediaSource Lambda to extract [MediaSource] from each item
 * @param modifier Modifier for the component
 * @param onPageChanged Callback when the current page changes
 * @param onDoubleTap Callback when user double-taps (typically for "like" action)
 * @param onSingleTap Callback when user single-taps (typically for play/pause)
 * @param overlayContent Optional composable to overlay on each item (e.g., profile info, description)
 *
 * @sample
 * ```kotlin
 * data class ReelItem(
 *     val id: String,
 *     val videoUrl: String,
 *     val username: String
 * )
 *
 * val items = listOf(
 *     ReelItem("1", "https://example.com/video1.mp4", "user1"),
 *     ReelItem("2", "https://example.com/video2.mp4", "user2")
 * )
 *
 * val reelsState = rememberReelsState(pageCount = { items.size })
 *
 * ComposeReels(
 *     items = items,
 *     state = reelsState,
 *     mediaSource = { MediaSource.Video(it.videoUrl) },
 *     onDoubleTap = { index, item -> /* Handle like */ },
 *     overlayContent = { item ->
 *         Text(item.username, modifier = Modifier.align(Alignment.BottomStart))
 *     }
 * )
 * ```
 */
@Composable
fun <T> ComposeReels(
    items: List<T>,
    state: ReelsState,
    mediaSource: (T) -> MediaSource,
    modifier: Modifier = Modifier,
    config: ReelsConfig = ReelsConfig(),
    onPageChanged: (Int, T) -> Unit = { _, _ -> },
    onDoubleTap: ((Int, T) -> Unit)? = null,
    onSingleTap: ((Int, T) -> Unit)? = null,
    overlayContent: @Composable (BoxScope.(T) -> Unit)? = null
) {
    ReelsPagerImpl(
        items = items,
        reelsState = state,
        config = config,
        mediaSource = mediaSource,
        onPageChanged = onPageChanged,
        onDoubleTap = onDoubleTap,
        onSingleTap = onSingleTap,
        modifier = modifier,
        overlayContent = overlayContent
    )
}
