/**
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.game.awesa.ui.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.ExperimentalPersistentRecording
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.media3.common.util.UnstableApi
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.beans.VideoUploadBean
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.database.dao.VideoMasterDAO
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.databinding.FragmentCaptureBinding
import com.game.awesa.services.TrimService
import com.game.awesa.ui.LoginActivity
import com.game.awesa.ui.dialogs.CustomDialog
import com.game.awesa.utils.Global
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class CaptureFragment : Fragment(), OnClickListener, OnResponse<UniversalObject>, OnConfirmListener {

    companion object {
        private const val BYTE = 1_000L
        const val SIXTY = 60
        const val FIVE_MILLISECONDS = 5000L
        const val SECOND_HALF_TIME = 2700L
        const val EXTRA_MATCH_HALF = "mHalf"
        const val EXTRA_MATCH_BEAN = "MatchBean"
        // default Quality selection if no input from UI
        const val DEFAULT_QUALITY_IDX = 0
        val TAG: String = CaptureFragment::class.java.simpleName
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    // UI with ViewBinding
    private lateinit var captureViewBinding: FragmentCaptureBinding

    /** Host's navigation controller */

    private val cameraCapabilities = mutableListOf<CameraCapability>()

    private lateinit var videoCapture: VideoCapture<Recorder>
    private var currentRecording: Recording? = null
    private lateinit var recordingState: VideoRecordEvent

    // Camera UI  states and inputs
    enum class UiState {
        IDLE,       // Not recording, all UI controls are active.
        RECORDING,  // Camera is recording, only display Pause/Resume & Stop button.
        FINALIZED,  // Recording just completes, disable all RECORDING UI controls.
        RECOVERY    // For future use.
    }

    private var cameraIndex = 0
    private var qualityIndex = DEFAULT_QUALITY_IDX
    private var audioEnabled = true

    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(requireContext()) }
    private var enumerationDeferred: Deferred<Unit>? = null

    private var mCamera: Camera? = null

    // from camera activity
    private var isClicked = false
    var isStart = false
    private var strTeamId = ""
    var mHalf = 1
    private var secondsPassed = 0
    var mTimer: CountDownTimer? = null
    var actionTimer: CountDownTimer? = null
    var strUserId = ""
    private var mMatchBean: MatchesBean.InfoBean? = null
    private var mediaFile: File? = null

    private var mImageView: ImageView? = null
    private var reaction = ""

    private var matchId = ""
    private val mArrayZoom = floatArrayOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f)

    private var customDialog: CustomDialog? = null
    private var isDialogOpen = false

    private val requestAudioPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            audioEnabled = isGranted
        }

    /**
     * CaptureEvent listener.
     */
    private val captureListener = Consumer<VideoRecordEvent> { event ->
        // Cache the recording state
        recordingState = event

        updateUI(event)

        if (event is VideoRecordEvent.Finalize && !event.hasError()) {
            // Display the captured video
            // Video record completed
            mediaFile = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                File(getAbsolutePathFromUri(event.outputResults.outputUri))
            } else {
                // force MediaScanner to re-scan the media file.
                File(getAbsolutePathFromUri(event.outputResults.outputUri))
            }
            saveVideo()
            captureViewBinding.rlAnotherVideo.visibility = View.VISIBLE
            captureViewBinding.llUpload.visibility = View.VISIBLE
            captureViewBinding.btnReTakeVideo.visibility = View.VISIBLE
        }
    }

    // System function implementations
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        captureViewBinding = FragmentCaptureBinding.inflate(inflater, container, false)
        return captureViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCameraFragment()
    }

    // main cameraX capture functions
    /**
     *   Always bind preview + video capture use case combinations in this sample
     *   (VideoCapture can work on its own). The function should always execute on
     *   the main thread.
     */
    private suspend fun bindCaptureUsecase() {
        val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()

        val cameraSelector = getCameraSelector(cameraIndex)

        // create the user required QualitySelector (video resolution): we know this is
        // supported, a valid qualitySelector will be created.
        val quality = cameraCapabilities[cameraIndex].qualities[qualityIndex]
        val qualitySelector = QualitySelector.from(quality)

        val resolutionSelector = ResolutionSelector.Builder().setAspectRatioStrategy(
            AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY).build()
        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build().apply {
                surfaceProvider = captureViewBinding.previewView.surfaceProvider
            }

        // build a recorder, which can:
        //   - record video/audio to MediaStore(only shown here), File, ParcelFileDescriptor
        //   - be used create recording(s) (the recording performs recording)
        val recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        try {
            cameraProvider.unbindAll()
            mCamera = cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                videoCapture,
                preview
            )
        } catch (ex: UnsupportedOperationException) {
            // we are on main thread, let's reset the controls on the UI.
            Log.e(TAG, "Use case binding failed", ex)
            resetUIandState()
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
        captureViewBinding.txtZoom.text = String.format(
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
        captureViewBinding.txtZoom.text = String.format(
            Locale.getDefault(),
            "%.1fx",
            cameraInfo.zoomState.value!!.linearZoom * 10
        )
    }

    /**
     * Kick start the video recording
     *   - config Recorder to capture to MediaStoreOutput
     *   - register RecordEvent Listener
     *   - apply audio request from user
     *   - start recording!
     * After this function, user could start/pause/resume/stop recording and application listens
     * to VideoRecordEvent for the current recording status.
     */

    @OptIn(ExperimentalPersistentRecording::class)
    private fun startRecording() {
        val name = "match_" + matchId + "_half_" + mHalf + ".mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        // configure Recorder and Start recording to the mediaStoreOutput.
        currentRecording = videoCapture.output
            .prepareRecording(requireContext(), mediaStoreOutput)
            .asPersistentRecording() // Audio data is recorded after the VideoCapture is unbound
            .apply {
                if (ContextCompat.checkSelfPermission(
                    this@CaptureFragment.requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    return
                }
                if (audioEnabled) withAudioEnabled() }
            .start(mainThreadExecutor, captureListener)

        Log.i(TAG, "$mHalf Half: Recording started")
    }

    private fun getAbsolutePathFromUri(contentUri: Uri): String {
        var cursor: Cursor? = null
        return try {
            cursor = requireContext()
                .contentResolver
                .query(contentUri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
            if (cursor == null) {
                return ""
            }
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } catch (e: IllegalArgumentException) {
            Log.e(
                TAG, String.format(Locale.getDefault(),
                    "Failed in getting absolute path for Uri %s with Exception %s",
                    contentUri.toString(), e.localizedMessage
                )
            )
            ""
        } finally {
            cursor?.close()
        }
    }

    /**
     * Retrieve the asked camera's type(lens facing type). In this sample, only 2 types:
     *   idx is even number:  CameraSelector.LENS_FACING_BACK
     *          odd number:   CameraSelector.LENS_FACING_FRONT
     */
    private fun getCameraSelector(idx: Int): CameraSelector {
        if (cameraCapabilities.size == 0) {
            Log.i(TAG, "Error: This device does not have any camera, bailing out")
            requireActivity().finish()
        }
        return (cameraCapabilities[idx % cameraCapabilities.size].camSelector)
    }

    data class CameraCapability(val camSelector: CameraSelector, val qualities: List<Quality>)

    /**
     * Query and cache this platform's camera capabilities, run only once.
     */
    init {
        enumerationDeferred = lifecycleScope.async {
            whenCreated {
                val provider = ProcessCameraProvider.getInstance(requireContext()).await()

                provider.unbindAll()
                for (camSelector in arrayOf(
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    CameraSelector.DEFAULT_FRONT_CAMERA
                )) {
                    try {
                        if (provider.hasCamera(camSelector)) {
                            val camera = provider.bindToLifecycle(requireActivity(), camSelector)
                            QualitySelector
                                .getSupportedQualities(camera.cameraInfo)
                                .filter { quality ->
                                    listOf(Quality.HD)
                                        .contains(quality)
                                }.also {
                                    cameraCapabilities.add(CameraCapability(camSelector, it))
                                }
                        }
                    } catch (ex: UnsupportedOperationException) {
                        Log.e(TAG, ex.localizedMessage, ex)
                    }
                }
            }
        }
    }

    /**
     * One time initialize for CameraFragment (as a part of fragment layout's creation process).
     * This function performs the following:
     *   - initialize but disable all UI controls except the Quality selection.
     *   - set up the Quality selection recycler view.
     *   - bind use cases to a lifecycle camera, enable UI controls.
     */
    private fun initCameraFragment() {
        initializeUI()
        viewLifecycleOwner.lifecycleScope.launch {
            if (enumerationDeferred != null) {
                enumerationDeferred!!.await()
                enumerationDeferred = null
            }

            bindCaptureUsecase()
        }
    }

     /**
     * Initialize UI. Preview and Capture actions are configured in this function.
     * Note that preview and capture are both initialized either by UI or CameraX callbacks
     * (except the very 1st time upon entering to this fragment in onCreateView()
     */
    @SuppressLint("ClickableViewAccessibility", "MissingPermission")
    private fun initializeUI() {
        captureViewBinding.imgTor.setOnClickListener(this)
        captureViewBinding.imgTor1.setOnClickListener(this)
        captureViewBinding.imgChance.setOnClickListener(this)
        captureViewBinding.imgChance1.setOnClickListener(this)
        captureViewBinding.imgHighlight.setOnClickListener(this)
        captureViewBinding.imgHighlight1.setOnClickListener(this)
        captureViewBinding.imgZoomIn.setOnClickListener(this)
        captureViewBinding.imgZoomOut.setOnClickListener(this)

        strUserId =
            if (UserSessions.getUserInfo(context) != null) UserSessions.getUserInfo(
                context
            ).id.toString() else "0"
        val arguments = arguments
        if (arguments != null && arguments.containsKey(EXTRA_MATCH_HALF)) {
            mHalf = arguments.getInt(EXTRA_MATCH_HALF)
        }
        if (arguments != null && arguments.containsKey(EXTRA_MATCH_BEAN)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mMatchBean =
                    arguments.getSerializable(EXTRA_MATCH_BEAN, MatchesBean.InfoBean::class.java)
            } else {
                mMatchBean = arguments.getSerializable(EXTRA_MATCH_BEAN) as MatchesBean.InfoBean?
            }
        }
        if (mMatchBean != null) {
            matchId = mMatchBean!!.id.toString()
            captureViewBinding.tvTeamOneName.text = mMatchBean!!.team1
            captureViewBinding.tvTeamTwoName.text = mMatchBean!!.team2
        }

         when(mHalf) {
             1 -> {
                 captureViewBinding.btnReTakeVideo.visibility = View.VISIBLE
                 captureViewBinding.rlAnotherVideo.visibility = View.VISIBLE
                 captureViewBinding.llUpload.visibility = View.GONE
                 captureViewBinding.btnReTakeVideo.text = getString(com.game.awesa.R.string.lbl_start_first_half)
                 captureViewBinding.btnReTakeVideo.setOnClickListener {
                     mHalf = 1
                     captureViewBinding.btnReTakeVideo.visibility = View.GONE
                     captureViewBinding.rlAnotherVideo.visibility = View.GONE
                     captureViewBinding.llUpload.visibility = View.GONE
                     initVideo()
                     startTimerCounter()
                 }
             }
             2 -> {
                 startTimerCounter()
                 databaseManager.executeQuery {
                     val dao = MatchActionsDAO(it, context)
                     val teamOneScore: Int =
                         dao.getGoalCount(mMatchBean!!.team_id.toString(), mMatchBean!!.id.toString())
                     val teamTwoScore: Int = dao.getGoalCount(
                         mMatchBean!!.opponent_team_id.toString(),
                         mMatchBean!!.id.toString()
                     )
                     captureViewBinding.tvTeamOneScore.text = String.format(Locale.getDefault(), "%d", teamOneScore)
                     captureViewBinding.tvTeamTwoScore.text = String.format(Locale.getDefault(),"%d", teamTwoScore)

                     CommonMethods.successToast(
                         context,
                         getString(com.game.awesa.R.string.msg_action_success, reaction)
                     )
                     changeImage(1)
                 }
             }
         }

        captureViewBinding.ivStop.setOnClickListener(this)
    }

    /**
     * UpdateUI according to CameraX VideoRecordEvent type:
     *   - user starts capture.
     *   - this app enables capture run-time UI (pause/resume/stop).
     *   - user controls recording with run-time UI, eventually tap "stop" to end.
     *   - this app informs CameraX recording to stop with recording.stop() (or recording.close()).
     *   - CameraX notify this app that the recording is indeed stopped, with the Finalize event.
     */
    private fun updateUI(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Status -> {
                var recordedSeconds = TimeUnit.NANOSECONDS.toSeconds(event.recordingStats.recordedDurationNanos)
                if (mHalf == 2) {
                    recordedSeconds += SECOND_HALF_TIME
                }
                val time = convertRecordedTime(recordedSeconds)
                captureViewBinding.tvTimer.text = time

                when(mHalf) {
                    1 -> {
                        if (isStart && recordedSeconds >= SECOND_HALF_TIME) {
                            stopRecording()
                            return
                        }
                    }
                    2 -> {
                        if (isStart && recordedSeconds >= SECOND_HALF_TIME * 2) {
                            stopRecording()
                            return
                        }
                    }
                }
            }

            is VideoRecordEvent.Start -> {
                showUI(UiState.RECORDING)
            }

            is VideoRecordEvent.Finalize -> if(event.hasError()) {
                Log.i(TAG, event.error.toString(), event.cause)
            } else {
                showUI(UiState.FINALIZED)
            }
        }
    }

    private fun convertRecordedTime(recordedSeconds: Long): String {
        val minutes = recordedSeconds / SIXTY
        val secs = recordedSeconds % SIXTY
        val time = String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
        return time
    }

    /**
     * initialize UI for recording:
     *  - at recording: hide audio, qualitySelection,change camera UI; enable stop button
     *  - otherwise: show all except the stop button
     */
    private fun showUI(state: UiState) {
        captureViewBinding.let {
            when (state) {
                UiState.IDLE -> {
                }

                UiState.RECORDING -> {
                }

                UiState.FINALIZED -> {
                }

                else -> {
                    val errorMsg = "Error: showUI($state) is not supported"
                    Log.e(TAG, errorMsg)
                    error(errorMsg)
                }
            }
        }
    }

    /**
     * ResetUI (restart):
     *    in case binding failed, let's give it another change for re-try. In future cases
     *    we might fail and user get notified on the status
     */
    private fun resetUIandState() {
        showUI(UiState.IDLE)

        cameraIndex = 0
        qualityIndex = DEFAULT_QUALITY_IDX
        audioEnabled = true
    }

    override fun onResume() {
        super.onResume()
        if (currentRecording != null) {
            currentRecording?.resume()
            resumeTimer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (currentRecording != null) {
            currentRecording?.pause()
            pauseTimer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun makeAction(imageView: ImageView) {
        if (isActionClick) {
            actionTimer()
            if (!isClicked && isStart) {
                if (CommonMethods.isNetworkAvailable(context)) {
                    mImageView = imageView
                    changeImage(0)
                    saveActions()
                } else {
                    changeImage(1)
                    errorMsg(resources.getString(com.game.awesa.R.string.error_internet))
                }
            }
        }
    }

    private fun changeImage(type: Int) {
        isClicked = type == 0

        when (mImageView) {
            captureViewBinding.imgTor -> {
                reaction = "goal"
                CommonMethods.loadImageDrawable(
                    context,
                    if (type == 0) com.game.awesa.R.drawable.loading_img_one else com.game.awesa.R.drawable.tor_one,
                    mImageView,
                    type
                )
            }
            captureViewBinding.imgChance -> {
                reaction = "chance"
                CommonMethods.loadImageDrawable(
                    context,
                    if (type == 0) com.game.awesa.R.drawable.loading_img_one else com.game.awesa.R.drawable.chance_one,
                    mImageView,
                    type
                )
            }
            captureViewBinding.imgHighlight -> {
                reaction = "Highlight"
                CommonMethods.loadImageDrawable(
                    context,
                    if (type == 0) com.game.awesa.R.drawable.loading_img_one else com.game.awesa.R.drawable.highlight,
                    mImageView,
                    type
                )
            }
            captureViewBinding.imgTor1 -> {
                reaction = "goal"
                CommonMethods.loadImageDrawable(
                    context,
                    if (type == 0) com.game.awesa.R.drawable.loading_img else com.game.awesa.R.drawable.tor_two,
                    mImageView,
                    type
                )
            }
            captureViewBinding.imgChance1 -> {
                reaction = "chance"
                CommonMethods.loadImageDrawable(
                    context,
                    if (type == 0) com.game.awesa.R.drawable.loading_img else com.game.awesa.R.drawable.chance_two,
                    mImageView,
                    type
                )
            }
            captureViewBinding.imgHighlight1 -> {
                reaction = "Highlight"
                CommonMethods.loadImageDrawable(
                    context,
                    if (type == 0) com.game.awesa.R.drawable.loading_img else com.game.awesa.R.drawable.highlight,
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
            if (mImageView == captureViewBinding.imgTor ||
                mImageView == captureViewBinding.imgChance ||
                mImageView == captureViewBinding.imgHighlight
            ) {
                strTeamId = mMatchBean!!.team_id.toString()
            } else {
                strTeamId = mMatchBean!!.opponent_team_id.toString()
            }
        }
    }

    private fun stopRecording() {
        if (currentRecording == null || recordingState is VideoRecordEvent.Finalize) {
            return
        }

        val recording = currentRecording
        if (recording != null && isStart) {
            captureViewBinding.ivStop.setVisibility(View.GONE)
            recording.stop()
            currentRecording = null
        }
    }

    @Suppress("MagicNumber")
    override fun onClick(v: View) {
        when (v.id) {
            com.game.awesa.R.id.imgZoomIn -> {
                zoomIn()
            }

            com.game.awesa.R.id.imgZoomOut -> {
                zoomOut()
            }

            com.game.awesa.R.id.iv_stop -> {
                stopRecording()
            }

            com.game.awesa.R.id.imgTor -> {
                makeAction(captureViewBinding.imgTor)
            }

            com.game.awesa.R.id.imgTor1 -> {
                makeAction(captureViewBinding.imgTor1)
            }

            com.game.awesa.R.id.imgChance -> {
                makeAction(captureViewBinding.imgChance)
            }

            com.game.awesa.R.id.imgChance1 -> {
                makeAction(captureViewBinding.imgChance1)
            }

            com.game.awesa.R.id.imgHighlight -> {
                makeAction(captureViewBinding.imgHighlight)
            }

            com.game.awesa.R.id.imgHighlight1 -> {
                makeAction(captureViewBinding.imgHighlight1)
            }
        }
    }

    @Suppress("MagicNumber")
    private fun startTimerCounter() {
        if (secondsPassed + 1 < Tags.recording_duration / BYTE) {
            captureViewBinding.countdownTimerTxt.text = "3"
            captureViewBinding.countdownTimerTxt.visibility = View.VISIBLE
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
            mTimer = object : CountDownTimer(FIVE_MILLISECONDS, BYTE) {
                override fun onTick(millisUntilFinished: Long) {
                    captureViewBinding.countdownTimerTxt.text = "${millisUntilFinished / BYTE}"
                    captureViewBinding.countdownTimerTxt.setAnimation(scaleAnimation)
                    captureViewBinding.tvHalfStatus.setText(
                        if (mHalf == 1) getString(com.game.awesa.R.string.lbl_first_half) else getString(
                            com.game.awesa.R.string.lbl_second_half
                        )
                    )
                }

                override fun onFinish() {
                    captureViewBinding.countdownTimerTxt.visibility = View.GONE
                    isStart = true
                    captureVideo()
                    mTimer!!.cancel()
                }
            }
            mTimer!!.start()
        }
    }

    private fun saveActions() {
        databaseManager.executeQuery {
            val dao = MatchActionsDAO(it, context)
            val mBean = ReactionsBean()
            mBean.match_id = if (mMatchBean != null && mMatchBean!!.id > 0) mMatchBean!!.id else 0
            mBean.team_id = if (CommonMethods.isValidString(strTeamId)) strTeamId.toInt() else 0
            mBean.team_name = if (mMatchBean != null && mMatchBean!!.id > 0) {
                if (mMatchBean!!.team_id == strTeamId.toInt()) {
                    mMatchBean!!.team1
                } else {
                    mMatchBean!!.team2
                }
            } else {
                getString(com.game.awesa.R.string.app_name)
            }

            var recordedSeconds = TimeUnit.NANOSECONDS.toSeconds(
                recordingState.recordingStats.recordedDurationNanos
            )

            if (mHalf == 2) {
                recordedSeconds += SECOND_HALF_TIME
            }

            mBean.half = mHalf
            mBean.time = convertRecordedTime(recordedSeconds)
            mBean.timestamp = TimeUnit.NANOSECONDS.toSeconds(
                recordingState.recordingStats.recordedDurationNanos
            )
            mBean.reaction = reaction
            mBean.file_name = ""
            mBean.video = ""
            mBean.upload_status = 0
            mBean.created_date = CommonMethods.getCurrentFormatedDate("yyyy-MM-dd HH:mm:ss")

            //2023-08-16 13:06:03 //yyyy/MM/dd HH:mm:ss
            val mList: ArrayList<ReactionsBean> = ArrayList()
            mList.add(mBean)
            dao.insert(mList)
            val teamOneScore: Int =
                dao.getGoalCount(mMatchBean!!.team_id.toString(), mMatchBean!!.id.toString())
            val teamTwoScore: Int = dao.getGoalCount(
                mMatchBean!!.opponent_team_id.toString(),
                mMatchBean!!.id.toString()
            )
            captureViewBinding.tvTeamOneScore.text = String.format(Locale.getDefault(), "%d", teamOneScore)
            captureViewBinding.tvTeamTwoScore.text = String.format(Locale.getDefault(),"%d", teamTwoScore)

            CommonMethods.successToast(
                context,
                getString(com.game.awesa.R.string.msg_action_success, reaction)
            )
            changeImage(1)
        }
    }

    @OptIn(UnstableApi::class)
    @Suppress("MagicNumber")
    private fun saveVideo() {
        val fileName = mediaFile.toString()
            .substring(mediaFile.toString().lastIndexOf('/') + 1, mediaFile.toString().length)
        val extension = "mp4"
        val timeStamp = SimpleDateFormat(
            "yyyy/MM/dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date())
        databaseManager.executeQuery {
            val dao = VideoMasterDAO(it, context)
            val mBean = VideoUploadBean()
            mBean.match_id = mMatchBean?.id.toString()
            mBean.video_name = fileName
            mBean.video_ext = extension
            mBean.video_path = mediaFile.toString()
            mBean.video_half = mHalf.toString()
            mBean.upload_status = 0
            mBean.date = timeStamp
            val mList: ArrayList<VideoUploadBean> = ArrayList()
            mList.add(mBean)
            dao.insert(mList)
            captureViewBinding.llUpload.visibility = View.GONE

            CommonMethods.checkTrimServiceWithData(requireActivity(), TrimService::class.java, matchId)
            when (mHalf) {
                1 -> {
                    captureViewBinding.btnReTakeVideo.text = getString(com.game.awesa.R.string.lbl_start_second_half)
                    captureViewBinding.btnReTakeVideo.visibility = View.VISIBLE
                    captureViewBinding.btnReTakeVideo.setOnClickListener {
                        startActivity(
                            Intent(
                                context,
                                CameraActivity::class.java
                            ).putExtra(EXTRA_MATCH_HALF, 2).putExtra(EXTRA_MATCH_BEAN, mMatchBean)
                        )
                        requireActivity().finish()
                    }
                }
                0, 3 -> {
                    captureViewBinding.btnReTakeVideo.visibility = View.VISIBLE
                    captureViewBinding.btnReTakeVideo.setOnClickListener {
                        mHalf = if (mHalf == 3) 2 else 1
                        initVideo()
                        captureViewBinding.btnReTakeVideo.visibility = View.GONE
                        captureViewBinding.rlAnotherVideo.visibility = View.GONE
                        captureViewBinding.llUpload.visibility = View.GONE
                        startTimerCounter()
                    }
                }
                else -> {
                    makeConfirmation(getString(com.game.awesa.R.string.msg_video_success))
                }
            }
        }
    }

    fun makeConfirmation(msg: String) {
        if (!isDialogOpen) {
            if (customDialog == null) {
                customDialog = CustomDialog(
                    context,
                    msg,
                    getString(com.game.awesa.R.string.lbl_interview),
                    getString(com.game.awesa.R.string.lbl_end_video),
                    this,
                    "1"
                )
                customDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            isDialogOpen = true
            if (customDialog!!.isShowing) {
                customDialog!!.dismiss()
            }
            customDialog!!.show()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onConfirm(isTrue: Boolean, type: String) {
        isDialogOpen = false
        if (isTrue) {
            if (type == "99") {
                UserSessions.clearUserInfo(context)
                val intent = Intent(context, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                requireActivity().finishAffinity()
            } else {
                val intent = Intent(context, ProcessingActivity::class.java)
                intent.putExtra(ProcessingActivity.EXTRA_MATCH_BEAN, mMatchBean)
                startActivity(intent)
                requireActivity().finish()
            }
        } else {
            val intent = Intent(context, InterviewActivityNew::class.java)
            intent.putExtra(ProcessingActivity.EXTRA_MATCH_BEAN, mMatchBean)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    @Suppress("MagicNumber")
    override fun onSuccess(response: UniversalObject) {
        try {
            when (response.methodName) {
                Tags.SB_CREATE_MATCH_ACTION_API -> {
                    val mBean = response.response as CommonBean
                    if (mBean.status == 1 && CommonMethods.isValidArrayList(mBean.scores)) {
                        captureViewBinding.tvTeamOneScore.text =
                            String.format(Locale.getDefault(), "%d", mBean.scores[0].team1_score)
                        captureViewBinding.tvTeamTwoScore.text =
                            String.format(Locale.getDefault(),"%d", mBean.scores[0].team2_score)
                    } else if (mBean.status == 99) {
                        UserSessions.clearUserInfo(context)
                        Global().makeConfirmation(mBean.msg, requireActivity(), this)
                    }
                    changeImage(1)
                }
            }
        } catch (ex: JsonSyntaxException) {
            Log.e(TAG, ex.localizedMessage, ex)
            errorMsg(getString(com.game.awesa.R.string.something_wrong))
        } catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage, ex)
            errorMsg(getString(com.game.awesa.R.string.something_wrong))
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
            context,
            strMsg,
            resources.getString(com.game.awesa.R.string.app_name),
            resources.getString(com.game.awesa.R.string.lbl_ok)
        );
    }

    private fun captureVideo() {
        if (!this@CaptureFragment::recordingState.isInitialized ||
            recordingState is VideoRecordEvent.Finalize
        ) {
            makeAnimation(0)
            captureViewBinding.ivStop.setVisibility(View.VISIBLE)

            startRecording()
        } else {
            when (recordingState) {
                is VideoRecordEvent.Start -> {
                    currentRecording?.pause()
                    captureViewBinding.ivStop.visibility = View.VISIBLE
                }

                is VideoRecordEvent.Pause -> currentRecording?.resume()
                is VideoRecordEvent.Resume -> currentRecording?.pause()
                else -> error("recordingState in unknown state")
            }
        }
    }

    private fun initVideo() {
        databaseManager.executeQuery {
            val dao = MatchActionsDAO(it, context)
            try {
                dao.deleteByMatch(
                    if (mMatchBean != null && mMatchBean!!.id > 0) mMatchBean!!.id.toString() else "0",
                    mHalf.toString(),
                    1
                )
            } catch (Ex: Exception) {
                Ex.printStackTrace()
            }
            val dao1 = VideoMasterDAO(it, context)
            try {
                dao1.deleteVideoByMatch(
                    if (mMatchBean != null && mMatchBean!!.id > 0) mMatchBean!!.id else 0, mHalf
                )
            } catch (Ex: Exception) {
                Ex.printStackTrace()
            }

            try {
                val teamOneScore: Int =
                    dao.getGoalCount(mMatchBean!!.team_id.toString(), mMatchBean!!.id.toString())
                val teamTwoScore: Int = dao.getGoalCount(
                    mMatchBean!!.opponent_team_id.toString(),
                    mMatchBean!!.id.toString()
                )
                captureViewBinding.tvTeamOneScore.text = String.format(Locale.getDefault(), "%d", teamOneScore)
                captureViewBinding.tvTeamTwoScore.text = String.format(Locale.getDefault(),"%d", teamTwoScore)
            } catch (Ex: Exception) {
                Ex.printStackTrace()
            }
        }

    }

    private var animation: Animation? = null
    private fun makeAnimation(type: Int) {
        if (animation == null) {
            animation = AnimationUtils.loadAnimation(
                context,
                com.game.awesa.R.anim.blink
            )
        }
        if (type == 0) {
            captureViewBinding.ivRec.startAnimation(animation)
        } else {
            animation!!.cancel()
            animation!!.reset()
        }
    }

    var isActionClick = true
    private fun actionTimer() {
        actionTimer = object : CountDownTimer(FIVE_MILLISECONDS, BYTE) {
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
