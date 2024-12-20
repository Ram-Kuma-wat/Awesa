package com.game.awesa.ui.recorder


import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivityCameraRecordBinding
import com.game.awesa.ui.BaseActivity
import com.otaliastudios.cameraview.FileCallback
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class InterviewActivityNew : BaseActivity() {
    companion object {
        const val EXTRA_MATCH_BEAN = "mMatchBean"
        val TAG: String = MatchOverviewActivity::class.java.simpleName
    }

    lateinit var binding: ActivityCameraRecordBinding

    private var mMatchBean: MatchesBean.InfoBean? = null

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_record)
        if (intent.hasExtra(EXTRA_MATCH_BEAN)) {
            mMatchBean = CommonMethods.getSerializable(intent, EXTRA_MATCH_BEAN, MatchesBean.InfoBean::class.java)
        }

         val mFragment = InterviewFragment()
         val bundle = Bundle()
         if (mMatchBean != null) {
             bundle.putSerializable("MatchBean", mMatchBean)
         }
         mFragment.setArguments(bundle)
         supportFragmentManager.commit {
             replace(R.id.container_body, mFragment)
         }

     }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }
}
