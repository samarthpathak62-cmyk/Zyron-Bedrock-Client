package com.zyron.client

import android.app.Application
import com.zyron.client.crash.CrashGuard
import com.zyron.client.nativebridge.ZyronNative
import com.zyron.client.perf.PerformanceMonitor

class ZyronApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashGuard.install(this)
        ZyronNative.load()
        PerformanceMonitor.start(this)
    }
}
