package com.game.awesa.services

import android.R.attr.path
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.matches.InterviewBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.RetrofitUtils
import com.codersworld.awesalibs.rest.UniverSelObjct
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Tags
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class InterviewUploadService :Service(), OnResponse<UniverSelObjct> {
    var list: ArrayList<InterviewBean> = ArrayList()


    override fun onCreate() {
        super.onCreate()
        CommonMethods.showLog("Upload Service Create")
        DatabaseManager.initializeInstance(DatabaseHelper(applicationContext))
        init()
        //fetchMatch(0)
        stopSelf()
    }
    fun init(){
        if (!CommonMethods.isValidArrayList(list)) {
            try {
                getSingleAction()
            } catch (ex: Exception) {
                ex.printStackTrace()
                init()
            }
        }
    }
    var counter=-1;
    fun upload(){
        counter++
        if(CommonMethods.isValidArrayList(list) && list.size>counter){
            var mBeanReactions  =  list[counter] as InterviewBean
            val user_id = UserSessions.getUserInfo(applicationContext).id.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val match_id = mBeanReactions.match_id.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
             val mVideo = RetrofitUtils.createFilePart("video",File(mBeanReactions.video).absolutePath,"image".toMediaTypeOrNull())
            ApiCall(applicationContext).uploadInterview(this, false,user_id,match_id,mVideo,mBeanReactions)

        }
    }
    fun getSingleAction(){
        counter=-1;
        list = ArrayList()
        DatabaseManager.getInstance().executeQuery { database ->
            val dao = InterviewsDAO(database, applicationContext)
            // mListReaction = dao.selectAll(mDao.getMatch_id() + "",  "2")
            list = dao.selectAllUploaded( "")
            if(CommonMethods.isValidArrayList(list)){
              //  for(a in list.indices) {
                upload()
//                }
            }else{
                Log.e("errrr","errrrrrr")
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        CommonMethods.showLog("Upload Service Destroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CommonMethods.showLog("Upload Service onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onSuccess(response: UniverSelObjct) {
        try{
            when(response.methodname){
                Tags.SB_UPLOAD_INTERVIEW_API->{
                    try{
                        var mBean = response.response as CommonBean
                        if (mBean.status==1 && CommonMethods.isValidString(response.msg)){
                            var  mReactionsBean = Gson().fromJson<InterviewBean>(response.msg,InterviewBean::class.java)
                            if (mReactionsBean !=null){
                                DatabaseManager.getInstance().executeQuery { database ->
                                    val dao = InterviewsDAO(database, applicationContext)
                                    dao.deleteAll(mReactionsBean.id.toString(),0);
                                    try{
                                        var file: File = File(mReactionsBean.video)
                                        file.delete()
                                        if (file.exists()) {
                                            file.canonicalFile.delete()
                                            if (file.exists()) {
                                                applicationContext.deleteFile(file.name)
                                            }
                                        }
                                    }catch (ex:Exception){
                                        ex.printStackTrace()
                                    }
                                    upload()
                                }
                            }
                        }
                    }catch (ex1:Exception){
                        ex1.printStackTrace()
                    }
                }
            }
        }catch (ex:Exception){
            ex.printStackTrace()
        }
    }

    override fun onError(type: String?, error: String?) {
     }
}