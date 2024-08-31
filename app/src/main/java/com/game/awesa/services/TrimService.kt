package com.game.awesa.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arthenica.mobileffmpeg.FFmpeg
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.DBVideoUplaodDao
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.database.dao.VideoMasterDAO
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.utils.VideosNotificationHandler
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.lang.Boolean
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import kotlin.Exception
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.arrayOf

/**
 * Service which trims recorded videos according to reactions
 */
@AndroidEntryPoint
class TrimService : Service() {
    var list: ArrayList<DBVideoUplaodDao> = ArrayList()
    private var strMatchId = ""

    companion object {
        private const val TAG = "TrimService"
    }

    @Inject lateinit var databaseManager: DatabaseManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"Trimming Service Create")

        getSingleAction()
    }

    private fun getDuration(path: String): Long {
        var mMediaPlayer: MediaPlayer? = null
        var duration: Long = 0
        try {
            mMediaPlayer = MediaPlayer()
            mMediaPlayer.setDataSource(applicationContext, Uri.parse(path))
            mMediaPlayer.prepare()
            duration = mMediaPlayer.duration.toLong()
        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage)
        } finally {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset()
                mMediaPlayer.release()
                mMediaPlayer = null
            }
        }
        return duration
    }

    private fun trim(mDao: DBVideoUplaodDao, mBeanReaction: ReactionsBean) {
        videoLength = getDuration(mDao.video_path)
        var video_path = mDao.video_path
        var start: Long = 0
        var end: Long = 0
        val time = mBeanReaction.time.split(":".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        if (time.size > 0) {
            start = (time[0].toLong() * 40 * 1000) + (time[1].toLong() * 1000)
        }

        if ((start + 3000) > videoLength) {
            end = videoLength
        } else {
            end = start + 3000
        }

        if ((start - 12000) < 0) {
            start = 0
        } else {
            start = start - 12000
        }

        var mediaFile: File? = null;
        var timeStamp1 = SimpleDateFormat("dd MMM yyyy").format(Date())
        var mediaStorageDir: File? = null
        if (Build.VERSION.SDK_INT >= 30) {
            mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Awesa/trim/" + timeStamp1
            )
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    //return null
                }
            }
        } else {
            mediaStorageDir = getExternalFilesDir("Awesa/trim/" + timeStamp1)
            mediaStorageDir!!.mkdirs()
            if (!mediaStorageDir!!.exists()) {
                if (!mediaStorageDir!!.mkdirs()) {
                }
            }
        }
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Toast.makeText(
                    applicationContext,
                    "Please Allow Storage Permission.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // For unique video file name appending current timeStamp with file name
        mediaFile =
            File(mediaStorageDir.path + File.separator + "m_" + mDao.match_id + "_a_" + mBeanReaction.id.toString() + "_h_" + mDao.video_half + ".mp4")
        val fileName = mediaFile.toString()
            .substring(mediaFile.toString().lastIndexOf('/') + 1, mediaFile.toString().length)
        trimVideo(
            video_path,
            mediaFile.absolutePath,
            start,
            end,
            fileName,
            mediaFile,
            mBeanReaction.id,mDao
        )
    }

    private fun trimVideo(
        inputFilePath: String,
        outputFilePath: String,
        startMs: Long,
        endMs: Long,
        fileName: String,
        mediaFile: File,
        id: Int,
        mDao: DBVideoUplaodDao
    ) {
        val start = (startMs / 1000).toString()
        val duration = ((endMs - startMs) / 1000).toString()
        val cmd = arrayOf(
            "-i", inputFilePath,
            "-ss", start,
            "-t", duration,
            "-c", "copy",
            outputFilePath
        )
        FFmpeg.executeAsync(cmd) { executionId: Long, returnCode: Int ->
            if (returnCode == 0) {
                databaseManager.executeQuery1({ database ->
                    val actionDao = MatchActionsDAO(database, applicationContext)
                    actionDao.updateVideo(fileName, mediaFile.toString(), id);
                    getSingleAction()
                }, "processData" )

                // Trim successful
            } else {
                getSingleAction()
                // Trim failed
            }
            initVideoView(mDao)
        }
    }

    private var mListReaction: ArrayList<ReactionsBean> = ArrayList<ReactionsBean>()
    private var videoLength: Long = 0;

    private fun initVideoView(mDao: DBVideoUplaodDao) {
        videoLength = 0;
        val videoPath = mDao.video_path
        if (File(videoPath).exists()) {
            mListReaction = java.util.ArrayList()
            databaseManager.executeQuery1 ({ database ->
                val dao = MatchActionsDAO(database, applicationContext)
                mListReaction = dao.selectAllForDelete(mDao.match_id + "", mDao.video_half + "")
            },"Three")
            if (!CommonMethods.isValidArrayList(mListReaction)) {
                databaseManager.executeQuery1 ({ database ->
                    val dao1 = MatchActionsDAO(database, applicationContext)
                    mListReaction =
                        dao1.selectAllForDelete(mDao.match_id + "", mDao.video_half + "")
                    if (!CommonMethods.isValidArrayList(mListReaction)) {
                        val dao = VideoMasterDAO(database, applicationContext)
                        dao.deleteVideoById(mDao.getmId());
                        try {
                            val file: File = File(mDao.video_path)
                            file.delete()
                            if (file.exists()) {
                                file.canonicalFile.delete()
                                if (file.exists()) {
                                    applicationContext.deleteFile(file.name)
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                },"Four")
            }
        } else {
            databaseManager.executeQuery1( { database ->
                val dao = VideoMasterDAO(database, applicationContext)
                dao.deleteVideoById(mDao.getmId());
            },"Five")
        }
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
            if (intent.hasExtra("data_string")) {
                strMatchId = intent.getStringExtra("data_string") as String
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun getSingleAction() {
        mListReaction = ArrayList()
        databaseManager.executeQuery { database ->
            val dao = MatchActionsDAO(database, applicationContext)
            mListReaction = dao.selectAllForTrim(if(CommonMethods.isValidString(strMatchId)) strMatchId else "",1)
            if (CommonMethods.isValidArrayList(mListReaction)) {
                val dao1 = VideoMasterDAO(database, applicationContext)
                list = dao1.selectAll1(
                    mListReaction[0].match_id.toString(),
                    mListReaction[0].half.toString()
                )
                if (CommonMethods.isValidArrayList(list)) {
                    trim(list[0], mListReaction[0])
                    sendBroadCast("1")
                }else{
                    sendBroadCast("0")
                }
            } else {
                sendBroadCast("0")
                stopSelf()
            }
        }
    }

    private fun sendBroadCast(isTrim:String){
        try {
            val broadcastIntent = Intent("video_trim")
            broadcastIntent.putExtra("Data",Gson().toJson(mListReaction))
            broadcastIntent.putExtra("isTrim",isTrim.toString())
            Boolean.valueOf(LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent))
            sendBroadcast(broadcastIntent)
        } catch (ex:Exception) {
            Log.e(TAG, ex.localizedMessage)
        }

    }
}
