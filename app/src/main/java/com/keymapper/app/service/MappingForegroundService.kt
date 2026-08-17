package com.keymapper.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.keymapper.app.AppContainer
import com.keymapper.app.R
import com.keymapper.app.floating.FloatingWindowManager
import com.keymapper.app.ui.MainActivity

class MappingForegroundService : Service() {

    companion object {
        private const val TAG = "K2ER-FgSvc"
        private const val CHANNEL_ID = "k2er_channel"
        private const val NOTIFICATION_ID = 1001

        @Volatile private var running = false
        fun isRunning() = running

        fun start(context: Context) {
            val intent = Intent(context, MappingForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MappingForegroundService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        running = true
        AppContainer.getOrCreate(this)
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification("K2ER 运行中"))
        InputMonitor.start(this)
        FloatingWindowManager.getInstance(this).show()
        Log.i(TAG, "✅ K2ER ForegroundService started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopSelf(); return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        InputMonitor.stop()
        FloatingWindowManager.getInstance(this).hide()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(NotificationChannel(
                    CHANNEL_ID, "K2ER 映射服务", NotificationManager.IMPORTANCE_LOW
                ).apply { description = "手柄映射后台运行" })
            }
        }
    }

    private fun buildNotification(text: String): Notification {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stopPi = PendingIntent.getService(this, 1,
            Intent(this, MappingForegroundService::class.java).setAction("STOP"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("K2ER 手柄映射")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pi)
            .addAction(0, "停止", stopPi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
}
