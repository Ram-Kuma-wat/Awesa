package com.game.awesa.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.beans.VideoUploadBean
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.database.dao.VideoMasterDAO
import com.game.awesa.utils.Media3Transformer
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

/**
 * Service which trims recorded videos according to reactions
 */

@AndroidEntryPoint
class TrimService : Service() {

    companion object {
        private val TAG = TrimService::class.java.simpleName
        const val EXTRA_MATCH_ID = "matchId"
        const val EXTRA_MATCH_HALF = "mHalf"
        const val EXTRA_MATCH_FILE = "mFile"
    }

    @Inject
    lateinit var media3Transformer: Media3Transformer

    @Inject lateinit var databaseManager: DatabaseManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"Trimming Service Create")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Trimming Service Destroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Trimming Service onStartCommand")
        if (intent == null) {
            // If the system restarts the service, we won't be able to restore the state of uploads,
            // so we'll just stop it
            stopSelf()
        } else {
            if (intent.hasExtra(EXTRA_MATCH_ID)
                && intent.hasExtra(EXTRA_MATCH_HALF)
                && intent.hasExtra(EXTRA_MATCH_FILE)) {
                getActions(
                    matchId = intent.getStringExtra(EXTRA_MATCH_ID) as String,
                    mHalf = intent.getIntExtra(EXTRA_MATCH_HALF, -1),
                    mFile = intent.getSerializableExtra(EXTRA_MATCH_FILE) as File
                )
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun getActions(matchId: String, mHalf: Int, mFile: File) {
        databaseManager.executeQuery { database ->
            val actionsDao = MatchActionsDAO(database, applicationContext)
            val mListReaction = actionsDao.selectAllForTrim(matchId) // 1 - LIMIT 1, 0 - ALL

            media3Transformer.trimVideo(
                matchId = matchId.toInt(),
                half = mHalf,
                inputUri = mFile.toUri(),
                actions = mListReaction
            )
        }
    }
}
