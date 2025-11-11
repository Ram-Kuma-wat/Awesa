package com.game.awesa.utils

import android.content.Context
import android.util.Log
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.matches.InterviewBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.ApiRequest
import com.codersworld.awesalibs.rest.RetrofitRequest
import com.codersworld.awesalibs.rest.RetrofitUtils
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.ProgressCallback
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.R
import com.game.awesa.utils.VideoUploadsRepository.UploadResult.UploadFailure
import com.game.awesa.utils.VideoUploadsRepository.UploadResult.UploadSuccess
import com.game.awesa.utils.VideoUploadsRepository.UploadResult.UploadInterviewSuccess
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onEach
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import javax.inject.Inject

class VideoUploadsRepository @Inject constructor(
    private val context: Context,
    private val databaseManager: DatabaseManager
): OnResponse<UniversalObject> {

    companion object {
        const val TAG = "VideoUploadsRepository"
    }

    fun uploadMedia(reactionModel: ReactionsBean): Flow<UploadResult> {
        return callbackFlow {
            Log.d(TAG, "Dispatching request to upload ${reactionModel.video}")

            if (reactionModel.video.isNullOrEmpty()) {
                trySend(UploadFailure(error = MediaUploadException(
                        model = reactionModel,
                        errorMessage = "Missing video url")
                    )
                )
                close()
                return@callbackFlow
            }

            val userId = UserSessions.getUserInfo(context).id.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val reactionId = reactionModel.id.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val matchId = reactionModel.match_id.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val teamId = reactionModel.team_id.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val time = reactionModel.time.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val reaction = reactionModel.reaction.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val half = reactionModel.half.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val mVideoPart = RetrofitUtils.createFilePart(
                "video",
                File(reactionModel.video).absolutePath,
                "video/mp4".toMediaTypeOrNull()
            )

            val mRequest = RetrofitRequest.getRetrofitInstance(1, "").create(
                ApiRequest::class.java
            )

            try {
                mRequest.makeActions(
                    userId,
                    reactionId,
                    matchId,
                    teamId,
                    time,
                    reaction,
                    half,
                    mVideoPart,
                    object : ProgressCallback {
                        override fun onSuccess(file: String) {
                            Log.d(TAG, file)
                        }

                        override fun onError(error: Exception) {
                            Log.e(TAG, error.localizedMessage, error)
                        }

                        override fun onProgress(progress: Long) {
                            trySend(UploadResult.UploadProgress(progress = (progress / 100.0).toFloat()))
                        }
                    }
                ).enqueue(object : Callback<CommonBean> {
                    override fun onFailure(call: Call<CommonBean>,  t: Throwable) {
                        this@VideoUploadsRepository.onError(
                            Tags.SB_CREATE_MATCH_ACTION_API,
                            t.localizedMessage
                        )

                        if (t is java.io.FileNotFoundException) {
                            trySend(UploadFailure(
                                error = MediaUploadException(
                                    model = reactionModel,
                                    errorMessage = "Missing video url")
                                )
                            )
                            close()
                        }
                    }

                    override fun onResponse(call: Call<CommonBean>, response: Response<CommonBean>) {
                        if (response.isSuccessful) {
                            Log.d(TAG, "Video uploaded successfully")
                            try {
                                Log.d(TAG, response.body().toString())
                                val responseObject = UniversalObject(
                                    result = response.body(),
                                    methodName = Tags.SB_CREATE_MATCH_ACTION_API,
                                    status =  true,
                                    msg = Gson().toJson(reactionModel)
                                )

                                this@VideoUploadsRepository.onSuccess(responseObject)

                                trySend(UploadSuccess(response = responseObject, reaction = reactionModel))
                                close()

                            } catch (e: java.lang.Exception) {
                                this@VideoUploadsRepository.onError(
                                    Tags.SB_CREATE_MATCH_ACTION_API,
                                    e.localizedMessage
                                )
                            }
                        } else {
                            this@VideoUploadsRepository.onError(
                                Tags.SB_CREATE_MATCH_ACTION_API,
                                context.resources.getString(R.string.something_wrong)
                            )
                        }
                    }
                })
            } catch (e: IOException) {
                this@VideoUploadsRepository.onError(
                    Tags.SB_CREATE_MATCH_ACTION_API,
                    e.localizedMessage
                )
            }

            awaitClose {}
        }.onEach {
            if (it is UploadSuccess) {
                try {
                    Log.d(TAG, "UploadSuccess for ${reactionModel.video}")
                    if (File(reactionModel.video).exists()) {
                        File(reactionModel.video).delete()
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "Permission denied for file ${e.localizedMessage}", e)
                } catch (e: java.lang.NullPointerException) {
                    Log.e(TAG, "UploadSuccess for ${e.localizedMessage}", e)
                }
            }

            if (it is UploadFailure) {
                Log.d(TAG, "UploadFailure for ${reactionModel.video}")
            }
        }
    }

    fun uploadMedia(interviewModel: InterviewBean): Flow<UploadResult> {
        return callbackFlow {
            Log.d(TAG, "Dispatching request to upload ${interviewModel.video}")

            if (!CommonMethods.isValidString(interviewModel.video)) {
                trySend(UploadFailure(
                    error = MediaUploadException(
                        model = interviewModel,
                        errorMessage = "Missing video url")
                )
                )
                close()
                return@callbackFlow
            }

            val userId = UserSessions.getUserInfo(context).id.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val matchId = interviewModel.match_id.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val mVideo = RetrofitUtils.createFilePart(
                "video",
                File(interviewModel.video).absolutePath,
                "video/mp4".toMediaTypeOrNull()
            )

            val mRequest = RetrofitRequest.getRetrofitInstance(1, "").create(
                ApiRequest::class.java
            )

            try {
                mRequest.uploadInterview(
                    userId,
                    matchId,
                    mVideo,
                    object : ProgressCallback {
                        override fun onSuccess(file: String) {
                            Log.d(TAG, file)
                        }

                        override fun onError(error: Exception) {
                            Log.d(TAG, error.localizedMessage, error)
                        }

                        override fun onProgress(progress: Long) {
                            trySend(UploadResult.UploadProgress(progress = (progress / 100.0).toFloat()))
                        }
                    }
                ).enqueue(object : Callback<CommonBean> {
                    override fun onFailure(call: Call<CommonBean>,  t: Throwable) {
                        this@VideoUploadsRepository.onError(
                            Tags.SB_UPLOAD_INTERVIEW_API,
                            t.localizedMessage
                        )

                        if (t is java.io.FileNotFoundException) {
                            trySend(UploadFailure(
                                error = MediaUploadException(
                                    model = interviewModel,
                                    errorMessage = "Missing video url")
                            )
                            )
                        }
                    }

                    override fun onResponse(call: Call<CommonBean>, response: Response<CommonBean>) {
                        if (response.isSuccessful) {
                            Log.d(TAG,"Video uploaded successfully")
                            try {
                                val responseObject = UniversalObject(
                                    result = response.body(),
                                    methodName = Tags.SB_UPLOAD_INTERVIEW_API,
                                    status =  true,
                                    msg = Gson().toJson(interviewModel)
                                )

                                this@VideoUploadsRepository.onSuccess(responseObject)

                                trySend(UploadInterviewSuccess(response = responseObject, interview = interviewModel))
                                close()

                            } catch (e: java.lang.Exception) {
                                this@VideoUploadsRepository.onError(
                                    Tags.SB_UPLOAD_INTERVIEW_API,
                                    e.localizedMessage
                                )
                            }
                        } else {
                            this@VideoUploadsRepository.onError(
                                Tags.SB_UPLOAD_INTERVIEW_API,
                                context.resources.getString(R.string.something_wrong)
                            )
                        }
                    }

                })
            } catch (e: Exception) {
                this@VideoUploadsRepository.onError(
                    Tags.SB_CREATE_MATCH_ACTION_API,
                    e.localizedMessage
                )
            }

            awaitClose {}
        }.onEach {
            if (it is UploadFailure) {
                Log.d(TAG, "UploadFailure for ${interviewModel.video}")
            }
        }
    }

    fun getMatchReactions(matchId: String?) : Flow<ArrayList<ReactionsBean>?> {
        return callbackFlow {
            databaseManager.executeQuery { database ->
                val dao = MatchActionsDAO(database, context)
                val list = dao.selectAllUploaded(matchId, "", 2,"")
                trySend(list)
                close()
            }

            awaitClose {}
        }
    }

    fun getMatchInterviews(matchId: String?) : Flow<ArrayList<InterviewBean>?> {
        return callbackFlow {
            databaseManager.executeQuery { database ->
                val dao = InterviewsDAO(database, context)
                val list = dao.selectAllUploaded(matchId)

                trySend(list)
                close()
            }

            awaitClose {}
        }
    }

    override fun onSuccess(response: UniversalObject?) {
        Log.e(TAG, response?.msg.toString())
    }

    override fun onError(type: String?, error: String?) {
        Log.e(TAG, error.toString())
    }

    class MediaUploadException(
        val model: Any? = null,
        val errorMessage: String
    ) : Exception()

    sealed class UploadResult {
        data class UploadProgress(val progress: Float) : UploadResult()
        data class UploadSuccess(val response: UniversalObject?, val reaction: ReactionsBean) : UploadResult()
        data class UploadInterviewSuccess(val response: UniversalObject?, val interview: InterviewBean) : UploadResult()
        data class UploadFailure(val error: MediaUploadException) : UploadResult()
    }
}
