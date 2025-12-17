# ComposeReels

A Jetpack Compose library for creating Instagram Reels / TikTok / YouTube Shorts style short-form video feeds.

[![](https://jitpack.io/v/manjees/compose-reels.svg)](https://jitpack.io/#manjees/compose-reels)

## Demo

<video src="https://github.com/user-attachments/assets/f3cf1714-1e0c-43df-851b-752f16d92c07" controls width="300"></video>

## Features

- Vertical snap scrolling (VerticalPager)
- Video & Image mixed feed support
- ExoPlayer with player pooling (memory efficient)
- Pinch-to-zoom with spring animation
- Double-tap gesture detection
- Play/Pause & Mute controls
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
    implementation("com.github.manjees:compose-reels:1.0.0")
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

## Configuration

```kotlin
ReelsConfig(
    autoPlay = true,           // Auto-play videos
    isZoomEnabled = true,      // Enable pinch-to-zoom
    isMuted = false,           // Start muted
    infiniteScroll = false,    // Loop back to first item
    preloadCount = 2,          // Preload N items ahead
    playerPoolSize = 3         // Max ExoPlayer instances
)
```

## ReelsState API

```kotlin
val reelsState = rememberReelsState(pageCount = { items.size })

// Properties
reelsState.currentPage      // Current page index
reelsState.isPlaying        // Playback state
reelsState.isMuted          // Mute state
reelsState.isZoomed         // Zoom state

// Methods
reelsState.togglePlayPause()
reelsState.toggleMute()
reelsState.play()
reelsState.pause()
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
