package com.codersworld.awesalibs.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.provider.Settings
import com.codersworld.awesalibs.R


/**
 * requires android.permission.ACCESS_NETWORK_STATE
 */
@SuppressLint("MissingPermission")
object NetworkUtils {
    const val TYPE_UNKNOWN: Int = -1

    /**
     * returns information on the active network connection
     */
    @SuppressLint("MissingPermission")
    fun getActiveNetworkInfo(context: Context?): NetworkInfo? {
        if (context == null) {
            return null
        }
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            ?: return null
        // note that this may return null if no network is currently active
        return cm.activeNetworkInfo
    }

    /**
     * returns the ConnectivityManager.TYPE_xxx if there's an active connection, otherwise
     * returns TYPE_UNKNOWN
     */
    private fun getActiveNetworkType(context: Context): Int {
        val info = getActiveNetworkInfo(context)
        if (info == null || !info.isConnected) {
            return TYPE_UNKNOWN
        }
        return info.type
    }

    /**
     * returns true if a network connection is available
     */
    fun isNetworkAvailable(context: Context?): Boolean {
        val info = getActiveNetworkInfo(context)
        return (info != null && info.isConnected)
    }

    /**
     * returns true if the user is connected to WiFi
     */
    fun isWiFiConnected(context: Context): Boolean {
        return (getActiveNetworkType(context) == ConnectivityManager.TYPE_WIFI)
    }

    /**
     * returns true if the user is connected with the mobile data connection
     */
    fun isMobileConnected(context: Context): Boolean {
        val networkType = getActiveNetworkType(context)
        return (networkType == ConnectivityManager.TYPE_MOBILE
            || networkType == ConnectivityManager.TYPE_MOBILE_DUN)
    }

    /**
     * returns true if airplane mode has been enabled
     */
    @Suppress("deprecation")
    fun isAirplaneModeOn(context: Context): Boolean {
        // prior to JellyBean 4.2 this was Settings.System.AIRPLANE_MODE_ON, JellyBean 4.2
        // moved it to Settings.Global
        return Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    }

    /**
     * returns true if there's an active network connection, otherwise displays a toast error
     * and returns false
     */
    fun checkConnection(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        if (isNetworkAvailable(context)) {
            return true
        }
        ToastUtils.showToast(context, R.string.no_network_message)
        return false
    }
}
