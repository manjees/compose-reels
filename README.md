# ComposeReels

A Jetpack Compose library for creating Instagram Reels / TikTok / YouTube Shorts style short-form video feeds.

[![](https://jitpack.io/v/manjees/compose-reels.svg)](https://jitpack.io/#manjees/compose-reels)

## Demo

<video src="https://github.com/user-attachments/assets/f3cf1714-1e0c-43df-851b-752f16d92c07" controls width="300"></video>

## Features

- Vertical snap scrolling (VerticalPager)
- Video & Image mixed feed support
- ExoPlayer with player pooling (memory efficient)
- Local media support — `asset://`, `file://`, `content://`, `android.resource://` in addition to HTTP(S)
- Disk cache (150 MB LRU) for seamless re-playback of network videos
- Pinch-to-zoom with spring animation
- Double-tap gesture detection
- Long-press to boost playback to fast mode (TikTok-style 2x) with visual indicator
- Play/Pause & Mute controls
- Error handling with built-in retry UI (customizable)
- Analytics callbacks (video start / pause with watch time / completed)
- Infinite scroll support
- Lifecycle-aware playback management
- Fully customizable overlay UI

## Installation

Add JitPack repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.manjees:compose-reels:1.0.2")
}
```

## Usage

### Basic Usage

```kotlin
data class ReelItem(
    val id: String,
    val videoUrl: String,
    val username: String
)

@Composable
fun ReelsScreen() {
    val items = remember { listOf(
        ReelItem("1", "https://example.com/video1.mp4", "user1"),
        ReelItem("2", "https://example.com/video2.mp4", "user2")
    )}

    val reelsState = rememberReelsState(pageCount = { items.size })

    ComposeReels(
        items = items,
        state = reelsState,
        mediaSource = { item -> MediaSource.Video(item.videoUrl) }
    )
}
```

### With Custom Overlay

```kotlin
ComposeReels(
    items = items,
    state = reelsState,
    config = ReelsConfig(
        autoPlay = true,
        isZoomEnabled = true,
        infiniteScroll = true
    ),
    mediaSource = { item -> MediaSource.Video(item.videoUrl) },
    onDoubleTap = { index, item -> /* Handle like */ },
    onSingleTap = { index, item -> reelsState.togglePlayPause() }
) { item ->
    // Your custom overlay UI
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "@${item.username}",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            color = Color.White
        )
    }
}
```

### Mixed Media (Video + Image)

```kotlin
ComposeReels(
    items = items,
    state = reelsState,
    mediaSource = { item ->
        when {
            item.isVideo -> MediaSource.Video(item.url, item.thumbnailUrl)
            else -> MediaSource.Image(item.url)
        }
    }
)
```

### Local Media

Videos and images don't have to come from the network — any URI scheme supported by ExoPlayer and Coil works. Useful for bundled content, downloaded files, or content picked via `MediaStore`.

```kotlin
// Video bundled in app assets (place file under src/main/assets/)
MediaSource.Video("asset:///clip.mp4")

// Video downloaded to the app's internal storage
MediaSource.Video("file:///${context.filesDir}/downloads/clip.mp4")

// Video via ContentResolver (e.g. from the photo picker)
MediaSource.Video(pickedUri.toString())

// Image bundled in app assets (Coil's asset URI form)
MediaSource.Image("file:///android_asset/photo.jpg")
```

### Error Handling

When a video fails to load, a built-in retry UI is shown and `onError` is invoked.

```kotlin
ComposeReels(
    items = items,
    state = reelsState,
    mediaSource = { MediaSource.Video(it.videoUrl) },
    onError = { index, item, error ->
        Log.e("Reels", "Failed at $index: ${error.errorCodeName}")
    }
)
```

Override the retry UI with your own composable via `errorContent`:

```kotlin
ComposeReels(
    items = items,
    state = reelsState,
    mediaSource = { MediaSource.Video(it.videoUrl) },
    errorContent = { item, error ->
        // `this` is a BoxScope — align, pad, etc. as needed
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Couldn't load video", color = Color.White)
            TextButton(onClick = { /* trigger your own retry flow */ }) {
                Text("Try again")
            }
        }
    }
)
```

### Analytics

Pass a `ReelsAnalytics` to track playback events. All callbacks are optional.

```kotlin
ComposeReels(
    items = items,
    state = reelsState,
    mediaSource = { MediaSource.Video(it.videoUrl) },
    analytics = ReelsAnalytics(
        onVideoStart = { index ->
            tracker.log("video_start", mapOf("index" to index))
        },
        onVideoPaused = { index, watchTimeMs ->
            tracker.log("video_pause", mapOf("index" to index, "ms" to watchTimeMs))
        },
        onVideoCompleted = { index ->
            tracker.log("video_complete", mapOf("index" to index))
        }
    )
)
```

## Configuration

```kotlin
ReelsConfig(
    autoPlay = true,           // Auto-play videos
    isZoomEnabled = true,      // Enable pinch-to-zoom
    isMuted = false,           // Start muted
    infiniteScroll = false,    // Loop back to first item
    preloadCount = 2,          // Preload N items in both directions
    playerPoolSize = 7,        // Max ExoPlayer instances — defaults to (preloadCount * 2) + 3
    longPressFastPlaybackEnabled = true, // Enable long-press fast playback
    longPressFastPlaybackSpeed = 2f      // Speed multiplier while pressing
)
```

> **Note:** `playerPoolSize` must be at least `(preloadCount * 2) + 1` to support preloading in both directions plus the current page. The default adds two extra slots of fling-time headroom. Use `ReelsConfig.minPoolSizeFor(preloadCount)` to read the hard minimum.

## ReelsState API

```kotlin
val reelsState = rememberReelsState(pageCount = { items.size })

// Properties
reelsState.currentPage      // Current page index
reelsState.isPlaying        // Playback state
reelsState.isMuted          // Mute state
reelsState.isZoomed         // Zoom state
reelsState.playbackSpeed    // Current playback speed multiplier

// Methods
reelsState.togglePlayPause()
reelsState.toggleMute()
reelsState.play()
reelsState.pause()
reelsState.setPlaybackSpeed(speed)
reelsState.scrollToPage(index)
reelsState.animateScrollToPage(index)
```

## Requirements

- Min SDK: 24
- Target SDK: 34+
- Jetpack Compose

## License

```
Copyright 2024 manjees

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
