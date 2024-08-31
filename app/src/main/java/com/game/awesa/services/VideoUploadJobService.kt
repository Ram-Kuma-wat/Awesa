package com.game.awesa.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Configuration
import com.game.awesa.utils.VideosNotificationHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Service which uploads trimmed videos
 */
@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class VideoUploadJobService : JobService() {

    @Inject lateinit var  notifHandler: VideosNotificationHandler

    companion object {
        private const val MIN_JOB_ID = 0
        private const val MAX_JOB_ID = 10000
    }

    init {
        val builder: Configuration.Builder = Configuration.Builder()
        builder.setJobSchedulerJobIdRange(MIN_JOB_ID, MAX_JOB_ID)
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        notifHandler.attachToService(this, params!!)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        jobFinished(params!!, false)
        return true
    }
}
