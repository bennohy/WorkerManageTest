package com.evermore.benno.workermanagetest

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ContentTriggerWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        this.triggeredContentAuthorities.forEach {
            Log.d("ContentTriggerWorker", "triggeredContentAuthorities > $it")
        }
        this.triggeredContentUris.forEach {
            Log.d("ContentTriggerWorker", "triggeredContentUris > $it")
        }
        return Result.success()
    }
}