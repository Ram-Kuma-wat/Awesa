package com.game.awesa.utils

import android.content.Context
import androidx.lifecycle.LiveData


/**
 * Network observing strategy allows to implement different strategies for monitoring network
 * connectivity change. Network monitoring API may differ depending of specific Android version.
 */
interface NetworkObservingStrategy {
    /**
     * Observes network connectivity
     *
     * @param context of the Activity or an Application
     * @return Observable representing stream of the network connectivity
     */
//    fun observeNetworkConnectivity(context: Context?): LiveData<Connectivity?>?

    /**
     * Handles errors, which occurred during observing network connectivity
     *
     * @param message to be processed
     * @param exception which was thrown
     */
    fun onError(message: String?, exception: Exception?)
}
