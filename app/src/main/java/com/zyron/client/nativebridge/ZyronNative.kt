package com.zyron.client.nativebridge

object ZyronNative {
    @Volatile private var loaded = false
    @Volatile var loadError: String? = null
        private set

    fun load(): Boolean {
        if (loaded) return true
        return try {
            System.loadLibrary("zyron_runtime")
            loaded = nativeInit()
            loaded
        } catch (t: Throwable) {
            loadError = t.message
            false
        }
    }

    fun ready(): Boolean = loaded
    external fun nativeInit(): Boolean
    external fun nativeVersion(): String
    external fun nativeAbi(): String
    external fun nativeNanos(): Long
    external fun nativeSetFpsCap(fps: Int)
    external fun nativeGetFpsCap(): Int
}
