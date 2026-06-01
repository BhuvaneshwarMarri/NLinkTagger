package com.smaarig.nlinktagger.data.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.smaarig.nlinktagger.data.local.NLinkDatabase
import com.smaarig.nlinktagger.data.repository.NLinkRepository
import java.io.File

class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val database = NLinkDatabase.getDatabase(applicationContext)
            val repository = NLinkRepository(database.nlinkDao())
            val backupData = repository.getBackupData()
            val json = Gson().toJson(backupData)
            
            val backupFile = File(applicationContext.getExternalFilesDir(null), "auto_backup.json")
            backupFile.writeText(json)
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
