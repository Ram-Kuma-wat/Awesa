package com.game.awesa.services

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Service
import android.content.Intent
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.DBVideoUplaodDao
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.database.dao.VideoMasterDAO
import com.codersworld.awesalibs.mp4compose.FillMode
import com.codersworld.awesalibs.mp4compose.composer.Mp4Composer
import com.codersworld.awesalibs.utils.CommonMethods
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSink.DEFAULT_BUFFER_SIZE
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date


class TrimService : Service() {
    var list: ArrayList<DBVideoUplaodDao> = ArrayList()


    override fun onCreate() {
        super.onCreate()
        CommonMethods.showLog("Triming Service Create")
        DatabaseManager.initializeInstance(DatabaseHelper(applicationContext))
        getSingleAction()
        fetchMatch(5)
        stopSelf()
    }

    fun fetchMatch(type: Int) {
        if (type >= 0) {
            DatabaseManager.getInstance().executeQuery { database ->
                val dao = VideoMasterDAO(database, applicationContext)
                list = dao.selectAll()
            }
            if (CommonMethods.isValidArrayList(list)) {
                initVideoView(list[0])
                return
            }
        }
    }

    fun getDuration(path: String): Long {
        var mMediaPlayer: MediaPlayer? = null
        var duration: Long = 0
        try {
            mMediaPlayer = MediaPlayer()
            mMediaPlayer.setDataSource(applicationContext, Uri.parse(path))
            mMediaPlayer.prepare()
            duration = mMediaPlayer.duration.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset()
                mMediaPlayer.release()
                mMediaPlayer = null
            }
        }
        return duration
    }

    fun trim(mDao: DBVideoUplaodDao, mBeanReaction: ReactionsBean) {
        videoLength = getDuration(mDao.video_path)
        var video_path = mDao.video_path
        var start: Long = 0
        var end: Long = 0
        val time = mBeanReaction.time.split(":".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        if (time.size > 0) {
            start = (time[0].toLong() * 60 * 1000) + (time[1].toLong() * 1000)
        }
        if ((start + 1000) > videoLength) {
            end = videoLength
        } else {
            end = start + 1000
        }
        if ((start - 9000) < 0) {
            start = 0
        } else {
            start = start - 9000
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
        val date = Date()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss")
            .format(date.time)
        // For unique video file name appending current timeStamp with file name
        mediaFile =
            File(mediaStorageDir.path + File.separator + "m_" + mDao.match_id + "_a_" + mBeanReaction.id.toString() + "_h_" + mDao.video_half + ".mp4")
        var fileName = mediaFile.toString()
            .substring(mediaFile.toString().lastIndexOf('/') + 1, mediaFile.toString().length)
        genVideoUsingMuxer(
            video_path,
            mediaFile.absolutePath,
            start,
            end,
            fileName,
            mediaFile,
            mBeanReaction.id
        )
    }


    @SuppressLint("WrongConstant")
    @Throws(IOException::class)
    private fun genVideoUsingMuxer(
        srcPath: String,
        dstPath: String,
        startMs: Long,
        endMs: Long,
        fileName: String,
        mediaFile: File,
        id: Int
    ) {
        var useAudio = true;
        var useVideo = true;
        // Set up MediaExtractor to read from the source.
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(srcPath)
        } catch (ex1: Exception) {
            try {
                extractor.setDataSource(srcPath, null)
            } catch (ex2: Exception) {
                // try{
                extractor.setDataSource(applicationContext, Uri.fromFile(File(srcPath)), null)
                /* }catch (ex3:Exception){
                     ex3.printStackTrace()
                 }*/
            }
        }
        //get the track counts from the recorded video by the external path
        val trackCount = extractor.trackCount
        // Set up MediaMuxer for the destination.
        val muxer: MediaMuxer
        muxer = MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        // Set up the tracks and retrieve the max buffer size for selected
        // tracks.
        val indexMap = HashMap<Int, Int>(trackCount)
        var bufferSize = -1
        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            var selectCurrentTrack = false
            if (mime!!.startsWith("audio/") && useAudio) {
                selectCurrentTrack = true
            } else if (mime.startsWith("video/") && useVideo) {
                selectCurrentTrack = true
            }
            if (selectCurrentTrack) {
                extractor.selectTrack(i)
                val dstIndex = muxer.addTrack(format)
                indexMap[i] = dstIndex
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    val newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    bufferSize = if (newSize > bufferSize) newSize else bufferSize
                }
            }
        }

        if (bufferSize < 0) {
            bufferSize = CacheDataSink.DEFAULT_BUFFER_SIZE
        }
        // Set up the orientation and starting time for extractor.
        val retrieverSrc = MediaMetadataRetriever()
        retrieverSrc.setDataSource(srcPath)
        val degreesString = retrieverSrc.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION
        )
        if (degreesString != null) {
            val degrees = degreesString.toInt()
            if (degrees >= 0) {
                muxer.setOrientationHint(degrees)
            }
        }

        if (startMs > 0) {
            extractor.seekTo((startMs * 1000).toLong(), MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        }
        // Copy the samples from MediaExtractor to MediaMuxer. We will loop
        // for copying each sample and stop when we get to the end of the source
        // file or exceed the end time of the trimming.
        val offset = 0
        var trackIndex = -1
        val dstBuf = ByteBuffer.allocate(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()
        try {
            muxer.start()
            while (true) {
                bufferInfo.offset = offset
                bufferInfo.size = extractor.readSampleData(dstBuf, offset)
                if (bufferInfo.size < 0) {
                    Log.d("TRIM", "Saw input EOS.")
                    bufferInfo.size = 0
                    break
                } else {
                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    if (endMs > 0 && bufferInfo.presentationTimeUs > (endMs * 1000)) {
                        Log.d("TRIM", "The current sample is over the trim end time.")
                        // trim("00:10")

                        break
                    } else {
                        bufferInfo.flags = extractor.sampleFlags
                        trackIndex = extractor.sampleTrackIndex
                        muxer.writeSampleData(
                            indexMap[trackIndex]!!, dstBuf,
                            bufferInfo
                        )
                        extractor.advance()
                    }
                }
            }
            muxer.stop()
            DatabaseManager.getInstance().executeQuery { database ->
                val actionDao = MatchActionsDAO(database, applicationContext)
                actionDao.updateVideo(fileName, mediaFile.toString(), id);
                getSingleAction()
            }
            //deleting the old file
            val file = File(srcPath)
            file.delete()
        } catch (e: IllegalStateException) {
            // Swallow the exception due to malformed source.
            Log.w("TRIM", "The source video file is malformed")
        } finally {
            muxer.release()
        }
        return
    }


    var mListReaction: ArrayList<ReactionsBean> = ArrayList<ReactionsBean>()
    var videoLength: Long = 0;

    fun initVideoView(mDao: DBVideoUplaodDao) {
        videoLength = 0;
        var video_path = mDao.video_path
        if (File(video_path).exists()) {
            videoLength = getDuration(video_path)
            mListReaction = java.util.ArrayList()
            DatabaseManager.getInstance().executeQuery { database ->
                val dao = MatchActionsDAO(database, applicationContext)
                mListReaction = dao.selectAll(mDao.getMatch_id() + "", mDao.getVideo_half() + "", 0)
            }
            if (!CommonMethods.isValidArrayList(mListReaction)) {
                DatabaseManager.getInstance().executeQuery { database ->
                    val dao1 = MatchActionsDAO(database, applicationContext)
                    mListReaction =
                        dao1.selectAll(mDao.getMatch_id() + "", mDao.getVideo_half() + "", 1)
                    if (!CommonMethods.isValidArrayList(mListReaction)) {
                        val dao = VideoMasterDAO(database, applicationContext)
                        if (dao1.getTotalCount(mDao.getMatch_id() + "") == 0) {
                            dao.deleteVideoById(mDao.getmId());
                            try {
                                var file: File = File(mDao.video_path)
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
                    }
                    fetchMatch(5)
                }
            }
        } else {
            DatabaseManager.getInstance().executeQuery { database ->
                val dao = VideoMasterDAO(database, applicationContext)
                dao.deleteVideoById(mDao.getmId());
                fetchMatch(5)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        CommonMethods.showLog("Triming Service Destroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CommonMethods.showLog("Triming Service onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    fun getSingleAction() {
        mListReaction = ArrayList()
        DatabaseManager.getInstance().executeQuery { database ->
            val dao = MatchActionsDAO(database, applicationContext)
            // mListReaction = dao.selectAll(mDao.getMatch_id() + "",  "2")
            mListReaction = dao.selectSingle(0)
            if (CommonMethods.isValidArrayList(mListReaction)) {
                val dao1 = VideoMasterDAO(database, applicationContext)
                //list = dao1.selectAll1("","")
                list = dao1.selectAll1(
                    mListReaction[0].match_id.toString(),
                    mListReaction[0].half.toString()
                )
                // CommonMethods.successToast(applicationContext,list.size.toString())
                if (CommonMethods.isValidArrayList(list)) {
                    trim(list[0], mListReaction[0])
                }
            } else {
                Log.e("errrr", "errrrrrr")
            }
        }
    }

}