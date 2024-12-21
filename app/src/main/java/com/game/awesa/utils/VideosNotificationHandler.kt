package com.game.awesa.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.codersworld.awesalibs.beans.matches.MatchesBean.VideosBean
import com.game.awesa.R
import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import androidx.annotation.RequiresApi
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.game.awesa.services.VideoUploadJobService
import com.game.awesa.ui.matches.MatchDetailActivity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shows the standard uploading arrow animated notification icon to signify that videos are being uploaded
 */
@Singleton
class VideosNotificationHandler @Inject constructor(
    val context: Context
)  {
    companion object {
        const val CHANNEL_ID = "image_upload_channel"
        const val FOREGROUND_NOTIFICATION_ID = 1
        private const val DATABASE_UPDATE_NOTIFICATION_ID = 2
        private const val UPLOAD_FAILURE_NOTIFICATION_ID = 3
        private const val ONE_HUNDRED_PERCENT = 100

        private fun calculateVideoUpdateNotificationId(productId: Long) =
            "$productId$DATABASE_UPDATE_NOTIFICATION_ID".hashCode()

        private fun calculateUploadFailureNotificationId(productId: Long) =
            "$productId$UPLOAD_FAILURE_NOTIFICATION_ID".hashCode()
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val notificationBuilder: NotificationCompat.Builder

    init {
        createChannel()
        notificationBuilder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        ).apply {
            color = ContextCompat.getColor(context, R.color.bright_pink)
            setSmallIcon(android.R.drawable.stat_sys_upload)
            setOnlyAlertOnce(true)
            setOngoing(true)
            setTimeoutAfter(100)
            setProgress(0, 0, true)
            setGroup(FOREGROUND_NOTIFICATION_ID.toString())
        }
    }

    fun attachToService(service: Service) {
        val notification = notificationBuilder.build()
        service.startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun attachToService(service: VideoUploadJobService, params: JobParameters) {
        val notification = notificationBuilder.build()
        service.setNotification(
            params,
            FOREGROUND_NOTIFICATION_ID,
            notification,
            JobService.JOB_END_NOTIFICATION_POLICY_REMOVE
        )
    }

    fun update(currentUpload: Int, totalUploads: Int) {
        val title = if (totalUploads == 1) {
            context.getString(R.string.video_uploading_single_notif_message)
        } else {
            context.getString(R.string.video_uploading_multi_notif_message, currentUpload, totalUploads)
        }

        notificationBuilder.apply {
            setContentTitle(title)
            setProgress(ONE_HUNDRED_PERCENT, 0, false)
        }
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notificationBuilder.build())
    }

    fun setProgress(progress: Float) {
        notificationBuilder.apply {
            setProgress(ONE_HUNDRED_PERCENT, (progress * ONE_HUNDRED_PERCENT).toInt(), false)
        }
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notificationBuilder.build())
    }

    fun showUpdatingMatchNotification(video: VideosBean?) {
        val title = context.getString(R.string.video_update_notification, video?.title.orEmpty())
            .replace("  ", " ")

        notificationBuilder.setContentTitle(title)
        notificationBuilder.setProgress(0, 0, true)
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notificationBuilder.build())
    }

    fun postUpdateSuccessNotification(productId: Long, video: ReactionsBean, imagesCount: Int) {
        val notificationBuilder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        ).apply {
            color = ContextCompat.getColor(context, R.color.bright_pink)
            setSmallIcon(R.drawable.ic_done_secondary)
            setContentTitle(context.getString(R.string.video_update_success_notification_title))
            setContentText(
                context.getString(
                    R.string.video_update_success_notification_content,
                    imagesCount,
                    video.team_name
                )
            )
            setContentIntent(getMatchDetailsIntent(productId))
            setAutoCancel(true)
            setGroup(DATABASE_UPDATE_NOTIFICATION_ID.toString())
        }
        notificationManager.notify(calculateVideoUpdateNotificationId(productId), notificationBuilder.build())
    }

    fun postUpdateFailureNotification(matchId: Long, video: ReactionsBean?) {
        val title = context.getString(R.string.video_update_failure_notification, video?.team_name.orEmpty())
            .replace("  ", " ")
        val notificationBuilder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        ).apply {
            color = ContextCompat.getColor(context, R.color.bright_pink)
            setSmallIcon(android.R.drawable.stat_notify_error)
            setContentTitle(title)
            setContentIntent(getMatchDetailsIntent(matchId))
            setAutoCancel(true)
            setGroup(DATABASE_UPDATE_NOTIFICATION_ID.toString())
        }
        notificationManager.notify(calculateUploadFailureNotificationId(matchId), notificationBuilder.build())
    }

    fun removeUploadFailureNotification(matchId: Long) {
        notificationManager.cancel(matchId.toInt() + UPLOAD_FAILURE_NOTIFICATION_ID)
    }

    private fun getMatchDetailsIntent(matchId: Long): PendingIntent? {
        val intent: Intent = Intent(context, MatchDetailActivity::class.java)
         intent.putExtra("game_id", matchId)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    /**
     * Ensures the notification channel for image uploads is created - only required for Android O+
     */
    private fun createChannel() {
        // first check if the channel already exists
        notificationManager.getNotificationChannel(CHANNEL_ID)?.let {
            return
        }

        val channelName = context.getString(R.string.videos_upload_channel_title)
        val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    fun removeForegroundNotification() {
        notificationManager.cancelAll()
        notificationManager.cancel(FOREGROUND_NOTIFICATION_ID)
    }
}
