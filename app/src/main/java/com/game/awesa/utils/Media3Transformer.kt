package com.game.awesa.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.database.dao.VideoMasterDAO
import com.game.awesa.di.AppCoroutineScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class Media3Transformer @Inject constructor(
    private val context: Context,
    private val databaseManager: DatabaseManager,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope
) {

    companion object {
        private val TAG = Media3Transformer::class.java.simpleName
        private const val BYTE = 1_000
        private const val MILLISECONDS = 1_000
        private const val TRIM_START_SECONDS = 12 // -12 seconds
        private const val TRIM_END_SECONDS = 3 // +3 seconds
    }

    @SuppressLint("UnusedParameter")
    fun trimVideo(
        matchId: Int,
        half: Int,
        inputUri: Uri,
        actions: List<ReactionsBean>
    ) {

        val handler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG, "exception: ${exception.localizedMessage}", exception)
        }
        if (inputUri.path == null) {
            databaseManager.executeQuery { database ->
                val dao = VideoMasterDAO(database, context)
                dao.deleteVideoByMatch(matchId, half)
            }
            return
        }

        var actionCount: Int = actions.size

        if (actionCount == 0) {
            val videoFile = File(inputUri.path!!)
            if (videoFile.exists()) {
                databaseManager.executeQuery { database ->
                    val dao = VideoMasterDAO(database, context)
                    dao.deleteVideoByMatch(matchId, half)
                    videoFile.delete()
                }
            }
            return
        }

        appCoroutineScope.launch(handler) {
            actions.forEachIndexed { _, reaction ->
                try {
                    val exported = exportVideo(reaction, inputUri)

                    if (exported) {
                        actionCount -= 1
                    }

                } catch (ex: CancellationException) {
                    Log.e(TAG, "Error trimming video: ${ex.localizedMessage}")
                } catch (ex: IllegalStateException) {
                    Log.e(TAG, "Error trimming video: ${ex.localizedMessage}")
                } catch (ex: IOException) {
                    Log.e(TAG, "Error trimming video: ${ex.localizedMessage}")
                }
            }
        }.invokeOnCompletion { error ->
            if (actionCount == 0) {
                val videoFile = File(inputUri.path)
                if (videoFile.exists()) {
                    databaseManager.executeQuery { database ->
                        val dao = VideoMasterDAO(database, context)
                        dao.deleteVideoByMatch(matchId, half)
                        videoFile.delete()
                    }
                }
                return@invokeOnCompletion
            }
            Log.i(TAG, "${error?.localizedMessage}")
        }
    }

    /**
     * Trims a video segment synchronously.
     */
    private suspend fun exportVideo(
        reaction: ReactionsBean,
        inputUri: Uri
    ): Boolean {
        val startTime = maxOf(reaction.timestamp - TRIM_START_SECONDS, 0L) // -12 seconds
        val endTime = reaction.timestamp + TRIM_END_SECONDS // +3 seconds

        val outputDir = createMediaFolder()

        // Define output file for the trimmed segment
        val trimmedFileName = StringBuilder()
        trimmedFileName.append(outputDir.path + File.separator)
        trimmedFileName.append("m_" + reaction.match_id + "_a_")
        trimmedFileName.append(reaction.id.toString() + "_h_" + reaction.half + ".mp4")

        val outputFile = File(trimmedFileName.toString())
        // For unique video file name appending current timeStamp with file name
        val fileName = outputFile.toString()
            .substring(outputFile.toString().lastIndexOf('/') + 1, outputFile.toString().length)

        // Start the transformation and wait for it to complete
        return suspendCancellableCoroutine<Boolean> { continuation ->
            val cmd = arrayOf(
                "-i", inputUri.path,
                "-y",
                "-ss", startTime.toString(),
                "-to", endTime.toString(),
                "-c", "copy",
                outputFile.absolutePath
            )

            val session: FFmpegSession = FFmpegKit.executeWithArguments(cmd)

            if (ReturnCode.isSuccess(session.returnCode)) {
                databaseManager.executeQuery { database ->
                    val actionDao = MatchActionsDAO(database, context)
                    actionDao.updateVideo(fileName, outputFile.toString(), reaction.id)
                }
                continuation.resume(true)
            } else if (ReturnCode.isCancel(session.returnCode)) {
                continuation.resumeWithException(IOException("Export canceled"))
            } else {
                val msg =
                    "Command failed with state ${session.state} and rc ${session.returnCode}.${session.failStackTrace}"
                continuation.resumeWithException(IOException(msg))
            }

        }
    }

    private fun createMediaFolder(): File {
        val timeStamp = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
        var mediaStorageDir: File? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "/Awesa/trim/$timeStamp"
            )
        } else {
            mediaStorageDir = context.getExternalFilesDir("/Awesa/trim/$timeStamp")
        }
        if (!mediaStorageDir!!.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Toast.makeText(
                    context,
                    "Please Allow Storage Permission.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        return mediaStorageDir
    }
}
