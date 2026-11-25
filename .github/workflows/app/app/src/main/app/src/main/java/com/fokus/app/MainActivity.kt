package com.fokus.app

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setPadding(48, 48, 48, 48)

        val title = TextView(this)
        title.text = "Fokus App"
        title.textSize = 28f
        root.addView(title)

        status = TextView(this)
        status.textSize = 16f
        status.setPadding(0, 24, 0, 24)
        root.addView(status)

        root.addView(makeButton("1. Izinkan Akses Penggunaan") {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        })
        root.addView(makeButton("2. Izinkan Tampil di Atas Aplikasi Lain") {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        })
        root.addView(makeButton("3. Mulai Penjaga Fokus") { startGuard() })
        root.addView(makeButton("Hentikan Penjaga Fokus") {
            stopService(Intent(this, MonitorService::class.java))
            Toast.makeText(this, "Penjaga fokus dihentikan", Toast.LENGTH_SHORT).show()
        })

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        val a = if (hasUsageAccess()) "aktif" else "belum"
        val b = if (Settings.canDrawOverlays(this)) "aktif" else "belum"
        status.text = "Akses Penggunaan: $a\nTampil di Atas Aplikasi: $b"
    }

    private fun makeButton(text: String, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = text
        b.setOnClickListener { onClick() }
        return b
    }

    private fun hasUsageAccess(): Boolean {
        val ops = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = ops.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun startGuard() {
        if (!hasUsageAccess()) {
            Toast.makeText(this, "Aktifkan izin nomor 1 dulu", Toast.LENGTH_LONG).show()
            return
        }
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Aktifkan izin nomor 2 dulu", Toast.LENGTH_LONG).show()
            return
        }
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        startForegroundService(Intent(this, MonitorService::class.java))
        Toast.makeText(this, "Penjaga fokus aktif", Toast.LENGTH_SHORT).show()
    }
}
