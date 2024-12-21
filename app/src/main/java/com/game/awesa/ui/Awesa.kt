package com.game.awesa.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import com.codersworld.awesalibs.beans.VideoUploadBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.VideoMasterDAO
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.services.TrimService
import com.game.awesa.utils.AndroidNetworkObservingStrategy
import com.game.awesa.utils.AppInitializer
import com.game.awesa.utils.VideoUploadsWorker
import dagger.Lazy
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import java.io.File
import java.util.Locale
import javax.inject.Inject

@UnstableApi
open class Awesa : MultiDexApplication(), HasAndroidInjector, CameraXConfig.Provider, Configuration.Provider {
    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject lateinit var appInitializer: Lazy<AppInitializer>

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var videoUploadsWorker: VideoUploadsWorker

    @Inject lateinit var databaseManager: DatabaseManager

    val recorder: Recorder by lazy {
        Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
    }

    val preview: Preview by lazy {
        val resolutionSelector = ResolutionSelector.Builder().setAspectRatioStrategy(
            AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY).build()
        Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
    }

    val videoCapture: VideoCapture<Recorder> by lazy {
        VideoCapture.withOutput(recorder)
    }

    val currentRecording: MutableLiveData<Recording?> by lazy {
        MutableLiveData()
    }

    val recordEvent: MutableLiveData<VideoRecordEvent?> by lazy {
        MutableLiveData()
    }

    val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(this) }

    private val captureListener = Consumer<VideoRecordEvent> { event ->
        recordEvent.value = event
    }

    companion object {
        val TAG: String = Awesa::class.java.simpleName
        lateinit var simpleCache: SimpleCache
        const val EXO_PLAYER_CACHE_SIZE: Long = 90 * 1024 * 1024
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var exoDatabaseProvider: StandaloneDatabaseProvider

        fun setGermanAsDefault(context: Context): Context {
            val locale = Locale("de")
            Locale.setDefault(locale)
            val config = android.content.res.Configuration()
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        }
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

                if (UserSessions.getUserInfo(this) == null) return@observeForever

                videoUploadsWorker.fetchVideos()
            } else {
                videoUploadsWorker.cancelUploads()
            }
        }

        appInitializer.get().init(this)
    }

    fun startRecording(recorder: PendingRecording) {
        currentRecording.value = recorder.start(mainThreadExecutor, captureListener)
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
        currentRecording.value = null
        super.onTerminate()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}
