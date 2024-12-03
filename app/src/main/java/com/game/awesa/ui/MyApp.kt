package com.game.awesa.ui

import android.content.Intent
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.hilt.work.HiltWorkerFactory
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.beans.VideoUploadBean
import com.codersworld.awesalibs.database.dao.VideoMasterDAO
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.services.TrimService
import com.game.awesa.utils.AndroidNetworkObservingStrategy
import com.game.awesa.utils.AppInitializer
import com.game.awesa.utils.VideoUploadsWorker
import dagger.Lazy
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

@UnstableApi
open class MyApp : MultiDexApplication(), HasAndroidInjector, CameraXConfig.Provider, Configuration.Provider {
    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject lateinit var appInitializer: Lazy<AppInitializer>

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var videoUploadsWorker: VideoUploadsWorker

    @Inject lateinit var databaseManager: DatabaseManager

    companion object {
        const val TAG = "MyApp"
        lateinit var simpleCache: SimpleCache
        const val EXO_PLAYER_CACHE_SIZE: Long = 90 * 1024 * 1024
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var exoDatabaseProvider: StandaloneDatabaseProvider
    }

    @Inject lateinit var networkObserver: AndroidNetworkObservingStrategy

    override fun onCreate() {
        super.onCreate()
        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(EXO_PLAYER_CACHE_SIZE)
        exoDatabaseProvider = StandaloneDatabaseProvider(this.applicationContext)
        simpleCache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider)

        networkObserver.observeNetworkConnectivity(this)

        startTrimService()

        networkObserver.getLiveConnectivityState().observeForever { connectivity ->
            if (connectivity.networkState!!.isConnected) {
                videoUploadsWorker.fetchVideos()
            } else {
                videoUploadsWorker.cancelUploads()
            }
        }

        appInitializer.get().init(this)
    }

    private fun startTrimService() {
        databaseManager.executeQuery { database ->
            val mVideoMasterDAO = VideoMasterDAO(database, applicationContext)
            val mList = mVideoMasterDAO.selectAll() as ArrayList<VideoUploadBean>
            if (CommonMethods.isValidArrayList(mList)) {
                loop@ for (index in mList.indices) {
                    val mIntent = Intent(this, TrimService::class.java)
                    mIntent.putExtra("matchId", mList[index].match_id)
                    this.startService(mIntent)
                    break@loop
                }
            }
        }
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
