package com.fokus.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class FocusActivity : Activity() {
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setPadding(64, 64, 64, 64)
        root.setBackgroundColor(Color.parseColor("#12202F"))

        val title = TextView(this)
        title.text = "Fokus dulu! 🎯"
        title.textSize = 30f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        root.addView(title)

        val msg = TextView(this)
        msg.text = "Tugasmu menunggu. Selesaikan dulu, lalu scroll lagi nanti."
        msg.textSize = 18f
        msg.setTextColor(Color.LTGRAY)
        msg.gravity = Gravity.CENTER
        msg.setPadding(0, 32, 0, 32)
        root.addView(msg)

        val countdown = TextView(this)
        countdown.textSize = 22f
        countdown.setTextColor(Color.WHITE)
        countdown.gravity = Gravity.CENTER
        root.addView(countdown)

        val back = Button(this)
        back.text = "Kembali ke tugas"
        back.setOnClickListener { goHome() }
        root.addView(back)

        val open = Button(this)
        open.text = "Saya butuh buka"
        open.isEnabled = false
        open.setOnClickListener {
            MonitorService.allowedUntil = System.currentTimeMillis() + 10 * 60 * 1000
            finish()
        }
        root.addView(open)

        setContentView(root)

        timer = object : CountDownTimer(15000, 1000) {
            override fun onTick(ms: Long) {
                countdown.text = "Tombol buka aktif dalam ${ms / 1000 + 1} detik"
            }

            override fun onFinish() {
                countdown.text = "Kamu boleh buka (10 menit)"
                open.isEnabled = true
            }
        }.start()
    }

    private fun goHome() {
        val i = Intent(Intent.ACTION_MAIN)
        i.addCategory(Intent.CATEGORY_HOME)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
        finish()
    }

    override fun onBackPressed() {
        goHome()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
