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
    private var mActions = 0
    private var mAdapter: OverviewAdapter?=null
    private var mListData: ArrayList<ReactionsBean> = ArrayList()
    private var mBeanReaction: ReactionsBean? = null
    private var actionPosition =-1

    var strInterview = ""
    var strInterviewId = 0
    private var customDialog: CustomDialog? = null
    private var isDialogOpen = false
    var mApiCall: ApiCall? = null

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_match_overview)
        initApiCall()
        mMatchBean = CommonMethods.getSerializable(
            intent,
            EXTRA_MATCH_BEAN,
            MatchesBean.InfoBean::class.java
        )

        if (mMatchBean != null) {
            matchId = mMatchBean!!.id.toString()
            CommonMethods.checkTrimServiceWithData(this@MatchOverviewActivity, TrimService::class.java, matchId)
            CommonMethods.loadImage(this@MatchOverviewActivity, mMatchBean!!.team1_image,binding.imgTeam1)
            CommonMethods.loadImage(this@MatchOverviewActivity, mMatchBean!!.team2_image,binding.imgTeam2)
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL,false)
        mAdapter = OverviewAdapter(this@MatchOverviewActivity, mListData, this)
        binding.rvHistory.adapter = mAdapter
        getData(0)
        binding.imgHome.setOnClickListener {
            updateMatchCount()
        }

        binding.swRefresh.setOnRefreshListener {
            getData(0)
        }
        databaseManager.executeQuery {
            val mInterviewsDAO = InterviewsDAO(it, this@MatchOverviewActivity)
            val mList =
                mInterviewsDAO.selectAll(mMatchBean!!.id.toString()) as ArrayList<InterviewBean>
            databaseManager.closeDatabase()
            if (CommonMethods.isValidArrayList(mList)) {
                binding.imgThumbnail.setImageBitmap(
                    CommonMethods.createVideoThumb(
                        this@MatchOverviewActivity,
                        Uri.fromFile(File(mList[0].video))
                    )
                )
                strInterview = mList[0].video
                strInterviewId = mList[0].id
                binding.llInterview.visibility = View.VISIBLE
            } else {
                binding.llInterview.visibility = View.GONE
            }
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

    private fun getData(mType: Int) {
       if(mType == 0) {
           binding.swRefresh.isRefreshing = true
       }
        databaseManager.executeQuery {
            val mMatchActionsDAO = MatchActionsDAO(it, this@MatchOverviewActivity)
            val mList =
                mMatchActionsDAO.selectAllForPreview(mMatchBean!!.id.toString()) as ArrayList<ReactionsBean>
            databaseManager.closeDatabase()
            if (CommonMethods.isValidArrayList(mList)) {
                mListData = mList
                mAdapter!!.addAll(mListData)
//                checkCompression()
            }
            if (mType == 0) {
                binding.swRefresh.isRefreshing = false
            }
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
                    if (fileSize < 4){
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
        if (type == 1){
            if (mReactionsBean !=null && CommonMethods.isValidString(mReactionsBean.video)) {
                val intent = Intent(this@MatchOverviewActivity, VideoPreviewActivity::class.java)
                intent.putExtra(VideoPreviewActivity.EXTRA_VIDEO_PATH, mReactionsBean.video)
                startActivity(intent)
            }
        }else if (type == 99) {
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
        }else if(type == 3) {
            val pn = EditActionDialog(mBeanReaction,this)
            pn.show(supportFragmentManager, pn.tag)
        }else{
            makeConfirmation(getString(R.string.msg_delete_action),"1")
        }
    }

    override fun onConfirm(isTrue: Boolean, type: String) {
        isDialogOpen = false
        if (isTrue) {
            if (type.equals("1")) {
                if (mAdapter != null && actionPosition >= 0) {
                    mAdapter!!.delete(actionPosition)
                    databaseManager.executeQuery {
                        val mMatchActionsDAO = MatchActionsDAO(it, this@MatchOverviewActivity)
                        mMatchActionsDAO.deleteAll(mBeanReaction!!.id.toString(), 0)
                    }
                }
            }else if (type.equals("2")) {
                databaseManager.executeQuery {
                    val mInterviewsDAO = InterviewsDAO(it, this@MatchOverviewActivity)
                    mInterviewsDAO.deleteAll(strInterviewId.toString(), 0)

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
            mActions = actionDao.getTotalCount(matchId)

            if (CommonMethods.isNetworkAvailable(this@MatchOverviewActivity)) {
                mApiCall!!.updateMatchCount(
                    this,
                    true,
                    UserSessions.getUserInfo(this@MatchOverviewActivity).id.toString(),
                    matchId,
                    mActions.toString()
                )
            } else {
                CommonMethods.errorDialog(
                    this@MatchOverviewActivity,
                    getResources().getString(R.string.error_internet),
                    getResources().getString(R.string.app_name),
                    getResources().getString(R.string.lbl_ok)
                );
            }
        }
    }

    override fun onSuccess(response: UniversalObject) {
        binding.swRefresh.isRefreshing = false
        try {
            Logs.e(response.methodName)
            when (response.methodName) {
                Tags.SB_UPDATE_MATCH_COUNT_API -> {
                    var mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        CommonMethods.successToast(this@MatchOverviewActivity,getString(R.string.msg_video_upload))

                        val intent = Intent(this@MatchOverviewActivity, MainActivity::class.java)

                        videoUploadsWorker.fetchVideos(matchId = matchId)

                        // Clear the current task stack and start a new task
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        // Optionally, you can kill the current process to ensure a clean restart
//                        Process.killProcess(Process.myPid())
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
         CommonMethods.errorDialog(
            this@MatchOverviewActivity,
            strMsg,
            getResources().getString(R.string.app_name),
            getResources().getString(R.string.lbl_ok)
        );
    }

    override fun onError(type: String, error: String) {
        errorMsg(error)
        //method body
    }

    override fun onBackPressed() {
        super.onBackPressed()
        updateMatchCount()
    }
}


