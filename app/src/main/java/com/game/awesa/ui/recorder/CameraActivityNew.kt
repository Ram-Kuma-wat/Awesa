package com.game.awesa.ui.recorder

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.databinding.DataBindingUtil
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
class CameraActivityNew : BaseActivity(), View.OnClickListener, OnResponse<UniversalObject>,
    OnConfirmListener, FileCallback {
    lateinit var binding: ActivityCameraRecordBinding

    var mMatchBean: MatchesBean.InfoBean? = null;
    var mHalf = 1

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_record)
        if (intent.hasExtra("mHalf")) {
            mHalf = intent.getIntExtra("mHalf", 1)
        }
        if (intent.hasExtra("MatchBean")) {
            mMatchBean = CommonMethods.getSerializable(intent, "MatchBean", MatchesBean.InfoBean::class.java)
        }
        //CommonMethods.loadImageDrawable(this@CameraActivity,R.drawable.loading_img_one,binding.imgTor)
         val mFragment = CaptureFragment()
         val bundle = Bundle()
         if (mMatchBean != null) {
             bundle.putSerializable("MatchBean", mMatchBean)
             bundle.putSerializable("mHalf", mHalf)
         }
         mFragment.setArguments(bundle)
         supportFragmentManager
             .beginTransaction()
             .replace(R.id.container_body, mFragment)
             .commit()


     }

    override fun onClick(v: View) {}
    override fun onFileReady(file: File?) {
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    override fun onConfirm(isTrue: Boolean?, type: String?) {

    }

    override fun onSuccess(response: UniversalObject?) {
    }

    override fun onError(type: String?, error: String?) {
    }

}
