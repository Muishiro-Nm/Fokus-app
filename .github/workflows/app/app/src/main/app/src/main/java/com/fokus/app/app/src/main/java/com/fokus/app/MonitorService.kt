package com.fokus.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class MonitorService : Service() {

    companion object {
        @Volatile
        var allowedUntil = 0L

        val watched = setOf(
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.instagram.android",
            "com.google.android.youtube",
            "com.facebook.katana"
        )
    }

    private val handler = Handler(Looper.getMainLooper())
    private var current = ""
    private var lastEventTime = 0L

    private val loop = object : Runnable {
        override fun run() {
            check()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("fokus", "Penjaga Fokus", NotificationManager.IMPORTANCE_LOW)
        )
        val n = Notification.Builder(this, "fokus")
            .setContentTitle("Penjaga Fokus aktif")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .build()
        startForeground(1, n)

        lastEventTime = System.currentTimeMillis()
        handler.removeCallbacks(loop)
        handler.post(loop)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(loop)
        super.onDestroy()
    }

    private fun check() {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(now - 5000, now)
        val e = UsageEvents.Event()
        var latest: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            if (e.timeStamp > lastEventTime && e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                latest = e.packageName
                lastEventTime = e.timeStamp
            }
        }
        if (latest != null && latest != current) {
            current = latest
            if (watched.contains(latest) && now > allowedUntil) {
                val i = Intent(this, FocusActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            }
        }
    }
}
