package com.game.awesa.ui.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.ExperimentalPersistentRecording
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import androidx.media3.common.util.UnstableApi
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.VideoUploadBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.database.dao.VideoMasterDAO
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.databinding.ActivityVideoRecordBinding
import com.game.awesa.services.TrimService
import com.game.awesa.ui.Awesa
import com.game.awesa.ui.BaseActivity
import com.game.awesa.ui.LoginActivity
import com.game.awesa.ui.dialogs.RecordingDialog
import com.game.awesa.utils.Global
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class CameraActivity : BaseActivity(), OnClickListener, OnResponse<UniversalObject>,
    OnConfirmListener {
    companion object {
        private const val BYTE = 1_000L
        const val SIXTY = 60
        const val FIVE_MILLISECONDS = 5000L
        const val SECOND_HALF_TIME = 2700L
        const val FULL_TIME = 5400L
        const val EXTRA_TIME = 2400L
        const val EXTRA_MATCH_HALF = "mHalf"
        const val EXTRA_MATCH_BEAN = "MatchBean"
        const val EXTRA_RECORDING_ID = "PendingRecordingId"
        const val EXTRA_UI_START = "UiState"
        val TAG: String = CameraActivity::class.java.simpleName
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject lateinit var awesa: Awesa

    private lateinit var cameraProvider: ProcessCameraProvider

    private lateinit var recordingState: VideoRecordEvent

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    // Camera UI  states and inputs
    enum class UiState: Serializable {
        IDLE,       // Not recording, all UI controls are active.
        RECORDING,  // Camera is recording, only display Pause/Resume & Stop button.
        FINALIZED,  // Recording just completes, disable all RECORDING UI controls.
        RECOVERY    // For future use.
    }

    private var uiState: UiState = UiState.IDLE

    private var audioEnabled = true

    private var mCamera: Camera? = null

    private val viewModel: CameraViewModel by viewModels()

    private var isClicked = false

    private var strTeamId = ""
    private var secondsPassed = 0
    var mTimer: CountDownTimer? = null
    var actionTimer: CountDownTimer? = null

    private var mImageView: ImageView? = null
    private var reaction = ""

    private val mArrayZoom = floatArrayOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f)

    private val requestAudioPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            audioEnabled = isGranted
        }

    lateinit var binding: ActivityVideoRecordBinding

    private var mMatchBean: MatchesBean.InfoBean? = null
    private var mHalf = 1

    private var canExit: Boolean = true

    private var callback = object: OnBackPressedCallback(canExit) {
        override fun handleOnBackPressed() {
//            if (doubleBackPressed) {
//                finish()
//                return
//            }
//            doubleBackPressed = true
//            showMessage()
//            Handler(Looper.getMainLooper()).postDelayed({
//                doubleBackPressed = false
//            }, 2000)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_record)
        binding.imgTor.setOnClickListener(this)
        binding.imgTor1.setOnClickListener(this)
        binding.imgChance.setOnClickListener(this)
        binding.imgChance1.setOnClickListener(this)
        binding.imgHighlight.setOnClickListener(this)
        binding.imgHighlight1.setOnClickListener(this)
        binding.imgZoomIn.setOnClickListener(this)
        binding.imgZoomOut.setOnClickListener(this)
        binding.ivStop.setOnClickListener(this)
        binding.btnStartVideo.setOnClickListener {
            binding.statusOverlay.visibility = View.GONE
            updateGoals()
            startTimerCounter()
        }

        onBackPressedDispatcher.addCallback(this, callback)

        awesa.preview.surfaceProvider = binding.previewView.surfaceProvider

        if (savedInstanceState != null) {
            mHalf = savedInstanceState.getInt(EXTRA_MATCH_HALF, 1)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mMatchBean = savedInstanceState.getSerializable(EXTRA_MATCH_BEAN,
                    MatchesBean.InfoBean::class.java)
                uiState = savedInstanceState.getSerializable(EXTRA_UI_START, UiState::class.java) ?: UiState.IDLE
            } else {
                uiState = savedInstanceState.getSerializable(EXTRA_UI_START) as UiState
                mMatchBean = savedInstanceState.getSerializable(EXTRA_MATCH_BEAN) as? MatchesBean.InfoBean
            }

            updateGoals()

            if (uiState == UiState.IDLE) {
                setupObservers()
            } else {
                setupObservers(handle = { captureVideo() })
            }
        } else {
            setupObservers()
            handleIntent()
        }
    }

    override fun onStart() {
        super.onStart()
        initializeUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(EXTRA_MATCH_HALF, mHalf)
        outState.putSerializable(EXTRA_MATCH_BEAN, mMatchBean)
        outState.putSerializable(EXTRA_UI_START, uiState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mHalf = savedInstanceState.getInt(EXTRA_MATCH_HALF, 1)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mMatchBean = savedInstanceState.getSerializable(EXTRA_MATCH_BEAN,
                MatchesBean.InfoBean::class.java)
            uiState = savedInstanceState.getSerializable(EXTRA_UI_START, UiState::class.java) ?: UiState.IDLE
        } else {
            uiState = savedInstanceState.getSerializable(EXTRA_UI_START) as UiState
            mMatchBean = savedInstanceState.getSerializable(EXTRA_MATCH_BEAN) as? MatchesBean.InfoBean
        }

    }

    private fun handleIntent() {
        if (intent.hasExtra(EXTRA_MATCH_HALF)) {
            mHalf = intent.getIntExtra(EXTRA_MATCH_HALF, 1)
        }

        if (intent.hasExtra(EXTRA_MATCH_BEAN)) {
            mMatchBean = CommonMethods.getSerializable(
                intent,
                EXTRA_MATCH_BEAN,
                MatchesBean.InfoBean::class.java
            )
        }
    }

    private fun setupObservers(handle: (() -> Unit)? = null) {
        viewModel.processCameraProvider?.observe(this) { provider ->
            cameraProvider = provider
            bindCaptureUsecase()

            if (handle != null) handle()
        }

        viewModel.recordEvent.observe(this) { event ->
            updateUI(event)

            if (event is VideoRecordEvent.Finalize && !event.hasError()) {
                // Display the captured video
                // Video record completed
                val mediaFile = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    File(getAbsolutePathFromUri(event.outputResults.outputUri))
                } else {
                    // force MediaScanner to re-scan the media file.
                    File(getAbsolutePathFromUri(event.outputResults.outputUri))
                }

                saveVideo(mediaFile)

            }
        }
    }

    // main cameraX capture functions
    /**
     *   Always bind preview + video capture use case combinations in this sample
     *   (VideoCapture can work on its own). The function should always execute on
     *   the main thread.
     */
    private fun bindCaptureUsecase() {
        try {
            cameraProvider.unbindAll()
            mCamera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                awesa.videoCapture,
                awesa.preview
            )
        } catch (ex: UnsupportedOperationException) {
            // we are on main thread, let's reset the controls on the UI.
            Log.e(TAG, "Use case binding failed", ex)
        }
    }

    @Suppress("MagicNumber")
    private fun zoomIn() {
        if(mCamera == null) return

        val cameraInfo = mCamera!!.cameraInfo

        if (cameraInfo.zoomState.value!!.linearZoom == 0.5f) return

        val cameraControl = mCamera!!.cameraControl
        // Linear zoom ranges from 0.0 (min zoom) to 1.0 (max zoom)
        cameraControl.setLinearZoom(cameraInfo.zoomState.value!!.linearZoom + 0.1f)
        binding.txtZoom.text = String.format(
            Locale.getDefault(),
            "%.1fx",
            cameraInfo.zoomState.value!!.linearZoom * 10
        )
    }

    @Suppress("MagicNumber")
    private fun zoomOut() {
        if(mCamera == null) return

        val cameraInfo = mCamera!!.cameraInfo

        if (cameraInfo.zoomState.value!!.linearZoom <= 0.0f) return

        val cameraControl = mCamera!!.cameraControl
        // Linear zoom ranges from 0.0 (min zoom) to 1.0 (max zoom)
        cameraControl.setLinearZoom(cameraInfo.zoomState.value!!.linearZoom - 0.1f)
        binding.txtZoom.text = String.format(
            Locale.getDefault(),
            "%.1fx",
            cameraInfo.zoomState.value!!.linearZoom * 10
        )
    }

    @OptIn(ExperimentalPersistentRecording::class)
    @Suppress("MagicNumber")
    private fun startRecording() {

        if (viewModel.currentRecording.value != null) {
            Log.d(TAG, viewModel.currentRecording.value.toString())
            viewModel.currentRecording.value?.resume()
            return
        }

        val name = when(mHalf) {
            1 -> "match_" + mMatchBean?.id + "_half_" + mHalf + ".mp4"
            2 -> "match_" + mMatchBean?.id + "_half_" + mHalf + ".mp4"
            3 -> "match_" + mMatchBean?.id + "_extratime" + ".mp4"
            4 -> "match_" + mMatchBean?.id + "_interview" + ".mp4"
            else -> error("Match Period $mHalf is invalid")
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        // configure Recorder and Start recording to the mediaStoreOutput.
        val recording = awesa.recorder.prepareRecording(this, mediaStoreOutput)
            .asPersistentRecording()
            .apply {
                if (ContextCompat.checkSelfPermission(
                        this@CameraActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    return
                }
                if (audioEnabled) withAudioEnabled() }


        viewModel.startRecording(recording)

        Log.i(TAG, "$mHalf Half: Recording started")
    }

    private fun getAbsolutePathFromUri(contentUri: Uri): String {
        var cursor: Cursor? = null
        return try {
            cursor = this
                .contentResolver
                .query(contentUri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
            if (cursor == null) {
                return ""
            }
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } catch (e: CursorIndexOutOfBoundsException) {
            Log.e(
                TAG, String.format(
                    Locale.getDefault(),
                    "Failed in getting absolute path for Uri %s with Exception %s",
                    contentUri.toString(), e.localizedMessage
                ),
                e
            )
            ""
        } catch (e: IllegalArgumentException) {
            Log.e(
                TAG, String.format(
                    Locale.getDefault(),
                    "Failed in getting absolute path for Uri %s with Exception %s",
                    contentUri.toString(), e.localizedMessage
                ),
                e
            )
            ""
        } finally {
            cursor?.close()
        }
    }

    /**
     * Initialize UI. Preview and Capture actions are configured in this function.
     * Note that preview and capture are both initialized either by UI or CameraX callbacks
     * (except the very 1st time upon entering to this fragment in onCreateView()
     */
    @SuppressLint("ClickableViewAccessibility", "MissingPermission")
    private fun initializeUI() {
        if (mMatchBean == null) return

        binding.tvTeamOneName.text = mMatchBean?.team1
        binding.tvTeamTwoName.text = mMatchBean?.team2

        when(mHalf) {
            1 -> {
                when (uiState) {
                    UiState.IDLE -> {
                        Log.d(TAG, uiState.toString())
                        binding.statusOverlay.visibility = View.VISIBLE
                        binding.btnStartVideo.text = getString(R.string.lbl_start_first_half)
                    }
                    UiState.RECORDING -> {
                        binding.statusOverlay.visibility = View.GONE
                        Log.d(TAG, uiState.toString())
                    }
                    UiState.FINALIZED -> {
                        Log.d(TAG, uiState.toString())
                    }
                    UiState.RECOVERY -> {
                        Log.d(TAG, uiState.toString())
                    }
                }
            }
            2 -> {
                when (uiState) {
                    UiState.IDLE -> {
                        Log.d(TAG, uiState.toString())
                        binding.statusOverlay.visibility = View.VISIBLE
                        binding.btnStartVideo.text = getString(R.string.lbl_start_second_half)
                    }
                    UiState.RECORDING -> {
                        binding.statusOverlay.visibility = View.GONE
                        Log.d(TAG, uiState.toString())
                    }
                    UiState.FINALIZED -> {
                        Log.d(TAG, uiState.toString())
                    }
                    UiState.RECOVERY -> {
                        Log.d(TAG, uiState.toString())
                    }
                }

                changeImage(1)
            }
            3 -> {
                when (uiState) {
                    UiState.IDLE -> {
                        Log.d(TAG, uiState.toString())
                        binding.statusOverlay.visibility = View.VISIBLE
                        binding.btnStartVideo.text = getString(R.string.lbl_start_extratime)
                    }
                    UiState.RECORDING -> {
                        binding.statusOverlay.visibility = View.GONE
                        Log.d(TAG, uiState.toString())
                    }
                    UiState.FINALIZED -> {
                        Log.d(TAG, uiState.toString())
                    }
                    UiState.RECOVERY -> {
                        Log.d(TAG, uiState.toString())
                    }
                }
            }
            4 -> {
                when (uiState) {
                    UiState.IDLE -> {
                        Log.d(TAG, uiState.toString())
                        binding.statusOverlay.visibility = View.VISIBLE
                        binding.btnStartVideo.text = getString(R.string.lbl_start_interview1)
                    }
                    UiState.RECORDING -> {
                        binding.statusOverlay.visibility = View.GONE
                        Log.d(TAG, uiState.toString())
                    }
                    UiState.FINALIZED -> {
                        Log.d(TAG, uiState.toString())
                    }
                    UiState.RECOVERY -> {
                        Log.d(TAG, uiState.toString())
                    }
                }
            }
        }
    }

    /**
     * UpdateUI according to CameraX VideoRecordEvent type:
     *   - user starts capture.
     *   - this app enables capture run-time UI (pause/resume/stop).
     *   - user controls recording with run-time UI, eventually tap "stop" to end.
     *   - this app informs CameraX recording to stop with recording.stop() (or recording.close()).
     *   - CameraX notify this app that the recording is indeed stopped, with the Finalize event.
     */
    private fun updateUI(event: VideoRecordEvent?) {
        if (event == null) return
        // Cache the recording state
        recordingState = event
        when (event) {
            is VideoRecordEvent.Resume, is VideoRecordEvent.Pause -> {
                uiState = UiState.RECORDING
            }
            is VideoRecordEvent.Status -> {
                uiState = UiState.RECORDING
                var recordedSeconds = TimeUnit.NANOSECONDS.toSeconds(event.recordingStats.recordedDurationNanos)
                if (mHalf == 2) {
                    recordedSeconds += SECOND_HALF_TIME
                } else if (mHalf == 3) {
                    recordedSeconds += (SECOND_HALF_TIME * 2)
                }
                val time = convertRecordedTime(recordedSeconds)
                binding.tvTimer.text = time
            }
            is VideoRecordEvent.Start -> {
                uiState = UiState.RECORDING
            }
            is VideoRecordEvent.Finalize -> if(event.hasError()) {
                Log.i(TAG, event.error.toString(), event.cause)
            } else {
                uiState = UiState.FINALIZED
            }
        }
    }

    private fun convertRecordedTime(recordedSeconds: Long): String {
        val minutes = recordedSeconds / SIXTY
        val secs = recordedSeconds % SIXTY
        val time = String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
        return time
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.currentRecording.value != null) {
            viewModel.currentRecording.value?.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.currentRecording.value != null) {
            viewModel.currentRecording.value?.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun makeAction(imageView: ImageView) {
        if (isActionClick) {
            actionTimer()
            if (!isClicked && uiState == UiState.RECORDING) {
                if (CommonMethods.isNetworkAvailable(this)) {
                    mImageView = imageView
                    changeImage(0)
                    saveActions()
                } else {
                    changeImage(1)
                    errorMsg(resources.getString(R.string.error_internet))
                }
            }
        }
    }

    private fun changeImage(type: Int) {
        isClicked = type == 0

        when (mImageView) {
            binding.imgTor -> {
                reaction = "goal"
                CommonMethods.loadImageDrawable(
                    this,
                    if (type == 0) R.drawable.loading_img_one else R.drawable.tor_one,
                    mImageView,
                    type
                )
            }
            binding.imgChance -> {
                reaction = "chance"
                CommonMethods.loadImageDrawable(
                    this,
                    if (type == 0) R.drawable.loading_img_one else R.drawable.chance_one,
                    mImageView,
                    type
                )
            }
            binding.imgHighlight -> {
                reaction = "Highlight"
                CommonMethods.loadImageDrawable(
                    this,
                    if (type == 0) R.drawable.loading_img_one else R.drawable.highlight,
                    mImageView,
                    type
                )
            }
            binding.imgTor1 -> {
                reaction = "goal"
                CommonMethods.loadImageDrawable(
                    this,
                    if (type == 0) R.drawable.loading_img else R.drawable.tor_two,
                    mImageView,
                    type
                )
            }
            binding.imgChance1 -> {
                reaction = "chance"
                CommonMethods.loadImageDrawable(
                    this,
                    if (type == 0) R.drawable.loading_img else R.drawable.chance_two,
                    mImageView,
                    type
                )
            }
            binding.imgHighlight1 -> {
                reaction = "Highlight"
                CommonMethods.loadImageDrawable(
                    this,
                    if (type == 0) R.drawable.loading_img else R.drawable.highlight,
                    mImageView,
                    type
                )
            }
        }
        if (type == 1) {
            mImageView = null
            reaction = ""
            strTeamId = ""
        } else {
            if (mImageView == binding.imgTor ||
                mImageView == binding.imgChance ||
                mImageView == binding.imgHighlight
            ) {
                strTeamId = mMatchBean!!.team_id.toString()
            } else {
                strTeamId = mMatchBean!!.opponent_team_id.toString()
            }
        }
    }

    @Suppress("MagicNumber")
    private fun stopRecording() {
        if (viewModel.currentRecording.value == null || recordingState is VideoRecordEvent.Finalize) {
            return
        }

        if (viewModel.currentRecording.value != null && uiState == UiState.RECORDING) {
            binding.ivStop.setVisibility(View.GONE)
            uiState = UiState.IDLE
            viewModel.setRecording(null)
        }
    }

    @Suppress("MagicNumber")
    override fun onClick(v: View) {
        when (v.id) {
            R.id.imgZoomIn -> {
                zoomIn()
            }
            R.id.imgZoomOut -> {
                zoomOut()
            }
            R.id.iv_stop -> {
                stopRecording()
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

            R.id.imgHighlight -> {
                makeAction(binding.imgHighlight)
            }

            R.id.imgHighlight1 -> {
                makeAction(binding.imgHighlight1)
            }
        }
    }

    @Suppress("MagicNumber")
    private fun startTimerCounter() {
        if (secondsPassed + 1 < Tags.recording_duration / BYTE) {
            binding.countdownTimerTxt.text = "5"
            binding.countdownTimerTxt.visibility = View.VISIBLE
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
            mTimer = object : CountDownTimer(
                FIVE_MILLISECONDS,
                BYTE
            ) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.countdownTimerTxt.text = "${millisUntilFinished / BYTE}"
                    binding.countdownTimerTxt.setAnimation(scaleAnimation)

                    binding.tvHalfStatus.text = when(mHalf) {
                        1 -> getString(R.string.lbl_first_half)
                        2 -> getString(R.string.lbl_second_half)
                        3 -> getString(R.string.lbl_extratime)
                        4 -> getString(R.string.lbl_interview1)
                        else -> error("Unknown State")
                    }
                }

                override fun onFinish() {
                    binding.countdownTimerTxt.visibility = View.GONE
                    captureVideo()
                    mTimer?.cancel()
                }
            }
            mTimer?.start()
        }
    }

    private fun saveActions() {
        databaseManager.executeQuery { database ->
            val dao = MatchActionsDAO(database, this)
            val mBean = ReactionsBean()
            mBean.match_id = mMatchBean?.id ?: -1
            mBean.team_id = strTeamId.toInt()
            mBean.team_name = if (mMatchBean!!.team_id == strTeamId.toInt()) {
                mMatchBean?.team1
            } else {
                mMatchBean?.team2
            }

            var recordedSeconds = TimeUnit.NANOSECONDS.toSeconds(
                recordingState.recordingStats.recordedDurationNanos
            )

            if (mHalf == 1) {
                if (recordedSeconds > SECOND_HALF_TIME) {
                    mBean.time = "45+${convertRecordedTime(recordedSeconds - SECOND_HALF_TIME)}"
                } else {
                    mBean.time = convertRecordedTime(recordedSeconds)
                }
            }

            if (mHalf == 2) {
                if(recordedSeconds > SECOND_HALF_TIME) {
                    mBean.time = "90+${convertRecordedTime(recordedSeconds - SECOND_HALF_TIME)}"
                } else {
                    recordedSeconds += SECOND_HALF_TIME
                    mBean.time = convertRecordedTime(recordedSeconds)
                }
            }

            if (mHalf == 3) {
                recordedSeconds += SECOND_HALF_TIME * 2
                mBean.time = convertRecordedTime(recordedSeconds)
            }

            mBean.half = mHalf
            mBean.timestamp = TimeUnit.NANOSECONDS.toSeconds(
                recordingState.recordingStats.recordedDurationNanos
            )
            mBean.reaction = reaction
            mBean.file_name = ""
            mBean.video = ""
            mBean.upload_status = 0
            mBean.created_date = CommonMethods.getCurrentFormatedDate("yyyy-MM-dd HH:mm:ss")

            val mList: ArrayList<ReactionsBean> = ArrayList()
            mList.add(mBean)
            dao.insert(mList)
            val teamOneScore: Int =
                dao.getGoalCount(mMatchBean!!.team_id.toString(), mMatchBean!!.id.toString())
            val teamTwoScore: Int = dao.getGoalCount(
                mMatchBean!!.opponent_team_id.toString(),
                mMatchBean!!.id.toString()
            )
            binding.tvTeamOneScore.text = String.format(Locale.getDefault(), "%d", teamOneScore)
            binding.tvTeamTwoScore.text = String.format(Locale.getDefault(),"%d", teamTwoScore)

            CommonMethods.successToast(
                this,
                getString(R.string.msg_action_success, reaction)
            )
            changeImage(1)
        }
    }

    @Suppress("MagicNumber")
    private fun saveVideo(file: File) {
        when(mHalf) {
            1 -> {
                saveMatchVideo(file, mHalf)
                mHalf = 2
                binding.statusOverlay.visibility = View.VISIBLE
                binding.btnStartVideo.text = getString(R.string.lbl_start_second_half)
                uiState = UiState.IDLE
            }
            2 -> {
                saveMatchVideo(file, mHalf)
                makeConfirmation()
                uiState = UiState.IDLE
            }
            3 -> {
                saveMatchVideo(file, mHalf)
                makeConfirmation()
                uiState = UiState.IDLE
            }
            4 -> {
                saveInterviewVideo(file)
            }
        }
    }

    @Suppress("MagicNumber")
    private fun saveMatchVideo(file: File, half: Int) {
        val fileName = file.nameWithoutExtension
        val extension = "mp4"
        val timeStamp = SimpleDateFormat(
            "yyyy/MM/dd HH:mm:ss",
            Locale.US
        ).format(Date())
        databaseManager.executeQuery {
            val dao = VideoMasterDAO(it, this)
            val mBean = VideoUploadBean()
            mBean.match_id = mMatchBean?.id.toString()
            mBean.video_name = fileName
            mBean.video_ext = extension
            mBean.video_path = file.toString()
            mBean.video_half = half.toString()
            mBean.upload_status = 0
            mBean.date = timeStamp

            dao.insert(mBean)
            val intent = Intent(this, TrimService::class.java)
            intent.putExtra(TrimService.EXTRA_MATCH_HALF, mHalf)
            intent.putExtra(TrimService.EXTRA_MATCH_ID, mMatchBean?.id.toString())
            intent.putExtra(TrimService.EXTRA_MATCH_FILE, file)
            CommonMethods.checkTrimServiceWithData(this, intent)
        }
    }

    private fun saveInterviewVideo(file: File) {
        val fileName = file.nameWithoutExtension

        val timeStamp = SimpleDateFormat(
            "yyyy/MM/dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date())

        databaseManager.executeQuery { database ->
            val dao = InterviewsDAO(database, this)
            val matchId = mMatchBean?.id.toString()
            dao.insert(matchId, fileName, file.toString(), "0", timeStamp)

            val intent = Intent(this, ProcessingActivity::class.java)
            intent.putExtra(ProcessingActivity.EXTRA_MATCH_BEAN, mMatchBean)
            startActivity(intent)
            finish()
        }
    }

    fun makeConfirmation(showExtraTime: Boolean = false) {
        val dialog = RecordingDialog(this, showExtraTime)
        dialog.show()
        dialog.setOnClickEndVideoListener {
            val intent = Intent(this, ProcessingActivity::class.java)
            intent.putExtra(ProcessingActivity.EXTRA_MATCH_BEAN, mMatchBean)
            startActivity(intent)
            dialog.dismiss()
            finish()
        }
        dialog.setOnClickRecordInterviewListener {
            mHalf = 4
            binding.statusOverlay.visibility = View.VISIBLE
            binding.btnStartVideo.text = getString(R.string.lbl_start_interview)
            dialog.dismiss()
        }
        dialog.setOnClickRecordExtraTimeListener {
            mHalf = 3
            binding.statusOverlay.visibility = View.VISIBLE
            binding.btnStartVideo.text = getString(R.string.lbl_start_extratime)
            dialog.dismiss()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onConfirm(isTrue: Boolean, type: String) {
        if (isTrue) {
            if (type == "99") {
                UserSessions.clearUserInfo(this)
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finishAffinity()
            } else {
                val intent = Intent(this, ProcessingActivity::class.java)
                intent.putExtra(ProcessingActivity.EXTRA_MATCH_BEAN, mMatchBean)
                startActivity(intent)
                finish()
            }
        } else {
            binding.statusOverlay.visibility = View.VISIBLE
            binding.btnStartVideo.text = getString(R.string.lbl_start_interview1)
        }
    }

    @Suppress("MagicNumber")
    override fun onSuccess(response: UniversalObject) {
        try {
            when (response.methodName) {
                Tags.SB_CREATE_MATCH_ACTION_API -> {
                    val mBean = response.response as CommonBean
                    if (mBean.status == 1 && CommonMethods.isValidArrayList(mBean.scores)) {
                        binding.tvTeamOneScore.text =
                            String.format(Locale.getDefault(), "%d", mBean.scores[0].team1_score)
                        binding.tvTeamTwoScore.text =
                            String.format(Locale.getDefault(),"%d", mBean.scores[0].team2_score)
                    } else if (mBean.status == 99) {
                        UserSessions.clearUserInfo(this)
                        Global().makeConfirmation(mBean.msg, this, this)
                    }
                    changeImage(1)
                }
            }
        } catch (ex: JsonSyntaxException) {
            Log.e(TAG, ex.localizedMessage, ex)
            errorMsg(getString(R.string.something_wrong))
        } catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage, ex)
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
            this,
            strMsg,
            resources.getString(R.string.app_name),
            resources.getString(R.string.lbl_ok)
        );
    }

    private fun captureVideo() {
        if (!this::recordingState.isInitialized ||
            recordingState is VideoRecordEvent.Finalize
        ) {
            makeAnimation(0)
            binding.ivStop.setVisibility(View.VISIBLE)

            startRecording()
        } else {
            when (recordingState) {
                is VideoRecordEvent.Start -> {
                    viewModel.currentRecording.value?.pause()
                    binding.ivStop.visibility = View.VISIBLE
                }
                is VideoRecordEvent.Status -> {
                    viewModel.currentRecording.value?.resume()
                    binding.ivStop.visibility = View.VISIBLE
                }

                is VideoRecordEvent.Pause -> {
                    viewModel.currentRecording.value?.resume()
                }
                is VideoRecordEvent.Resume -> {
                    viewModel.currentRecording.value?.pause()
                }
            }
        }
    }

    private fun updateGoals() {
        databaseManager.executeQuery { database ->
            val matchDao = MatchActionsDAO(database, this)
            try {
                val teamOneScore: Int =
                    matchDao.getGoalCount(mMatchBean!!.team_id.toString(), mMatchBean!!.id.toString())
                val teamTwoScore: Int = matchDao.getGoalCount(
                    mMatchBean!!.opponent_team_id.toString(),
                    mMatchBean!!.id.toString()
                )
                binding.tvTeamOneScore.text = String.format(Locale.getDefault(), "%d", teamOneScore)
                binding.tvTeamTwoScore.text = String.format(Locale.getDefault(),"%d", teamTwoScore)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private var animation: Animation? = null
    private fun makeAnimation(type: Int) {
        if (animation == null) {
            animation = AnimationUtils.loadAnimation(
                this,
                R.anim.blink
            )
        }
        if (type == 0) {
            binding.ivRec.startAnimation(animation)
        } else {
            animation!!.cancel()
            animation!!.reset()
        }
    }

    var isActionClick = true
    private fun actionTimer() {
        actionTimer = object : CountDownTimer(
            FIVE_MILLISECONDS,
            BYTE
        ) {
            override fun onTick(millisUntilFinished: Long) {
                isActionClick = false
            }

            override fun onFinish() {
                isActionClick = true
                actionTimer?.cancel()
            }
        }.start()
    }
}
