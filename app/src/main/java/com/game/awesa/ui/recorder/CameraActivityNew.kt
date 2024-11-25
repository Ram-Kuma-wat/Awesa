package com.game.awesa.ui.recorder

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivityCameraRecordBinding
import com.game.awesa.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraActivityNew : BaseActivity() {
    lateinit var binding: ActivityCameraRecordBinding

    private var mMatchBean: MatchesBean.InfoBean? = null;
    private var mHalf = 1

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_record)

        if (intent.hasExtra(CaptureFragment.EXTRA_MATCH_HALF)) {
            mHalf = intent.getIntExtra(CaptureFragment.EXTRA_MATCH_HALF, 1)
        }

        if (intent.hasExtra(CaptureFragment.EXTRA_MATCH_BEAN)) {
            mMatchBean = CommonMethods.getSerializable(
                intent,
                CaptureFragment.EXTRA_MATCH_BEAN,
                MatchesBean.InfoBean::class.java
            )
        }

         val mFragment = CaptureFragment()
         val bundle = Bundle()

         if (mMatchBean != null) {
             bundle.putSerializable(CaptureFragment.EXTRA_MATCH_BEAN, mMatchBean)
             bundle.putSerializable(CaptureFragment.EXTRA_MATCH_HALF, mHalf)
         }

         mFragment.setArguments(bundle)
         supportFragmentManager.commit {
             replace(R.id.container_body, mFragment)
         }
     }
}
