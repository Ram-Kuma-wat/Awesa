package com.game.awesa.ui

import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import com.game.awesa.utils.AndroidNetworkObservingStrategy
import com.game.awesa.utils.AppInitializer
import com.game.awesa.utils.VideoUploadsWorker
import com.game.awesa.utils.VideosNotificationHandler
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import dagger.Lazy
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
open class MyApp : MultiDexApplication(), HasAndroidInjector, CameraXConfig.Provider, Configuration.Provider {
    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject lateinit var appInitializer: Lazy<AppInitializer>

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var videoUploadsWorker: VideoUploadsWorker

    companion object {
        const val TAG = "MyApp"
        lateinit var simpleCache: SimpleCache
        const val exoPlayerCacheSize: Long = 90 * 1024 * 1024
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var exoDatabaseProvider: ExoDatabaseProvider
    }

    private val networkObserver = AndroidNetworkObservingStrategy()

    override fun onCreate() {
        super.onCreate()
        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        exoDatabaseProvider = ExoDatabaseProvider(this.applicationContext)
        simpleCache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider)

        networkObserver.observeNetworkConnectivity(this)

        networkObserver.getLiveConnectivityState().observeForever { connectivity -> // .distinctUntilChanged()
            if (connectivity.networkState!!.isConnected) {
                videoUploadsWorker.fetchVideos(matchId = null)
            } else {
                videoUploadsWorker.cancelUploads()
            }
        }

        appInitializer.get().init(this)
    }

    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(Log.ERROR).build()
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onTerminate() {
        networkObserver.tryToUnregisterCallback()
        super.onTerminate()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}
