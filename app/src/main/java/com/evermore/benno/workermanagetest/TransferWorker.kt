package com.evermore.benno.workermanagetest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request

class TransferWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    private val notifyManager by lazy { applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private var notifyBuild: NotificationCompat.Builder? = null
    private var notifyId = 9549113

    override suspend fun doWork(): Result {
        Log.d("WorkerTest", "doWork")
        var notify = notification("test", "networkListener", "TransferWorker", "transfer...")
        notifyBuild = notify
        notifyManager.notify(notifyId, notify.build())
        for (i in 0..15) {
            Log.d("WorkerTest", "count>$i")
            if (isStopped) {
                Log.d("WorkerTest", "isStopped")
                break
            }
            notify.setContentText("transfer $i")
            notifyManager.notify(notifyId, notify.build())
            Thread.sleep(1000)
        }
//        for (i in 0..10) {
//            Log.d("WorkerTest", "count 2>$i")
//            Thread.sleep(1000)
//        }
        val request = Request.Builder().url("https://www.gamer.com.tw/").method(
            "GET", null
        ).build()
        val response = OkHttpClient.Builder().build().newCall(request).execute()
        Log.d("WorkerTest", "http >$response")
        Log.d("WorkerTest", "count finish")
        if (!isStopped) {
            Log.d("WorkerTest", "worker complete")
            applicationContext.stopService(Intent(applicationContext, DataTransferMonitorService::class.java))
        }
        return Result.success()
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