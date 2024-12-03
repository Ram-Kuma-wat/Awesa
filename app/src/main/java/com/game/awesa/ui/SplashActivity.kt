package com.game.awesa.ui

import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.beans.VideoUploadBean
import com.codersworld.awesalibs.database.dao.MatchActionsDAO
import com.codersworld.awesalibs.database.dao.VideoMasterDAO
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivitySplashBinding
import com.game.awesa.utils.ErrorReporter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    @Inject
    lateinit var databaseManager: DatabaseManager
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
        val errReporter = ErrorReporter()
        errReporter.Init(this)
        errReporter.CheckErrorAndSendMail(this)
        UserSessions.saveUpdate(this@SplashActivity, 0)

        databaseManager.executeQuery { database ->
            val actionDao = MatchActionsDAO(database, applicationContext)
            //actionDao.updateVideoAll();

            //actionDao.deleteUploadedVideos();
            val mVideoMasterDAO = VideoMasterDAO(database, applicationContext)
            var mList = mVideoMasterDAO.selectAll() as ArrayList<VideoUploadBean>
            //Log.e("mList",Gson().toJson(mList))
            if (CommonMethods.isValidArrayList(mList)) {
                for (a in mList.indices) {
                    var mListReaction =
                        actionDao.selectAllForPreview(mList[a].getMatch_id() + "") as ArrayList<ReactionsBean>
                    if (!CommonMethods.isValidArrayList(mListReaction)) {
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
                            mVideoMasterDAO.deleteVideoById(mList[a].getId())
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

    private fun callActivityIntent() {
        CommonMethods.moveWithClear(this, LoginActivity::class.java)
    }

    fun animateImage() {
        var animationOut: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)
        var animationIn: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        val animListener: AnimationListener = object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                binding.img.setImageResource(R.drawable.sponser_one)
                binding.img.startAnimation(animationIn)
            }
        }
        binding.img.startAnimation(animationOut);
        animationOut.setAnimationListener(animListener);
    }

}
