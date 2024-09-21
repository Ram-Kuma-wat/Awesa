package com.game.awesa.ui.recorder

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.matches.InterviewBean
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnReactionListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.listeners.QueryExecutor
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
    @Inject
    lateinit var videoUploadsWorker: VideoUploadsWorker

    @Inject lateinit var databaseManager: DatabaseManager

    lateinit var binding:ActivityMatchOverviewBinding
    private var mMatchBean : MatchesBean.InfoBean? = null;
    private var match_id :String="";
    private var mActions =0;
    private var mAdapter : OverviewAdapter?=null
    private var mListData : ArrayList<ReactionsBean> = ArrayList();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = DataBindingUtil.setContentView(this, R.layout.activity_match_overview)
        //setContentView(R.layout.activity_video_preview)
        initApiCall()
        mMatchBean = CommonMethods.getSerializable(intent,"mMatchBean",MatchesBean.InfoBean::class.java)
        //CommonMethods.checkService(this@MatchOverviewActivity, TrimService::class.java)
        CommonMethods.checkServiceWIthData(this@MatchOverviewActivity, TrimService::class.java,match_id)
        if (mMatchBean !=null){
            match_id = mMatchBean!!.id.toString()
            CommonMethods.loadImage(this@MatchOverviewActivity,mMatchBean!!.team1_image,binding.imgTeam1)
            CommonMethods.loadImage(this@MatchOverviewActivity,mMatchBean!!.team2_image,binding.imgTeam2)
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        mAdapter = OverviewAdapter(this@MatchOverviewActivity,mListData,this)
        binding.rvHistory.adapter = mAdapter
        getData(0)
        binding.imgHome.setOnClickListener {
            updateMatchCount()
        }


        binding.swRefresh.setOnRefreshListener {
            getData(0)
        }
        databaseManager.executeQuery1(QueryExecutor {
            var mInterviewsDAO = InterviewsDAO(it, this@MatchOverviewActivity)
            var mList = mInterviewsDAO.selectAll(mMatchBean!!.id.toString()) as ArrayList<InterviewBean>
           // Log.e("InterviewmList",Gson().toJson(mList))
            databaseManager.closeDatabase()
            if (CommonMethods.isValidArrayList(mList)) {
                binding.imgThumbnail.setImageBitmap(CommonMethods.createVideoThumb(this@MatchOverviewActivity, Uri.fromFile(File(mList[0].video))))
                strInterview = mList[0].video
                strInterviewId = mList[0].id
                binding.llInterview.visibility = View.VISIBLE
            }else{
                binding.llInterview.visibility = View.GONE
            }
        },"MatchOverview1")
        binding.rlPlay.setOnClickListener {
            if (CommonMethods.isValidString(strInterview)){
                startActivity(Intent(this@MatchOverviewActivity,VideoPreviewActivity::class.java).putExtra("strPath",strInterview))
            }
        }
        binding.imgThumbnail.setOnClickListener {
            if (CommonMethods.isValidString(strInterview)){
                startActivity(Intent(this@MatchOverviewActivity,VideoPreviewActivity::class.java).putExtra("strPath",strInterview))
            }
        }
        binding.imgPlay.setOnClickListener {
            if (CommonMethods.isValidString(strInterview)){
                startActivity(Intent(this@MatchOverviewActivity,VideoPreviewActivity::class.java).putExtra("strPath",strInterview))
            }
        }
        binding.imgDelete.setOnClickListener {
            if (CommonMethods.isValidString(strInterview)){
                makeConfirmation(getString(R.string.msg_delete_interview),"2")
            }
        }
    }
    var strInterview = "";
    var strInterviewId = 0;

    fun getData(mType:Int){
       if(mType==0){
           binding.swRefresh.isRefreshing=true
       }
        databaseManager.executeQuery1(QueryExecutor {
            var mMatchActionsDAO = MatchActionsDAO(it, this@MatchOverviewActivity)
            var mList = mMatchActionsDAO.selectAllForPreview(mMatchBean!!.id.toString()) as ArrayList<ReactionsBean>
            databaseManager.closeDatabase()
            if (CommonMethods.isValidArrayList(mList)) {
                mListData = mList
                mAdapter!!.addAll(mListData)
                checkCompression()
            }
            if (mType==0){
                binding.swRefresh.isRefreshing=false
            }
        },"MatchOverview2")
    }

fun checkCompression(){
    if (CommonMethods.isValidArrayList(mListData)){
        var counter=0;
        for(a in mListData.indices){
            if (!CommonMethods.isValidString(mListData[a].video)){
                counter++
            }else{
                val file: File = File(mListData[a].video)
                val file_size =( ((file.length() / 1024).toString().toInt())/1024).toString().toInt()
                if (file_size<4){
                    counter++
                    databaseManager.executeQuery1(QueryExecutor {
                        var mMatchActionsDAO = MatchActionsDAO(it, this@MatchOverviewActivity)
                        mMatchActionsDAO.updateVideo("","",mListData[a].id)
                        /*try{
                            var file: File = File(mListData[a].video)
                            file.delete()
                            if (file.exists()) {
                                file.canonicalFile.delete()
                                if (file.exists()) {
                                    applicationContext.deleteFile(file.name)
                                }
                            }
                        }catch (ex:Exception){
                            ex.printStackTrace()
                        }*/
                    },"MatchOverview3")
                }
            }
        }
        if (counter>0){
            //CommonMethods.checkService(this@MatchOverviewActivity,TrimService::class.java)
            val handler = Handler()
            handler.postDelayed({
                //getData(1)
                                }, 3000)
        }
    }
}
    var mBeanReaction:ReactionsBean? = null
    var actionPosition=-1;
    override fun OnReactionAction(mReactionsBean: ReactionsBean?, type: Int, position:Int) {
        if (type < 99) {
            actionPosition = position
            mBeanReaction = mReactionsBean
        }
        if (type==1){
            if (mReactionsBean !=null && CommonMethods.isValidString(mReactionsBean!!.video)) {
                startActivity(Intent(this@MatchOverviewActivity,VideoPreviewActivity::class.java).putExtra("strPath", mReactionsBean!!.video))
            }
        }else if (type==99){
            if (mReactionsBean !=null && CommonMethods.isValidString(mReactionsBean!!.video)) {
                databaseManager.executeQuery1(QueryExecutor {
                    databaseManager.openDatabase()
                    var mMatchActionsDAO = MatchActionsDAO(it, this@MatchOverviewActivity)
                    mMatchActionsDAO.updateAction(mBeanReaction!!.reaction.toString(), mBeanReaction!!.id)
                },"MatchOverview4")
                if (mAdapter != null && actionPosition >= 0) {
                    mAdapter!!.update(mBeanReaction,actionPosition)
                }
            }
        }else if(type==3){
            var pn = EditActionDialog(mBeanReaction,this)
            pn.show(supportFragmentManager, pn.getTag())
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
                    databaseManager.executeQuery1(QueryExecutor {
                        var mMatchActionsDAO = MatchActionsDAO(it, this@MatchOverviewActivity)
                        mMatchActionsDAO.deleteAll(mBeanReaction!!.id.toString(), 0)
                    },"MatchOverview5")
                }
            }else if (type.equals("2")){
                databaseManager.executeQuery1(QueryExecutor {
                    var mInterviewsDAO = InterviewsDAO(it, this@MatchOverviewActivity)
                    mInterviewsDAO.deleteAll(strInterviewId.toString(), 0)
                    try{
                        /*var file: File = File(strInterview)
                        file.delete()
                        if (file.exists()) {
                            file.canonicalFile.delete()
                            if (file.exists()) {
                                applicationContext.deleteFile(file.name)
                            }
                        }*/
                    }catch (ex:Exception){
                        ex.printStackTrace()
                    }

                    binding.llInterview.visibility = View.GONE
                },"MatchOverview6")
            }
        }
    }

    private var customDialog: CustomDialog? = null
    private var isDialogOpen = false
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

    var mApiCall: ApiCall? = null
    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@MatchOverviewActivity)
        }
    }
    fun updateMatchCount() {
        databaseManager.executeQuery { database ->
            val actionDao = MatchActionsDAO(database, applicationContext)
            mActions = actionDao.getTotalCount(match_id);

            if (CommonMethods.isNetworkAvailable(this@MatchOverviewActivity)) {
                mApiCall!!.updateMatchCount(
                    this,
                    true,
                    UserSessions.getUserInfo(this@MatchOverviewActivity).id.toString(),
                    match_id,
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

//                        CommonMethods.checkService(this@MatchOverviewActivity, InterviewUploadService::class.java)

                        val intent = Intent(this@MatchOverviewActivity, MainActivity::class.java)

                        videoUploadsWorker.fetchVideos(matchId = match_id)

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
        updateMatchCount()

        //super.onBackPressed()
    }
}


