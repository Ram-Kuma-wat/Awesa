package com.game.awesa.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class AndroidNetworkObservingStrategy : NetworkObservingStrategy {

    companion object {
        const val LOG = "AndroidNetworkObservingStrategy"
        const val ERROR_MSG_NETWORK_CALLBACK: String = "could not unregister network callback"
        const val ERROR_MSG_RECEIVER: String = "could not unregister receiver"
    }

    // it has to be initialized in the Observable due to Context
    private var networkCallback: NetworkCallback? = null
    private val networkState = NetworkState()
    private val liveConnectivityState = MutableLiveData<Connectivity>()
    private var manager: ConnectivityManager? = null

    fun getLiveConnectivityState() : LiveData<Connectivity> {
        return liveConnectivityState
    }

    fun observeNetworkConnectivity(context: Context) {
        val service = Context.CONNECTIVITY_SERVICE
        manager = context.getSystemService(service) as ConnectivityManager
        networkCallback = createNetworkCallback(context)
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        manager?.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    fun tryToUnregisterCallback() {
        try {
            manager?.unregisterNetworkCallback(networkCallback!!)
        } catch (exception: Exception) {
            onError("could not unregister network callback", exception)
        }
    }

    override fun onError(message: String?, exception: Exception?) {
        Log.e(LOG, message, exception)
    }

    private fun createNetworkCallback(context: Context
    ): NetworkCallback {
        return object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(LOG, network.toString())
                networkState.network = network
                networkState.isConnected = true
                liveConnectivityState.postValue(Connectivity.create(context, networkState))
            }

            override fun onLost(network: Network) {
                Log.e(LOG, network.toString())
                networkState.network = network
                networkState.isConnected = false
                liveConnectivityState.postValue(Connectivity.create(context, networkState))
            }
        }
    }
}
