package com.game.awesa.utils

import android.content.Context
import android.net.ConnectivityManager

class Connectivity private constructor(builder: Builder = builder()) {
    private val type: Int
    private val subType: Int
    private val available: Boolean
    private val failover: Boolean
    private val roaming: Boolean
    private val typeName: String
    private val subTypeName: String?
    private val reason: String?
    private val extraInfo: String?

    var networkState: NetworkState? = null

    init {
        networkState = builder.networkState
        type = builder.type
        subType = builder.subType
        available = builder.available
        failover = builder.failover
        roaming = builder.roaming
        typeName = builder.typeName
        subTypeName = builder.subTypeName
        reason = builder.reason
        extraInfo = builder.extraInfo
    }

    fun type(): Int {
        return type
    }

    fun subType(): Int {
        return subType
    }

    fun available(): Boolean {
        return available
    }

    fun failover(): Boolean {
        return failover
    }

    fun roaming(): Boolean {
        return roaming
    }

    fun typeName(): String {
        return typeName
    }

    fun subTypeName(): String? {
        return subTypeName
    }

    fun reason(): String? {
        return reason
    }

    fun extraInfo(): String? {
        return extraInfo
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val that = o as Connectivity

        if (type != that.type) {
            return false
        }
        if (subType != that.subType) {
            return false
        }
        if (available != that.available) {
            return false
        }
        if (failover != that.failover) {
            return false
        }
        if (roaming != that.roaming) {
            return false
        }

        if (typeName != that.typeName) {
            return false
        }
        if (if (subTypeName != null) subTypeName != that.subTypeName else that.subTypeName != null) {
            return false
        }
        if (if (reason != null) reason != that.reason else that.reason != null) {
            return false
        }

        return if (extraInfo != null) extraInfo == that.extraInfo else that.extraInfo == null
    }

    override fun hashCode(): Int {
        var result = networkState.hashCode()
        result = 31 * result + type
        result = 31 * result + subType
        result = 31 * result + (if (available) 1 else 0)
        result = 31 * result + (if (failover) 1 else 0)
        result = 31 * result + (if (roaming) 1 else 0)
        result = 31 * result + typeName.hashCode()
        result = 31 * result + (subTypeName?.hashCode() ?: 0)
        result = 31 * result + (reason?.hashCode() ?: 0)
        result = 31 * result + (extraInfo?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return ("Connectivity{"
            + ", type="
            + type
            + ", subType="
            + subType
            + ", available="
            + available
            + ", failover="
            + failover
            + ", roaming="
            + roaming
            + ", typeName='"
            + typeName
            + '\''
            + ", subTypeName='"
            + subTypeName
            + '\''
            + ", reason='"
            + reason
            + '\''
            + ", extraInfo='"
            + extraInfo
            + '\''
            + '}')
    }

    @Suppress("TooManyFunctions")
    class Builder {
        var type: Int = UNKNOWN_TYPE
        var subType: Int = UNKNOWN_SUB_TYPE
        var available: Boolean = false
        var failover: Boolean = false
        var roaming: Boolean = false
        var typeName: String = "NONE"
        var subTypeName: String = "NONE"
        var reason: String = ""
        var extraInfo: String = ""
        internal var networkState: NetworkState = NetworkState()

        fun type(type: Int): Builder {
            this.type = type
            return this
        }

        fun subType(subType: Int): Builder {
            this.subType = subType
            return this
        }

        fun available(available: Boolean): Builder {
            this.available = available
            return this
        }

        fun failover(failover: Boolean): Builder {
            this.failover = failover
            return this
        }

        fun roaming(roaming: Boolean): Builder {
            this.roaming = roaming
            return this
        }

        fun typeName(name: String): Builder {
            this.typeName = name
            return this
        }

        fun subTypeName(subTypeName: String): Builder {
            this.subTypeName = subTypeName
            return this
        }

        fun reason(reason: String): Builder {
            this.reason = reason
            return this
        }

        fun extraInfo(extraInfo: String): Builder {
            this.extraInfo = extraInfo
            return this
        }

        fun networkState(networkState: NetworkState): Builder {
            this.networkState = networkState
            return this
        }

        fun build(): Connectivity {
            return Connectivity(this)
        }
    }

    @Suppress("TooManyFunctions")
    companion object {
        const val UNKNOWN_TYPE: Int = -1
        const val UNKNOWN_SUB_TYPE: Int = -1
        fun create(): Connectivity {
            return builder().build()
        }

        fun create(context: Context): Connectivity {
            return create(context, getConnectivityManager(context))
        }

        fun create(context: Context, networkState: NetworkState): Connectivity {
            return create(context, getConnectivityManager(context), networkState)
        }

        private fun getConnectivityManager(context: Context): ConnectivityManager {
            val service = Context.CONNECTIVITY_SERVICE
            return context.getSystemService(service) as ConnectivityManager
        }

        private fun create(context: Context, manager: ConnectivityManager?): Connectivity {

            if (manager == null) {
                return create()
            }

            return create()
        }

        private fun create(
            context: Context,
            manager: ConnectivityManager?,
            networkState: NetworkState
        ): Connectivity {

            if (manager == null) {
                return create()
            }
            networkState.networkCapabilities = manager.getNetworkCapabilities(networkState.network)
            networkState.linkProperties = manager.getLinkProperties(networkState.network)
            return create(networkState)
        }

        private fun create(networkState: NetworkState): Connectivity {
            return Builder()
                .networkState(networkState)
                .build()
        }

        private fun builder(): Builder {
            return Builder()
        }

        fun type(type: Int): Builder {
            return builder().type(type)
        }

        fun subType(subType: Int): Builder {
            return builder().subType(subType)
        }

        fun available(available: Boolean): Builder {
            return builder().available(available)
        }

        fun failover(failover: Boolean): Builder {
            return builder().failover(failover)
        }

        fun roaming(roaming: Boolean): Builder {
            return builder().roaming(roaming)
        }

        fun typeName(typeName: String): Builder {
            return builder().typeName(typeName)
        }

        fun subTypeName(subTypeName: String): Builder {
            return builder().subTypeName(subTypeName)
        }

        fun reason(reason: String): Builder {
            return builder().reason(reason)
        }

        fun extraInfo(extraInfo: String): Builder {
            return builder().extraInfo(extraInfo)
        }
    }
}
