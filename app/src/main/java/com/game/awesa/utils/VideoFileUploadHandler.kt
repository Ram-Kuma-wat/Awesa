package com.game.awesa.utils

import android.os.Parcelable
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.game.awesa.di.AppCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoFileUploadHandler @Inject constructor(
    private val notificationHandler: VideosNotificationHandler,
    private val worker: VideoUploadsWorker,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "VideoFileUploadHandler"
    }

    private val uploadsStatus = MutableStateFlow(emptyList<VideoUploadData>())
    private val externalObservers = mutableListOf<Long>()


    init {
//        worker.events
//            .onEach { event ->
//                Log.d(TAG, "MediaFileUploadHandler -> handling $event")
//                when (event) {
//                    is Event.MediaUploadEvent -> handleMediaUploadEvent(event)
////                    Event.ServiceStopped -> clearPendingUploads()
//                }
//            }
//            .launchIn(appCoroutineScope)
    }

//    private fun handleMediaUploadEvent(event: Event.MediaUploadEvent) {
//        val statusList = uploadsStatus.value.toMutableList()
//        val index = statusList.indexOfFirst {
//            it.remoteProductId == event.productId && it.localUri == event.localUri
//        }
//        if (index == -1) {
//            Log.w(TAG, "MediaFileUploadHandler -> received event for unmatched media")
//            return
//        }
//
//        val newStatus = event.toStatus()
//
//        when (event) {
//            is Event.MediaUploadEvent.FetchSucceeded -> {
//                enqueueMediaUpload(event)
//            }
//            is Event.MediaUploadEvent.FetchFailed -> {
//                statusList[index] = newStatus
//                showUploadFailureNotifIfNoObserver(event.productId, statusList)
//            }
//            is Event.MediaUploadEvent.UploadSucceeded -> {
//                if (externalObservers.contains(event.productId)) {
//                    Log.d(TAG, "MediaFileUploadHandler -> Upload successful, while handler is observed")
//                    statusList.removeAt(index)
//                } else {
//                    Log.d(TAG, "MediaFileUploadHandler -> Upload successful with no observers")
//                    statusList[index] = newStatus
//                }
//            }
//            is Event.MediaUploadEvent.UploadFailed -> {
//                Log.e(TAG, "MediaFileUploadHandler -> Upload failed", event.error)
//                statusList[index] = newStatus
//
//                showUploadFailureNotifIfNoObserver(event.productId, statusList)
//            }
//        }
//        uploadsStatus.value = statusList
//    }

    /***
     * Identifies both an event and status.
     * Holds a reference to the productId and localUri to keep track of each upload
     */
    @Parcelize
    data class VideoUploadData(
        val remoteVideoId: Long,
        val localUri: String,
        val uploadStatus: UploadStatus
    ) : Parcelable

    sealed class UploadStatus : Parcelable {
        @Parcelize
        object InProgress : UploadStatus()

        @Parcelize
        data class Failed(
            val reaction: ReactionsBean? = null,
            val mediaErrorMessage: String
        ) : UploadStatus()

        @Parcelize
        data class UploadSuccess(val reaction: ReactionsBean) : UploadStatus()
    }
}
