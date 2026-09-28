package com.zyron.client.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zyron.client.nativebridge.ZyronNative

class InitActivity : AppCompatActivity() {
    private val steps = listOf(
        "Zyron files",
        "Native ARM64 library",
        "Configuration",
        "Cached resources",
        "Runtime state",
        "Overlay compositor"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#08090B"))
            setPadding(48, 96, 48, 48)
        }
        val status = TextView(this).apply {
            setTextColor(Color.parseColor("#E8EAED"))
            textSize = 14f
        }
        root.addView(TextView(this).apply {
            text = "PRELOAD"
            setTextColor(Color.parseColor("#8FB8B8"))
            textSize = 12f
        })
        root.addView(status)
        setContentView(root)

        val nativeOk = ZyronNative.load()
        if (!nativeOk) {
            status.text = "Native library failed: ${ZyronNative.loadError ?: "unknown"}\nCompanion Java overlay will still run."
        }
        var i = 0
        val handler = Handler(Looper.getMainLooper())
        val tick = object : Runnable {
            override fun run() {
                if (i < steps.size) {
                    status.append("\n${steps[i]}  ok")
                    i++
                    handler.postDelayed(this, 180)
                } else {
                    startActivity(Intent(this@InitActivity, HubActivity::class.java))
                    finish()
                }
            }
        }
        handler.post(tick)
    }
}
