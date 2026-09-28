package com.zyron.client.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.zyron.client.bedrock.BedrockLauncher
import com.zyron.client.nativebridge.ZyronNative
import com.zyron.client.overlay.OverlayService
import com.zyron.client.perf.PerformanceMonitor

class HubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 64, 40, 40)
            setBackgroundColor(Color.parseColor("#08090B"))
        }
        col.addView(tv("ZYRON", 28f, "#E8EAED"))
        col.addView(tv("Launch overlay, then Bedrock. The menu stays on top.", 14f, "#8B919A"))
        val snap = PerformanceMonitor.snapshot(this)
        col.addView(tv("${snap.model}\nAndroid ${snap.android}  RAM ${snap.ramMb}MB  ${snap.cores} cores\nThermal ${snap.thermal}  Recommend ${snap.recommended}", 12f, "#8B919A"))
        col.addView(tv("Native ${if (ZyronNative.ready()) ZyronNative.nativeVersion() + " " + ZyronNative.nativeAbi() else ZyronNative.loadError ?: "not loaded"}", 12f, "#8FB8B8"))

        col.addView(btn("Grant overlay permission") {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        })
        col.addView(btn("Start Zyron overlay") {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission required", Toast.LENGTH_LONG).show()
                return@btn
            }
            ContextCompat.startForegroundService(this, Intent(this, OverlayService::class.java))
        })
        col.addView(btn("Launch Minecraft Bedrock") {
            if (!BedrockLauncher.installed(this)) {
                Toast.makeText(this, "Minecraft Bedrock is not installed. Zyron does not bundle it.", Toast.LENGTH_LONG).show()
                return@btn
            }
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Enable overlay first so the menu can sit on the game.", Toast.LENGTH_LONG).show()
                return@btn
            }
            ContextCompat.startForegroundService(this, Intent(this, OverlayService::class.java))
            BedrockLauncher.launch(this)
        })
        col.addView(tv("Worlds are never deleted. Config export lives in Zyron storage only.", 12f, "#5C636C"))
        setContentView(ScrollView(this).apply { addView(col) })
    }

    private fun tv(t: String, size: Float, color: String) = TextView(this).apply {
        text = t
        textSize = size
        setTextColor(Color.parseColor(color))
        setPadding(0, 12, 0, 12)
    }

    private fun btn(t: String, click: () -> Unit) = Button(this).apply {
        text = t
        setOnClickListener { click() }
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.topMargin = 12
        layoutParams = lp
    }
}
