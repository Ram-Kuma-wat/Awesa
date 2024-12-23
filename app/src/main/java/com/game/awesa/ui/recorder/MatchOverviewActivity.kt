package com.game.awesa.ui.recorder

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.matches.InterviewBean
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnReactionListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Logs
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.R
import com.game.awesa.databinding.ActivityMatchOverviewBinding
import com.game.awesa.services.TrimService
import com.game.awesa.ui.dashboard.MainActivity
import com.game.awesa.ui.dialogs.CustomDialog
import com.game.awesa.ui.dialogs.EditActionDialog
import com.game.awesa.utils.VideoUploadsWorker
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
@Suppress("TooManyFunctions")
class MatchOverviewActivity : AppCompatActivity(),OnReactionListener, OnConfirmListener,
    OnResponse<UniversalObject> {

    companion object {
        const val EXTRA_MATCH_BEAN = "mMatchBean"
        val TAG: String = MatchOverviewActivity::class.java.simpleName
    }

    @Inject
    lateinit var videoUploadsWorker: VideoUploadsWorker

    @Inject lateinit var databaseManager: DatabaseManager

    lateinit var binding:ActivityMatchOverviewBinding
    private var mMatchBean : MatchesBean.InfoBean? = null
    private var matchId: String? = null
    private var mAdapter: OverviewAdapter?=null
    private var mListData: ArrayList<ReactionsBean> = ArrayList()
    private var mBeanReaction: ReactionsBean? = null
    private var actionPosition =-1

    private var strInterview = ""
    private var strInterviewId = 0
    private var customDialog: CustomDialog? = null
    private var isDialogOpen = false
    var mApiCall: ApiCall? = null

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        handleIntent()
        initApiCall()

        getData()
    }

    @OptIn(UnstableApi::class)
    private fun initUI() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_match_overview)
        binding.rvHistory.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL,false)
        mAdapter = OverviewAdapter(this@MatchOverviewActivity, mListData, this)
        binding.rvHistory.adapter = mAdapter
        binding.imgHome.setOnClickListener {
            updateMatchCount()
        }

        binding.swRefresh.setOnRefreshListener {
            getData()
        }

        binding.rlPlay.setOnClickListener {
            if (CommonMethods.isValidString(strInterview)){
                val intent = Intent(this@MatchOverviewActivity, VideoPreviewActivity::class.java)
                intent.putExtra(VideoPreviewActivity.EXTRA_VIDEO_PATH, strInterview)
                startActivity(intent)
            }
        }
        binding.imgThumbnail.setOnClickListener {
            if (CommonMethods.isValidString(strInterview)){
                val intent = Intent(this@MatchOverviewActivity,VideoPreviewActivity::class.java)
                intent.putExtra(VideoPreviewActivity.EXTRA_VIDEO_PATH, strInterview)
                startActivity(intent)
            }
        }
        binding.imgPlay.setOnClickListener {
            if (CommonMethods.isValidString(strInterview)){
                val intent = Intent(this@MatchOverviewActivity,VideoPreviewActivity::class.java)
                intent.putExtra(VideoPreviewActivity.EXTRA_VIDEO_PATH, strInterview)
                startActivity(intent)
            }
        }
        binding.imgDelete.setOnClickListener {
            if (CommonMethods.isValidString(strInterview)){
                makeConfirmation(getString(R.string.msg_delete_interview),"2")
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun handleIntent() {
        mMatchBean = CommonMethods.getSerializable(
            intent,
            EXTRA_MATCH_BEAN,
            MatchesBean.InfoBean::class.java
        )

        if (mMatchBean != null) {
            matchId = mMatchBean!!.id.toString()
            CommonMethods.loadImage(this@MatchOverviewActivity, mMatchBean!!.team1_image,binding.imgTeam1)
            CommonMethods.loadImage(this@MatchOverviewActivity, mMatchBean!!.team2_image,binding.imgTeam2)
        }
    }

    private fun getData() {
        binding.swRefresh.isRefreshing = true
        databaseManager.executeQuery { database ->
            val mMatchActionsDAO = MatchActionsDAO(database, this@MatchOverviewActivity)
            val actionsList =
                mMatchActionsDAO.selectAllForPreview(mMatchBean!!.id.toString()) as ArrayList<ReactionsBean>
            mListData = actionsList
            mAdapter?.addAll(mListData)
//            checkCompression()

            val mInterviewsDAO = InterviewsDAO(database, this@MatchOverviewActivity)
            val interviews =
                    mInterviewsDAO.selectAll(mMatchBean!!.id.toString()) as ArrayList<InterviewBean>

            if (interviews.isNotEmpty()) {
                binding.imgThumbnail.setImageBitmap(
                    CommonMethods.createVideoThumb(
                        this@MatchOverviewActivity,
                        Uri.fromFile(File(interviews[0].video))
                    )
                )
                strInterview = interviews[0].video
                strInterviewId = interviews[0].id
                binding.llInterview.visibility = View.VISIBLE
            } else {
                binding.llInterview.visibility = View.GONE
            }

            binding.swRefresh.isRefreshing = false
        }
    }

    private fun checkCompression() {
        if (CommonMethods.isValidArrayList(mListData)) {
            var counter = 0
            for(a in mListData.indices) {
                if (!CommonMethods.isValidString(mListData[a].video)){
                    counter++
                }else{
                    val file: File = File(mListData[a].video)
                    val fileSize = (((file.length() / 1024).toString().toInt()) / 1024).toString().toInt()
                    if (fileSize < 4) {
                        counter++
                        databaseManager.executeQuery {
                            val mMatchActionsDAO = MatchActionsDAO(it, this@MatchOverviewActivity)
                            mMatchActionsDAO.updateVideo("", "", mListData[a].id)
                        }
                    }
                }
            }
            if (counter > 0){
                val handler = Handler()
                handler.postDelayed({}, 3000)
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun OnReactionAction(mReactionsBean: ReactionsBean?, type: Int, position:Int) {
        if (type < 99) {
            actionPosition = position
            mBeanReaction = mReactionsBean
        }
        if (type == 1) {
            if (mReactionsBean !=null && CommonMethods.isValidString(mReactionsBean.video)) {
                val intent = Intent(this@MatchOverviewActivity, VideoPreviewActivity::class.java)
                intent.putExtra(VideoPreviewActivity.EXTRA_VIDEO_PATH, mReactionsBean.video)
                startActivity(intent)
            }
        } else if (type == 99) {
            if (mReactionsBean !=null && CommonMethods.isValidString(mReactionsBean.video)) {
                databaseManager.executeQuery {
                    databaseManager.openDatabase()
                    val mMatchActionsDAO = MatchActionsDAO(it, this@MatchOverviewActivity)
                    mMatchActionsDAO.updateAction(
                        mBeanReaction!!.reaction.toString(),
                        mBeanReaction!!.id
                    )
                }
                if (mAdapter != null && actionPosition >= 0) {
                    mAdapter!!.update(mBeanReaction,actionPosition)
                }
            }
        } else if(type == 3) {
            val pn = EditActionDialog(mBeanReaction,this)
            pn.show(supportFragmentManager, pn.tag)
        } else {
            makeConfirmation(getString(R.string.msg_delete_action),"1")
        }
    }

    override fun onConfirm(isTrue: Boolean, type: String) {
        isDialogOpen = false
        if (isTrue) {
            if (type == "1") {
                if (mAdapter != null && actionPosition >= 0) {
                    mAdapter!!.delete(actionPosition)
                    databaseManager.executeQuery {
                        val mMatchActionsDAO = MatchActionsDAO(it, this@MatchOverviewActivity)
                        mMatchActionsDAO.deleteAll(mBeanReaction!!.id)
                    }
                }
            } else if (type == "2") {
                databaseManager.executeQuery {
                    val mInterviewsDAO = InterviewsDAO(it, this@MatchOverviewActivity)
                    mInterviewsDAO.deleteAll(strInterviewId)

                    binding.llInterview.visibility = View.GONE
                }
            }
        }
    }

    fun makeConfirmation(msg:String,type:String){
        if (!isDialogOpen) {
            if (customDialog == null) {
                customDialog = CustomDialog(this@MatchOverviewActivity,msg,getString(R.string.lbl_cancel) ,this, type)
                customDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            isDialogOpen = true
            if (customDialog != null && customDialog!!.isShowing) {
                customDialog!!.dismiss()
            }
            customDialog!!.show()
        }
    }

    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@MatchOverviewActivity)
        }
    }
    private fun updateMatchCount() {
        databaseManager.executeQuery { database ->
            val actionDao = MatchActionsDAO(database, applicationContext)
            val actionCount = actionDao.getTotalCount(matchId)

            val interViewDao = InterviewsDAO(database, applicationContext)
            val interviewCount = interViewDao.getRowCount(matchId)

            if (CommonMethods.isNetworkAvailable(this@MatchOverviewActivity)) {
                mApiCall!!.updateMatchCount(
                    this,
                    true,
                    UserSessions.getUserInfo(this@MatchOverviewActivity).id.toString(),
                    matchId,
                    (actionCount + interviewCount).toString()
                )
            } else {
                CommonMethods.errorDialog(
                    this@MatchOverviewActivity,
                    getResources().getString(R.string.error_internet),
                    getResources().getString(R.string.app_name),
                    getResources().getString(R.string.lbl_ok)
                )
            }
        }
    }

    override fun onSuccess(response: UniversalObject) {
        binding.swRefresh.isRefreshing = false
        try {
            when (response.methodName) {
                Tags.SB_UPDATE_MATCH_COUNT_API -> {
                    val mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        CommonMethods.successToast(this@MatchOverviewActivity,getString(R.string.msg_video_upload))

                        val intent = Intent(this@MatchOverviewActivity, MainActivity::class.java)

                        matchId?.let {
                            videoUploadsWorker.fetchVideos(matchId = it)
                        }

                        // Clear the current task stack and start a new task
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    } else if (CommonMethods.isValidString(mCommonBean.msg)) {
                        errorMsg(mCommonBean.msg);
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong));
                    }
                }
            }
        } catch (ex: Exception) {
            errorMsg(getResources().getString(R.string.something_wrong));
        }
    }

    fun errorMsg(strMsg: String) {
         CommonMethods.errorDialog(
            this@MatchOverviewActivity,
            strMsg,
            getResources().getString(R.string.app_name),
            getResources().getString(R.string.lbl_ok)
        );
    }

    override fun onError(type: String, error: String) {
        errorMsg(error)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        updateMatchCount()
    }
}


