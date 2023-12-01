package com.game.awesa.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.multidex.BuildConfig

public class Global {
    var mContext: Context? = null

    fun Global() {}

    fun Global(ctx: Context?) {
        mContext = ctx
    }

    fun Global(ctx: Activity?) {
        mContext = ctx
    }


    fun getAppVersion(): String {
        return BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE.toString()
    }
    fun getVersionName(mActivity: Activity): String {
        try {
            val manager = mActivity.packageManager
            val info = manager.getPackageInfo(mActivity.packageName, PackageManager.GET_ACTIVITIES)
            return info.versionName
        }catch (ex:Exception){
            ex.printStackTrace()
        return "1.8"
        }
    }

}