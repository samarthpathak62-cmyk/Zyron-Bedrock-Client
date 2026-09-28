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

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#08090B"))
        }
        root.addView(TextView(this).apply {
            text = "ZYRON"
            textSize = 36f
            setTextColor(Color.parseColor("#E8EAED"))
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "BEDROCK CLIENT"
            textSize = 12f
            setTextColor(Color.parseColor("#8FB8B8"))
            gravity = Gravity.CENTER
            letterSpacing = 0.28f
        })
        setContentView(root)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, InitActivity::class.java))
            finish()
        }, 1400)
    }
}
