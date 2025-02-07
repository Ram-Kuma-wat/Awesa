package com.game.awesa.ui.recorder

 import android.content.Intent
 import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
 import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
 import androidx.media3.common.util.UnstableApi
 import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivitySplashBinding
import com.game.awesa.utils.MyBounceInterpolator

class TutorialActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MATCH_BEAN = "MatchBean"
        val TAG: String = TutorialActivity::class.java.simpleName
    }

    lateinit var binding: ActivitySplashBinding

    private var mMatchBean: MatchesBean.InfoBean? = null

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.img.setImageResource(R.drawable.recording_hint)
        binding.btnContinue.visibility=View.VISIBLE
        animateButton()

        if (intent.hasExtra(EXTRA_MATCH_BEAN)) {
            mMatchBean =
                CommonMethods.getSerializable(
                    intent,
                    EXTRA_MATCH_BEAN,
                    MatchesBean.InfoBean::class.java)
        }
        binding.btnContinue.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra(CameraActivity.EXTRA_MATCH_BEAN, mMatchBean)
            intent.putExtra(CameraActivity.EXTRA_MATCH_HALF, 1)
            startActivity(intent)
            finish()
        }
    }

    @Suppress("MagicNumber")
    fun animateButton() {
        // Load the animation
        val buttonBounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        val animationDuration: Double = 3.0 * 1000
        buttonBounceAnimation.duration = animationDuration.toLong()

        // Use custom animation interpolator to achieve the bounce effect
        val interpolator = MyBounceInterpolator(0.20, 20.0)
        buttonBounceAnimation.interpolator = interpolator

        // Animate the button
        binding.btnContinue.startAnimation(buttonBounceAnimation)

        // Run button animation again after it finished
        buttonBounceAnimation.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                animateButton()
            }
        })
    }

}