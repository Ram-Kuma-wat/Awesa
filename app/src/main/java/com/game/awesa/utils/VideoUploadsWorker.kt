package com.game.awesa.utils

import android.content.Context
import android.content.Intent
import android.database.SQLException
import android.util.Log
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.matches.InterviewBean
import com.codersworld.awesalibs.beans.matches.MatchesBean.VideosBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.rest.UniversalObject
import com.game.awesa.di.AppCoroutineScope
import com.game.awesa.ui.matches.MatchDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import com.game.awesa.utils.VideoUploadsRepository.UploadResult.UploadFailure
import com.game.awesa.utils.VideoUploadsRepository.UploadResult.UploadSuccess
import com.game.awesa.utils.VideoUploadsRepository.UploadResult.UploadProgress
import com.game.awesa.utils.VideoUploadsRepository.UploadResult.UploadInterviewSuccess
import com.google.gson.JsonIOException
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
import java.io.Serializable
import java.lang.IllegalStateException
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

    private val cancelledMatches = mutableSetOf<Long>()
    private val currentJobs = mutableMapOf<Long, List<Job>>()
    // A reference to all videos being uploaded to update the notification with the correct index
    private val uploadList = mutableListOf<VideoUploadEntry>()

    private val mutex = Mutex()

    init {
        observeQueue()
        handleServiceStatus()
    }

    private fun areEquivalent(old: Work, new: Work): Boolean {
//        val hasSameId = old.videoId == new.videoId
        val hasSameUrl = old.localUri == new.localUri

        return hasSameUrl // hasSameId &&
    }

    private fun observeQueue() {
        queue
            .distinctUntilChanged(::areEquivalent)
            .onEach { work ->
                pendingWorkList.update { list -> list + work }
                uploadList.add(VideoUploadEntry(work.matchId, work.localUri))

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
                    Log.d(TAG, "> Stop Service")
                    videoUploadServiceWrapper.stopService()
                    uploadList.clear()
                } else {
                    Log.d(TAG, "-> Start Service")
                    videoUploadServiceWrapper.startService()
                }
            }
            .launchIn(appCoroutineScope)
    }

    private fun handleWork(work: Work) {
        if (cancelledMatches.contains(work.matchId)) {
            Log.d(TAG, "-> Skipping work $work since it's cancelled")
            return
        }

        val job = appCoroutineScope.launch {
            Log.d(TAG, "-> start work handling $work")

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
        currentJobs[work.matchId] = currentJobs.getOrElse(work.matchId) { emptyList() } + job

        job.invokeOnCompletion {
            // Remove the job from the list jobs
            currentJobs[work.matchId] = currentJobs[work.matchId]!! - job
        }
    }

    fun fetchVideos(matchId: String? = null) {
        Log.d(TAG, "-> fetch videos for upload $matchId")

        if (matchId !== null) {
            cancelUpload(matchId.toLong())
        } else {
            cancelUploads()
        }

        appCoroutineScope.launch {
            videoUploadsRepository.getMatchReactions(matchId = matchId).collect {
                it?.forEach { reaction -> enqueueWork(
                    Work.UploadVideo(
                        matchId = reaction.match_id.toLong(),
                        localUri = reaction.video,
                        reactionModel = reaction
                    )
                )
                }
            }

            videoUploadsRepository.getMatchInterviews(matchId = matchId).collect {
                it?.forEach { interview -> enqueueWork(
                    Work.UploadInterviewVideo(
                        matchId = interview.match_id.toLong(),
                        localUri = interview.video,
                        interviewModel = interview
                    )
                )
                }
            }
        }
    }

    private fun enqueueWork(work: Work) {
        cancelledMatches.remove(work.matchId)
        queue.tryEmit(work)
    }

    fun cancelUpload(matchId: Long) {
        cancelledMatches.add(matchId)
        currentJobs[matchId]?.forEach {
            it.cancel()
        }
        uploadList.removeAll { it.matchId == matchId }
    }

    fun cancelUploads() {
        currentJobs.keys.forEach { key ->
            currentJobs[key]?.forEach { job ->
                job.cancel()
                uploadList.removeAll { it.matchId == key }
            }
        }
    }

    private suspend fun uploadMedia(work: Work.UploadVideo) {
//        mutex.withLock {
//            Log.d(TAG, "-> start uploading media ${work.localUri}")
//
//            val doneUploads = uploadList.count { it.isDone }
//            notificationHandler.update(doneUploads + 1, uploadList.size)
//        }

        mutex.withLock {
            Log.d(TAG, "-> start uploading media ${work.localUri}")

            val doneUploads = uploadList.count { it.isDone }
            notificationHandler.update(doneUploads + 1, uploadList.size)
            videoUploadsRepository.uploadMedia(work.reactionModel).collect {
                when (it) {
                    is UploadFailure -> {
                        Log.w(TAG, "-> upload failed for ${work.localUri}")
                        val index =
                            uploadList.indexOfFirst {
                                item -> item.matchId == work.matchId && item.localUri == work.localUri
                            }
                        try {
                            uploadList[index] = uploadList[index].copy(isDone = true)
                        } catch (e: ConcurrentModificationException) {
                            Log.e(TAG, e.localizedMessage, e)
                        }
                    }
                    is UploadProgress -> notificationHandler.setProgress(it.progress)
                    is UploadSuccess -> {
                        Log.d(TAG, "-> upload succeeded for ${work.localUri}")
                        notificationHandler.setProgress(1f)
                        handleResponse(response = it.response, reactionModel = it.reaction)

                        val index =
                            uploadList.indexOfFirst {
                                    item -> item.matchId == work.matchId && item.localUri == work.localUri
                            }

                        try {
                            uploadList[index] = uploadList[index].copy(isDone = true)
                        } catch (e: ConcurrentModificationException) {
                            Log.e(TAG, e.localizedMessage, e)
                        }

                        try {
                            Log.d(TAG, "-> Deleting file for action ${work.matchId} @ ${work.localUri}")
                            if (File(work.localUri).exists()) {
                                File(work.localUri).delete()
                            }
                        } catch (e: java.lang.NullPointerException) {
                            Log.e(TAG, "-> Failed Deleting file for action ${work.matchId} @ ${work.localUri}", e)
                        }

//                        //  Safely update shared list inside lock
//                        mutex.withLock {
//                            val index =
//                                uploadList.indexOfFirst {
//                                        item -> item.videoId == work.videoId && item.localUri == work.localUri
//                                }
//                            uploadList[index] = uploadList[index].copy(isDone = true)
//                        }
                    }

                    is UploadInterviewSuccess -> {}
                }
            }

            val hasMoreUploads = pendingWorkList.value.any {
                it != work && it.matchId == work.matchId && (it is Work.UploadVideo || it is Work.UploadInterviewVideo)
            }
            if (!hasMoreUploads) {
                Log.d(TAG, "-> all uploads are done")
            }
        }
    }

    private suspend fun uploadMedia(work: Work.UploadInterviewVideo) {
//        mutex.withLock {
//            Log.d(TAG, "-> start uploading interview ${work.localUri}")
//
//            // Update notification safely
//            val doneUploads = uploadList.count { it.isDone }
//            notificationHandler.update(doneUploads + 1, uploadList.size)
//        }

        mutex.withLock {
            Log.d(TAG, "-> Start uploading interview ${work.localUri}")

            val doneUploads = uploadList.count { it.isDone }
            notificationHandler.update(doneUploads + 1, uploadList.size)
            videoUploadsRepository.uploadMedia(work.interviewModel).collect {
                when (it) {
                    is UploadFailure -> {
                        Log.w(TAG, "-> Upload interview failed for ${work.localUri}")

                    }
                    is UploadProgress -> notificationHandler.setProgress(it.progress)
                    is UploadInterviewSuccess -> {
                        Log.d(TAG, "-> Upload interview succeeded for ${work.localUri}")
                        notificationHandler.setProgress(1f)
                        handleResponse(response = it.response, interviewModel = it.interview)
                        val index =
                            uploadList.indexOfFirst {
                                item -> item.matchId == work.matchId && item.localUri == work.localUri
                            }

                        try {
                            uploadList[index] = uploadList[index].copy(isDone = true)
                        } catch (e: ConcurrentModificationException) {
                            Log.e(TAG, e.localizedMessage, e)
                        }

                        try {
                            Log.d(TAG, "-> Deleting file for interview ${work.matchId} @ ${work.localUri}")
                            if (File(work.localUri).exists()) {
                                File(work.localUri).delete()
                            }
                        } catch (e: NullPointerException) {
                            Log.e(TAG, "-> Failed Deleting file for interview ${work.matchId} @ ${work.localUri}", e)
                        }
                        // Safely update shared list inside lock
//                        mutex.withLock {
//                            val index = uploadList.indexOfFirst {
//                                    item -> item.videoId == work.videoId && item.localUri == work.localUri
//                            }
//                            if (index >= 0) {
//                                uploadList[index] = uploadList[index].copy(isDone = true)
//                            }
//                        }
                    }
                    is UploadSuccess -> {}
                }
            }

            val hasMoreUploads = pendingWorkList.value.any {
                it != work && it.matchId == work.matchId && (it is Work.UploadVideo || it is Work.UploadInterviewVideo)
            }
            if (!hasMoreUploads) {
                Log.d(TAG, "-> All uploads are done")
            }
        }
    }

    private fun handleResponse(response: UniversalObject?, reactionModel: ReactionsBean?) {
        try {
            val mBean = response?.response as? CommonBean
            if (mBean != null && mBean.videos != null) {
                val broadcastIntent = Intent(MatchDetailActivity.INTENT_UPLOAD_VIDEO)
                broadcastIntent.action = MatchDetailActivity.INTENT_ACTION_UPLOAD
                broadcastIntent.putExtra(MatchDetailActivity.VIDEO_PARAMETER, mBean.videos)
                broadcastIntent.putExtra(MatchDetailActivity.TYPE_PARAMETER, VideoType.reaction)
                broadcastIntent.putExtra(MatchDetailActivity.LOCAL_VIDEO_PARAMETER, reactionModel)

                context.sendBroadcast(broadcastIntent)

                if (reactionModel == null) return

                databaseManager.executeQuery { database ->
                    val dao = MatchActionsDAO(database, context)
                    dao.deleteAll(reactionModel.id)
                }
            }

        } catch (ex: JsonIOException) {
            Log.e(TAG, "GSON Error: ${ex.localizedMessage}")
        } catch (ex: IllegalStateException) {
            Log.e(TAG, "Database Error: ${ex.localizedMessage}")
        } catch (ex: SQLException) {
            Log.e(TAG, "Query Error: ${ex.localizedMessage}")
        } catch (ex: SecurityException) {
            Log.e(TAG, "Access Denied: ${ex.localizedMessage}")
        }
    }

    private  fun handleResponse(response: UniversalObject?, interviewModel: InterviewBean?) {
        try {
            val mBean = response?.response as? CommonBean
            if (mBean != null && mBean.status == 1) {
                val broadcastIntent = Intent(MatchDetailActivity.INTENT_UPLOAD_VIDEO)
                broadcastIntent.action = MatchDetailActivity.INTENT_ACTION_UPLOAD
                val interViewVideoBean = VideosBean()
                interViewVideoBean.video = mBean.videos?.video
                interViewVideoBean.local_id = mBean.localId
                interViewVideoBean.match_id = mBean.videos.match_id
                interViewVideoBean.thumbnail = mBean.videos?.thumbnail
                broadcastIntent.putExtra(MatchDetailActivity.VIDEO_PARAMETER, interViewVideoBean)
                broadcastIntent.putExtra(MatchDetailActivity.TYPE_PARAMETER, VideoType.interview)
                broadcastIntent.putExtra(MatchDetailActivity.LOCAL_VIDEO_PARAMETER, interviewModel)

                context.sendBroadcast(broadcastIntent)

                if (interviewModel == null) return

                databaseManager.executeQuery { database ->
                    val dao = InterviewsDAO(database, context)
                    dao.deleteAll(interviewModel.id)
                }
            }
        } catch (ex: IllegalStateException) {
            Log.e(TAG, "Database Error: ${ex.localizedMessage}")
        } catch (ex: SQLException) {
            Log.e(TAG, "Query Error: ${ex.localizedMessage}")
        } catch (ex: SecurityException) {
            Log.e(TAG, "Access Denied: ${ex.localizedMessage}")
        }
    }

    sealed class Work {
        abstract val matchId: Long
        abstract val localUri: String

        data class UploadVideo(
            override val matchId: Long,
            override val localUri: String,
            val reactionModel: ReactionsBean,
        ) : Work()

        data class UploadInterviewVideo(
            override val matchId: Long,
            override val localUri: String,
            val interviewModel: InterviewBean,
        ) : Work()
    }

    /**
     * This class is only used to be able to count total of uploads, and number of completed ones, and update
     * notification accordingly
     */
    data class VideoUploadEntry(
        val matchId: Long,
        val localUri: String,
        val isDone: Boolean = false
    )

    enum class VideoType: Serializable {
        reaction,
        interview
    }
}
