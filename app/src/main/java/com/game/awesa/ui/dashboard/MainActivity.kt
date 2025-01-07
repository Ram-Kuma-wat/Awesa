package com.game.awesa.ui.dashboard

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnPageChangeListener
import com.game.awesa.R
import com.game.awesa.databinding.ActivityMainBinding
import com.game.awesa.ui.BaseActivity
import com.game.awesa.ui.dashboard.extension.JBNavigationPosition
import com.game.awesa.ui.dashboard.extension.active
import com.game.awesa.ui.dashboard.extension.createFragment
import com.game.awesa.ui.dashboard.extension.findNavigationByPosition
import com.game.awesa.ui.dashboard.extension.findNavigationPositionById
import com.game.awesa.ui.dashboard.extension.findTabByPosition
import com.game.awesa.ui.dashboard.extension.getTag
import com.game.awesa.ui.dashboard.extension.switchFragment
import com.game.awesa.ui.dialogs.CustomDialog
import com.game.awesa.utils.AndroidNetworkObservingStrategy
import com.game.awesa.utils.ErrorReporter
import com.game.awesa.utils.VideoUploadsWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity(), OnPageChangeListener,OnConfirmListener {
    companion object {
        const val EXTRA_ACTIVE_TAB = "mActiveTab"
        val TAG: String = MainActivity::class.java.simpleName
    }

    lateinit var binding: ActivityMainBinding
    private var navPosition: JBNavigationPosition = JBNavigationPosition.HOME

    @Inject
    lateinit var networkObserver: AndroidNetworkObservingStrategy
    @Inject lateinit var videoUploadsWorker: VideoUploadsWorker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@MainActivity,R.layout.activity_main)

        if(savedInstanceState != null) {
            val position = savedInstanceState.getInt(EXTRA_ACTIVE_TAB, 0)
            navPosition = findNavigationByPosition(position)
            binding.bottomNavigation.apply {
                // Set a default position
                active(navPosition.position) // Extension function
                // Set a listener for handling selection events on bottom navigation items
                setOnItemSelectedListener { item ->
                    navPosition = findNavigationPositionById(item.itemId)
                    switchFragment(navPosition)
                }
            }
        } else {
            binding.bottomNavigation.apply {
                // Set a default position
                active(navPosition.position) // Extension function
                // Set a listener for handling selection events on bottom navigation items
                setOnItemSelectedListener { item ->
                    navPosition = findNavigationPositionById(item.itemId)
                    switchFragment(navPosition)
                }
            }
        }

        binding.bottomNavigation.selectedItemId = R.id.navHome
        val errReporter = ErrorReporter()
        errReporter.Init(this)
        errReporter.checkErrorAndSendMail(this)

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    if (navPosition != JBNavigationPosition.HOME) {
                        binding.bottomNavigation.selectedItemId = R.id.navHome
                    } else {
                        val customDialog = CustomDialog(
                            this@MainActivity,
                            resources.getString(R.string.lbl_exit_app_msg),
                            resources.getString(R.string.lbl_cancel) ,
                            this@MainActivity,
                            "2")
                        customDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        customDialog.show()
                    }
                } catch (ex: IllegalStateException) {
                    Log.e(TAG, ex.localizedMessage, ex)
                } catch (ex: UninitializedPropertyAccessException) {
                    Log.e(TAG, ex.localizedMessage, ex)
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(EXTRA_ACTIVE_TAB, navPosition.position)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val position = savedInstanceState.getInt(EXTRA_ACTIVE_TAB, 0)
        navPosition = findNavigationByPosition(position)
        binding.bottomNavigation.selectedItemId = findTabByPosition(position)
    }

    private fun switchFragment(navPosition: JBNavigationPosition): Boolean {
        return findFragment(navPosition).let {
            supportFragmentManager.switchFragment(it, navPosition.getTag())
        }
    }

    private fun findFragment(position: JBNavigationPosition): Fragment {
        return supportFragmentManager.findFragmentByTag(position.getTag())
            ?: position.createFragment()
    }

    override fun onPageChange(mPage: Int) {
        makePageChange(mPage)
    }

    private fun makePageChange(mPage: Int) {
        navPosition = findNavigationPositionById(binding.bottomNavigation.selectedItemId)
        binding.llHeader.visibility = View.VISIBLE
        binding.bottomNavigation.selectedItemId = mPage
    }

    override fun onConfirm(isTrue: Boolean, type: String) {
        if (isTrue) {
            when(type) {
                "2"-> {
                    finishAffinity()
                }
            }
        }
    }
}
