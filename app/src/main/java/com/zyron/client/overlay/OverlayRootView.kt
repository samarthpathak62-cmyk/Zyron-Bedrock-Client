package com.zyron.client.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.SystemClock
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import com.zyron.client.config.ConfigStore
import com.zyron.client.perf.PerformanceMonitor

@SuppressLint("ViewConstructor", "ClickableViewAccessibility", "UseSwitchCompatOrMaterialCode")
class OverlayRootView(context: Context) : FrameLayout(context) {
    private val store = ConfigStore(context)
    private var menuOpen = false
    private var frames = 0
    private var lastFpsAt = SystemClock.elapsedRealtime()
    private var fps = 0
    private val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E8EAED")
        textSize = 28f
        typeface = Typeface.MONOSPACE
    }
    private val btn: TextView
    private var menu: View? = null
    private val clicks = ArrayDeque<Long>()

    init {
        setWillNotDraw(false)
        btn = TextView(context).apply {
            text = "Z"
            textSize = 18f
            setTextColor(Color.parseColor("#081010"))
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#8FB8B8"))
            setOnClickListener { toggleMenu() }
        }
        val lp = LayoutParams(dp(56), dp(56), Gravity.BOTTOM or Gravity.END)
        lp.setMargins(0, 0, dp(16), dp(16))
        addView(btn, lp)
        isClickable = false
        isFocusable = false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val now = SystemClock.elapsedRealtime()
            clicks.addLast(now)
            while (clicks.isNotEmpty() && now - clicks.first() > 1000L) clicks.removeFirst()
        }
        return false
    }

    override fun dispatchDraw(canvas: Canvas) {
        frames++
        val now = SystemClock.elapsedRealtime()
        if (now - lastFpsAt >= 500L) {
            fps = ((frames * 1000) / (now - lastFpsAt)).toInt()
            frames = 0
            lastFpsAt = now
        }
        val snap = PerformanceMonitor.last
        val y0 = 48f
        canvas.drawText("FPS $fps", 24f, y0, hudPaint)
        canvas.drawText("CPS ${clicks.size}", 24f, y0 + 36f, hudPaint)
        canvas.drawText("CAP ${store.fpsCap().let { if (it == 0) "UNL" else it }}", 24f, y0 + 72f, hudPaint)
        if (snap != null) {
            canvas.drawText("BAT ${snap.batteryPct}%  ${snap.thermal.uppercase()}", 24f, y0 + 108f, hudPaint)
        }
        super.dispatchDraw(canvas)
        postInvalidateOnAnimation()
    }

    private fun toggleMenu() {
        menuOpen = !menuOpen
        if (menuOpen) {
            val panel = buildMenu()
            menu = panel
            addView(panel, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            passTouches(false)
        } else {
            menu?.let { removeView(it) }
            menu = null
            passTouches(true)
        }
    }

    private fun passTouches(pass: Boolean) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val lp = layoutParams as WindowManager.LayoutParams
        lp.flags = if (pass) {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        } else {
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        }
        lp.format = PixelFormat.TRANSLUCENT
        runCatching { wm.updateViewLayout(this, lp) }
    }

    private fun buildMenu(): View {
        val scroll = ScrollView(context).apply { setBackgroundColor(Color.parseColor("#CC111318")) }
        val col = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(24))
        }
        col.addView(label("ZYRON CLIENT MENU", 18f, true))
        col.addView(label("Session keeps running. This is overlay-only.", 12f, false))
        listOf(30, 60, 90, 120, 144, 165, 240, 0).forEach { cap ->
            col.addView(Button(context).apply {
                text = if (cap == 0) "FPS Unlimited" else "FPS $cap"
                setOnClickListener {
                    store.setFpsCap(cap)
                    PerformanceMonitor.applyFps(cap)
                }
            })
        }
        listOf("performance", "balanced", "battery", "lowend", "custom").forEach { mode ->
            col.addView(Button(context).apply {
                text = "Mode $mode"
                setOnClickListener { store.setPerfMode(mode) }
            })
        }
        val modules = listOf("CPS Counter", "Attack Indicator", "Custom Crosshair", "Hit Feedback", "Coordinates", "FPS Display", "Ping Display", "Thermal Guard")
        modules.forEach { name ->
            col.addView(Switch(context).apply {
                text = name
                isChecked = true
                setTextColor(Color.parseColor("#E8EAED"))
            })
        }
        col.addView(Button(context).apply {
            text = "Close menu"
            setOnClickListener { toggleMenu() }
        })
        scroll.addView(col)
        return scroll
    }

    private fun label(text: String, size: Float, bold: Boolean) = TextView(context).apply {
        this.text = text
        textSize = size
        setTextColor(Color.parseColor("#E8EAED"))
        setPadding(0, dp(8), 0, dp(8))
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
