package com.zyron.client.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CrashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val log = intent.getStringExtra(EXTRA_LOG) ?: "No log"
        val source = intent.getStringExtra(EXTRA_SOURCE) ?: "zyron"
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
            setBackgroundColor(Color.parseColor("#08090B"))
        }
        col.addView(TextView(this).apply {
            text = "ZYRON CRASH"
            setTextColor(Color.parseColor("#C45C5C"))
        })
        col.addView(TextView(this).apply {
            text = "Source guess: $source"
            setTextColor(Color.parseColor("#E8EAED"))
            textSize = 16f
            setPadding(0, 12, 0, 12)
        })
        col.addView(TextView(this).apply {
            text = log
            setTextColor(Color.parseColor("#8B919A"))
            textSize = 12f
            typeface = android.graphics.Typeface.MONOSPACE
        })
        col.addView(Button(this).apply {
            text = "Copy log"
            setOnClickListener {
                val clip = android.content.ClipData.newPlainText("zyron-crash", log)
                (getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(clip)
            }
        })
        col.addView(Button(this).apply {
            text = "Restart"
            setOnClickListener {
                startActivity(Intent(this@CrashActivity, SplashActivity::class.java))
                finish()
            }
        })
        setContentView(ScrollView(this).apply { addView(col) })
    }

    companion object {
        const val EXTRA_LOG = "log"
        const val EXTRA_SOURCE = "source"
    }
}
