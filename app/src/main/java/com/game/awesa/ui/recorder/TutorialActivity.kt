package com.game.awesa.ui.recorder

 import android.content.Intent
 import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
 import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivitySplashBinding
import com.game.awesa.utils.MyBounceInterpolator


class TutorialActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //    ApiHelper.setApplicationlanguage(this, UserSessions().getLanguage(this))
    }

    private var mMatchBean: MatchesBean.InfoBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.img.setImageResource(R.drawable.recording_hint)
        binding.btnContinue.visibility=View.VISIBLE
        animateButton()

        if (intent.hasExtra(CaptureFragment.EXTRA_MATCH_BEAN)) {
            mMatchBean =
                CommonMethods.getSerializable(
                    intent,
                    CaptureFragment.EXTRA_MATCH_BEAN,
                    MatchesBean.InfoBean::class.java)
        }
        binding.btnContinue.setOnClickListener {
            val intent = Intent(this@TutorialActivity, CameraActivity::class.java)
            intent.putExtra(CaptureFragment.EXTRA_MATCH_BEAN, mMatchBean)
            startActivity(intent)
            finish()
        }
    }

    fun animateButton() {
        // Load the animation
        val myAnim = AnimationUtils.loadAnimation(this, com.game.awesa.R.anim.bounce)
        val animationDuration: Double = 3.0 * 1000
        myAnim.duration = animationDuration.toLong()

        // Use custom animation interpolator to achieve the bounce effect
        val interpolator = MyBounceInterpolator(0.20, 20.0)
        myAnim.interpolator = interpolator

        // Animate the button
        binding.btnContinue.startAnimation(myAnim)

        // Run button animation again after it finished
        myAnim.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                animateButton()
            }
        })
    }

}