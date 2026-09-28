package com.zyron.client.importing

import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class ImportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri: Uri? = intent.data ?: run {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
        }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 64, 40, 40)
            setBackgroundColor(Color.parseColor("#08090B"))
        }
        val info = TextView(this).apply {
            setTextColor(Color.parseColor("#E8EAED"))
            textSize = 16f
        }
        col.addView(info)
        setContentView(col)
        if (uri == null) {
            info.text = "No pack attached."
            return
        }
        val name = uri.lastPathSegment ?: "pack"
        val kind = when {
            name.endsWith(".mcworld", true) -> "mcworld"
            name.endsWith(".mcpack", true) -> "mcpack"
            else -> "unknown"
        }
        if (kind == "unknown") {
            info.text = "Unsupported file. Use .mcpack or .mcworld."
            return
        }
        val valid = validateZip(uri)
        info.text = "Import $name as $kind?\n${if (valid) "ZIP looks valid." else "Could not fully validate. Import still requires confirmation."}\n\nZyron copies into games/com.mojang through the system picker. It will not delete worlds."
        col.addView(Button(this).apply {
            text = "Confirm import"
            setOnClickListener { copyWithSaf(uri, kind, name) }
        })
        col.addView(Button(this).apply {
            text = "Cancel"
            setOnClickListener { finish() }
        })
    }

    private fun validateZip(uri: Uri): Boolean = try {
        contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zis ->
                var e = zis.nextEntry
                var n = 0
                while (e != null && n < 40) {
                    n++
                    e = zis.nextEntry
                }
                n > 0
            }
        } ?: false
    } catch (_: Exception) { false }

    private fun copyWithSaf(uri: Uri, kind: String, name: String) {
        val dest = File(getExternalFilesDir(null), "import-queue").apply { mkdirs() }
        val out = File(dest, name)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(out).use { output -> input.copyTo(output) }
        }
        Toast.makeText(this, "Queued at ${out.absolutePath}. Open the folder in Files and move it into Minecraft/games/com.mojang (${if (kind == "mcworld") "minecraftWorlds" else "resource_packs"}).", Toast.LENGTH_LONG).show()
        finish()
    }
}
