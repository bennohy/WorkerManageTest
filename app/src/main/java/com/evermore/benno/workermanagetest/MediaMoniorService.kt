package com.evermore.benno.workermanagetest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat

class MediaMoniorService : Service() {

    private val notifyManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private var notifyBuild: NotificationCompat.Builder? = null
    private var notifyId = 100720

//    val internalImageUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI
    val externlImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//    val internalObserver = MediaContentObserver(this, internalImageUri, Handler(Looper.getMainLooper()))
    val externalObserver = MediaContentObserver(this, externlImageUri, Handler(Looper.getMainLooper()))

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MediaMoniorService", "**onStartCommand**")
        val notify = notification("MediaObserver", "MediaAutoBackup", "AutoBackup", "Photos auto backup service is running...")
        notifyBuild = notify
        val notification = notify.build()
        startForeground(notifyId, notification)
//        contentResolver.registerContentObserver(internalImageUri, Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q, internalObserver)
        contentResolver.registerContentObserver(externlImageUri, Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q, externalObserver)
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        Log.d("MediaMoniorService", "**onDestroy**")
//        contentResolver.unregisterContentObserver(internalObserver)
        contentResolver.unregisterContentObserver(externalObserver)
        super.onDestroy()
    }

    private fun notification(channelId: String, channel: String, title: String, content: String): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channel, NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
            }
            notifyManager.createNotificationChannel(channel)
        }
        val smallIcon = R.mipmap.ic_launcher
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
    }
}