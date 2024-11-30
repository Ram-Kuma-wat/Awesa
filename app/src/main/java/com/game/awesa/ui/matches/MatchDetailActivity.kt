package com.game.awesa.ui.matches

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.databinding.DataBindingUtil
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
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("TooManyFunctions")
class MatchDetailActivity : BaseActivity(), OnConfirmListener, OnResponse<UniversalObject>,
    OnMatchListener {

    companion object {
        const val INTENT_UPLOAD_VIDEO = "com.game.awesa.UPLOAD_VIDEO"
        const val INTENT_ACTION_UPLOAD = "videoUpload"
        const val VIDEO_PARAMETER = "remote_video"
        const val TYPE_PARAMETER = "type"
        const val LOCAL_VIDEO_PARAMETER = "local_video"
    }

    @Inject
    lateinit var databaseManager: DatabaseManager
    @Inject lateinit var videoUploadsWorker: VideoUploadsWorker
    lateinit var binding: ActivityMatchDetailBinding
    var mApiCall: ApiCall? = null
    private var gameId = ""
    private lateinit var matchBean: MatchesBean.InfoBean
    var mAdapter: VideoAdapter? = null
    var matchId = ""
    private var totalCount = 0
    private var uploadedCount = 0
    private var mVideo: VideosBean? = null
    private var videoPosition: Int = -1
    private var customDialog: CustomDialog? = null
    private var isDialogOpen = false
    private var deleteType = ""

    private var localVideos: MutableList<VideosBean> = mutableListOf()
    private var localInterview: VideosBean? = null

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @UnstableApi
        @OptIn(UnstableApi::class)
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val type: VideoUploadsWorker.VideoType? = CommonMethods.getSerializable(intent, TYPE_PARAMETER,
                    VideoUploadsWorker.VideoType::class.java)
                when(type) {
                    VideoUploadsWorker.VideoType.reaction -> {
                            val mBean: VideosBean? = CommonMethods.getSerializable(intent, VIDEO_PARAMETER,
                                VideosBean::class.java
                            )

                            val mBeanOld: ReactionsBean? = CommonMethods.getSerializable(intent,
                                LOCAL_VIDEO_PARAMETER,
                                ReactionsBean::class.java
                            )

                            val currentList = mAdapter?.currentList?.toMutableList()

                            currentList?.replaceAll {
                                if(it.local_id == mBean?.local_id) mBean else it
                            }

                            mAdapter?.submitList(currentList)

                            if (mBeanOld != null) {
                                databaseManager.executeQuery { database ->
                                    val mMatchActionsDAO = MatchActionsDAO(database, this@MatchDetailActivity)
                                    val mActions = mMatchActionsDAO.getTotalCount(matchId)
                                    if (mActions > 0) {
                                        uploadedCount += 1
                                        binding.llUploadProgress.visibility = View.VISIBLE
                                        updateProgressCount()
                                    } else {
                                        binding.llUploadProgress.visibility = View.GONE
                                    }
                                }
                            }
                    }
                    VideoUploadsWorker.VideoType.interview -> {
                        val mInterview: VideosBean? = CommonMethods.getSerializable(intent, VIDEO_PARAMETER,
                            VideosBean::class.java
                        )

                        updateInterviewUI(View.VISIBLE, mInterview!!, false)
                    }

                    null -> {}
                }
            } catch (ex: java.lang.Exception) {
                Log.e("MatchDetailActivity", ex.localizedMessage, ex)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setupObserver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        handleIntent()
        initApiCall()

        loadVideos()

        binding.swRefresh.setOnRefreshListener {
            loadVideos()
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

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun setupObserver() {
        val intentFilter = IntentFilter(INTENT_UPLOAD_VIDEO)
        intentFilter.addAction(INTENT_ACTION_UPLOAD)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(mReceiver, intentFilter)
        }
    }

    private fun initUI() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_match_detail)
        mAdapter = VideoAdapter(this)
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
                            mBeanMatch.info[0].team1_image, binding.imgTeam1
                        )
                        CommonMethods.loadImage(
                            this@MatchDetailActivity,
                            mBeanMatch.info[0].team2_image, binding.imgTeam2
                        )

                        CommonMethods.changeView(binding.mNestedScroll, binding.llNoData)
                        uploadedCount = mBeanMatch.info[0].videos.size
                        updateProgressCount()
                        val updatedList = localVideos
                        updatedList.addAll(0, mBeanMatch.info[0].videos)
                        mAdapter?.submitList(updatedList, Runnable {
                            if(!mBeanMatch.info[0].interview.isNullOrEmpty()) {
                                localInterview = VideosBean()
                                localInterview?.thumbnail = mBeanMatch.info[0].interview_thumbnail
                                localInterview?.video = mBeanMatch.info[0].interview
                                updateInterviewUI(View.VISIBLE, localInterview!!, false)
                            }
                        })
                    } else if (mBeanMatch.status == 99) {
                        UserSessions.clearUserInfo(this@MatchDetailActivity)
                        Global().makeConfirmation(mBeanMatch.msg, this@MatchDetailActivity, this)
                    } else if (CommonMethods.isValidString(mBeanMatch.msg)) {
                        errorMsg(mBeanMatch.msg);
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong));
                    }

                    if (totalCount == 0 && uploadedCount == 0 && localInterview == null ) {
                        CommonMethods.changeView(binding.llNoData, binding.mNestedScroll)
                    } else {
                        CommonMethods.changeView(binding.mNestedScroll, binding.llNoData)
                    }
                }

                Tags.SB_DELETE_VIDEO_API -> {
                    val mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        if (deleteType == "1") {
                            val currentList = mAdapter?.currentList?.toMutableList()
                            currentList?.removeIf { it.local_id == mCommonBean.localId }
                            mAdapter?.submitList(currentList)
                        } else {
                            binding.llInterview.visibility = View.GONE
                        }
                    } else if (CommonMethods.isValidString(mCommonBean.msg)) {
                        errorMsg(mCommonBean.msg)
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
    private fun loadVideos() {
        if(binding.swRefresh.isRefreshing.not()) {
            SFProgress.showProgressDialog(this@MatchDetailActivity,true)
        }
        databaseManager.executeQuery { database ->
              val mMatchActionsDAO = MatchActionsDAO(database, this@MatchDetailActivity)
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

            val mData = mMatchActionsDAO.selectAllForPreview(matchId) as ArrayList<ReactionsBean>
            val mList = mutableListOf<VideosBean>()
            if (CommonMethods.isValidArrayList(mData)) {
                for (a in mData) {
                    val mBean = VideosBean()
                    mBean.isDelete = "0"
                    mBean.local_id = a.id
                    mBean.match_id = a.match_id
                    mBean.half = a.half
                    mBean.time = a.time
                    mBean.reaction = a.reaction
                    mBean.local_video = a.video
                    mBean.title = matchBean.team1+" <b>Vs</b> "+matchBean.team2+" : <b>"+a.reaction+"</b>"
                    mBean.views = 0
                    mList.add(mBean)
                }
            }

            localVideos = mList

            getMatchActions()

            val mInterviewDao = InterviewsDAO(database, this@MatchDetailActivity)
            val interviews = mInterviewDao.selectAllUploaded(matchId) as ArrayList<InterviewBean>
            if (interviews.isNotEmpty()) {
                localInterview = VideosBean()
                localInterview?.video = interviews[0].video
                updateInterviewUI(View.VISIBLE, localInterview!!)
            }

            if(binding.swRefresh.isRefreshing.not()) {
                SFProgress.hideProgressDialog(this@MatchDetailActivity)
            }
        }
    }

    @UnstableApi
    private fun updateInterviewUI(show: Int, interview: VideosBean, isLoading: Boolean = true) {
        binding.llInterview.visibility = show

        if (isLoading) {
            binding.imgDelete.visibility = View.GONE
            binding.pbLoading.visibility = View.VISIBLE
        } else {
            binding.imgDelete.visibility = View.VISIBLE
            binding.pbLoading.visibility = View.GONE
        }

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
        )
    }

    override fun onError(type: String, error: String) {
        errorMsg(error)
    }

    private fun getMatchActions() {
        if (CommonMethods.isNetworkAvailable(this@MatchDetailActivity)) {
            val userId =  UserSessions.getUserInfo(this@MatchDetailActivity).id.toString()

            mApiCall?.getMatchDetail(
                this,
                 binding.swRefresh.isRefreshing.not(),
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

    @SuppressLint("EmptyFunctionBlock")
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

    override fun onStop() {
        super.onStop()
        unregisterReceiver(mReceiver)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        setupObserver()
    }

}
