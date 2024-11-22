package com.game.awesa.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.matches.InterviewBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.di.AppCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import com.game.awesa.utils.VideoUploadsRepository.UploadResult.UploadFailure
import com.game.awesa.utils.VideoUploadsRepository.UploadResult.UploadSuccess
import com.game.awesa.utils.VideoUploadsRepository.UploadResult.UploadProgress
import com.game.awesa.utils.VideoUploadsRepository.UploadResult.UploadInterviewSuccess
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/***
 * This class is responsible for queuing and handling different tasks related to product images upload.
 * It handles three types of works:
 * - [Work.UploadVideo]
 * - [Work.UploadInterviewVideo]
 *
 * Uploading videos and updating product is done sequentially (using a [Mutex] lock) because our repositories don't
 * play well with parallel requests, due to the use of a single shared continuation.
 *
 */

@Singleton
class VideoUploadsWorker @Inject constructor(
    private val context: Context,
    private val videoUploadsRepository: VideoUploadsRepository,
    private val notificationHandler: VideosNotificationHandler,
    private val videoUploadServiceWrapper: VideoUploadsServiceWrapper,
    private val databaseManager: DatabaseManager,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "VideoUploadsWorker"
        const val DURATION_BEFORE_STOPPING_SERVICE = 1000L
    }

    private val queue = MutableSharedFlow<Work>(extraBufferCapacity = Int.MAX_VALUE)
    private val pendingWorkList = MutableStateFlow<List<Work>>(emptyList())

    private val cancelledVideos = mutableSetOf<Long>()
    private val currentJobs = mutableMapOf<Long, List<Job>>()
    // A reference to all videos being uploaded to update the notification with the correct index
    private val uploadList = mutableListOf<VideoUploadEntry>()

    private val mutex = Mutex()

    init {
        observeQueue()
        handleServiceStatus()
    }

    private fun areEquivalent(old: Work, new: Work): Boolean {
        val hasSameId = old.videoId == new.videoId
        val hasSameUrl = old.localUri == new.localUri

        return hasSameId && hasSameUrl
    }

    private fun observeQueue() {
        queue
            .distinctUntilChanged(::areEquivalent)
            .onEach { work ->
                pendingWorkList.update { list -> list + work }
                uploadList.add(VideoUploadEntry(work.videoId, work.localUri))

                handleWork(work)
            }
            .launchIn(appCoroutineScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleServiceStatus() {
        pendingWorkList
            .transformLatest { list ->
                val done = list.isEmpty()
                if (done) {
                    // Add a delay to avoid stopping the service if there is an event coming to the queue
                    kotlinx.coroutines.delay(DURATION_BEFORE_STOPPING_SERVICE)
                }
                emit(done)
            }
            .distinctUntilChanged()
            .onEach { done ->
                if (done) {
                    Log.d(TAG, "VideoFileUploadHandler -> stop service")
                    videoUploadServiceWrapper.stopService()
                    uploadList.clear()
                } else {
                    Log.d(TAG, "VideoFileUploadHandler -> start service")
                    videoUploadServiceWrapper.startService()
                }
            }
            .launchIn(appCoroutineScope)
    }

    private fun handleWork(work: Work) {
        if (cancelledVideos.contains(work.videoId)) {
            Log.d(TAG, "VideoUploadsWorker -> skipping work $work since it's cancelled")
            return
        }

        val job = appCoroutineScope.launch {
            Log.d(TAG, "VideoUploadsWorker -> start work handling $work")

            try {
                when (work) {
                    is Work.UploadVideo -> uploadMedia(work)
                    is Work.UploadInterviewVideo -> uploadMedia(work)
                }
            } finally {
                pendingWorkList.update { list -> list - work }
            }
        }

        // Save a reference to the job for cancelling it if needed
        currentJobs[work.videoId] = currentJobs.getOrElse(work.videoId) { emptyList() } + job

        job.invokeOnCompletion {
            // Remove the job from the list jobs
            currentJobs[work.videoId] = currentJobs[work.videoId]!! - job
        }
    }

    fun fetchVideos(matchId: String?) {
        cancelUploads()
        Log.d(TAG, "VideoUploadsWorker -> fetch videos for upload")

        appCoroutineScope.launch {
            videoUploadsRepository.getMatchReactions(matchId = matchId).collect {
                it?.forEach { reaction -> enqueueWork(
                    Work.UploadVideo(
                        videoId = reaction.id.toLong(),
                        localUri = reaction.video,
                        reactionModel = reaction
                    )
                )
                }
            }

            videoUploadsRepository.getMatchInterviews(matchId = matchId).collect {
                it?.forEach { interview -> enqueueWork(
                    Work.UploadInterviewVideo(
                        videoId = interview.id.toLong(),
                        localUri = interview.video,
                        interviewModel = interview
                    )
                )
                }
            }
        }
    }

    private fun enqueueWork(work: Work) {
        cancelledVideos.remove(work.videoId)
        queue.tryEmit(work)
    }

    fun cancelUpload(videoId: Long) {
        cancelledVideos.add(videoId)
        currentJobs[videoId]?.forEach {
            it.cancel()
        }
        uploadList.removeAll { it.videoId == videoId }
    }

    fun cancelUploads() {
        currentJobs.keys.forEach { key ->
            currentJobs[key]?.forEach { job ->
                job.cancel()
                uploadList.removeAll { it.videoId == key }
            }
        }

    }

    private suspend fun uploadMedia(work: Work.UploadVideo) {
        mutex.withLock {
            Log.d(TAG, "VideoUploadsWorker -> start uploading media ${work.localUri}")

            val doneUploads = uploadList.count { it.isDone }
            notificationHandler.update(doneUploads + 1, uploadList.size)
            videoUploadsRepository.uploadMedia(work.reactionModel).collect {
                when (it) {
                    is UploadFailure -> {
                        Log.w(TAG, "VideoFileUploadHandler -> upload failed for ${work.localUri}")
                        val index =
                            uploadList.indexOfFirst {
                                item -> item.videoId == work.videoId && item.localUri == work.localUri
                            }
                        uploadList[index] = uploadList[index].copy(isDone = true)
                    }
                    is UploadProgress -> notificationHandler.setProgress(it.progress)
                    is UploadSuccess -> {
                        Log.d(TAG, "VideoFileUploadHandler -> upload succeeded for ${work.localUri}")
                        notificationHandler.setProgress(1f)
                        handleResponse(response = it.response, reactionModel = it.reaction)
                        val index =
                            uploadList.indexOfFirst {
                                item -> item.videoId == work.videoId && item.localUri == work.localUri
                            }
                        uploadList[index] = uploadList[index].copy(isDone = true)
                    }

                    is UploadInterviewSuccess -> {}
                }
            }

            val hasMoreUploads = pendingWorkList.value.any {
                it != work && it.videoId == work.videoId && (it is Work.UploadVideo || it is Work.UploadInterviewVideo)
            }
            if (!hasMoreUploads) {
                Log.d(TAG, "VideoUploadsWorker -> all uploads are done")
            }
        }
    }

    private suspend fun uploadMedia(work: Work.UploadInterviewVideo) {
        mutex.withLock {
            Log.d(TAG, "VideoUploadsWorker -> start uploading interview ${work.localUri}")

            val doneUploads = uploadList.count { it.isDone }
            notificationHandler.update(doneUploads + 1, uploadList.size)
            videoUploadsRepository.uploadMedia(work.interviewModel).collect {
                when (it) {
                    is UploadFailure -> {
                        Log.w(TAG, "VideoFileUploadHandler -> upload interview failed for ${work.localUri}")

                    }
                    is UploadProgress -> notificationHandler.setProgress(it.progress)
                    is UploadInterviewSuccess -> {
                        Log.d(TAG, "VideoFileUploadHandler -> upload interview succeeded for ${work.localUri}")
                        notificationHandler.setProgress(1f)
                        handleResponse(response = it.response, interviewModel = it.interview)
                        val index =
                            uploadList.indexOfFirst {
                                item -> item.videoId == work.videoId && item.localUri == work.localUri
                            }
                        uploadList[index] = uploadList[index].copy(isDone = true)
                    }

                    is UploadSuccess -> {}
                }
            }

            val hasMoreUploads = pendingWorkList.value.any {
                it != work && it.videoId == work.videoId && (it is Work.UploadVideo || it is Work.UploadInterviewVideo)
            }
            if (!hasMoreUploads) {
                Log.d(TAG, "VideoUploadsWorker -> all uploads are done")
            }
        }
    }

    private fun handleResponse(response: UniversalObject?, reactionModel: ReactionsBean?) {
        try {
            val mBean = response?.response as? CommonBean
            if (mBean != null && mBean.videos != null) {
                val broadcastIntent = Intent("videoUpload")
                broadcastIntent.putExtra("Data", Gson().toJson(mBean.videos))
                try {
                    broadcastIntent.putExtra("old_data", Gson().toJson(reactionModel))
                } catch (ex: Exception) {
                    Log.e(TAG, ex.localizedMessage)
                }

                context.sendBroadcast(broadcastIntent)

                if (mBean.status == 1 && CommonMethods.isValidString(response.msg)) {
                    if (reactionModel != null) {
                        databaseManager.executeQuery { database ->
                            val dao = MatchActionsDAO(database, context)
                            dao.deleteAll(reactionModel.id.toString(), 0)
                            try {
                                File(reactionModel.video).delete()
                            } catch (e: Exception) {
                                Log.e(TAG, e.localizedMessage)
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage)
        }
    }

    private  fun handleResponse(response: UniversalObject?, interviewModel: InterviewBean?) {
        try{
            val mBean = response?.response as? CommonBean

            if (mBean != null &&  mBean.status == 1) {
                if (interviewModel != null) {
                    databaseManager.executeQuery { database ->
                        val dao = InterviewsDAO(database, context)
                        dao.deleteAll(interviewModel.id.toString(),0)
                        try{
                            File(interviewModel.video).delete()
                        }catch (ex:Exception){
                            Log.e(TAG, ex.localizedMessage)
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage)
        }
    }

    sealed class Work {
        abstract val videoId: Long
        abstract val localUri: String

        data class UploadVideo(
            override val videoId: Long,
            override val localUri: String,
            val reactionModel: ReactionsBean,
        ) : Work()

        data class UploadInterviewVideo(
            override val videoId: Long,
            override val localUri: String,
            val interviewModel: InterviewBean,
        ) : Work()
    }

    /**
     * This class is only used to be able to count total of uploads, and number of completed ones, and update
     * notification accordingly
     */
    data class VideoUploadEntry(
        val videoId: Long,
        val localUri: String,
        val isDone: Boolean = false
    )
}
