package com.game.awesa.utils

 import android.content.Context
import android.content.pm.PackageManager
  import androidx.multidex.BuildConfig
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.game.awesa.ui.dialogs.CustomDialog
import java.io.File
import java.io.FileOutputStream


import android.app.Activity
import android.os.Build
import android.view.View
 import android.view.WindowInsetsController
 import androidx.core.content.ContextCompat


@Suppress("DEPRECATION")
public class Global {
    var mContext: Context? = null

    fun Global() {}

    fun Global(ctx: Context?) {
        mContext = ctx
    }

    fun Global(ctx: Activity) {
        mContext = ctx
    }

    var customDialog: CustomDialog? = null
    var isDialogOpen = false

    fun makeConfirmation(msg: String, mActivity: Activity, mListener: OnConfirmListener) {
        if (!isDialogOpen) {
            if (customDialog == null) {
                customDialog = CustomDialog(mActivity,msg,"" ,mListener, "99")
            }
            isDialogOpen = true
            if (customDialog!!.isShowing) {
                customDialog!!.dismiss()
            }
            customDialog!!.show()
        }
    }

    fun getAppVersion(): String {
        return BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE.toString()
    }
    fun getVersionName(mActivity: Activity): String? {
        try {
            val manager = mActivity.packageManager
            val info = manager.getPackageInfo(mActivity.packageName, PackageManager.GET_ACTIVITIES)
            return info.versionName
        }catch (ex: PackageManager.NameNotFoundException){
            ex.printStackTrace()
        return "1.8"
        }
    }
    fun copyWatermarkFromAssets(context: Context): String {
        val inputStream = context.assets.open("watermark.png") // if in assets folder
        val file = File(context.filesDir, "watermark.png")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return file.absolutePath
    }


    /**
     * Universal helper to set status bar color and icons visibility
     *
     * @param activity  - The current Activity
     * @param colorRes  - The status bar color resource
     * @param lightIcons - true = white icons, false = dark icons
     */
    fun setStatusBarColor(activity: Activity, colorRes: Int, lightIcons: Boolean = true) {
        val window = activity.window
        val color = ContextCompat.getColor(activity, colorRes)

        // Always set status bar background
        window.statusBarColor = color

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // ✅ Android 11+ — modern API
            val controller = window.insetsController
            if (controller != null) {
                if (lightIcons) {
                    controller.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    controller.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
                return
            }
            // If controller is null → fallback to legacy below
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // ✅ Android 6 – 10 — fallback
            @Suppress("DEPRECATION")
            var flags = window.decorView.systemUiVisibility
            flags = if (lightIcons) {
                // Force white icons
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                // Force dark icons
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = flags
        }
    }
}