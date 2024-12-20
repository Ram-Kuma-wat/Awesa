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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.databinding.FragmentInterviewBinding
import com.game.awesa.services.TrimService
import com.game.awesa.ui.LoginActivity
import com.game.awesa.ui.recorder.CaptureFragment.Companion.SIXTY
import com.game.awesa.ui.recorder.extensions.getAspectRatio
import com.game.awesa.ui.recorder.extensions.getNameString
import com.game.awesa.utils.GenericListAdapter
import com.game.awesa.utils.Global
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
class InterviewFragment : Fragment(), OnClickListener, OnResponse<UniversalObject>,
    OnConfirmListener {

    companion object {
        const val EXTRA_MATCH_BEAN = "mMatchBean"
        private const val BYTE = 1_000L
        const val INTERVIEW_TIME = 1200L
        // default Quality selection if no input from UI
        const val DEFAULT_QUALITY_IDX = 0
        val TAG: String = InterviewFragment::class.java.simpleName
    }

    @Inject
    lateinit var databaseManager: DatabaseManager
    // UI with ViewBinding
    private lateinit var captureViewBinding: FragmentInterviewBinding

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

    // from camera activity
    var isStart = false
    private var secondsPassed = 0
    var mTimer: CountDownTimer? = null
    private var strUserId = ""
    private var mMatchBean: MatchesBean.InfoBean? = null
    private var mediaFile: File? = null

    private var matchId = ""

    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(requireContext()) }
    private var enumerationDeferred: Deferred<Unit>? = null

    private val requestAudioPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                audioEnabled = true
            } else {
                audioEnabled = false
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    /**
     * CaptureEvent listener.
     */
    private val captureListener = Consumer<VideoRecordEvent> { event ->
        // cache the recording state
        recordingState = event

        updateUI(event)

        if (event is VideoRecordEvent.Finalize && !event.hasError()) {
            // display the captured video
            // Video record completed
            mediaFile = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                File(getAbsolutePathFromUri(event.outputResults.outputUri))
            } else {
                // force MediaScanner to re-scan the media file.
                File(getAbsolutePathFromUri(event.outputResults.outputUri))
            }
            saveVideo()
            captureViewBinding.statusOverlay.visibility = View.VISIBLE
            captureViewBinding.progressLayout.visibility = View.VISIBLE
            captureViewBinding.btnStartVideo.visibility = View.VISIBLE
        }
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

        val preview = Preview.Builder()
            .setTargetAspectRatio(quality.getAspectRatio(quality))
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
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                videoCapture,
                preview
            )
        } catch (exc: UnsupportedOperationException) {
            // we are on main thread, let's reset the controls on the UI.
            Log.e(TAG, "Use case binding failed", exc)
            resetUIandState("bindToLifecycle failed: $exc")
        }
        enableUI(true)
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
        // create MediaStoreOutputOptions for our recorder: resulting our recording!
        val name = "match_" + matchId + "_interview_" + ".mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            requireActivity().contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        // configure Recorder and Start recording to the mediaStoreOutput.
        currentRecording = videoCapture.output
            .prepareRecording(requireActivity(), mediaStoreOutput)
            .asPersistentRecording() // Audio data is recorded after the VideoCapture is unbound
            .apply {
                if (ContextCompat.checkSelfPermission(
                        this@InterviewFragment.requireContext(),
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    return
                }
                if (audioEnabled) withAudioEnabled()
            }
            .start(mainThreadExecutor, captureListener)

        Log.i(TAG, "Recording started")
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
                "VideoViewerFragment", String.format(Locale.getDefault(),
                    "Failed in getting absolute path for Uri %s with Exception %s",
                    contentUri.toString(), e.toString()
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
                        // just get the camera.cameraInfo to query capabilities
                        // we are not binding anything here.
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
                    } catch (ex: IllegalArgumentException) {
                        Log.e(TAG, "Camera Face $camSelector is not supported ${ex.localizedMessage}")
                    } catch (ex: UnsupportedOperationException) {
                        Log.e(TAG, "Camera Face $camSelector is not supported ${ex.localizedMessage}")
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
            initializeQualitySectionsUI()

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
        strUserId =
            if (UserSessions.getUserInfo(requireActivity()) != null) UserSessions.getUserInfo(
                requireActivity()
            ).id.toString() else "0"
        val arguments = arguments
        if (arguments != null && arguments.containsKey("MatchBean")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mMatchBean =
                    arguments.getSerializable("MatchBean", MatchesBean.InfoBean::class.java)
            } else {
                mMatchBean = arguments.getSerializable("MatchBean") as MatchesBean.InfoBean?
            }
        }
        if (mMatchBean != null) {
            matchId = mMatchBean!!.id.toString()
            captureViewBinding.tvTeamOneName.text = mMatchBean!!.team1
            captureViewBinding.tvTeamTwoName.text = mMatchBean!!.team2
        }

        captureViewBinding.btnStartVideo.visibility = View.VISIBLE
        captureViewBinding.statusOverlay.visibility = View.VISIBLE
        captureViewBinding.progressLayout.visibility = View.GONE
        captureViewBinding.btnStartVideo.text = getString(com.game.awesa.R.string.lbl_start_interview1)
        captureViewBinding.btnStartVideo.setOnClickListener {
            captureViewBinding.btnStartVideo.visibility = View.GONE
            captureViewBinding.statusOverlay.visibility = View.GONE
            captureViewBinding.progressLayout.visibility = View.GONE
            initVideo()
            startTimerCounter()
        }
        databaseManager.executeQuery {
            val dao = MatchActionsDAO(it, requireActivity())
            val teamOneScore: Int =
                dao.getGoalCount(mMatchBean!!.team_id.toString(), mMatchBean!!.id.toString())
            val teamTwoScore: Int = dao.getGoalCount(
                mMatchBean!!.opponent_team_id.toString(),
                mMatchBean!!.id.toString()
            )
            captureViewBinding.tvTeamOneScore.text =
                String.format(Locale.getDefault(), "%d", teamOneScore)
            captureViewBinding.tvTeamTwoScore.text =
                String.format(Locale.getDefault(), "%d", teamTwoScore)
        }
        captureViewBinding.ivStop.setOnClickListener(this)

        // audioEnabled by default is disabled.
        captureViewBinding.audioSelection.isChecked = audioEnabled
        captureViewBinding.audioSelection.setOnClickListener {
            audioEnabled = captureViewBinding.audioSelection.isChecked
        }
    }

    /**
     * UpdateUI according to CameraX VideoRecordEvent type:
     *   - user starts capture.
     *   - this app disables all UI selections.
     *   - this app enables capture run-time UI (pause/resume/stop).
     *   - user controls recording with run-time UI, eventually tap "stop" to end.
     *   - this app informs CameraX recording to stop with recording.stop() (or recording.close()).
     *   - CameraX notify this app that the recording is indeed stopped, with the Finalize event.
     *   - this app starts VideoViewer fragment to view the captured result.
     */
    private fun updateUI(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Status -> {
                val recordedSeconds = TimeUnit.NANOSECONDS.toSeconds(event.recordingStats.recordedDurationNanos)
                val time = convertRecordedTime(recordedSeconds)
                captureViewBinding.tvTimer.text = time

                if (isStart && recordedSeconds >= INTERVIEW_TIME) {
                    stopRecording()
                    return
                }
            }

            is VideoRecordEvent.Start -> {
                showUI(UiState.RECORDING)
            }

            is VideoRecordEvent.Finalize -> if(event.hasError()) {
                Log.i(CaptureFragment.TAG, event.error.toString(), event.cause)
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
     * Enable/disable UI:
     *    User could select the capture parameters when recording is not in session
     *    Once recording is started, need to disable able UI to avoid conflict.
     */
    private fun enableUI(enable: Boolean) {
        arrayOf(
            captureViewBinding.audioSelection,
            captureViewBinding.qualitySelection
        ).forEach {
            it.isEnabled = enable
        }
        // disable the camera button if no device to switch
        if (cameraCapabilities.size <= 1) {
            //captureViewBinding.cameraButton.isEnabled = false
        }
        // disable the resolution list if no resolution to switch
        if (cameraCapabilities[cameraIndex].qualities.size <= 1) {
            captureViewBinding.qualitySelection.apply { isEnabled = false }
        }
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
                    it.audioSelection.visibility = View.VISIBLE
                    it.qualitySelection.visibility = View.VISIBLE
                }

                UiState.RECORDING -> {
                    it.audioSelection.visibility = View.INVISIBLE
                    it.qualitySelection.visibility = View.INVISIBLE
                }

                UiState.FINALIZED -> {
                }

                else -> {
                    val errorMsg = "Error: showUI($state) is not supported"
                    Log.e(TAG, errorMsg)
                    return
                }
            }
        }
    }

    /**
     * ResetUI (restart):
     *    in case binding failed, let's give it another change for re-try. In future cases
     *    we might fail and user get notified on the status
     */
    private fun resetUIandState(reason: String) {
        enableUI(true)
        showUI(UiState.IDLE)

        cameraIndex = 0
        qualityIndex = DEFAULT_QUALITY_IDX
        audioEnabled = true
        captureViewBinding.audioSelection.isChecked = audioEnabled
        initializeQualitySectionsUI()
    }

    /**
     *  initializeQualitySectionsUI():
     *    Populate a RecyclerView to display camera capabilities:
     *       - one front facing
     *       - one back facing
     *    User selection is saved to qualityIndex, will be used
     *    in the bindCaptureUsecase().
     */
    private fun initializeQualitySectionsUI() {
        val selectorStrings = cameraCapabilities[cameraIndex].qualities.map {
            it.getNameString()
        }
        // create the adapter to Quality selection RecyclerView
        captureViewBinding.qualitySelection.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = GenericListAdapter(
                selectorStrings,
                itemLayoutId = com.game.awesa.R.layout.video_quality_item
            ) { holderView, qcString, position ->

                holderView.apply {
                    findViewById<TextView>(com.game.awesa.R.id.qualityTextView)?.text = qcString
                    // select the default quality selector
                    isSelected = (position == qualityIndex)
                }

                holderView.setOnClickListener { view ->
                    if (qualityIndex == position) return@setOnClickListener

                    captureViewBinding.qualitySelection.let {
                        // deselect the previous selection on UI.
                        it.findViewHolderForAdapterPosition(qualityIndex)
                            ?.itemView
                            ?.isSelected = false
                    }
                    // turn on the new selection on UI.
                    view.isSelected = true
                    qualityIndex = position

                    // rebind the use cases to put the new QualitySelection in action.
                    enableUI(false)
                    viewLifecycleOwner.lifecycleScope.launch {
                        bindCaptureUsecase()
                    }
                }
            }
            isEnabled = false
        }
    }

    // System function implementations
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        captureViewBinding = FragmentInterviewBinding.inflate(inflater, container, false)
        return captureViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCameraFragment()
    }

    override fun onResume() {
        super.onResume()
        if (currentRecording != null) {
            currentRecording?.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (currentRecording != null) {
            currentRecording?.pause()
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

    override fun onClick(v: View) {
        when (v.id) {
            com.game.awesa.R.id.iv_stop -> {
                stopRecording()
            }
        }
    }

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
            mTimer = object : CountDownTimer(5000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    captureViewBinding.countdownTimerTxt.text = "" + millisUntilFinished / 1000
                    captureViewBinding.countdownTimerTxt.setAnimation(scaleAnimation)
                    captureViewBinding.tvHalfStatus.text = getString(com.game.awesa.R.string.lbl_interview1)
                }

                override fun onFinish() {
                    captureViewBinding.countdownTimerTxt.visibility = View.GONE
                    isStart = true
                    captureVideo()
                    mTimer!!.cancel()
                }
            }.start()
        }
    }

    @OptIn(UnstableApi::class)
    private fun saveVideo() {
        val fileName = mediaFile.toString()
            .substring(mediaFile.toString().lastIndexOf('/') + 1, mediaFile.toString().length)

        val timeStamp = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date())

        databaseManager.executeQuery {
            val dao = InterviewsDAO(it, requireActivity())
            val matchId =
                if (mMatchBean != null && mMatchBean!!.id > 0) mMatchBean!!.id.toString() else ""
            dao.insert(matchId, fileName, mediaFile.toString(), "0", timeStamp)

            CommonMethods.checkTrimServiceWithData(requireActivity(), TrimService::class.java, matchId)

            val intent = Intent(requireActivity(), ProcessingActivity::class.java)
            intent.putExtra(EXTRA_MATCH_BEAN, mMatchBean)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onConfirm(isTrue: Boolean, type: String) {
         if (isTrue) {
            if (type == "99") {
                UserSessions.clearUserInfo(requireActivity())
                startActivity(
                    Intent(
                        requireActivity(),
                        LoginActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                requireActivity().finishAffinity()
            } else {
                // Overview
                val intent = Intent(requireActivity(), MatchOverviewActivity::class.java)
                intent.putExtra(MatchOverviewActivity.EXTRA_MATCH_BEAN, mMatchBean)
                startActivity(intent)
                requireActivity().finish()
            }
            // CommonMethods.moveWithClear(requireActivity(), LoginActivity::class.java)
        } else {
            //interview
            val intent = Intent(requireActivity(), InterviewActivityNew::class.java)
            intent.putExtra(InterviewActivityNew.EXTRA_MATCH_BEAN, mMatchBean)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onSuccess(response: UniversalObject) {
        try {
            when (response.methodName) {
                Tags.SB_CREATE_MATCH_ACTION_API -> {
                    try {
                        val mBean = response.response as CommonBean
                        if (mBean.status == 1 && CommonMethods.isValidArrayList(mBean.scores)) {
                            captureViewBinding.tvTeamOneScore.text = String.format(
                                Locale.getDefault(), "%d", mBean.scores[0].team1_score
                            )
                            captureViewBinding.tvTeamTwoScore.text = String.format(
                                Locale.getDefault(),"%d", mBean.scores[0].team2_score
                            )
                        } else if (mBean.status == 99) {
                            UserSessions.clearUserInfo(requireActivity())
                            Global().makeConfirmation(mBean.msg, requireActivity(), this)
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, ex.localizedMessage)
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage)
            errorMsg(getString(com.game.awesa.R.string.something_wrong))
        }
    }

    override fun onError(type: String, error: String) {
        when (type) {
            Tags.SB_CREATE_MATCH_ACTION_API -> {
            }
        }
    }

    fun errorMsg(strMsg: String) {
        CommonMethods.errorDialog(
            requireActivity(),
            strMsg,
            getResources().getString(com.game.awesa.R.string.app_name),
            getResources().getString(com.game.awesa.R.string.lbl_ok)
        )
    }

    private fun captureVideo() {
        if (!this@InterviewFragment::recordingState.isInitialized ||
            recordingState is VideoRecordEvent.Finalize
        ) {
            makeAnimation(0)
            captureViewBinding.ivStop.setVisibility(View.VISIBLE)

            enableUI(false)  // Our eventListener will turn on the Recording UI.
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
            val dao = MatchActionsDAO(it, requireActivity())

            try {
                val teamOneScore: Int =
                    dao.getGoalCount(mMatchBean!!.team_id.toString(), mMatchBean!!.id.toString())
                val teamTwoScore: Int = dao.getGoalCount(
                    mMatchBean!!.opponent_team_id.toString(),
                    mMatchBean!!.id.toString()
                )
                captureViewBinding.tvTeamOneScore.text = teamOneScore.toString()
                captureViewBinding.tvTeamTwoScore.text = teamTwoScore.toString()
            } catch (ex: Exception) {
                Log.e(TAG, ex.localizedMessage)
            }
        }
    }

    private var animation: Animation? = null
    private fun makeAnimation(type: Int) {
        if (animation == null) {
            animation = AnimationUtils.loadAnimation(
                requireActivity(),
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
}
