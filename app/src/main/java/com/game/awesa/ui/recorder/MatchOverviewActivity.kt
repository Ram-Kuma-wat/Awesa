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
import com.codersworld.awesalibs.beans.matches.InterviewBean
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnReactionListener
import com.codersworld.awesalibs.listeners.QueryExecutor
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivityMatchOverviewBinding
import com.game.awesa.services.TrimService
import com.game.awesa.services.VideoUploadService
import com.game.awesa.ui.dashboard.MainActivity
import com.game.awesa.ui.dialogs.CustomDialog
import java.io.File

class MatchOverviewActivity : AppCompatActivity(),OnReactionListener, OnConfirmListener {
    lateinit var binding:ActivityMatchOverviewBinding
    var mMatchBean : MatchesBean.InfoBean? = null;
    var mAdapter : OverviewAdapter?=null
    var mListData : ArrayList<ReactionsBean> = ArrayList();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = DataBindingUtil.setContentView(this, R.layout.activity_match_overview)
        //setContentView(R.layout.activity_video_preview)
        DatabaseManager.initializeInstance(DatabaseHelper(applicationContext))
        mMatchBean = CommonMethods.getSerializable(intent,"mMatchBean",MatchesBean.InfoBean::class.java)
        CommonMethods.checkService(this@MatchOverviewActivity, TrimService::class.java)
        if (mMatchBean !=null){
            CommonMethods.loadImage(this@MatchOverviewActivity,mMatchBean!!.team1_image,binding.imgTeam1)
            CommonMethods.loadImage(this@MatchOverviewActivity,mMatchBean!!.team2_image,binding.imgTeam2)
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        mAdapter = OverviewAdapter(this@MatchOverviewActivity,mListData,this)
        binding.rvHistory.adapter = mAdapter
        getData(0)
        binding.imgHome.setOnClickListener {
            CommonMethods.successToast(this@MatchOverviewActivity,getString(R.string.msg_video_upload))
            CommonMethods.moveWithClear(this@MatchOverviewActivity,MainActivity::class.java)
            CommonMethods.checkService(this@MatchOverviewActivity, VideoUploadService::class.java)
        }


        binding.swRefresh.setOnRefreshListener {
            getData(0)
        }
        DatabaseManager.getInstance().executeQuery(QueryExecutor {
            var mInterviewsDAO = InterviewsDAO(it, this@MatchOverviewActivity)
            var mList = mInterviewsDAO.selectAll(mMatchBean!!.id.toString()) as ArrayList<InterviewBean>
            DatabaseManager.getInstance().closeDatabase()
            if (CommonMethods.isValidArrayList(mList)) {
                binding.imgThumbnail.setImageBitmap(CommonMethods.createVideoThumb(this@MatchOverviewActivity, Uri.fromFile(File(mList[0].video))))
                strInterview = mList[0].video
                strInterviewId = mList[0].id
                binding.llInterview.visibility = View.VISIBLE
            }else{
                binding.llInterview.visibility = View.GONE
            }
        })
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
        DatabaseManager.getInstance().executeQuery(QueryExecutor {
            var mMatchActionsDAO = MatchActionsDAO(it, this@MatchOverviewActivity)
            var mList = mMatchActionsDAO.selectAllForPreview(mMatchBean!!.id.toString()) as ArrayList<ReactionsBean>
            DatabaseManager.getInstance().closeDatabase()
            if (CommonMethods.isValidArrayList(mList)) {
                mListData = mList
                mAdapter!!.addAll(mListData)
                checkCompression()
            }
            if (mType==0){
                binding.swRefresh.isRefreshing=false
            }
        })
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
                    DatabaseManager.getInstance().executeQuery(QueryExecutor {
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
                    })
                }
            }
        }
        var mPerc = (((mListData.size-counter)*100)/mListData.size)
        binding.pbProgress.progress = mPerc.toInt()
        binding.txtProgress.text = mPerc.toInt().toString()+"%"
        if (counter>0){
            CommonMethods.checkService(this@MatchOverviewActivity,TrimService::class.java)
            val handler = Handler()
            handler.postDelayed({ getData(1) }, 10000)
        }
    }
}
    var mBeanReaction:ReactionsBean? = null
    var actionPosition=-1;
    override fun OnReactionAction(mReactionsBean: ReactionsBean?, type: Int, position:Int) {
        actionPosition = position
        mBeanReaction = mReactionsBean
        if (type==1){
            if (mReactionsBean !=null && CommonMethods.isValidString(mReactionsBean!!.video)) {
                startActivity(Intent(this@MatchOverviewActivity,VideoPreviewActivity::class.java).putExtra("strPath", mReactionsBean!!.video))
            }
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
                    DatabaseManager.getInstance().executeQuery(QueryExecutor {
                        var mMatchActionsDAO = MatchActionsDAO(it, this@MatchOverviewActivity)
                        mMatchActionsDAO.deleteAll(mBeanReaction!!.id.toString(), 0)
                    })
                }
            }else if (type.equals("2")){
                DatabaseManager.getInstance().executeQuery(QueryExecutor {
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
                })
            }
        }
    }

    var customDialog: CustomDialog? = null
    var isDialogOpen = false
    fun makeConfirmation(msg:String,type:String){
        if (!isDialogOpen) {
            if (customDialog == null) {
                customDialog = CustomDialog(this@MatchOverviewActivity,msg,getString(R.string.lbl_cancel) ,this, type)
                customDialog!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            isDialogOpen = true
            if (customDialog!! != null && customDialog!!.isShowing()) {
                customDialog!!.dismiss()
            }
            customDialog!!.show()
        }
    }

    override fun onBackPressed() {

    }
}