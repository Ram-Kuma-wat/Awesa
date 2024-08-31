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

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
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
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.recyclerview.widget.LinearLayoutManager
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.listeners.QueryExecutor
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.databinding.FragmentInterviewBinding
import com.game.awesa.services.TrimService
import com.game.awesa.ui.LoginActivity
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
import javax.inject.Inject

@AndroidEntryPoint
class InterviewFragment : Fragment(), OnClickListener, OnResponse<UniversalObject>,
    OnConfirmListener {
    @Inject
    lateinit var databaseManager: DatabaseManager
    // UI with ViewBinding
    private lateinit var captureViewBinding: FragmentInterviewBinding
    private val captureLiveStatus = MutableLiveData<String>()

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

        /* captureViewBinding.previewView.updateLayoutParams<ConstraintLayout.LayoutParams> {
             val orientation = this@CaptureFragment.resources.configuration.orientation
             dimensionRatio = quality.getAspectRatioString(
                 quality,
                 (orientation == Configuration.ORIENTATION_PORTRAIT)
             )
         }*/

        val preview = Preview.Builder()
            .setTargetAspectRatio(quality.getAspectRatio(quality))
            .build().apply {
                setSurfaceProvider(captureViewBinding.previewView.surfaceProvider)
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
        } catch (exc: Exception) {
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
    @SuppressLint("MissingPermission")
    private fun startRecording() {
        /*      var timeStamp = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())
              mediaFile = File(
                  mediaStorageDir.path + File.separator +
                          "match_" + "_" + timeStamp + "_half_" + mHalf + ".mp4"
              )*/
        // create MediaStoreOutputOptions for our recorder: resulting our recording!
        val name = /*"match_" +
                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) +*/
            "match_" + match_id + "_interview_" + ".mp4"
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
            .apply { if (audioEnabled) withAudioEnabled() }
            .start(mainThreadExecutor, captureListener)

        Log.i(TAG, "Recording started")
    }

    /**
     * CaptureEvent listener.
     */
    private val captureListener = Consumer<VideoRecordEvent> { event ->
        // cache the recording state
        if (event !is VideoRecordEvent.Status)
            recordingState = event

        updateUI(event)

        if (event is VideoRecordEvent.Finalize) {
            // display the captured video

            //video record completed
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                mediaFile =
                    File(getAbsolutePathFromUri(event.outputResults.outputUri))//File( event.outputResults.outputUri.path)
            } else {
                // force MediaScanner to re-scan the media file.
                mediaFile = File(getAbsolutePathFromUri(event.outputResults.outputUri))
                /*     MediaScannerConnection.scanFile(
                         context, arrayOf(path), null
                     ) { _, uri ->
                         // playback video on main thread with VideoView
                         if (uri != null) {
                             lifecycleScope.launch {
                                 showVideo(uri)
                             }
                         }
                     }*/
            }
            saveVideo()
            captureViewBinding.rlAnotherVideo.visibility = View.VISIBLE
            captureViewBinding.llUpload.visibility = View.VISIBLE
            captureViewBinding.btnReTakeVideo.visibility = View.VISIBLE

            /*       lifecycleScope.launch {
                       navController.navigate(
                           CaptureFragmentDirections.actionCaptureToVideoViewer(
                               event.outputResults.outputUri
                           )
                       )
                   }*/
        }
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
        } catch (e: RuntimeException) {
            Log.e(
                "VideoViewerFragment", String.format(
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
                                    listOf(/*Quality.UHD,*/ Quality.FHD/*, Quality.HD, Quality.SD*/)
                                        .contains(quality)
                                }.also {
                                    cameraCapabilities.add(CameraCapability(camSelector, it))
                                }
                        }
                    } catch (exc: java.lang.Exception) {
                        Log.e(TAG, "Camera Face $camSelector is not supported")
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
                try {
                    mMatchBean = arguments!!.getSerializable("MatchBean") as MatchesBean.InfoBean?
                } catch (ignored: Throwable) {
                }
            }
        }
        if (mMatchBean != null) {
            match_id = mMatchBean!!.id.toString()
            captureViewBinding.tvTeamOneName.setText(mMatchBean!!.team1)
            captureViewBinding.tvTeamTwoName.setText(mMatchBean!!.team2)
        }

        captureViewBinding.btnReTakeVideo.visibility = View.VISIBLE
        captureViewBinding.rlAnotherVideo.visibility = View.VISIBLE
        captureViewBinding.llUpload.visibility = View.GONE
        captureViewBinding.btnReTakeVideo.setText(getString(com.game.awesa.R.string.lbl_start_interview1))
        captureViewBinding.btnReTakeVideo.setOnClickListener {
            captureViewBinding.btnReTakeVideo.visibility = View.GONE
            captureViewBinding.rlAnotherVideo.visibility = View.GONE
            captureViewBinding.llUpload.visibility = View.GONE
            initVideo()
            startTimerCounter()
        }
        databaseManager.executeQuery(QueryExecutor {
            val dao = MatchActionsDAO(it, requireActivity())
            val mCOUNT: Int =
                dao.getRowCount(mMatchBean!!.team_id.toString(), mMatchBean!!.id.toString())
            val mCOUNT1: Int = dao.getRowCount(
                mMatchBean!!.opponent_team_id.toString(),
                mMatchBean!!.id.toString()
            )
            captureViewBinding.tvTeamOneScore.setText(mCOUNT.toString())
            captureViewBinding.tvTeamTwoScore.setText(mCOUNT1.toString())
        })
        captureViewBinding.ivStop.setOnClickListener(this)

        // audioEnabled by default is disabled.
        captureViewBinding.audioSelection.isChecked = audioEnabled
        captureViewBinding.audioSelection.setOnClickListener {
            audioEnabled = captureViewBinding.audioSelection.isChecked
        }


        /*     captureViewBinding.stopButton.apply {
                 setOnClickListener {
                     // stopping: hide it after getting a click before we go to viewing fragment
                     captureViewBinding.stopButton.visibility = View.INVISIBLE
                     if (currentRecording == null || recordingState is VideoRecordEvent.Finalize) {
                         return@setOnClickListener
                     }

                     val recording = currentRecording
                     if (recording != null) {
                         recording.stop()
                         currentRecording = null
                     }
                     captureViewBinding.captureButton.setImageResource(R.drawable.ic_start)
                 }
                 // ensure the stop button is initialized disabled & invisible
                 visibility = View.INVISIBLE
                 isEnabled = false
             }*/

        /*       captureLiveStatus.observe(viewLifecycleOwner) {
                   captureViewBinding.captureStatus.apply {
                       post { text = it }
                   }
               }
               captureLiveStatus.value = getString(R.string.Idle)*/
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
        val state = if (event is VideoRecordEvent.Status) recordingState.getNameString()
        else event.getNameString()
        when (event) {
            is VideoRecordEvent.Status -> {
                // placeholder: we update the UI with new status after this when() block,
                // nothing needs to do here.
            }

            is VideoRecordEvent.Start -> {
                showUI(UiState.RECORDING, event.getNameString())
            }

            is VideoRecordEvent.Finalize -> {
                showUI(UiState.FINALIZED, event.getNameString())
            }

            /*    is VideoRecordEvent.Pause -> {
                    captureViewBinding.captureButton.setImageResource(R.drawable.ic_resume)
                }

                is VideoRecordEvent.Resume -> {
                    captureViewBinding.captureButton.setImageResource(R.drawable.ic_pause)
                }*/
        }

        val stats = event.recordingStats
        val size = stats.numBytesRecorded / 1000
        val time = java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(stats.recordedDurationNanos)
        var text = "${state}: recorded ${size}KB, in ${time}second"
        if (event is VideoRecordEvent.Finalize)
            text = "${text}\nFile saved to: ${event.outputResults.outputUri}"

        // captureLiveStatus.value = text
        Log.i(TAG, "recording event: $text")
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
    private fun showUI(state: UiState, status: String = "idle") {
        captureViewBinding.let {
            when (state) {
                UiState.IDLE -> {
                    /*
                                        it.captureButton.setImageResource(R.drawable.ic_start)
                                        it.stopButton.visibility = View.INVISIBLE

                                        it.cameraButton.visibility = View.VISIBLE
                    */
                    it.audioSelection.visibility = View.VISIBLE
                    it.qualitySelection.visibility = View.VISIBLE
                }

                UiState.RECORDING -> {
                    //it.cameraButton.visibility = View.INVISIBLE
                    it.audioSelection.visibility = View.INVISIBLE
                    it.qualitySelection.visibility = View.INVISIBLE

                    /*
                                        it.captureButton.setImageResource(R.drawable.ic_pause)
                                        it.captureButton.isEnabled = true
                                        it.stopButton.visibility = View.VISIBLE
                                        it.stopButton.isEnabled = true
                    */
                }

                UiState.FINALIZED -> {
                    /*
                                        it.captureButton.setImageResource(R.drawable.ic_start)
                                        it.stopButton.visibility = View.INVISIBLE
                    */
                }

                else -> {
                    val errorMsg = "Error: showUI($state) is not supported"
                    Log.e(TAG, errorMsg)
                    return
                }
            }
            // it.captureStatus.text = status
        }
    }

    /**
     * ResetUI (restart):
     *    in case binding failed, let's give it another change for re-try. In future cases
     *    we might fail and user get notified on the status
     */
    private fun resetUIandState(reason: String) {
        enableUI(true)
        showUI(UiState.IDLE, reason)

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

    override fun onDestroyView() {
        //_captureViewBinding = null
        super.onDestroyView()
    }

    companion object {
        // default Quality selection if no input from UI
        const val DEFAULT_QUALITY_IDX = 0
        val TAG: String = InterviewFragment::class.java.simpleName
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
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

    // from camera activity
    var isClicked = false
    var isStart = false
    var strTime = "00:00"
    var strTeam_id = ""
    var sec_passed = 0
    var seconds = 0;
    var mTimer: CountDownTimer? = null
    var actionTimer: CountDownTimer? = null
    var strUserId = "";
    var mMatchBean: MatchesBean.InfoBean? = null;
    var seconds1 = 0;
    var handler = Handler()
    var mediaFile: File? = null

    var match_id = ""


    fun stopRecording() {
       try {
           if (currentRecording == null || recordingState is VideoRecordEvent.Finalize) {
               return;
           }
       }catch (ex1:Exception){
           ex1.printStackTrace()
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
        if (sec_passed + 1 < Tags.recording_duration / 1000) {
            captureViewBinding.countdownTimerTxt.setText("3")
            captureViewBinding.countdownTimerTxt.setVisibility(View.VISIBLE)
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
                    captureViewBinding.countdownTimerTxt.setText("" + millisUntilFinished / 1000)
                    captureViewBinding.countdownTimerTxt.setAnimation(scaleAnimation)
                    captureViewBinding.tvHalfStatus.setText(getString(com.game.awesa.R.string.lbl_interview1))
                }

                override fun onFinish() {
                    captureViewBinding.countdownTimerTxt.setVisibility(View.GONE)
                    isStart = true
                    runTimer()
                    //Start_or_Stop_Recording()
                    captureVideo()
                    mTimer!!.cancel()
                }
            }//.start()
            mTimer!!.start()
        }
    }


    fun saveVideo() {
        var fileName = mediaFile.toString()
            .substring(mediaFile.toString().lastIndexOf('/') + 1, mediaFile.toString().length)
        var extension = "mp4"//mediaFile.toString().substring(mediaFile.toString().lastIndexOf("."))
        var timeStamp = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())


        databaseManager.executeQuery(QueryExecutor {
            val dao = InterviewsDAO(it, requireActivity())
            var match_id = if(mMatchBean !=null && mMatchBean!!.id>0) mMatchBean!!.id.toString() else ""
            dao.insert(match_id,fileName,mediaFile.toString(),"0",timeStamp)

            var masterDataBaseId = dao.lastInsertedId
            val mCOUNT: Int = dao.getRowCount("")
            // binding.llUpload.visibility=View.GONE
            CommonMethods.checkServiceWIthData(requireActivity(), TrimService::class.java,match_id)
           // CommonMethods.checkService(requireActivity(), TrimService::class.java)
//            CommonMethods.checkService(requireActivity(), InterviewUploadService::class.java)
           // val intent = Intent(requireActivity(), MatchOverviewActivity::class.java)
            val intent = Intent(requireActivity(), ProcessingActivity::class.java)
            intent.putExtra("mMatchBean",mMatchBean)
            startActivity(intent)
            requireActivity().finish()
        })
    }

    override fun onConfirm(isTrue: Boolean, type: String) {
         if (isTrue) {
            if (type.equals("99")) {
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
                //overview
                val intent = Intent(requireActivity(), MatchOverviewActivity::class.java)
               // val intent = Intent(requireActivity(), ProcessingActivity::class.java)
                intent.putExtra("mMatchBean", mMatchBean)
                startActivity(intent)
                requireActivity().finish()
            }
            //CommonMethods.moveWithClear(requireActivity(), LoginActivity::class.java)
        } else {
            //interview
            val intent = Intent(requireActivity(), InterviewActivityNew::class.java)
            intent.putExtra("mMatchBean", mMatchBean)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onSuccess(response: UniversalObject) {
        try {
            when (response.methodName) {
                Tags.SB_CREATE_MATCH_ACTION_API -> {
                    try {
                        var mBean = response.response as CommonBean
                        if (mBean.status == 1 && CommonMethods.isValidArrayList(mBean.scores)) {
                            captureViewBinding.tvTeamOneScore.setText(mBean.scores[0].team1_score.toString())
                            captureViewBinding.tvTeamTwoScore.setText(mBean.scores[0].team2_score.toString())
                        } else if (mBean.status == 99) {
                            UserSessions.clearUserInfo(requireActivity())
                            Global().makeConfirmation(mBean.msg, requireActivity(), this)
                        }
                    } catch (ex1: Exception) {
                        ex1.printStackTrace()
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
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
        );
    }

    private fun runTimer() {
        seconds = 0;
        seconds1 = 45 * 60;
        handler.post(object : java.lang.Runnable {
            override fun run() {
                var minutes = seconds / 60
                var secs = seconds % 60
                var time = String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
/*
                if (mHalf == 2) {
                    val minutes1 = seconds1 / 60
                    val secs1 = seconds1 % 60
                    var time1 = String.format(Locale.getDefault(), "%02d:%02d", minutes1, secs1)
                    captureViewBinding.tvTimer.setText(time1)
                } else {
*/
                    captureViewBinding.tvTimer.setText(time)
//                }
                strTime = time;
                if (isStart) {
                    seconds++
                    seconds1++
                    if (seconds >= Tags.interview_duration) {
                        stopRecording()
                        return;
                    }
                }
                handler.postDelayed(this, 1000)
            }
        })
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
                else -> throw IllegalStateException("recordingState in unknown state")
            }
        }
    }

    fun initVideo() {
        databaseManager.executeQuery(QueryExecutor {
            val dao = MatchActionsDAO(it, requireActivity())

            try {
                val mCOUNT: Int =
                    dao.getRowCount(mMatchBean!!.team_id.toString(), mMatchBean!!.id.toString())
                val mCOUNT1: Int = dao.getRowCount(
                    mMatchBean!!.opponent_team_id.toString(),
                    mMatchBean!!.id.toString()
                )
                captureViewBinding.tvTeamOneScore.setText(mCOUNT.toString())
                captureViewBinding.tvTeamTwoScore.setText(mCOUNT1.toString())
            } catch (Ex: Exception) {
                Ex.printStackTrace()
            }
        })

    }

    var animation1: Animation? = null;
    fun makeAnimation(type: Int) {
        if (animation1 == null) {
            animation1 = AnimationUtils.loadAnimation(
                requireActivity(),
                com.game.awesa.R.anim.blink
            )
        }
        if (type == 0) {
            captureViewBinding.ivRec.startAnimation(animation1)
        } else {
            animation1!!.cancel()
            animation1!!.reset()
        }
    }

    var isActionClick = true
    fun actionTimer() {
        actionTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                isActionClick = false
            }

            override fun onFinish() {
                isActionClick = true
                actionTimer!!.cancel()
            }
        }//.start()
        actionTimer!!.start()
    }
}
