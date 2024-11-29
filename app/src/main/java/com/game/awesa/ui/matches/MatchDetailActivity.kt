package com.game.awesa.ui.matches

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.transition.Visibility
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.util.UnstableApi
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.matches.InterviewBean
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.beans.matches.MatchesBean.VideosBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnMatchListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Logs
import com.codersworld.awesalibs.utils.SFProgress
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.R
import com.game.awesa.databinding.ActivityMatchDetailBinding
import com.game.awesa.ui.BaseActivity
import com.game.awesa.ui.LoginActivity
import com.game.awesa.ui.dashboard.MainActivity
import com.game.awesa.ui.dialogs.CustomDialog
import com.game.awesa.ui.recorder.VideoPreviewActivity
import com.game.awesa.utils.Global
import com.game.awesa.utils.VideoUploadsWorker
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MatchDetailActivity : BaseActivity(), OnConfirmListener, OnResponse<UniversalObject>,
    OnMatchListener {
    @Inject
    lateinit var databaseManager: DatabaseManager
    @Inject lateinit var videoUploadsWorker: VideoUploadsWorker
    lateinit var binding: ActivityMatchDetailBinding
    var mApiCall: ApiCall? = null
    private var gameId = ""
    private lateinit var matchBean: MatchesBean.InfoBean
    var mAdapter: VideosAdapter? = null
    var matchId = ""
    private var totalCount = 0
    private var uploadedCount = 0
    private var mVideo: VideosBean? = null
    private var videoPosition: Int = -1
    private var customDialog: CustomDialog? = null
    private var isDialogOpen = false
    private var deleteType = ""
    private var mIntentFilter: IntentFilter? = null

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val type: VideoUploadsWorker.VideoType? = CommonMethods.getSerializable(intent, "type",
                    VideoUploadsWorker.VideoType::class.java)
                when(type) {
                    VideoUploadsWorker.VideoType.reaction -> {
                            val mBean: VideosBean? = CommonMethods.getSerializable(intent, "data",
                                VideosBean::class.java
                            )

                            val mBeanOld: ReactionsBean? = CommonMethods.getSerializable(intent,
                                "old_data",
                                ReactionsBean::class.java
                            )
                            if (mBeanOld != null) {
                                mAdapter?.updateVideo(mBeanOld, mBean)
                                databaseManager.executeQuery {
                                    val mMatchActionsDAO = MatchActionsDAO(it, this@MatchDetailActivity)
                                    val mActions = mMatchActionsDAO.getTotalCount(matchId)
                                    if (mActions > 0) {
                                        uploadedCount += 1
                                        binding.llUploadProgress.visibility = View.VISIBLE
                                        updateProgressCount()
                                        databaseManager.closeDatabase()
                                    } else {
                                        binding.llUploadProgress.visibility = View.GONE
                                    }
                                }
                            }
                    }
                    VideoUploadsWorker.VideoType.interview -> {
                        val mInterview: VideosBean? = CommonMethods.getSerializable(intent, "data",
                            VideosBean::class.java
                        )

                        val mInterviewOld: InterviewBean? = CommonMethods.getSerializable(intent,
                            "old_data",
                            InterviewBean::class.java
                        )

                        if (mInterviewOld != null) {
                            databaseManager.executeQuery {
                                val mMatchActionsDAO = MatchActionsDAO(it, this@MatchDetailActivity)
                                val mActions = mMatchActionsDAO.getTotalCount(matchId)
                                if (mActions > 0) {
                                    uploadedCount += 1
                                    binding.llUploadProgress.visibility = View.VISIBLE
                                    updateProgressCount()
                                    databaseManager.closeDatabase()
                                } else {
                                    binding.llUploadProgress.visibility = View.GONE
                                }
                            }
                        }
                    }

                    null -> {}
                }
            } catch (e: java.lang.Exception) {
                Log.e("MatchDetailActivity", e.localizedMessage)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        handleIntent()
        setupObserver()
        initApiCall()

        loadLocalVideos()

        getMatchActions()
        binding.swRefresh.setOnRefreshListener {
            getMatchActions()
        }
    }

    private fun handleIntent() {
        if (intent.hasExtra("game_id")) {
            gameId = intent.getStringExtra("game_id") as String
        }
        if (intent.hasExtra("matchBean")) {
            matchBean =
                CommonMethods.getSerializable(intent, "matchBean", MatchesBean.InfoBean::class.java)
            matchId = matchBean.id.toString()
            totalCount = matchBean.total_actions
        }
    }

    private fun setupObserver() {
        val intentFilter = IntentFilter()
        mIntentFilter = intentFilter
        intentFilter.addAction("videoUpload")
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter!!)
    }

    private fun initUI() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_match_detail)
        mAdapter = VideosAdapter(
            this@MatchDetailActivity,
            emptyList(),
            this@MatchDetailActivity
        )
        binding.rvHistory.adapter = mAdapter

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.imgHome.setOnClickListener {
            CommonMethods.moveWithClear(
                this@MatchDetailActivity,
                MainActivity::class.java
            )
        }
    }

    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@MatchDetailActivity)
        }
    }

    @UnstableApi
    override fun onSuccess(response: UniversalObject) {
        binding.swRefresh.isRefreshing = false

        try {
            Logs.d(response.methodName)
            when (response.methodName) {
                Tags.SB_MATCH_DETAIL_API -> {
                    val mBeanMatch: MatchesBean = response.response as MatchesBean
                    if (mBeanMatch.status == 1 && CommonMethods.isValidArrayList(mBeanMatch.info)) {
                        CommonMethods.loadImage(
                            this@MatchDetailActivity,
                            mBeanMatch.info[0].team1_image,binding.imgTeam1
                        )
                        CommonMethods.loadImage(
                            this@MatchDetailActivity,
                            mBeanMatch.info[0].team2_image,binding.imgTeam2
                        )

                        if(!mBeanMatch.info[0].interview.isNullOrEmpty()) {
                            val interViewBean = VideosBean()
                            interViewBean.thumbnail = mBeanMatch.info[0].interview_thumbnail
                            interViewBean.video = mBeanMatch.info[0].interview
                            updateInterviewUI(View.VISIBLE, interViewBean)
                        }

                        CommonMethods.changeView(binding.mNestedScroll, binding.llNoData)
                        uploadedCount = mBeanMatch.info[0].videos.size
                        updateProgressCount()
                        if (totalCount == uploadedCount) {
                            mAdapter?.addAll(mBeanMatch.info[0].videos)
                        } else {
                            mAdapter?.updateVideos(mBeanMatch.info[0].videos)
                        }

                    } else if (mBeanMatch.status == 99) {
                        UserSessions.clearUserInfo(this@MatchDetailActivity)
                        Global().makeConfirmation(mBeanMatch.msg, this@MatchDetailActivity, this)
                    } else if (CommonMethods.isValidString(mBeanMatch.msg)) {
                        errorMsg(mBeanMatch.msg);
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong));
                    }

//                    checkVideosProgress()
                    CommonMethods.changeView(binding.mNestedScroll, binding.llNoData)

                }

                Tags.SB_DELETE_VIDEO_API -> {
                    val mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        if (deleteType == "1") {
                            if (mAdapter != null) {
                                mAdapter!!.deleteVideo(videoPosition)
                            }
                        } else {
                            binding.llInterview.visibility = View.GONE
                        }
                    } else if (CommonMethods.isValidString(mCommonBean.msg)) {
                        errorMsg(mCommonBean.msg);
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong));
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("MatchDetailActivity", ex.localizedMessage, ex)
            errorMsg(getResources().getString(R.string.something_wrong));
        }
    }

    @OptIn(UnstableApi::class)
    private fun loadLocalVideos() {
        if(binding.swRefresh.isRefreshing.not()) {
            SFProgress.showProgressDialog(this@MatchDetailActivity,true)
        }
        databaseManager.executeQuery {
              val mMatchActionsDAO = MatchActionsDAO(it, this@MatchDetailActivity)
              val mActions = mMatchActionsDAO.getTotalCount(matchId)
              if (totalCount == 0) {
                  totalCount = uploadedCount + mActions
              }
              if (totalCount > 0 && mActions > 0) {
                  binding.llUploadProgress.visibility = View.VISIBLE
                  uploadedCount = totalCount - mActions
                  updateProgressCount()

              } else {
                  binding.llUploadProgress.visibility = View.GONE
              }

            val mList = mMatchActionsDAO.selectAllForPreview(matchId) as ArrayList<ReactionsBean>
            databaseManager.closeDatabase()
            val mListNew: ArrayList<VideosBean> = ArrayList()
            if (CommonMethods.isValidArrayList(mList)) {
                for (a in mList) {
                    val mBean: VideosBean = VideosBean()
                    mBean.isDelete = "0"
                    mBean.local_id = a.id
                    mBean.match_id = a.match_id
                    mBean.half = a.half
                    mBean.time = a.time
                    mBean.reaction = a.reaction
                    mBean.local_video = a.video
                    mBean.title = matchBean.team1 + " <b>Vs</b> "+matchBean.team2 + " : <b>" + a.reaction + "</b>"
                    mBean.views = 0
                    mListNew.add(mBean)
                }
                if (mAdapter != null) {
                    mAdapter!!.addAll(mListNew)
                }
            }

              val mInterviewDao = InterviewsDAO(it, this@MatchDetailActivity)
              val interviews = mInterviewDao.selectAllUploaded(matchId) as ArrayList<InterviewBean>
              if (interviews.isNotEmpty()) {
                  val interViewBean = VideosBean()
                  interViewBean.video = interviews[0].video
                  updateInterviewUI(View.VISIBLE, interViewBean)
              }

              if(binding.swRefresh.isRefreshing.not()) {
                  SFProgress.hideProgressDialog(this@MatchDetailActivity)
              }
        }
    }

    @UnstableApi
    private fun updateInterviewUI(show: Int, interview: VideosBean) {
        binding.llInterview.visibility = show
        binding.imgDelete.visibility = View.GONE
        binding.pbLoading.visibility = View.VISIBLE
        if (CommonMethods.isValidString(interview.video)) {
            if (File(interview.video).exists()) {
                binding.imgThumbnail.setImageBitmap(
                    CommonMethods.createVideoThumb(
                        this, Uri.fromFile(
                            File(interview.video)
                        )
                    )
                )
            } else {
                CommonMethods.loadImage(this@MatchDetailActivity, interview.thumbnail, binding.imgThumbnail)

                binding.imgDelete.visibility = View.VISIBLE
                binding.pbLoading.visibility = View.GONE
            }
        }

        binding.rlPlay.setOnClickListener {
            val intent = Intent(this@MatchDetailActivity, VideoPreviewActivity::class.java)
            if (File(interview.video).exists()) {
                intent.putExtra(VideoPreviewActivity.EXTRA_VIDEO_PATH, interview.video)
            } else {
                intent.putExtra(VideoPreviewActivity.EXTRA_BEAN_VIDEO, interview)
            }

            startActivity(intent)
        }
        binding.imgDelete.setOnClickListener {
            makeConfirmation(getString(R.string.msg_delete_interview), "2")
        }
    }

    private fun updateProgressCount() {
        binding.txtUploaded.text = getString(
            R.string.lbl_videos_uploaded, uploadedCount.toString(),
            totalCount.toString()
        )
    }

    fun errorMsg(strMsg: String) {
        CommonMethods.changeView(binding.llNoData, binding.mNestedScroll)
        CommonMethods.errorDialog(
            this@MatchDetailActivity,
            strMsg,
            getResources().getString(R.string.app_name),
            getResources().getString(R.string.lbl_ok)
        );
    }

    override fun onError(type: String, error: String) {
        // TODO: check Videos Progress
//        checkVideosProgress()
        errorMsg(error)
    }

    private fun getMatchActions() {
        if (CommonMethods.isNetworkAvailable(this@MatchDetailActivity)) {
            val userId =  UserSessions.getUserInfo(this@MatchDetailActivity).id.toString()
            // TODO: Test with specific user_ID
            // var userId = "72"

            mApiCall!!.getMatchDetail(
                this,
                 binding.swRefresh.isRefreshing.not(), // true,
                userId,
                gameId
            )
        } else {
            CommonMethods.errorDialog(
                this@MatchDetailActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok)
            )
        }

        videoUploadsWorker.fetchVideos(matchId = matchId)
    }

    private fun deleteVideos() {
        if (CommonMethods.isNetworkAvailable(this@MatchDetailActivity)) {
            mApiCall!!.deleteVideos(
                this,
                true,
                UserSessions.getUserInfo(this@MatchDetailActivity).id.toString(),
                gameId,
                deleteType,
                if (mVideo != null) mVideo!!.id.toString() else ""
            )
        } else {
            CommonMethods.errorDialog(
                this@MatchDetailActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok)
            )
        }
    }

    override fun onMatchClick(mBeanMatch: MatchesBean.InfoBean?) {}

    @UnstableApi
    override fun onVideoClick(mBeanVideo: VideosBean?) {
        if (mBeanVideo != null) {
            if (CommonMethods.isValidString(mBeanVideo.video)) {
                startActivity(
                    Intent(
                        this@MatchDetailActivity,
                        VideoPreviewActivity::class.java
                    ).putExtra(VideoPreviewActivity.EXTRA_BEAN_VIDEO, mBeanVideo)
                )
            }else{
                startActivity(Intent(
                    this@MatchDetailActivity,
                    VideoPreviewActivity::class.java)
                    .putExtra(VideoPreviewActivity.EXTRA_VIDEO_PATH, mBeanVideo.local_video))
            }
        }
    }

    override fun onVideoDelete(position: Int, mBeanVideo: VideosBean?) {
        mVideo = null
        videoPosition = -1
        if (mBeanVideo != null && CommonMethods.isValidString(mBeanVideo.isDelete)) {
            if (CommonMethods.isValidString(mBeanVideo.local_video)){
                makeConfirmation(getString(R.string.msg_delete_action), "44")
            } else {
                videoPosition = position
                mVideo = mBeanVideo
                makeConfirmation(getString(R.string.msg_delete_action), "1")
            }
        }
    }

    fun makeConfirmation(msg: String, type: String) {
        if (!isDialogOpen) {
            if (customDialog == null) {
                customDialog = CustomDialog(
                    this@MatchDetailActivity,
                    msg,
                    getString(R.string.lbl_cancel),
                    this,
                    type
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

    override fun onConfirm(isTrue: Boolean, type: String) {
        isDialogOpen = false
        deleteType = ""
        if (isTrue) {
            deleteType = type
            if (type == "1") {
                deleteVideos()
            } else if (type == "44") {
                //deleteVideos() //delete from local
            } else if (type == "2") {
                //delete interview
                deleteVideos()
            } else if (type == "99") {
                UserSessions.clearUserInfo(this@MatchDetailActivity)
                val intent = Intent(this@MatchDetailActivity, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finishAffinity()
            }
        } else {
            videoPosition = -1
            mVideo = null
        }
    }
    override fun onPause() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)
        } catch (e: java.lang.Exception) {
            Log.e("MatchDetailActivity", e.localizedMessage)
        }
        super.onPause()
    }
    override fun onStop() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)
        } catch (e: java.lang.Exception) {
            Log.e("MatchDetailActivity", e.localizedMessage)
        }
        super.onStop()
    }
    override fun onDestroy() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)
        } catch (e: java.lang.Exception) {
            Log.e("MatchDetailActivity", e.localizedMessage)
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        try {
            LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter!!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(mReceiver, mIntentFilter, RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(mReceiver, mIntentFilter)
            }
        } catch (e: Exception) {
            Log.e("MatchDetailActivity", e.localizedMessage)
        }
    }

}
