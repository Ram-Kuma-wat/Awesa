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
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseManager
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
import javax.inject.Inject

@AndroidEntryPoint
class MatchDetailActivity : BaseActivity(), OnConfirmListener, OnResponse<UniversalObject>,
    OnMatchListener {
    @Inject
    lateinit var databaseManager: DatabaseManager
    @Inject lateinit var videoUploadsWorker: VideoUploadsWorker
    lateinit var binding: ActivityMatchDetailBinding
    var mApiCall: ApiCall? = null
    var game_id = ""
    lateinit var matchBean: MatchesBean.InfoBean
    var mAdapter: VideosAdapter? = null
    var match_id = ""
    var match_title=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_match_detail)
        initApiCall()
        val intentFilter = IntentFilter()
        mIntentFilter = intentFilter
        intentFilter.addAction("videoUpload")
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,mIntentFilter!!)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.imgHome.setOnClickListener {
            CommonMethods.moveWithClear(
                this@MatchDetailActivity,
                MainActivity::class.java
            )
        }
        binding.rvHistory.layoutManager =
            LinearLayoutManager(this@MatchDetailActivity, RecyclerView.VERTICAL, false)
        if (intent.hasExtra("game_id")) {
            game_id = intent.getStringExtra("game_id") as String
        }
        if (intent.hasExtra("matchBean")) {
            matchBean =
                CommonMethods.getSerializable(intent, "matchBean", MatchesBean.InfoBean::class.java)
            match_id = if (matchBean != null) matchBean.id.toString() else ""
            match_title = if (matchBean != null) matchBean.team1+" <b>Vs</b> "+matchBean.team2 else ""
            totalCount = matchBean.total_actions
        }
        getMatches()
        binding.swRefresh.setOnRefreshListener {
            getMatches()
        }
    }

    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@MatchDetailActivity)
        }
    }
    var totalCount = 0;
    private var uploadedCount = 0;
    override fun onSuccess(response: UniversalObject) {
        binding.swRefresh.isRefreshing = false
        try {
            Logs.d(response.methodName)
            when (response.methodName) {
                Tags.SB_MATCH_DETAIL_API -> {
                    val mBeanMatch: MatchesBean = response.response as MatchesBean
                    if (mBeanMatch.status == 1 && CommonMethods.isValidArrayList(mBeanMatch.info)) {
                        CommonMethods.loadImage(this@MatchDetailActivity,mBeanMatch.info[0].team1_image,binding.imgTeam1)
                        CommonMethods.loadImage(this@MatchDetailActivity,mBeanMatch.info[0].team2_image,binding.imgTeam2)
                        if (CommonMethods.isValidString(mBeanMatch.info[0].interview)) {
                            binding.llInterview.visibility = View.VISIBLE
                            CommonMethods.loadImage(this@MatchDetailActivity,mBeanMatch.info[0].interview_thumbnail,binding.imgThumbnail)
                            binding.rlPlay.setOnClickListener {
                                val mBeanVideo: MatchesBean.VideosBean = MatchesBean.VideosBean()
                                mBeanVideo.video = mBeanMatch.info[0].interview
                                startActivity(Intent(this@MatchDetailActivity,VideoPreviewActivity::class.java).putExtra("mBeanVideo", mBeanVideo))
                            }
                            binding.imgDelete.setOnClickListener {
                                makeConfirmation(getString(R.string.msg_delete_interview), "2")
                            }
                        } else {
                            binding.llInterview.visibility = View.GONE
                        }
                        if (CommonMethods.isValidArrayList((mBeanMatch.info[0].videos)) || CommonMethods.isValidString(mBeanMatch.info[0].interview)) {
                            try {
                                match_title = mBeanMatch.info[0].videos[0].title
                            } catch (_:Exception){}
                            mAdapter = VideosAdapter(this@MatchDetailActivity,mBeanMatch.info[0].videos,this@MatchDetailActivity);
                            binding.rvHistory.adapter = mAdapter
                            CommonMethods.changeView(binding.mNestedScroll, binding.llNoData)
                            uploadedCount = mBeanMatch.info[0].videos.size
                         }
                    } else if (mBeanMatch.status == 99) {
                        UserSessions.clearUserInfo(this@MatchDetailActivity)
                        Global().makeConfirmation(mBeanMatch.msg, this@MatchDetailActivity, this)
                    } else if (CommonMethods.isValidString(mBeanMatch.msg)) {
                        errorMsg(mBeanMatch.msg);
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong));
                    }
                    if (mAdapter==null) {
                        uploadedCount = 0;
                        mAdapter = VideosAdapter(this@MatchDetailActivity,ArrayList(),this@MatchDetailActivity);
                    }
                    binding.rvHistory.adapter = mAdapter
                    checkVideosProgress()
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
                            if (matchBean != null) {
                                binding.llInterview.visibility = View.GONE
                            }
                        }
                    } else if (CommonMethods.isValidString(mCommonBean.msg)) {
                        errorMsg(mCommonBean.msg);
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong));
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("MatchDetailActivity", ex.localizedMessage)
            errorMsg(getResources().getString(R.string.something_wrong));
        }
        //method body
    }

    private fun checkVideosProgress( ) {
        SFProgress.showProgressDialog(this@MatchDetailActivity,true)
          databaseManager.executeQuery {
              val mMatchActionsDAO = MatchActionsDAO(it, this@MatchDetailActivity)
              val mActions = mMatchActionsDAO.getTotalCount(match_id);
              if (totalCount == 0) {
                  totalCount = uploadedCount + mActions;
              }
              if (totalCount > 0 && mActions > 0) {
                  binding.llUploadProgress.visibility = View.VISIBLE
                  binding.txtUploaded.text = getString(
                      R.string.lbl_videos_uploaded, uploadedCount.toString(),
                      totalCount.toString()
                  )
                  val mList = mMatchActionsDAO.selectAllForPreview(match_id) as ArrayList<ReactionsBean>
                  databaseManager.closeDatabase()
                  val mListNew: ArrayList<MatchesBean.VideosBean> = ArrayList()
                  if (CommonMethods.isValidArrayList(mList)) {
                      for (a in mList) {
                          val mBean: MatchesBean.VideosBean = MatchesBean.VideosBean()
                          mBean.isDelete = "0"
                          mBean.local_id = a.id
                          mBean.match_id = a.match_id
                          mBean.half = a.half
                          mBean.time = a.time
                          mBean.reaction = a.reaction
                          mBean.local_video = a.video
                          mBean.title = match_title + " : <b>" + a.reaction + "</b>"
                          mBean.views = 0
                          mListNew.add(mBean)
                      }
                      if (mAdapter != null && CommonMethods.isValidArrayList(mListNew)) {
                          mAdapter!!.addOne(mListNew)
                      }
                  }
              } else {
                  binding.llUploadProgress.visibility = View.GONE
              }
              SFProgress.hideProgressDialog(this@MatchDetailActivity)
          }
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
        checkVideosProgress()
        errorMsg(error)
        //method body
    }

    private fun getMatches() {
        if (CommonMethods.isNetworkAvailable(this@MatchDetailActivity)) {

            val userId =  UserSessions.getUserInfo(this@MatchDetailActivity).id.toString();
//            var userId = "72" // TODO: Test with specific user_ID

            mApiCall!!.getMatchDetail(
                this,
                true,
                userId,
                game_id
            )
        } else {
            CommonMethods.errorDialog(
                this@MatchDetailActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok)
            );
        }

        videoUploadsWorker.fetchVideos(matchId = match_id)
    }

    private fun deleteVideos() {
        if (CommonMethods.isNetworkAvailable(this@MatchDetailActivity)) {
            mApiCall!!.deleteVideos(
                this,
                true,
                UserSessions.getUserInfo(this@MatchDetailActivity).id.toString(),
                game_id,
                deleteType,
                if (mVideo != null) mVideo!!.id.toString() else ""
            )
        } else {
            CommonMethods.errorDialog(
                this@MatchDetailActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok)
            );
        }
    }

    override fun onMatchClick(mBeanMatch: MatchesBean.InfoBean?) {
        //
    }

    private var mVideo: MatchesBean.VideosBean? = null
    private var videoPosition: Int = -1

    override fun onVideoClick(mBeanVideo: MatchesBean.VideosBean?) {
        if (mBeanVideo != null) {
            if (CommonMethods.isValidString(mBeanVideo.video)) {
                startActivity(
                    Intent(
                        this@MatchDetailActivity,
                        VideoPreviewActivity::class.java
                    ).putExtra("mBeanVideo", mBeanVideo)
                )
            }else{
                startActivity(Intent(
                    this@MatchDetailActivity,
                    VideoPreviewActivity::class.java)
                    .putExtra("strPath", mBeanVideo.local_video))
            }
        }
    }

    override fun onVideoDelete(position: Int, mBeanVideo: MatchesBean.VideosBean?) {
        mVideo = null;
        videoPosition = -1
        if (mBeanVideo != null && CommonMethods.isValidString(mBeanVideo.isDelete)) {
            if (CommonMethods.isValidString(mBeanVideo.local_video)){
                makeConfirmation(getString(R.string.msg_delete_action), "44")
            }else {
                videoPosition = position
                mVideo = mBeanVideo
                makeConfirmation(getString(R.string.msg_delete_action), "1")
            }
            //delete video
        }
    }

    private var customDialog: CustomDialog? = null
    private var isDialogOpen = false
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

    private var deleteType = ""
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
                startActivity(
                    Intent(this@MatchDetailActivity, LoginActivity::class.java).setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
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
        } catch (e: java.lang.Exception) {
            Log.e("MatchDetailActivity", e.localizedMessage)
        }
    }
    private var mIntentFilter: IntentFilter? = null
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (intent.hasExtra("Data")){
                    val mBn:MatchesBean.VideosBean = Gson().fromJson(
                        intent.getStringExtra("Data"),
                        MatchesBean.VideosBean::class.java)
                    if (intent.hasExtra("old_data")){
                        val mBn1:ReactionsBean? = Gson().fromJson(
                            intent.getStringExtra("old_data"),ReactionsBean::class.java)
                        if (mBn1 != null) {
                            if (mAdapter !=null){
                                mAdapter!!.updateVideo(mBn1,mBn)
                            }
                            databaseManager.executeQuery {
                                val mMatchActionsDAO = MatchActionsDAO(it, this@MatchDetailActivity)
                                val mActions = mMatchActionsDAO.getTotalCount(match_id);
                                if (mActions > 0) {
                                    binding.llUploadProgress.visibility = View.VISIBLE
                                    binding.txtUploaded.text = getString(
                                        R.string.lbl_videos_uploaded,
                                        (totalCount - mActions).toString(),
                                        totalCount.toString()
                                    )
                                    databaseManager.closeDatabase()
                                } else {
                                    binding.llUploadProgress.visibility = View.GONE
                                }
                            }
                        }
                    }
                 }
            } catch (e: java.lang.Exception) {
                Log.e("MatchDetailActivity", e.localizedMessage)
            }
        }
    }

}
