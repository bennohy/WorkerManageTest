package com.evermore.benno.workermanagetest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.*
import com.evermore.benno.workermanagetest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData("WorkerTest").observe(this, Observer { workinfos ->
            workinfos.forEach {
                Log.d("WorkTest", "workinfo->$it")
            }
        })
//        WorkManager.getInstance(this).enqueueUniqueWork("WorkerTest", ExistingWorkPolicy.KEEP, uploadWork)

//        Handler(Looper.getMainLooper()).postDelayed({
//            WorkManager.getInstance(this).cancelUniqueWork("WorkerTest")
//        }, 5000)

        binding.meter.setOnClickListener {
            startForegroundService(Intent(this, DataTransferMonitorService::class.java))
            var constraints = Constraints.Builder()
            constraints.setRequiredNetworkType(NetworkType.METERED)

            var uploadMediaBuilder = OneTimeWorkRequestBuilder<TransferWorker>()
            uploadMediaBuilder.addTag("WorkerTest")
            uploadMediaBuilder.setConstraints(constraints.build())
            val uploadWork = uploadMediaBuilder.build()
            WorkManager.getInstance(this).enqueue(uploadWork)
        }

        binding.unmeter.setOnClickListener {
            var constraints = Constraints.Builder()
            constraints.setRequiredNetworkType(NetworkType.UNMETERED)

            var uploadMediaBuilder = OneTimeWorkRequestBuilder<TransferWorker>()
            uploadMediaBuilder.addTag("WorkerTest")
            uploadMediaBuilder.setConstraints(constraints.build())
            val uploadWork = uploadMediaBuilder.build()
            WorkManager.getInstance(this).enqueue(uploadWork)

        }

        binding.cancel.setOnClickListener {
            WorkManager.getInstance(this).cancelAllWork()
        }
    }
}