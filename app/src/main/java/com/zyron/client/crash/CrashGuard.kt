package com.zyron.client.crash

import android.content.Context
import android.content.Intent
import com.zyron.client.ui.CrashActivity
import java.io.File
import kotlin.system.exitProcess

object CrashGuard {
    fun install(context: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val dir = File(context.cacheDir, "crash").apply { mkdirs() }
                val file = File(dir, "last-crash.txt")
                val source = classify(throwable)
                val body = buildString {
                    appendLine("Zyron crash")
                    appendLine("thread ${thread.name}")
                    appendLine("source $source")
                    appendLine(throwable::class.java.name)
                    appendLine(throwable.message ?: "")
                    appendLine(throwable.stackTraceToString().replace(Regex("(?i)(token|password|bearer)=\\S+"), "$1=[redacted]"))
                }
                file.writeText(body)
                val intent = Intent(context, CrashActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra(CrashActivity.EXTRA_LOG, body)
                    putExtra(CrashActivity.EXTRA_SOURCE, source)
                }
                context.startActivity(intent)
            } catch (_: Throwable) {
            } finally {
                previous?.uncaughtException(thread, throwable)
                exitProcess(10)
            }
        }
    }

    private fun classify(t: Throwable): String {
        val m = (t.message + t.stackTraceToString()).lowercase()
        return when {
            "unsatisfiedlink" in m || "jni" in m || "libzyron" in m -> "native"
            "minecraft" in m || "bedrock" in m -> "runtime"
            "unknownhost" in m || "timeout" in m -> "network"
            "json" in m || "config" in m -> "resource"
            else -> "zyron"
        }
    }
      }
      
