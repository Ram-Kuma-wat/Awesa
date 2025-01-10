package com.game.awesa.ui.recorder

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivitySplashBinding
import com.game.awesa.utils.Media3Transformer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProcessingActivity : AppCompatActivity() {

    @Inject
    lateinit var media3Transformer: Media3Transformer

    companion object {
        const val EXTRA_MATCH_BEAN = "mMatchBean"
        const val SECOND_IN_MS = 1000L
        val TAG: String = ProcessingActivity::class.java.simpleName
    }

    @Inject lateinit var databaseManager: DatabaseManager
    lateinit var binding: ActivitySplashBinding
    var mTimer: CountDownTimer? = null

    var mMatchBean: MatchesBean.InfoBean? = null

    @Suppress("MagicNumber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.img.visibility = View.GONE
        binding.llProgress.visibility = View.VISIBLE

        if (intent.hasExtra(EXTRA_MATCH_BEAN)) {
            mMatchBean = CommonMethods.getSerializable(intent, EXTRA_MATCH_BEAN, MatchesBean.InfoBean::class.java)
        }

        mTimer = object : CountDownTimer(2500, SECOND_IN_MS) { // 2.5 seconds
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                mTimer!!.cancel()
                val intent = Intent(this@ProcessingActivity, MatchOverviewActivity::class.java)
                intent.putExtra(MatchOverviewActivity.EXTRA_MATCH_BEAN, mMatchBean)
                startActivity(intent)
                finish()
            }
        }.start()
    }
}
