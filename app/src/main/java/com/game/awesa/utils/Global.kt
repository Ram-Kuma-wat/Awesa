package com.game.awesa.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.multidex.BuildConfig
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.game.awesa.ui.dialogs.CustomDialog

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
    fun getVersionName(mActivity: Activity): String {
        try {
            val manager = mActivity.packageManager
            val info = manager.getPackageInfo(mActivity.packageName, PackageManager.GET_ACTIVITIES)
            return info.versionName
        }catch (ex: PackageManager.NameNotFoundException){
            ex.printStackTrace()
        return "1.8"
        }
    }

}