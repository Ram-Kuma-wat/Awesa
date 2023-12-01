package com.game.awesa.ui.recorder

import android.annotation.SuppressLint
  import android.content.Intent
  import android.content.pm.PackageManager
  import android.graphics.BitmapFactory
  import android.graphics.ImageFormat
  import android.graphics.PointF
  import android.graphics.Rect
  import android.graphics.YuvImage
  import android.net.Uri
  import android.os.Build.VERSION
  import android.os.Bundle
  import android.os.CountDownTimer
  import android.os.Environment
  import android.os.Handler
  import android.util.Log
  import android.view.View
  import android.view.animation.Animation
  import android.view.animation.AnimationUtils
  import android.view.animation.ScaleAnimation
  import android.widget.ImageView
  import android.widget.Toast
  import androidx.databinding.DataBindingUtil
  import androidx.lifecycle.lifecycleScope
  import com.codersworld.awesalibs.beans.CommonBean
  import com.codersworld.awesalibs.beans.matches.MatchesBean
  import com.codersworld.awesalibs.beans.matches.ReactionsBean
  import com.codersworld.awesalibs.database.DatabaseHelper
  import com.codersworld.awesalibs.database.DatabaseManager
  import com.codersworld.awesalibs.database.dao.DBVideoUplaodDao
  import com.codersworld.awesalibs.database.dao.InterviewsDAO
  import com.codersworld.awesalibs.database.dao.MatchActionsDAO
  import com.codersworld.awesalibs.database.dao.VideoMasterDAO
  import com.codersworld.awesalibs.listeners.OnResponse
  import com.codersworld.awesalibs.listeners.QueryExecutor
  import com.codersworld.awesalibs.rest.UniverSelObjct
  import com.codersworld.awesalibs.storage.UserSessions
  import com.codersworld.awesalibs.utils.CommonMethods
  import com.codersworld.awesalibs.utils.Tags
  import com.codersworld.awesalibs.videocompressor.CompressionListener
  import com.codersworld.awesalibs.videocompressor.VideoCompressor
  import com.codersworld.awesalibs.videocompressor.VideoQuality
  import com.codersworld.awesalibs.videocompressor.config.Configuration
  import com.codersworld.awesalibs.videocompressor.config.SaveLocation
  import com.codersworld.awesalibs.videocompressor.config.SharedStorageConfiguration
  import com.game.awesa.R
  import com.game.awesa.databinding.ActivityInterviewRecordBinding
  import com.game.awesa.databinding.ActivityVideoRecordBinding
import com.game.awesa.services.InterviewUploadService
import com.game.awesa.services.TrimService
  import com.game.awesa.ui.BaseActivity
  import com.otaliastudios.cameraview.CameraException
  import com.otaliastudios.cameraview.CameraListener
  import com.otaliastudios.cameraview.CameraLogger
  import com.otaliastudios.cameraview.CameraOptions
  import com.otaliastudios.cameraview.CameraView
  import com.otaliastudios.cameraview.PictureResult
  import com.otaliastudios.cameraview.VideoResult
  import com.otaliastudios.cameraview.controls.Mode
  import com.otaliastudios.cameraview.frame.Frame
  import com.otaliastudios.cameraview.frame.FrameProcessor
  import kotlinx.coroutines.launch
  import java.io.ByteArrayOutputStream
  import java.io.File
  import java.text.SimpleDateFormat
  import java.util.Date
  import java.util.Locale

class InterviewActivity : BaseActivity(), View.OnClickListener, OnResponse<UniverSelObjct> {
    lateinit var binding: ActivityInterviewRecordBinding
    var mMatchBean :MatchesBean.InfoBean? =null;
    var handler = Handler()
     var isStart=false
    var strTime = "00:00"
    var sec_passed = 0
    var seconds=0;
    var mTimer: CountDownTimer? = null
    var mediaFile: File? = null
    private val camera: CameraView by lazy { findViewById(R.id.camera) }
    private var captureTime: Long = 0
    var strUserId="";
    companion object {
        private val LOG = CameraLogger.create("Awesa")
        private const val USE_FRAME_PROCESSOR = false
        private const val DECODE_BITMAP = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_interview_record)
        strUserId =if ( UserSessions.getUserInfo(this@InterviewActivity) !=null ) UserSessions.getUserInfo(this@InterviewActivity).id.toString() else "0"
        if (intent.hasExtra("mMatchBean")) {
            mMatchBean = CommonMethods.getSerializable(intent,"mMatchBean", MatchesBean.InfoBean::class.java)
        }
        if (mMatchBean !=null){
            binding.tvTeamOneName.setText(mMatchBean!!.team1)
            binding.tvTeamTwoName.setText(mMatchBean!!.team2)
            DatabaseManager.getInstance().executeQuery(QueryExecutor {
                val dao = MatchActionsDAO(it, this@InterviewActivity)
                val mCOUNT: Int = dao.getRowCount(mMatchBean!!.team_id.toString(),mMatchBean!!.id.toString())
                val mCOUNT1: Int = dao.getRowCount(mMatchBean!!.opponent_team_id.toString(),mMatchBean!!.id.toString())
                binding.tvTeamOneScore.setText(mCOUNT.toString())
                binding.tvTeamTwoScore.setText(mCOUNT1.toString())
            })
        }
        DatabaseManager.initializeInstance(DatabaseHelper(applicationContext))
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
        camera.setLifecycleOwner(this)
        camera.addCameraListener(Listener())
        if (USE_FRAME_PROCESSOR) {
            camera.addFrameProcessor(object : FrameProcessor {
                private var lastTime = System.currentTimeMillis()
                override fun process(frame: Frame) {
                    val newTime = frame.time
                    val delay = newTime - lastTime
                    lastTime = newTime
                    LOG.v("Frame delayMillis:", delay, "FPS:", 1000 / delay)
                    if (DECODE_BITMAP) {
                        if (frame.format == ImageFormat.NV21
                            && frame.dataClass == ByteArray::class.java
                        ) {
                            val data = frame.getData<ByteArray>()
                            val yuvImage = YuvImage(
                                data,
                                frame.format,
                                frame.size.width,
                                frame.size.height,
                                null
                            )
                            val jpegStream = ByteArrayOutputStream()
                            yuvImage.compressToJpeg(
                                Rect(
                                    0, 0,
                                    frame.size.width,
                                    frame.size.height
                                ), 100, jpegStream
                            )
                            val jpegByteArray = jpegStream.toByteArray()
                            val bitmap = BitmapFactory.decodeByteArray(
                                jpegByteArray,
                                0, jpegByteArray.size
                            )
                            bitmap.toString()
                        }
                    }
                }
            })
        }

        binding.btnReTakeVideo.visibility=View.VISIBLE
        binding.rlAnotherVideo.visibility=View.VISIBLE
        //binding.llUpload.visibility=View.GONE
        binding.btnReTakeVideo.setText(getString(R.string.lbl_start_interview))

        binding.btnReTakeVideo.setOnClickListener {
            binding.btnReTakeVideo.visibility=View.GONE
            binding.rlAnotherVideo.visibility=View.GONE
            //binding.llUpload.visibility=View.GONE
            startTimerCounter()
        }
        binding.ivStop.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_stop -> {
                captureVideo()
            }
        }
    }

    private fun startTimerCounter() {
        if (sec_passed + 1 < Tags.interview_duration / 1000) {
            binding.countdownTimerTxt.setText("3")
            binding.countdownTimerTxt.setVisibility(View.VISIBLE)
            val scaleAnimation: Animation = ScaleAnimation(
                1.0f,
                0.0f,
                1.0f,
                0.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            mTimer = object : CountDownTimer(5000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.countdownTimerTxt.setText("" + millisUntilFinished / 1000)
                    binding.countdownTimerTxt.setAnimation(scaleAnimation)
                    binding.tvHalfStatus.setText(  getString(R.string.lbl_interview1) )
                }

                override fun onFinish() {
                    binding.countdownTimerTxt.setVisibility(View.GONE)
                    isStart=true
                    runTimer()
                    //Start_or_Stop_Recording()
                    initlize_Video_progress()
                    captureVideo()
                    mTimer!!.cancel()
                }
            }//.start()
            mTimer!!.start()
        }
    }

    private inner class Listener : CameraListener() {
        override fun onCameraOpened(options: CameraOptions) {

        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
            message("Got CameraException #" + exception.reason, true)
        }

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            /*   if (camera.isTakingVideo) {
                   message("Captured while taking video. Size=" + result.size, false)
                   return
               }

               // This can happen if picture was taken with a gesture.
               val callbackTime = System.currentTimeMillis()
               if (captureTime == 0L) captureTime = callbackTime - 300
               LOG.w("onPictureTaken called! Launching activity. Delay:", callbackTime - captureTime)
               PicturePreviewActivity.pictureResult = result
               val intent = Intent(this@CameraActivity, PicturePreviewActivity::class.java)
               intent.putExtra("delay", callbackTime - captureTime)
               startActivity(intent)
               captureTime = 0
               LOG.w("onPictureTaken called! Launched activity.")*/
        }

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            LOG.w("onVideoTaken called! Launching activity.")
            binding.rlAnotherVideo.visibility=View.VISIBLE
          //  binding.llUpload.visibility=View.VISIBLE
            binding.btnReTakeVideo.visibility=View.GONE
          //  VideoPreviewActivity.videoResult = result
            uris.add(Uri.fromFile(mediaFile))
            saveVideo()
            //processVideo()
            LOG.w("onVideoTaken called! Launched activity.")
        }
         override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
            LOG.w("onVideoRecordingStart!")
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
            message("Video taken. Processing...", false)
            LOG.w("onVideoRecordingEnd!")
        }

        override fun onExposureCorrectionChanged(
            newValue: Float,
            bounds: FloatArray,
            fingers: Array<PointF>?
        ) {
            super.onExposureCorrectionChanged(newValue, bounds, fingers)
            message("Exposure correction:$newValue", false)
        }

        override fun onZoomChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>?) {
            super.onZoomChanged(newValue, bounds, fingers)
            message("Zoom:$newValue", false)
        }
    }

    private fun message(content: String, important: Boolean) {
        if (important) {
            LOG.w(content)
            // Toast.makeText(this, content, Toast.LENGTH_LONG).show()
        } else {
            LOG.i(content)
            //   Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
        }
    }
    var animation1: Animation? =null;
    fun makeAnimation(type:Int){
        if(animation1 ==null) {
            animation1 = AnimationUtils.loadAnimation(
                applicationContext,
                com.game.awesa.R.anim.blink
            )
        }
        if (type==0) {
            binding.ivRec.startAnimation(animation1)
        }else{
            animation1!!.cancel()
            animation1!!.reset()
        }
    }
    private fun captureVideo() {
        if (camera.mode == Mode.PICTURE) return run {
            message("Can't record HQ videos while in PICTURE mode.", false)
        }
        if (camera.isTakingPicture || camera.isTakingVideo) {
            isStart=false
            binding.ivStop.setVisibility(View.GONE)
            camera.stopVideo();
        } else {
            makeAnimation(0)
            var timeStamp1 = SimpleDateFormat("dd MMM yyyy").format(Date())
            var mediaStorageDir: File? = null
            if (VERSION.SDK_INT >= 30) {
                mediaStorageDir = File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                    ), "Awesa/videos/interview/"+timeStamp1
                )
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        //return null
                    }
                }
            }else{
                mediaStorageDir =
                    getExternalFilesDir(
                        "Awesa/videos/interview/"+timeStamp1
                    )
                mediaStorageDir!!.mkdirs()
                if (!mediaStorageDir!!.exists()) {
                    if (!mediaStorageDir!!.mkdirs()) {
                    }
                }
            }
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Toast.makeText(
                        this@InterviewActivity, "Please Allow Storage Permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            val date = Date()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.time)
            // For unique video file name appending current timeStamp with file name
            mediaFile = File(
                mediaStorageDir.path + File.separator +
                        "match_" + "_" + timeStamp + "interview" + ".mp4"
            )
            message("Recording for 25 seconds...", true)
            binding.ivStop.setVisibility(View.VISIBLE)
            camera.takeVideo(mediaFile!!, Tags.interview_duration.toInt())
        }

    }

    fun initlize_Video_progress() {
        this.sec_passed = 0
      /*  binding.videoProgress.enableAutoProgressView(Tags.interview_duration)
        binding.videoProgress.setDividerColor(Color.WHITE)
        binding.videoProgress.setDividerEnabled(false)
      //  binding.videoProgress.setDividerWidth(4)
        binding.videoProgress.setShader(intArrayOf(Color.CYAN, Color.CYAN, Color.CYAN))
        binding.videoProgress.SetListener(com.codersworld.awesalibs.autoimageslider.SegmentProgress.ProgressBarListener { mills ->
            time_in_milis = mills
            binding.ivStop.setVisibility(View.VISIBLE)
            sec_passed = (mills / 1000).toInt()
            val minutes = mills / 1000 / 60
            val seconds = mills / 1000 % 60
            strTime = "$minutes:$seconds"
            // txtWaitTimer.setText(getString(R.string.wait_time1).replace("XXXX", "00:" + (millisUntilFinished / 1000)));
            binding.tvTimer.setText("$minutes:$seconds")
            if (sec_passed > Tags.interview_duration / 1000 - 1) {
                captureVideo()
            }

        })*/
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid && !camera.isOpened) {
            camera.open()
        }
    }
    private val uris = mutableListOf<Uri>()

    @SuppressLint("SetTextI18n")
    private fun processVideo() {
        lifecycleScope.launch {
            VideoCompressor.start(
                context = applicationContext,
                uris,
                isStreamable = false,
                sharedStorageConfiguration = SharedStorageConfiguration(
                    saveAt = SaveLocation.pictures,
                    subFolderName = "Awesaaaa"
                ),
//                appSpecificStorageConfiguration = AppSpecificStorageConfiguration(
//
//                ),
                configureWith = Configuration(
                    quality = VideoQuality.LOW,
                    videoNames = uris.map { uri -> uri.pathSegments.last() },
                    isMinBitrateCheckEnabled = true,
                ),
                listener = object : CompressionListener {
                    override fun onProgress(index: Int, percent: Float) {
                        //Update UI
                        //binding.tvUpload.setText(percent.toInt().toString()+"%")
                    }

                    override fun onStart(index: Int) {
                       /* data.add(
                            index,
                            VideoDetailsModel("", uris[index], "")
                        )
                        adapter.notifyDataSetChanged()*/
                    }

                    override fun onSuccess(index: Int, size: Long, path: String?) {
                        mediaFile = File(path)
                       saveVideo()

                      /*  data[index] = VideoDetailsModel(
                            path,
                            uris[index],
                            getFileSize(size),
                            100F
                        )
                        adapter.notifyDataSetChanged()*/
                    }

                    override fun onFailure(index: Int, failureMessage: String) {
                        Log.wtf("failureMessage", failureMessage)
                    }

                    override fun onCancelled(index: Int) {
                        Log.wtf("TAG", "compression has been cancelled")
                    }
                },
            )
        }
    }

     fun saveVideo(){
        var fileName = mediaFile.toString().substring(mediaFile.toString().lastIndexOf('/') + 1, mediaFile.toString().length)

        var extension = mediaFile.toString().substring(mediaFile.toString().lastIndexOf("."))
        var timeStamp = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())
        DatabaseManager.getInstance().executeQuery(QueryExecutor {
            val dao = InterviewsDAO(it, this@InterviewActivity)
            var match_id = if(mMatchBean !=null && mMatchBean!!.id>0) mMatchBean!!.id.toString() else ""
            dao.insert(match_id,fileName,mediaFile.toString(),"0",timeStamp)

            var masterDataBaseId = dao.lastInsertedId
            val mCOUNT: Int = dao.getRowCount("")
           // binding.llUpload.visibility=View.GONE
            CommonMethods.checkService(this@InterviewActivity, TrimService::class.java)
            CommonMethods.checkService(this@InterviewActivity, InterviewUploadService::class.java)
            val intent = Intent(this@InterviewActivity, MatchOverviewActivity::class.java)
            intent.putExtra("mMatchBean",mMatchBean)
            startActivity(intent)
            finish()
        })
    }
    override fun onSuccess(response: UniverSelObjct) {
        try{
            when(response.methodname){
                Tags.SB_CREATE_MATCH_ACTION_API->{
                    try{
                        var mBean = response.response as CommonBean
                        if (mBean.status==1 && CommonMethods.isValidArrayList(mBean.scores)){
                            //binding.tvTeamOneScore.setText(mBean.scores[0].team1_score.toString())
                           // binding.tvTeamTwoScore.setText(mBean.scores[0].team2_score.toString())
                        }
                    }catch (ex1:Exception){
                        ex1.printStackTrace()
                    }
                 }
            }
        }catch (ex:Exception){
            ex.printStackTrace()
            errorMsg(getString(R.string.something_wrong))
        }
    }

    override fun onError(type: String, error: String) {
        when(type){
            Tags.SB_CREATE_MATCH_ACTION_API->{
              }
        }
    }

    fun errorMsg(strMsg: String) {
        CommonMethods.errorDialog(this@InterviewActivity,strMsg,getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok));
    }

    private fun runTimer() {
        handler.post(object : java.lang.Runnable {
            override fun run() {
                val minutes = seconds / 60
                val secs = seconds % 60
                val time = String.format(Locale.getDefault(),"%02d:%02d",minutes, secs)
                binding.tvTimer.setText(time)
                strTime = time;
                if (isStart) {
                    seconds++
                    if (seconds>=Tags.interview_duration){
                        captureVideo()
                        return;
                    }
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onBackPressed() {

        //super.onBackPressed()
    }
}