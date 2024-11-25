package com.game.awesa.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.game.awesa.di.AppCoroutineScope
import kotlinx.coroutines.CoroutineScope
import java.io.File
import javax.inject.Inject

@UnstableApi
class Media3Transformer @Inject constructor(
    private val context: Context,
    private val databaseManager: DatabaseManager,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope
) {

    companion object {
        private const val TAG = "Media3Transformer"
        private const val BYTE = 1_000
        private const val TRIM_START_SECONDS = 12 // -12 seconds
        private const val TRIM_END_SECONDS = 3 // +3 seconds
    }

    private val transformer: Transformer = Transformer.Builder(context).build()

    @SuppressLint("UnusedParameter")
    fun trimVideo(
        inputUri: Uri,
        outputDir: File,
        actions: List<ReactionsBean>
    ) {
        actions.forEachIndexed { _, reaction ->
            val startTime = maxOf(reaction.timestamp - TRIM_START_SECONDS, 0L) // -12 seconds
            val endTime = reaction.timestamp + TRIM_END_SECONDS // +3 seconds

            // Define output file for the trimmed segment
            val trimmedFileName = "m_" + reaction.match_id + "_a_" + reaction.id.toString() + "_h_" + reaction.half + ".mp4"
            val outputFile = File(outputDir, trimmedFileName)
            // For unique video file name appending current timeStamp with file name
            val fileName = outputFile.toString()
                .substring(outputFile.toString().lastIndexOf('/') + 1, outputFile.toString().length)
            val outputUri = Uri.fromFile(outputFile)

            // Prepare an MediaItem
            val editedMediaItem = MediaItem.Builder()
                .setUri(inputUri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(startTime)
                        .setEndPositionMs(endTime)
                        .build())
                .build()

            val trimmingListener = object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    val size = exportResult.fileSizeBytes / BYTE
                    val time = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(exportResult.durationMs)
                    Log.e(TAG,"Trimmed Video ${reaction.file_name} is ${size}KB, and ${time}seconds long")

                    databaseManager.executeQuery { database ->
                        val actionDao = MatchActionsDAO(database, context)
                        actionDao.updateVideo(fileName, outputFile.toString(), reaction.id)
                    }
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    Log.e(TAG, "Error trimming video: ${exportException.message}")
                }
            }

            transformer.addListener(trimmingListener)
            // Start the transformation
            transformer.start(editedMediaItem, outputUri.path.toString())
        }
    }
}
