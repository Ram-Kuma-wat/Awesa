package com.game.awesa.utils

import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities

/**
 * NetworkState data object
 */
class NetworkState {
    var isConnected: Boolean = false
    var network: Network? = null
    var networkCapabilities: NetworkCapabilities? = null
    var linkProperties: LinkProperties? = null
}
