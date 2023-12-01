package com.game.awesa.ui.recorder


import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.YuvImage
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build.VERSION
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
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
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.database.dao.VideoMasterDAO
import com.codersworld.awesalibs.listeners.OnConfirmListener
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
import com.game.awesa.databinding.ActivityVideoRecordBinding
import com.game.awesa.services.TrimService
import com.game.awesa.ui.BaseActivity
import com.game.awesa.ui.LoginActivity
import com.game.awesa.ui.dialogs.CustomDialog
import com.google.gson.Gson
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.CameraOptions
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.FileCallback
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.Preview
import com.otaliastudios.cameraview.frame.Frame
import com.otaliastudios.cameraview.frame.FrameProcessor
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CameraActivity : BaseActivity(), View.OnClickListener, OnResponse<UniverSelObjct>,
    OnConfirmListener, FileCallback {
    lateinit var binding: ActivityVideoRecordBinding
    var mMatchBean: MatchesBean.InfoBean? = null;
    var handler = Handler()
    var isClicked = false
    var isStart = false
    var strTime = "00:00"
    var strTeam_id = ""
    var mHalf = 1
    var sec_passed = 0
    var seconds = 0;
    var time_in_milis: Long = 0
    var mTimer: CountDownTimer? = null
    var actionType = "";
    private val MY_PERMISSIONS_RECORD_AUDIO = 1
    private val REQUEST_WRITE_PERMISSION = 500
    private val MY_CAMERA_REQUEST_CODE2 = 100

    var mediaFile: File? = null
    var mediaFilePicture: File? = null
    private val camera: CameraView by lazy { findViewById(R.id.camera) }
    private var captureTime: Long = 0
    var strUserId = "";

    companion object {
        private val LOG = CameraLogger.create("Awesa")
        private const val USE_FRAME_PROCESSOR = false
        private const val DECODE_BITMAP = false
    }

    var match_id = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_record)
        strUserId = if (UserSessions.getUserInfo(this@CameraActivity) != null) UserSessions.getUserInfo(this@CameraActivity).id.toString() else "0"
        if (intent.hasExtra("mHalf")) {
            mHalf = intent.getIntExtra("mHalf", 1)
        }
        if (intent.hasExtra("MatchBean")) {
            mMatchBean =
                CommonMethods.getSerializable(intent, "MatchBean", MatchesBean.InfoBean::class.java)
        }
        if (mMatchBean != null) {
            match_id = mMatchBean!!.id.toString()
            binding.tvTeamOneName.setText(mMatchBean!!.team1)
            binding.tvTeamTwoName.setText(mMatchBean!!.team2)
        }
        binding.imgTor.setOnClickListener(this)
        binding.imgTor1.setOnClickListener(this)
        binding.imgChance.setOnClickListener(this)
        binding.imgChance1.setOnClickListener(this)
        binding.imgWow.setOnClickListener(this)
        binding.imgWow1.setOnClickListener(this)
        binding.imgFail.setOnClickListener(this)
        binding.imgFail1.setOnClickListener(this)

        DatabaseManager.initializeInstance(DatabaseHelper(applicationContext))
        //CommonMethods.loadImageDrawable(this@CameraActivity,R.drawable.loading_img_one,binding.imgTor)
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
                        if (frame.format == ImageFormat.NV21 && frame.dataClass == ByteArray::class.java) {
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
                                Rect(0, 0, frame.size.width, frame.size.height),
                                100,
                                jpegStream
                            )
                            val jpegByteArray = jpegStream.toByteArray()
                            val bitmap =
                                BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.size)
                            bitmap.toString()
                        }
                    }
                }
            })
        }
        if (mHalf == 2) {
            startTimerCounter()
            DatabaseManager.getInstance().executeQuery(QueryExecutor {
                val dao = MatchActionsDAO(it, this@CameraActivity)
                val mCOUNT: Int =
                    dao.getRowCount(mMatchBean!!.team_id.toString(), mMatchBean!!.id.toString())
                val mCOUNT1: Int = dao.getRowCount(
                    mMatchBean!!.opponent_team_id.toString(),
                    mMatchBean!!.id.toString()
                )
                binding.tvTeamOneScore.setText(mCOUNT.toString())
                binding.tvTeamTwoScore.setText(mCOUNT1.toString())

                CommonMethods.successToast(
                    this@CameraActivity,
                    getString(R.string.msg_action_success, reaction)
                )
                changeImage(1)
            })

        } else {
            binding.btnReTakeVideo.visibility = View.VISIBLE
            binding.rlAnotherVideo.visibility = View.VISIBLE
            binding.llUpload.visibility = View.GONE
            binding.btnReTakeVideo.setText(getString(R.string.lbl_start_first_half))
            binding.btnReTakeVideo.setOnClickListener {
                mHalf = 1
                binding.btnReTakeVideo.visibility = View.GONE
                binding.rlAnotherVideo.visibility = View.GONE
                binding.llUpload.visibility = View.GONE
                initVideo()
                // binding.btnReTakeVideo.setText( getString(R.string.lbl_start_second_half))
                startTimerCounter()
            }
        }
        binding.ivStop.setOnClickListener(this)
    }

    private fun capturePictureSnapshot() {
        if (camera.isTakingPicture) return
        if (camera.preview != Preview.GL_SURFACE) return run {
            message("Picture snapshots are only allowed with the GL_SURFACE preview.", true)
        }
        captureTime = System.currentTimeMillis()
        message("Capturing picture snapshot...", false)
        camera.takePictureSnapshot()
    }

    fun makeAction(imageView: ImageView) {
        if (!isClicked && isStart) {
            if (CommonMethods.isNetworkAvailable(this@CameraActivity)) {
                mImageView = imageView
                changeImage(0)
                capturePictureSnapshot()
                saveActions()
                //ApiCall(this@CameraActivity).makeActions(this, false,strUserId,mMatchBean!!.id.toString(),strTeam_id,strTime,reaction,mHalf.toString())
            } else {
                changeImage(1)
                errorMsg(getResources().getString(R.string.error_internet));
                return;
            }
        }
    }

    var mImageView: ImageView? = null
    var reaction = ""

    fun changeImage(type: Int) {
        isClicked = if (type == 0) true else false

        if (mImageView == binding.imgTor) {
            reaction = "goal"
            CommonMethods.loadImageDrawable(
                this@CameraActivity,
                if (type == 0) R.drawable.loading_img_one else R.drawable.tor_one,
                mImageView,
                type
            )
        } else if (mImageView == binding.imgChance) {
            reaction = "chance"
            CommonMethods.loadImageDrawable(
                this@CameraActivity,
                if (type == 0) R.drawable.loading_img_one else R.drawable.chance_one,
                mImageView,
                type
            )
        } else if (mImageView == binding.imgWow) {
            reaction = "wow"
            CommonMethods.loadImageDrawable(
                this@CameraActivity,
                if (type == 0) R.drawable.loading_img_one else R.drawable.wow_one,
                mImageView,
                type
            )
        } else if (mImageView == binding.imgFail) {
            reaction = "fail"
            CommonMethods.loadImageDrawable(
                this@CameraActivity,
                if (type == 0) R.drawable.loading_img_one else R.drawable.fail_one,
                mImageView,
                type
            )
        } else if (mImageView == binding.imgTor1) {
            reaction = "goal"
            CommonMethods.loadImageDrawable(
                this@CameraActivity,
                if (type == 0) R.drawable.loading_img else R.drawable.tor_two,
                mImageView,
                type
            )
        } else if (mImageView == binding.imgChance1) {
            reaction = "chance"
            CommonMethods.loadImageDrawable(
                this@CameraActivity,
                if (type == 0) R.drawable.loading_img else R.drawable.chance_two,
                mImageView,
                type
            )
        } else if (mImageView == binding.imgWow1) {
            reaction = "wow"
            CommonMethods.loadImageDrawable(
                this@CameraActivity,
                if (type == 0) R.drawable.loading_img else R.drawable.wow_two,
                mImageView,
                type
            )
        } else if (mImageView == binding.imgFail1) {
            reaction = "fail"
            CommonMethods.loadImageDrawable(
                this@CameraActivity,
                if (type == 0) R.drawable.loading_img else R.drawable.fail_two,
                mImageView,
                type
            )
        }
        if (type == 1) {
            mImageView = null;
            reaction = ""
            strTeam_id = ""
        } else {
            if (mImageView == binding.imgTor || mImageView == binding.imgChance || mImageView == binding.imgWow || mImageView == binding.imgFail) {
                strTeam_id = mMatchBean!!.team_id.toString()
            } else {
                strTeam_id = mMatchBean!!.opponent_team_id.toString()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_stop -> {
                captureVideo()
            }

            R.id.imgTor -> {
                makeAction(binding.imgTor)
            }

            R.id.imgTor1 -> {
                makeAction(binding.imgTor1)
            }

            R.id.imgChance -> {
                makeAction(binding.imgChance)
            }

            R.id.imgChance1 -> {
                makeAction(binding.imgChance1)
            }

            R.id.imgFail -> {
                makeAction(binding.imgFail)
            }

            R.id.imgFail1 -> {
                makeAction(binding.imgFail1)
            }

            R.id.imgWow -> {
                makeAction(binding.imgWow)
            }

            R.id.imgWow1 -> {
                makeAction(binding.imgWow1)
            }
        }
    }

    private fun startTimerCounter() {
        if (sec_passed + 1 < Tags.recording_duration / 1000) {
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
                    binding.tvHalfStatus.setText(
                        if (mHalf == 1) getString(R.string.lbl_first_half) else getString(
                            R.string.lbl_second_half
                        )
                    )
                }

                override fun onFinish() {
                    binding.countdownTimerTxt.setVisibility(View.GONE)
                    isStart = true
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
            /*if (camera.isTakingVideo) {
                message("Captured while taking video. Size=" + result.size, false)
                return
            }*/
            var timeStamp1 = SimpleDateFormat("dd MMM yyyy").format(Date())
            var mediaStorageDir: File? = null
            if (VERSION.SDK_INT >= 30) {
                mediaStorageDir = File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                    ), "Awesa/pictures/" + timeStamp1
                )
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        //return null
                    }
                }
            } else {
                mediaStorageDir =
                    getExternalFilesDir(
                        "Awesa/pictures/" + timeStamp1
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
                        this@CameraActivity, "Please Allow Storage Permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            val date = Date()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.time)
            // For unique video file name appending current timeStamp with file name
            mediaFilePicture = File(
                mediaStorageDir.path + File.separator +
                        "match_" + match_id + "_" + timeStamp + "_half_" + mHalf + ".mp4"
            )
            result.toFile(mediaFilePicture!!, this@CameraActivity)
            // This can happen if picture was taken with a gesture.
            val callbackTime = System.currentTimeMillis()
            if (captureTime == 0L) captureTime = callbackTime - 300
            LOG.w("onPictureTaken called! Launching activity. Delay:", callbackTime - captureTime)
            /* PicturePreviewActivity.pictureResult = result
             val intent = Intent(this@CameraActivity, PicturePreviewActivity::class.java)
             intent.putExtra("delay", callbackTime - captureTime)
             startActivity(intent)*/
            captureTime = 0
            LOG.w("onPictureTaken called! Launched activity.")
        }

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            LOG.w("onVideoTaken called! Launching activity.")
            binding.rlAnotherVideo.visibility = View.VISIBLE
            binding.llUpload.visibility = View.VISIBLE
            binding.btnReTakeVideo.visibility = View.GONE
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

    var animation1: Animation? = null;
    fun makeAnimation(type: Int) {
        if (animation1 == null) {
            animation1 = AnimationUtils.loadAnimation(
                applicationContext,
                com.game.awesa.R.anim.blink
            )
        }
        if (type == 0) {
            binding.ivRec.startAnimation(animation1)
        } else {
            animation1!!.cancel()
            animation1!!.reset()
        }
    }

    private fun captureVideo() {
        if (camera.mode == Mode.PICTURE) return run {
            message("Can't record HQ videos while in PICTURE mode.", false)
        }
        if (camera.isTakingPicture || camera.isTakingVideo) {
            isStart = false
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
                    ), "Awesa/videos/" + timeStamp1
                )
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        //return null
                    }
                }
            } else {
                mediaStorageDir =
                    getExternalFilesDir(
                        "Awesa/videos/" + timeStamp1
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
                        this@CameraActivity, "Please Allow Storage Permission.",
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
                        "match_" + "_" + timeStamp + "_half_" + mHalf + ".mp4"
            )
            message("Recording for 25 seconds...", true)
            binding.ivStop.setVisibility(View.VISIBLE)
            camera.takeVideo(mediaFile!!, Tags.recording_duration.toInt())
        }

    }

    fun initlize_Video_progress() {
        this.sec_passed = 0
        /*  binding.videoProgress.enableAutoProgressView(Tags.recording_duration)
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
              if (sec_passed > Tags.recording_duration / 1000 - 1) {
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

    fun saveActions() {
        DatabaseManager.getInstance().executeQuery(QueryExecutor {
            val dao = MatchActionsDAO(it, this@CameraActivity)
            var mBean = ReactionsBean();
            mBean.match_id = if (mMatchBean != null && mMatchBean!!.id > 0) mMatchBean!!.id else 0
            mBean.team_id = if (CommonMethods.isValidString(strTeam_id)) strTeam_id.toInt() else 0
            mBean.team_name =
                if (mMatchBean != null && mMatchBean!!.id > 0) if (mMatchBean!!.team_id == strTeam_id.toInt()) mMatchBean!!.team1 else mMatchBean!!.team2 else getString(
                    R.string.app_name
                )
            mBean.half = mHalf
            mBean.time = strTime
            mBean.reaction = reaction
            mBean.file_name = ""
            mBean.video = ""
            mBean.upload_status = 0
            mBean.created_date = CommonMethods.getCurrentFormatedDate("yyyy-MM-dd HH:mm:ss")

            //2023-08-16 13:06:03 //yyyy/MM/dd HH:mm:ss
            var mList: ArrayList<ReactionsBean> = ArrayList()
            mList.add(mBean)
            dao.insert(mList)
            var masterDataBaseId = dao.lastInsertedId
            val mCOUNT: Int =
                dao.getRowCount(mMatchBean!!.team_id.toString(), mMatchBean!!.id.toString())
            val mCOUNT1: Int = dao.getRowCount(
                mMatchBean!!.opponent_team_id.toString(),
                mMatchBean!!.id.toString()
            )
            binding.tvTeamOneScore.setText(mCOUNT.toString())
            binding.tvTeamTwoScore.setText(mCOUNT1.toString())

            CommonMethods.successToast(
                this@CameraActivity,
                getString(R.string.msg_action_success, reaction)
            )
            changeImage(1)
        })

    }

    fun saveVideo() {
        var fileName = mediaFile.toString()
            .substring(mediaFile.toString().lastIndexOf('/') + 1, mediaFile.toString().length)

        var extension = mediaFile.toString().substring(mediaFile.toString().lastIndexOf("."))
        var timeStamp = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())
        DatabaseManager.getInstance().executeQuery(QueryExecutor {
            val dao = VideoMasterDAO(it, this@CameraActivity)
            var mBean = DBVideoUplaodDao();
            mBean.match_id =
                if (mMatchBean != null && mMatchBean!!.id > 0) mMatchBean!!.id.toString() else ""
            mBean.video_name = fileName
            mBean.video_ext = extension
            mBean.video_path = mediaFile.toString()
            mBean.video_half = mHalf.toString()
            mBean.upload_status = 0
            mBean.date = timeStamp
            var mList: ArrayList<DBVideoUplaodDao> = ArrayList()
            mList.add(mBean)
            dao.insert(mList)
            var masterDataBaseId = dao.latestInsertedId
            val mCOUNT: Int = dao.getTodoItemCount()
            binding.llUpload.visibility = View.GONE
            CommonMethods.checkService(this@CameraActivity, TrimService::class.java)
            if (mHalf == 1) {
                binding.btnReTakeVideo.setText(getString(R.string.lbl_start_second_half))
                binding.btnReTakeVideo.visibility = View.VISIBLE
                binding.btnReTakeVideo.setOnClickListener {
                    startActivity(
                        Intent(
                            this@CameraActivity,
                            CameraActivity::class.java
                        ).putExtra("mHalf", 2).putExtra("MatchBean", mMatchBean)
                    )
                    finish()
                }
            } else if (mHalf == 0 || mHalf == 3) {
                binding.btnReTakeVideo.visibility = View.VISIBLE
                binding.btnReTakeVideo.setOnClickListener {
                    mHalf = if (mHalf == 3) 2 else 1
                    initVideo()
                    binding.btnReTakeVideo.visibility = View.GONE
                    binding.rlAnotherVideo.visibility = View.GONE
                    binding.llUpload.visibility = View.GONE
                    // binding.btnReTakeVideo.setText( getString(R.string.lbl_start_second_half))
                    startTimerCounter()
                }
            } else {
                makeConfirmation(getString(R.string.msg_video_success))
                //CommonMethods.successToast(this@CameraActivity,getString(R.string.msg_video_success))
            }
        })
    }

    var customDialog: CustomDialog? = null
    var isDialogOpen = false
    fun makeConfirmation(msg: String) {
        if (!isDialogOpen) {
            if (customDialog == null) {
                customDialog = CustomDialog(
                    this@CameraActivity,
                    msg,
                    getString(R.string.lbl_interview),
                    getString(R.string.lbl_end_video),
                    this,
                    "1"
                )
                customDialog!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            isDialogOpen = true
            if (customDialog!! != null && customDialog!!.isShowing()) {
                customDialog!!.dismiss()
            }
            customDialog!!.show()
        }
    }

    override fun onConfirm(isTrue: Boolean, type: String) {
        isDialogOpen = false
        if (isTrue) {
            //overview
            val intent = Intent(this@CameraActivity, MatchOverviewActivity::class.java)
            intent.putExtra("mMatchBean", mMatchBean)
            startActivity(intent)
            finish()
            //CommonMethods.moveWithClear(this@CameraActivity, LoginActivity::class.java)
        } else {
            //interview
            val intent = Intent(this@CameraActivity, InterviewActivity::class.java)
            intent.putExtra("mMatchBean", mMatchBean)
            startActivity(intent)
            finish()
        }
    }

    override fun onSuccess(response: UniverSelObjct) {
        try {
            when (response.methodname) {
                Tags.SB_CREATE_MATCH_ACTION_API -> {
                    try {
                        var mBean = response.response as CommonBean
                        if (mBean.status == 1 && CommonMethods.isValidArrayList(mBean.scores)) {
                            binding.tvTeamOneScore.setText(mBean.scores[0].team1_score.toString())
                            binding.tvTeamTwoScore.setText(mBean.scores[0].team2_score.toString())
                        }
                    } catch (ex1: Exception) {
                        ex1.printStackTrace()
                    }
                    changeImage(1)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            errorMsg(getString(R.string.something_wrong))
        }
    }

    override fun onError(type: String, error: String) {
        when (type) {
            Tags.SB_CREATE_MATCH_ACTION_API -> {
                changeImage(1)
            }
        }
    }

    fun errorMsg(strMsg: String) {
        CommonMethods.errorDialog(
            this@CameraActivity,
            strMsg,
            getResources().getString(R.string.app_name),
            getResources().getString(R.string.lbl_ok)
        );
    }

    var seconds1 = 0;

    private fun runTimer() {
        seconds = 0;
        seconds1 = 45 * 60;
        handler.post(object : java.lang.Runnable {
            override fun run() {
                var minutes = seconds / 60
                var secs = seconds % 60
                var time = String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
                if (mHalf == 2) {
                    val minutes1 = seconds1 / 60
                    val secs1 = seconds1 % 60
                    var time1 = String.format(Locale.getDefault(), "%02d:%02d", minutes1, secs1)
                    binding.tvTimer.setText(time1)
                } else {
                    binding.tvTimer.setText(time)
                }
                strTime = time;
                if (isStart) {
                    seconds++
                    seconds1++
                    if (seconds >= Tags.recording_duration) {
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

    override fun onFileReady(file: File?) {
    }

    override fun onDestroy() {
        if (camera.isTakingVideo) {
            camera.stopVideo()
            mHalf = if (mHalf == 2) 3 else 0
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        binding.tvHalfStatus.setText(
            if (mHalf == 1 || mHalf == 0) getString(R.string.lbl_first_half) else getString(
                R.string.lbl_second_half
            )
        )
        binding.btnReTakeVideo.setText(
            if (mHalf == 1 || mHalf == 0) getString(R.string.lbl_start_first_half) else getString(
                R.string.lbl_start_second_half
            )
        )
    }

    override fun onPause() {
        if (camera.isTakingVideo) {
            camera.stopVideo()
            mHalf = if (mHalf == 2) 3 else 0
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
        super.onPause()
        val activityManager = applicationContext
            .getSystemService(ACTIVITY_SERVICE) as ActivityManager
        activityManager.moveTaskToFront(taskId, 0)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    fun initVideo() {
        DatabaseManager.getInstance().executeQuery(QueryExecutor {
            val dao = MatchActionsDAO(it, this@CameraActivity)
            try {
                dao.deleteByMatch(
                    if (mMatchBean != null && mMatchBean!!.id > 0) mMatchBean!!.id.toString() else "0",
                    mHalf.toString(),
                    1
                )
            } catch (Ex: Exception) {
                Ex.printStackTrace()
            }
            val dao1 = VideoMasterDAO(it, this@CameraActivity)
            try {
                dao1.deleteVideoByMatch(
                    if (mMatchBean != null && mMatchBean!!.id > 0) mMatchBean!!.id else 0, mHalf
                )
            } catch (Ex: Exception) {
                Ex.printStackTrace()
            }

            try {
                val mCOUNT: Int =
                    dao.getRowCount(mMatchBean!!.team_id.toString(), mMatchBean!!.id.toString())
                val mCOUNT1: Int = dao.getRowCount(
                    mMatchBean!!.opponent_team_id.toString(),
                    mMatchBean!!.id.toString()
                )
                binding.tvTeamOneScore.setText(mCOUNT.toString())
                binding.tvTeamTwoScore.setText(mCOUNT1.toString())
            } catch (Ex: Exception) {
                Ex.printStackTrace()
            }
        })

    }
}