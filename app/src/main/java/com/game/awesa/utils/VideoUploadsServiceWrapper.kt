package com.game.awesa.utils

import android.app.ForegroundServiceStartNotAllowedException
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.game.awesa.services.VideoUploadJobService
import com.game.awesa.services.VideoUploadService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper for the video uploads service - this allows clients to control the service without having a reference
 * to the [Context]
 */

@Singleton
class VideoUploadsServiceWrapper @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val TAG = "VideoUploadsServiceWrapper"
        private const val JOB_ID = 112255
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun startJobService() {
        val networkRequestBuilder = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        val jobInfo = JobInfo.Builder(JOB_ID, ComponentName(context, VideoUploadJobService::class.java))
            .setUserInitiated(true)
            .setRequiredNetwork(networkRequestBuilder.build())
            .build()

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(jobInfo)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun stopJobService() {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancel(JOB_ID)
    }

    @Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException")
    fun startService() {
        // we can't use foreground services on devices running >API 34
        if (SystemVersionUtils.isAtLeastU()) {
            startJobService()
        } else {
            try {
                ContextCompat.startForegroundService(context, Intent(context, VideoUploadService::class.java))
            } catch (e: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && e is ForegroundServiceStartNotAllowedException
                ) {
                    Log.e(TAG, e.localizedMessage, e)
                }
            }
        }
    }

    fun stopService() {
        if (SystemVersionUtils.isAtLeastU()) {
            stopJobService()
        } else {
            context.stopService(Intent(context, VideoUploadService::class.java))
        }
    }
}
