package com.game.awesa.ui

import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.codersworld.awesalibs.beans.matches.InterviewBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.DBVideoUplaodDao
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.database.dao.VideoMasterDAO
import com.codersworld.awesalibs.listeners.QueryExecutor
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivitySplashBinding
import com.game.awesa.services.InterviewUploadService
import com.game.awesa.services.TrimService
import com.game.awesa.services.VideoUploadService
import com.game.awesa.ui.recorder.MatchOverviewActivity
import com.game.awesa.ui.recorder.VideoPreviewActivity
import com.google.gson.Gson
import java.io.File


class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //    ApiHelper.setApplicationlanguage(this, UserSessions().getLanguage(this))
    }

    var counter = 0;
     var mTimer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        //setContentView(R.layout.activity_splash)
         DatabaseManager.initializeInstance(DatabaseHelper(applicationContext))
        DatabaseManager.getInstance().executeQuery { database ->
            val actionDao = MatchActionsDAO(database, applicationContext)
            //actionDao.deleteUploadedVideos();
            val mVideoMasterDAO = VideoMasterDAO(database, applicationContext)
            var mList = mVideoMasterDAO.selectAll() as ArrayList<DBVideoUplaodDao>
           if(CommonMethods.isValidArrayList(mList)){
               for(a in mList.indices){
                   var mListReaction = actionDao.selectAllForPreview(mList[a].getMatch_id() + "") as ArrayList<ReactionsBean>
                   if ( ! CommonMethods.isValidArrayList(mListReaction)){
                       if (actionDao.getTotalCount(mList[a].getMatch_id() + "") == 0) {
                         /*  try {
                               var file: File = File(mList[a].video_path)
                               file.delete()
                               if (file.exists()) {
                                   file.canonicalFile.delete()
                                   if (file.exists()) {
                                       applicationContext.deleteFile(file.name)
                                   }
                               }
                           } catch (ex: Exception) {
                               ex.printStackTrace()
                           }*/
                           mVideoMasterDAO.deleteVideoById(mList[a].getmId())
                       }
                   }
               }
           }
        }
        mTimer = object : CountDownTimer(2500, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                mTimer!!.cancel()
                if (counter == 0) {
                    animateImage()
                    counter++;
                    mTimer!!.start()
                } else {
                    callActivityIntent()
                }
                //textView.setText("FINISH!!")
            }
        }//.start()
        mTimer!!.start()
        //val handler = Handler()
        // handler.postDelayed({ callActivityIntent() }, 1500)//1500
       // throw RuntimeException("Test Crash")
    }
     fun getSingleAction(){
        counter=-1;
         DatabaseManager.getInstance().executeQuery { database ->
            val dao = InterviewsDAO(database, applicationContext)
             dao.updateVideoAll()
        }
    }

    private fun callActivityIntent() {
        //getSingleAction()
//        CommonMethods.checkService(this@SplashActivity, TrimService::class.java)
       CommonMethods.checkService(this@SplashActivity, VideoUploadService::class.java)
       CommonMethods.checkService(this@SplashActivity, InterviewUploadService::class.java)
        // var mUserSessions = UserSessions(this@SplashActivity)
        //mUserSessions.saveLanguage(this@SplashActivity, "1");
        CommonMethods.moveWithClear(this, LoginActivity::class.java)
    }
    fun animateImage(){
        var animationOut: Animation = AnimationUtils.loadAnimation(applicationContext,R.anim.fade_out)
        var animationIn: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        val animListener: AnimationListener = object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                binding.img.setImageResource(R.drawable.sponsor)
                binding.img.startAnimation(animationIn)
            }
        }
        binding.img.startAnimation(animationOut);
        animationOut.setAnimationListener(animListener);
    }

}