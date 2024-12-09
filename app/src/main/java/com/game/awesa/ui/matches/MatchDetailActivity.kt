package com.game.awesa.ui.matches

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.codersworld.awesalibs.listeners.OnDeleteVideoListener
import com.codersworld.awesalibs.listeners.OnMatchListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
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
import javax.inject.Inject

@AndroidEntryPoint
@Suppress("TooManyFunctions")
class MatchDetailActivity : BaseActivity(), OnConfirmListener, OnResponse<UniversalObject>,
    OnMatchListener, OnDeleteVideoListener {

    companion object {
        val TAG = MatchDetailActivity::class.java.simpleName
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

                        val currentList = mAdapter?.currentList?.toMutableList()
                        currentList?.removeIf { intArrayOf(-1, -2, -3).contains(it.local_id) }

                        currentList?.replaceAll {
                            if(it.local_id == mBean?.local_id) mBean else it
                        }

                        mAdapter?.submitList(formatList(currentList ?: emptyList()))

                        uploadedCount += 1
                        updateProgressCount()

                    }
                    VideoUploadsWorker.VideoType.interview -> {
                        val mInterview: VideosBean? = CommonMethods.getSerializable(intent, VIDEO_PARAMETER,
                            VideosBean::class.java
                        )

                        mInterview?.half = 3
                        mInterview?.local_id = Int.MAX_VALUE

                        val currentList = mAdapter?.currentList?.toMutableList()
                        currentList?.removeIf { intArrayOf(-1, -2, -3).contains(it.local_id) }

                        currentList?.replaceAll {
                            if(it.local_id == Int.MAX_VALUE) mInterview else it
                        }

                        mAdapter?.submitList(formatList(currentList ?: emptyList()))

                        uploadedCount += 1
                        updateProgressCount()
                    }

                    null -> {}
                }
            } catch (ex: TypeCastException) {
                Log.e(TAG, ex.localizedMessage, ex)
            } catch (ex: java.lang.Exception) {
                Log.e(TAG, ex.localizedMessage, ex)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setupObserver()
        videoUploadsWorker.fetchVideos(matchId = matchId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        handleIntent()
        initApiCall()

        loadVideos()
    }

    private fun handleIntent() {
        if (intent.hasExtra("game_id")) {
            gameId = intent.getStringExtra("game_id") as String
        }
        if (intent.hasExtra("matchBean")) {
            matchBean =
                CommonMethods.getSerializable(intent, "matchBean", MatchesBean.InfoBean::class.java)
            matchId = matchBean.id.toString()
            totalCount = matchBean.total_actions + (if (matchBean.interview.isNullOrEmpty()) 0 else 1)
        }
    }

    @Suppress("UnspecifiedRegisterReceiverFlag")
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
        mAdapter = VideoAdapter( onActonActionMatchListener = this, onDeleteVideoListener = this)
        binding.rvHistory.adapter = mAdapter

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.imgHome.setOnClickListener {
            CommonMethods.moveWithClear(
                this@MatchDetailActivity,
                MainActivity::class.java
            )
        }

        binding.swRefresh.setOnRefreshListener {
            loadVideos(isRefreshing = true)
        }
    }

    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@MatchDetailActivity)
        }
    }

    @UnstableApi
    @Suppress("LongMethod", "NestedBlockDepth", "CyclomaticComplexMethod", "MagicNumber")
    override fun onSuccess(response: UniversalObject) {
        binding.swRefresh.isRefreshing = false

        try {
            when (response.methodName) {
                Tags.SB_MATCH_DETAIL_API -> {
                    val mBeanMatch: MatchesBean = response.response as MatchesBean
                    if (mBeanMatch.status == 1 && mBeanMatch.info.isNotEmpty()) {

                        totalCount = mBeanMatch.info[0].total_actions
                        uploadedCount = mBeanMatch.info[0].videos.size

                        CommonMethods.loadImage(
                            this@MatchDetailActivity,
                            mBeanMatch.info[0].team1_image, binding.imgTeam1
                        )
                        CommonMethods.loadImage(
                            this@MatchDetailActivity,
                            mBeanMatch.info[0].team2_image, binding.imgTeam2
                        )

                        val currentList = mAdapter?.currentList?.toMutableList()
                        currentList?.removeIf { intArrayOf(-1, -2, -3).contains(it.local_id) }
                        currentList?.addAll(0, mBeanMatch.info[0].videos)

                        if (!mBeanMatch.info[0].interview.isNullOrEmpty()) {
                            // Add uploaded interview to uploaded count
                            uploadedCount += 1
                            val interview = VideosBean()
                            interview.id = -3
                            interview.local_id = Int.MAX_VALUE
                            interview.half = 3
                            interview.match_id = matchId.toInt()
                            interview.thumbnail = mBeanMatch.info[0].interview_thumbnail
                            interview.video = mBeanMatch.info[0].interview
                            interview.local_video = null
                            interview.views = 0

                            if (currentList?.any { it.local_id == Int.MAX_VALUE } == true) {
                                currentList.replaceAll {
                                    if(it.local_id == Int.MAX_VALUE) interview else it
                                }
                            } else {
                                currentList?.add(interview)
                            }
                        }

                        mAdapter?.submitList(formatList(currentList ?: emptyList()))

                        CommonMethods.changeView(binding.mNestedScroll, binding.llNoData)

                        updateProgressCount()
                    } else if (mBeanMatch.status == 99) {
                        UserSessions.clearUserInfo(this@MatchDetailActivity)
                        Global().makeConfirmation(mBeanMatch.msg, this@MatchDetailActivity, this)
                    } else if (CommonMethods.isValidString(mBeanMatch.msg)) {
                        errorMsg(mBeanMatch.msg)
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong))
                    }

                    if (totalCount == 0 && uploadedCount == 0 && mBeanMatch.info[0].interview.isNullOrEmpty() ) {
                        CommonMethods.changeView(binding.llNoData, binding.mNestedScroll)
                    } else {
                        CommonMethods.changeView(binding.mNestedScroll, binding.llNoData)
                    }
                }

                Tags.SB_DELETE_VIDEO_API -> {
                    val mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        if (mCommonBean.localId == -1) {
                            mCommonBean.setLocalId(Int.MAX_VALUE.toString())
                        }

                        val currentList = mAdapter?.currentList?.toMutableList()
                        currentList?.removeIf { intArrayOf(-1, -2, -3).contains(it.local_id) }
                        currentList?.removeIf { it.local_id == mCommonBean.localId }

                        mAdapter?.submitList(formatList(currentList ?: emptyList()))
                    } else if (CommonMethods.isValidString(mCommonBean.msg)) {
                        errorMsg(mCommonBean.msg)
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong))
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage, ex)
            errorMsg(getResources().getString(R.string.something_wrong))
        }
    }

    @OptIn(UnstableApi::class)
    @Suppress("MagicNumber")
    private fun loadVideos(isRefreshing: Boolean = false) {
        if(binding.swRefresh.isRefreshing.not()) {
            SFProgress.showProgressDialog(this@MatchDetailActivity,true)
        }

        mAdapter?.submitList(null)

        databaseManager.executeQuery { database ->
            val mMatchActionsDAO = MatchActionsDAO(database, this@MatchDetailActivity)
            val mActions = mMatchActionsDAO.getTotalCount(matchId)
            // Already uploaded + remaining action count
            totalCount = uploadedCount + mActions
            // Total count - remaining actions
            uploadedCount = totalCount - mActions

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

            val mInterviewDao = InterviewsDAO(database, this@MatchDetailActivity)
            val interviews = mInterviewDao.selectAllUploaded(matchId) as ArrayList<InterviewBean>

            if (interviews.isNotEmpty()) {
                val interview = VideosBean()
                interview.local_id = Int.MAX_VALUE
                interview.half = 3
                interview.match_id = matchId.toInt()
                interview.local_video = interviews[0].video
                mList.add(interview)
            }

            mAdapter?.submitList(formatList(mList))

            updateProgressCount()
            getMatchActions(isRefreshing)

            if(binding.swRefresh.isRefreshing.not()) {
                SFProgress.hideProgressDialog(this@MatchDetailActivity)
            }
        }
    }

    private fun updateProgressCount() {
        binding.llUploadProgress.visibility = if(uploadedCount == totalCount) View.GONE else View.VISIBLE
        binding.txtUploaded.text = getString(
            R.string.lbl_videos_uploaded, uploadedCount.toString(),
            totalCount.toString()
        )
    }

    @Suppress("MagicNumber")
    fun formatList(list: List<VideosBean>) : List<VideosBean> {
        val updatedList = mutableListOf<VideosBean>()
        val groups = list.groupBy { it.half }

        groups.forEach { (header, list) ->
            val headerItem = VideosBean()
            headerItem.match_id = matchId.toInt()
            when(header) {
                1 -> headerItem.local_id = -1
                2 -> headerItem.local_id = -2
                3 -> headerItem.local_id = -3
            }

            when (header) {
                1 -> {
                    headerItem.title = getString(R.string.lbl_first_half)
                }
                2 -> {
                    headerItem.title = getString(R.string.lbl_second_half)
                }
                3 -> {
                    headerItem.title = getString(R.string.lbl_interview)
                }
            }
            updatedList.add(
                headerItem
            )

            updatedList.addAll(list)
        }

        return updatedList.toList()
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

    private fun getMatchActions(isRefreshing: Boolean = false) {
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

    @Suppress("EmptyFunctionBlock")
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

    override fun onInterviewDelete(position: Int, mBeanVideo: VideosBean?) {
        mVideo = null
        videoPosition = -1

        if (mBeanVideo != null && CommonMethods.isValidString(mBeanVideo.isDelete)) {
            videoPosition = position
            mVideo = mBeanVideo
            makeConfirmation(getString(R.string.msg_delete_action), "2")
        }

    }

    override fun onActionDelete(position: Int, mBeanVideo: VideosBean?) {
        mVideo = null
        videoPosition = -1
        if (mBeanVideo != null && CommonMethods.isValidString(mBeanVideo.isDelete)) {
            if (CommonMethods.isValidString(mBeanVideo.local_video)) {
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
            when (type) {
                "1" -> {
                    deleteVideos()
                }
                "44" -> {
                    //deleteVideos() //delete from local
                }
                "2" -> {
                    //delete interview
                    deleteVideos()
                }
                "99" -> {
                    UserSessions.clearUserInfo(this@MatchDetailActivity)
                    val intent = Intent(this@MatchDetailActivity, LoginActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finishAffinity()
                }
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

    @Suppress("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        setupObserver()
    }

}
