package com.evermore.benno.workermanagetest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class DataTransferMonitorService : Service() {

    private val connectivityManager by lazy { getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    private var networkRebuild = false
    private var networkAvailable = 0
    private var networkChanged: Network? = null
    private val networkHandler = Handler(Looper.getMainLooper())

    private val notifyManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private var notifyBuild: NotificationCompat.Builder? = null
    private var notifyId = 9549113

    private val networkListener = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            networkAvailable = 0
            Log.d("DataTransferMonitorService", "The default network is now: $network")
            connectivityManager.getNetworkCapabilities(network)?.let { capabilities ->
//                Timber.e("The default network Capabilities is : $capabilities")
                val online = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                Log.d("DataTransferMonitorService", "The default network is online ? $online")
                if (online) {
                    networkAvailable = 0
                    networkHandler.removeCallbacks(checkNetworkStableRunnable)
                    networkHandler.postDelayed(checkNetworkStableRunnable, 2000)
                }
            }
        }

        override fun onLost(network: Network) {
            Log.d("DataTransferMonitorService", "The default network lost last network is $network")
            networkRebuild = true
            Handler(Looper.getMainLooper()).postDelayed({
                val activeNetwork = connectivityManager.activeNetwork
                Log.d("DataTransferMonitorService", "after lost network, current active is $activeNetwork")
                if (activeNetwork == null) {
                    WorkManager.getInstance(this@DataTransferMonitorService).cancelAllWork()
                    notifyBuild?.apply {
                        setContentTitle("Lost connection")
                        setContentText("wait for network...")
                    }
                    notifyBuild?.let {
                        notifyManager.notify(notifyId, it.build())
                    }
                }
            }, 100)
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            Log.d("DataTransferMonitorService", "The default network changed network is $network")
            val online = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            Log.d("DataTransferMonitorService", "The default network changed network is online ? $online")
            networkChanged?.let {
                networkRebuild = it != network
                networkAvailable++
                if (online) {
                    networkAvailable = 0
                    networkHandler.removeCallbacks(checkNetworkStableRunnable)
                    networkHandler.postDelayed(checkNetworkStableRunnable, 2000)
                }
            } ?: let {
                networkChanged = network
                networkRebuild = false
            }
        }
    }

    private var checkNetworkStableRunnable = kotlinx.coroutines.Runnable {
        if (networkRebuild) {
            Log.d("DataTransferMonitorService", "checkNetworkStableRunnable networkRebuild")
            networkRebuild = false
            var constraints = Constraints.Builder()
            constraints.setRequiredNetworkType(NetworkType.CONNECTED)
            var uploadMediaBuilder = OneTimeWorkRequestBuilder<TransferWorker>()
            uploadMediaBuilder.addTag("WorkerTest")
            uploadMediaBuilder.setConstraints(constraints.build())
            val uploadWork = uploadMediaBuilder.build()
            WorkManager.getInstance(this).enqueue(uploadWork)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DataTransferMonitorService", "**onStartCommand**")
        connectivityManager.registerDefaultNetworkCallback(networkListener)
        val notify = notification("test", "networkListener", "Lost connection", "wait for network...")
        notifyBuild = notify
        val notification = notify.build()
        startForeground(notifyId, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        Log.d("DataTransferMonitorService", "**onDestroy**")
        connectivityManager.unregisterNetworkCallback(networkListener)
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