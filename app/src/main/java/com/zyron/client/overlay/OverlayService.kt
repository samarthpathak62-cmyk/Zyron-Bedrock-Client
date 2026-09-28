package com.zyron.client.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.zyron.client.R
import com.zyron.client.config.ConfigStore
import com.zyron.client.nativebridge.ZyronNative
import com.zyron.client.perf.PerformanceMonitor

class OverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlay: OverlayRootView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(7, notification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = OverlayRootView(this)
        overlay = view
        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        windowManager?.addView(view, params)
        PerformanceMonitor.applyFps(ConfigStore(this).fpsCap())
        ZyronNative.load()
    }

    override fun onDestroy() {
        overlay?.let { windowManager?.removeView(it) }
        overlay = null
        super.onDestroy()
    }

    private fun notification(): Notification {
        val id = "zyron_overlay"
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(id, getString(R.string.overlay_channel), NotificationManager.IMPORTANCE_LOW)
        )
        return NotificationCompat.Builder(this, id)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.overlay_running))
            .setSmallIcon(R.drawable.ic_zyron)
            .setOngoing(true)
            .build()
    }
}
