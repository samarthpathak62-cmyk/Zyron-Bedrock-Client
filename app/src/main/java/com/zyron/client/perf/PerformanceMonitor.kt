package com.zyron.client.perf

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.zyron.client.config.ConfigStore
import com.zyron.client.nativebridge.ZyronNative

data class DeviceSnap(
    val ramMb: Long,
    val cores: Int,
    val model: String,
    val android: String,
    val batteryPct: Int,
    val thermal: String,
    val recommended: String
)

object PerformanceMonitor {
    @Volatile var last: DeviceSnap? = null
        private set

    fun start(context: Context) {
        last = snapshot(context)
        val rec = last?.recommended ?: "balanced"
        if (ConfigStore(context).perfMode() == "auto") {
            ConfigStore(context).setPerfMode(rec)
        }
    }

    fun snapshot(context: Context): DeviceSnap {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mem = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val ramMb = mem.totalMem / (1024 * 1024)
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val thermal = if (Build.VERSION.SDK_INT >= 29) {
            when (pm.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE, PowerManager.THERMAL_STATUS_LIGHT -> "nominal"
                PowerManager.THERMAL_STATUS_MODERATE -> "fair"
                PowerManager.THERMAL_STATUS_SEVERE -> "serious"
                else -> "critical"
            }
        } else "nominal"
        val rec = when {
            ramMb <= 3072 -> "lowend"
            ramMb <= 4096 -> "battery"
            ramMb >= 8192 && Runtime.getRuntime().availableProcessors() >= 8 -> "performance"
            else -> "balanced"
        }
        return DeviceSnap(
            ramMb = ramMb,
            cores = Runtime.getRuntime().availableProcessors(),
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            android = Build.VERSION.RELEASE,
            batteryPct = pct,
            thermal = thermal,
            recommended = rec
        )
    }

    fun applyFps(cap: Int) {
        if (ZyronNative.ready()) ZyronNative.nativeSetFpsCap(cap)
    }
}
