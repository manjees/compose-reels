package io.github.composereels.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * Provides a shared disk cache for video playback to avoid re-downloading content.
 */
@OptIn(UnstableApi::class)
internal object VideoCache {
    private const val CACHE_DIR_NAME = "compose_reels_video_cache"
    private const val CACHE_SIZE_BYTES = 150L * 1024 * 1024 // 150 MB

    @Volatile
    private var cache: Cache? = null
    @Volatile
    private var dataSourceFactory: DataSource.Factory? = null

    fun cacheDataSourceFactory(context: Context): DataSource.Factory {
        val appContext = context.applicationContext
        return dataSourceFactory ?: synchronized(this) {
            dataSourceFactory ?: createDataSourceFactory(appContext).also { dataSourceFactory = it }
        }
    }

    private fun createDataSourceFactory(context: Context): DataSource.Factory {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME).apply { mkdirs() }
        val cacheInstance = cache ?: SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(CACHE_SIZE_BYTES),
            StandaloneDatabaseProvider(context)
        ).also { cache = it }

        val upstreamFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        return CacheDataSource.Factory()
            .setCache(cacheInstance)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}
