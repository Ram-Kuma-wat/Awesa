package com.game.awesa.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.utils.VideosNotificationHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Service which uploads trimmed videos
 */
@AndroidEntryPoint
class VideoUploadService : Service() {
    companion object {
        private val TAG = VideoUploadService::class.java.simpleName
    }
    @Inject  lateinit var notifHandler: VideosNotificationHandler

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "Upload Service Create")
    }

    override fun onDestroy() {
        Log.e(TAG,"Upload Service Destroy")
        // After testing, sometimes the notification gets stuck after stopping the service if it wasn't
        // removed explicitly
        notifHandler.removeForegroundNotification()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG,"Upload Service onStartCommand")
        if (intent == null) {
            // If the system restarts the service, we won't be able to restore the state of uploads,
            // so we'll just stop it
            stopSelf()
        } else {
            notifHandler.attachToService(this)
        }
        return START_STICKY
    }
}
