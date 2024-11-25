package com.game.awesa.ui.dashboard

import android.app.ActivityManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnPageChangeListener
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivityMainBinding
import com.game.awesa.services.TrimService
import com.game.awesa.services.VideoUploadService
import com.game.awesa.ui.BaseActivity
import com.game.awesa.ui.dashboard.extension.JBNavigationPosition
import com.game.awesa.ui.dashboard.extension.active
import com.game.awesa.ui.dashboard.extension.createFragment
import com.game.awesa.ui.dashboard.extension.findNavigationPositionById
import com.game.awesa.ui.dashboard.extension.getTag
import com.game.awesa.ui.dashboard.extension.switchFragment
import com.game.awesa.ui.dialogs.CustomDialog
import com.game.awesa.utils.ErrorReporter
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity(), OnPageChangeListener,OnConfirmListener {
    lateinit var binding:ActivityMainBinding
    private var navPosition: JBNavigationPosition = JBNavigationPosition.HOME


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@MainActivity,R.layout.activity_main)
        binding.bottomNavigation.apply {
            // Set a default position
            active(navPosition.position) // Extension function
            // Set a listener for handling selection events on bottom navigation items
            setOnNavigationItemSelectedListener { item ->
                navPosition = findNavigationPositionById(item.itemId)
                switchFragment(navPosition)
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.navHome
        val errReporter = ErrorReporter()
        errReporter.Init(this)
        errReporter.CheckErrorAndSendMail(this)


    }
    private fun switchFragment(navPosition: JBNavigationPosition): Boolean {
        return findFragment(navPosition).let {
            supportFragmentManager.switchFragment(it, navPosition.getTag()) // Extension function
        }
    }

    private fun findFragment(position: JBNavigationPosition): Fragment {
        return supportFragmentManager.findFragmentByTag(position.getTag())
            ?: position.createFragment()
    }

    override fun onPageChange(mPage: String?) {
        makePageChange(mPage)
    }
    fun makePageChange(mPage: String?) {
        binding.llHeader.visibility = View.VISIBLE
        try {
            when (mPage) {
                "home" -> {
                     binding.bottomNavigation.selectedItemId = R.id.navHome
                }

                "profile" -> {
                    binding.bottomNavigation.selectedItemId = R.id.navProfile
                }

                "history" -> {
                    binding.bottomNavigation.selectedItemId = R.id.navHistory
                }

                "settings" -> {
                    binding.bottomNavigation.selectedItemId = R.id.navMore
                }
            }
        } catch (ex1: Exception) {
            makePageChange(mPage)
        }
    }
    override fun onBackPressed() {
        if (!navPosition.getTag().equals("FragmentHome", true)) {
            binding.bottomNavigation.selectedItemId = R.id.navHome
            //switchFragment(BBNavigationPosition.HOME)
        } else {
            val customDialog = CustomDialog(this@MainActivity, resources.getString(R.string.lbl_exit_app_msg),resources.getString(R.string.lbl_cancel) ,this, "2")
            customDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            customDialog.show()
        }
    }
    override fun onConfirm(isTrue: Boolean, type: String) {
        if (isTrue) {
            when(type){
                "2"->{
                    finishAffinity()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        CommonMethods.checkForegroundService(this@MainActivity, VideoUploadService::class.java) TODO: Refactor
//        CommonMethods.checkService(this@MainActivity, InterviewUploadService::class.java)
    }
}
