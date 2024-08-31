package com.game.awesa.ui.recorder

import android.content.Intent
import android.content.res.Configuration
import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.arthenica.mobileffmpeg.FFmpeg
import com.codersworld.awesalibs.beans.matches.InterviewBean
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.DBVideoUplaodDao
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.listeners.QueryExecutor
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivitySplashBinding
import com.game.awesa.services.TrimService
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class ProcessingActivity : AppCompatActivity() {
    @Inject lateinit var databaseManager: DatabaseManager
    lateinit var binding: ActivitySplashBinding
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //    ApiHelper.setApplicationlanguage(this, UserSessions().getLanguage(this))
    }
    var mTimer: CountDownTimer? = null

    var mMatchBean: MatchesBean.InfoBean? = null;
    var mListReaction: ArrayList<ReactionsBean> = ArrayList<ReactionsBean>()
    var videoLength: Long = 0;
    lateinit var firstHalf:DBVideoUplaodDao
    lateinit var secondHalf:DBVideoUplaodDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.img.visibility = View.GONE
        binding.llProgress.visibility = View.VISIBLE

        if (intent.hasExtra("mMatchBean")) {
            mMatchBean = CommonMethods.getSerializable(intent,"mMatchBean",MatchesBean.InfoBean::class.java)
            CommonMethods.checkServiceWIthData(this@ProcessingActivity, TrimService::class.java,mMatchBean!!.id.toString())
            databaseManager.executeQuery1(QueryExecutor {
                var mInterviewsDAO = InterviewsDAO(it, this@ProcessingActivity)
                var mList = mInterviewsDAO.selectAll(mMatchBean!!.id.toString()) as ArrayList<InterviewBean>
                databaseManager.closeDatabase()
               // Log.e("mListInterview",Gson().toJson(mList))
                if (CommonMethods.isValidArrayList(mList)) {
                     var timeStamp1 = SimpleDateFormat("dd MMM yyyy").format(Date())
                    var mediaStorageDir: File? = null
                    if (Build.VERSION.SDK_INT >= 30) {
                        mediaStorageDir = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "Awesa/interview/" + timeStamp1
                        )
                        if (!mediaStorageDir.exists()) {
                            if (!mediaStorageDir.mkdirs()) {
                                //return null
                            }
                        }
                    } else {
                        mediaStorageDir = getExternalFilesDir("Awesa/interview/" + timeStamp1)
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

                  //  moveFile(mList[0].video,mediaStorageDir.absolutePath);


                    // Locate the source file
                    var sourceFile:File  =  File(mList[0].video);
                    // Ensure the destination directory exists
                    var destDir:File  =  File(mediaStorageDir.absolutePath);
                    if (!destDir.exists()) {
                        destDir.mkdirs(); // Create the directory if it does not exist
                    }

                    // Define the destination file path
                    var destFile:File  =  File(destDir, sourceFile.getName());

                    // Move the file
                    var success: Boolean  = sourceFile.renameTo(destFile);

                    var fileName = destFile.toString()
                        .substring(destFile.toString().lastIndexOf('/') + 1, destFile.toString().length)
                    mInterviewsDAO.updateVideo(fileName, destFile.toString(), mList[0].id);


                }
            },"MatchOverview1")


/*
            databaseManager.executeQuery { database ->
                val dao = MatchActionsDAO(database, applicationContext)

                 mListReaction = dao.selectAllForTrim(mMatchBean!!.id.toString(),0)
                if (CommonMethods.isValidArrayList(mListReaction)) {
                    val dao1 = VideoMasterDAO(database, applicationContext)
                    var list = dao1.selectAll1(mListReaction[0].match_id.toString(),"")
                    Log.e("listlist",mListReaction.size.toString()+"=>"+Gson().toJson(list))
                    if (CommonMethods.isValidArrayList(list) && list.size>0){
                        if (list.size>0){
                            firstHalf = list[0]
                        }
                        if (list.size>1){
                            secondHalf = list[1]
                        }
                        trimData()
                    }
                }else{
                    val intent = Intent(this@ProcessingActivity, MatchOverviewActivity::class.java)
                    intent.putExtra("mMatchBean", mMatchBean)
                    startActivity(intent)

                }
            }
*/
        }
        mTimer = object : CountDownTimer(2500, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }
            //storage/emulated/0/Pictures/Awesa/trim/31 Jul 2024/m_8_a_14_h_2.mp4
            override fun onFinish() {
                mTimer!!.cancel()
                val intent = Intent(this@ProcessingActivity, MatchOverviewActivity::class.java)
                //val intent = Intent(requireActivity(), ProcessingActivity::class.java)
                intent.putExtra("mMatchBean", mMatchBean)
                startActivity(intent)
                finish()

            }
        }//.start()
        mTimer!!.start()
    }
    fun moveFile(sourceFilePath : String , destDirPath:String ):Boolean {
        // Locate the source file
        var sourceFile:File  =  File(sourceFilePath);
        // Ensure the destination directory exists
        var destDir:File  =  File(destDirPath);
        if (!destDir.exists()) {
            destDir.mkdirs(); // Create the directory if it does not exist
        }

        // Define the destination file path
        var destFile:File  =  File(destDir, sourceFile.getName());

        // Move the file
        var success: Boolean  = sourceFile.renameTo(destFile);

        return success;
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

    var counter=0;
    fun trimData(){
        if (counter<mListReaction.size) {
            var mBeanReaction =  mListReaction[counter]
            //Log.e("mHalfs",mBeanReaction.half.toString())
            var mVideo:DBVideoUplaodDao = if(mBeanReaction.half==1) firstHalf else secondHalf
            if (mVideo !=null && CommonMethods.isValidString(mVideo.video_path)){
                videoLength = getDuration(mVideo.video_path)

                var video_path = mVideo.video_path
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
                // For unique video file name appending current timeStamp with file name
                mediaFile =
                    File(mediaStorageDir.path + File.separator + "m_" + mVideo.match_id + "_a_" + mBeanReaction.id.toString() + "_h_" + mVideo.video_half + ".mp4")
                var fileName = mediaFile.toString()
                    .substring(mediaFile.toString().lastIndexOf('/') + 1, mediaFile.toString().length)




                try {
                    trimVideo1(
                        video_path,
                        mediaFile.absolutePath,
                        start,  // start time in milliseconds
                        end, // end time in milliseconds
                        fileName,
                        mediaFile,
                        mBeanReaction.id
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }else{
            val intent = Intent(this@ProcessingActivity, MatchOverviewActivity::class.java)
            intent.putExtra("mMatchBean", mMatchBean)
            startActivity(intent)

        }
    }
     var BUFFER_SIZE: Int = 1024 * 256 // 256KB buffer size
    fun trimVideo1(inputFilePath: String, outputFilePath: String, startMs: Long, endMs: Long,fileName: String, mediaFile: File,id: Int) {
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
                    counter = counter + 1
                    trimData()
                }, "processData" + counter)

                // Trim successful
            } else {
                trimData()
                // Trim failed
            }
        }
    }

    @Throws(IOException::class)
    fun trimVideo(inputFilePath: String, outputFilePath: String?, startMs: Long, endMs: Long,fileName: String, mediaFile: File,id: Int) {
        val extractor = MediaExtractor()
        extractor.setDataSource(inputFilePath)
        val trackIndex = selectTrack(extractor)
        try {
            //Log.e("outputFilePath", outputFilePath!!)
            if (trackIndex >= 0) {
/*
                Log.e("MDDDTT","No video track found in $inputFilePath")
            }else {
*/
                extractor.selectTrack(trackIndex)

                val format = extractor.getTrackFormat(trackIndex)
                val muxer =
                    MediaMuxer(outputFilePath!!, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val muxerTrackIndex = muxer.addTrack(format)
                muxer.start()

                val info = BufferInfo()
                extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                val buffer = ByteBuffer.allocate(BUFFER_SIZE)

                while (true) {
                    info.offset = 0
                    info.size = extractor.readSampleData(buffer, 0)
                    if (info.size < 0 || extractor.sampleTime > endMs * 1000) {
                        info.size = 0
                        break
                    } else {
                        info.presentationTimeUs = extractor.sampleTime
                        info.flags = mapSampleFlags(extractor.sampleFlags)
                        muxer.writeSampleData(muxerTrackIndex, buffer, info)
                        extractor.advance()
                    }
                }

                muxer.stop()
                muxer.release()
                extractor.release()


                databaseManager.executeQuery1({ database ->
                    val actionDao = MatchActionsDAO(database, applicationContext)
                    actionDao.updateVideo(fileName, mediaFile.toString(), id);
                    counter = counter + 1
                    trimData()
                }, "processData" + counter)
            }
        }catch (ex:Exception){
            ex.printStackTrace()
        }
    }

    private fun selectTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("video/")) {
                return i
            }
        }
        return -1
    }

    private fun mapSampleFlags(sampleFlags: Int): Int {
        var bufferFlags = 0
        if ((sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
            bufferFlags = bufferFlags or MediaCodec.BUFFER_FLAG_SYNC_FRAME
        }
        if ((sampleFlags and MediaExtractor.SAMPLE_FLAG_ENCRYPTED) != 0) {
            bufferFlags = bufferFlags or MediaCodec.BUFFER_FLAG_KEY_FRAME
        }
        if ((sampleFlags and MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME) != 0) {
            bufferFlags = bufferFlags or MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
        }
        return bufferFlags
    }

}
