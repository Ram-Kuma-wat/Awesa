package com.game.awesa.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivitySplashBinding
import com.game.awesa.ui.recorder.MatchOverviewActivity
import com.game.awesa.ui.recorder.ProcessingActivity
import com.game.awesa.utils.ErrorReporter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var databaseManager: DatabaseManager
    lateinit var binding: ActivitySplashBinding
    var counter = 0
    var mTimer: CountDownTimer? = null

    @Suppress("MagicNumber", "EmptyFunctionBlock")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        val errReporter = ErrorReporter()
        errReporter.Init(this)
        errReporter.checkErrorAndSendMail(this)
        UserSessions.saveUpdate(this@SplashActivity, 0)

        mTimer = object : CountDownTimer(2500, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                mTimer!!.cancel()
                if (counter == 0) {
                    callActivityIntent()
/*
                    animateImage()
                    counter++
                    mTimer!!.start()
*/
                } else {
                    callActivityIntent()
                }
            }
        }.start()
    }

    private fun callActivityIntent() {
//        val intent = Intent(this@SplashActivity, MatchOverviewActivity::class.java)
//        intent.putExtra(MatchOverviewActivity.EXTRA_MATCH_BEAN, mMatchBean)
  //      startActivity(intent)
    //    finish()

        CommonMethods.moveWithClear(this, LoginActivity::class.java)
    }

    @Suppress("EmptyFunctionBlock")
    fun animateImage() {
        val animationOut: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)
        val animationIn: Animation =
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
