package com.game.awesa.ui.matches

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.leagues.LeagueBean
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnLeagueListener
import com.codersworld.awesalibs.listeners.OnMatchListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.UniverSelObjct
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Logs
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.R
import com.game.awesa.databinding.ActivityLeagueBinding
import com.game.awesa.databinding.ActivityMatchDetailBinding
import com.game.awesa.ui.BaseActivity
import com.game.awesa.ui.dashboard.MainActivity
import com.game.awesa.ui.dialogs.CustomDialog
import com.game.awesa.ui.league.adapter.LeagueAdapter
import com.game.awesa.ui.recorder.VideoPreviewActivity
import com.game.awesa.ui.teams.OpponentTeamsActivity
import java.io.File


class MatchDetailActivity : BaseActivity(), OnConfirmListener, OnResponse<UniverSelObjct>,
    OnMatchListener {
    lateinit var binding: ActivityMatchDetailBinding
    var mApiCall: ApiCall? = null
    var game_id = ""
    lateinit var matchBean: MatchesBean.InfoBean
    var mAdapter: VideosAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_match_detail)
        initApiCall()
        DatabaseManager.initializeInstance(DatabaseHelper(this@MatchDetailActivity))
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

    override fun onSuccess(response: UniverSelObjct) {
        binding.swRefresh.isRefreshing = false
        try {
            Logs.e(response.methodname.toString())
            when (response.methodname) {
                Tags.SB_MATCH_DETAIL_API -> {
                    var mBeanMatch: MatchesBean = response.response as MatchesBean
                    if (mBeanMatch.status == 1 && CommonMethods.isValidArrayList(mBeanMatch.info)) {
                        CommonMethods.loadImage(
                            this@MatchDetailActivity,
                            mBeanMatch.info[0].team1_image,
                            binding.imgTeam1
                        )
                        CommonMethods.loadImage(
                            this@MatchDetailActivity,
                            mBeanMatch.info[0].team2_image,
                            binding.imgTeam2
                        )
                        if (CommonMethods.isValidString(mBeanMatch.info[0].interview)) {
                            binding.llInterview.visibility = View.VISIBLE
                            CommonMethods.loadImage(
                                this@MatchDetailActivity,
                                mBeanMatch.info[0].interview_thumbnail,
                                binding.imgThumbnail
                            )
                            binding.rlPlay.setOnClickListener {
                                var mBeanVideo: MatchesBean.VideosBean = MatchesBean.VideosBean()
                                mBeanVideo.video = mBeanMatch.info[0].interview
                                startActivity(
                                    Intent(
                                        this@MatchDetailActivity,
                                        VideoPreviewActivity::class.java
                                    ).putExtra("mBeanVideo", mBeanVideo)
                                )
                            }
                            binding.imgDelete.setOnClickListener {
                                makeConfirmation(getString(R.string.msg_delete_interview), "2")
                            }
                        } else {
                            binding.llInterview.visibility = View.GONE
                        }
                        if (CommonMethods.isValidArrayList((mBeanMatch.info[0].videos)) || CommonMethods.isValidString(
                                mBeanMatch.info[0].interview
                            )
                        ) {
                            mAdapter = VideosAdapter(
                                this@MatchDetailActivity,
                                mBeanMatch.info[0].videos,
                                this@MatchDetailActivity
                            );
                            binding.rvHistory.adapter = mAdapter
                            CommonMethods.changeView(binding.mNestedScroll, binding.llNoData)
                        } else {
                            CommonMethods.changeView(binding.llNoData, binding.mNestedScroll)
                        }
                    } else if (CommonMethods.isValidString(mBeanMatch.msg)) {
                        errorMsg(mBeanMatch.msg);
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong));
                    }
                }

                Tags.SB_DELETE_VIDEO_API -> {
                    var mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        if (deleteType.equals("1")) {
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
            ex.printStackTrace()
            errorMsg(getResources().getString(R.string.something_wrong));
        }
        //method body
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
        errorMsg(error)
        //method body
    }

    fun getMatches() {
        if (CommonMethods.isNetworkAvailable(this@MatchDetailActivity)) {
            mApiCall!!.getMatchDetail(
                this,
                true,
                UserSessions.getUserInfo(this@MatchDetailActivity).id.toString(),
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
    }
    fun deleteVideos() {
        if (CommonMethods.isNetworkAvailable(this@MatchDetailActivity)) {
            mApiCall!!.deleteVideos(
                this,
                true,
                UserSessions.getUserInfo(this@MatchDetailActivity).id.toString(),
                game_id,
                deleteType,
               if (mVideo !=null) mVideo!!.id.toString() else ""
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

    var mVideo: MatchesBean.VideosBean? = null
    var videoPosition: Int = -1

    override fun onVideoClick(mBeanVideo: MatchesBean.VideosBean?) {
        if (mBeanVideo != null && CommonMethods.isValidString(mBeanVideo!!.video)) {
            startActivity(
                Intent(
                    this@MatchDetailActivity,
                    VideoPreviewActivity::class.java
                ).putExtra("mBeanVideo", mBeanVideo)
            )
        }
    }

    override fun onVideoDelete(position: Int, mBeanVideo: MatchesBean.VideosBean?) {
        mVideo = null;
        videoPosition = -1
        if (mBeanVideo != null && CommonMethods.isValidString(mBeanVideo!!.isDelete)) {
            videoPosition = position
            mVideo = mBeanVideo
            //delete video
            makeConfirmation(getString(R.string.msg_delete_action), "1")
        }
    }

    var customDialog: CustomDialog? = null
    var isDialogOpen = false
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
                customDialog!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            isDialogOpen = true
            if (customDialog!! != null && customDialog!!.isShowing()) {
                customDialog!!.dismiss()
            }
            customDialog!!.show()
        }
    }

    var deleteType = ""
    override fun onConfirm(isTrue: Boolean, type: String) {
        isDialogOpen = false
        deleteType = ""
        if (isTrue) {
            deleteType = type
            if (type.equals("1")) {
                deleteVideos()
            } else if (type.equals("2")) {
                //delete interview
                if (matchBean != null) {
                    deleteVideos()
                }
            }
        } else {
            videoPosition = -1
            mVideo = null
        }
    }

}