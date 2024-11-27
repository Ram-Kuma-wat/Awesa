package com.game.awesa.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkConnectivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { ConnectivityUtils.notifyNetworkStatus(it) }
    }
}

// States represented as enums
enum class NetworkState(val isConnected : Boolean) {

    CONNECTED(true),

    DISCONNECTED(false),

    UNINITIALIZED(false)
}

object ConnectivityUtils {

    private val liveConnectivityState = MutableLiveData<NetworkState>()

    fun notifyNetworkStatus(ctx: Context) {
        val newState = getLatestConnectivityStatusWithContext(ctx)
        // do some work
        liveConnectivityState.value = newState
    }

    private fun getLatestConnectivityStatusWithContext(ctx: Context): NetworkState {
        val isConnected = isMobileConnected(ctx) || isWifiConnected(ctx) // isConnected(ctx)
        return if(isConnected) {
            NetworkState.CONNECTED
        } else {
            NetworkState.DISCONNECTED
        }
    }

    fun getLiveConnectivityState() : LiveData<NetworkState> {
        return liveConnectivityState
    }

    fun isConnected(context: Context): Boolean {
        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun isWifiConnected(context: Context): Boolean {
        return isConnected(context, ConnectivityManager.TYPE_WIFI)
    }

    fun isMobileConnected(context: Context): Boolean {
        return isConnected(context, ConnectivityManager.TYPE_MOBILE)
    }

    private fun isConnected(context: Context, type: Int): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return isConnected(connMgr, type)
    }

    private fun isConnected(connMgr: ConnectivityManager, type: Int): Boolean {
        val networks: Array<Network> = connMgr.allNetworks
        var networkInfo: NetworkInfo?
        for (mNetwork in networks) {
            networkInfo = connMgr.getNetworkInfo(mNetwork)
            if (networkInfo != null && networkInfo.type == type && networkInfo.isConnected) {
                return true
            }
        }
        return false
    }

}
