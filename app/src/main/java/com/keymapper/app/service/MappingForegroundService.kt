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
import com.keymapper.app.R
import com.keymapper.app.floating.FloatingWindowManager
import com.keymapper.app.ui.MainActivity

class MappingForegroundService : Service() {

    companion object {
        private const val TAG = "MapFgSvc"
        private const val CHANNEL_ID = "keymapper_service"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.keymapper.app.ACTION_START_FG"
        const val ACTION_STOP = "com.keymapper.app.ACTION_STOP_FG"

        @Volatile
        private var running = false
        fun isRunning() = running

        fun start(context: Context) {
            val intent = Intent(context, MappingForegroundService::class.java).setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MappingForegroundService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        running = true
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("K2ER 运行中"))
        FloatingWindowManager.getInstance(this).show()
        Log.i(TAG, "✅ ForegroundService started + floating ball")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.i(TAG, "⏹ stop action received")
                FloatingWindowManager.getInstance(this).hide()
                stopForeground(STOP_FOREGROUND_REMOVE)
                running = false
                stopSelf()
                return START_NOT_STICKY
            }
        }
        if (!FloatingWindowManager.canDrawOverlay(this)) {
            Log.w(TAG, "no overlay permission, ball won't show")
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        running = false
        FloatingWindowManager.getInstance(this).hide()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    CHANNEL_ID, "K2ER 映射服务",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "手柄映射后台运行"
                    setShowBadge(false)
                }
                nm.createNotificationChannel(ch)
            }
        }
    }

    private fun buildNotification(text: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, MappingForegroundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("K2ER 手柄映射")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .addAction(0, "停止", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
}
