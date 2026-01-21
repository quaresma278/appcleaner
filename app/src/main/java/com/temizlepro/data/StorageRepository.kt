package com.temizlepro.data

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Environment
import android.os.StatFs

class StorageRepository(private val context: Context) {
    fun getStorageSummary(): StorageSummary {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        val total = stat.totalBytes
        val free = stat.availableBytes
        val used = total - free
        return StorageSummary(totalBytes = total, freeBytes = free, usedBytes = used)
    }

    fun getAppCacheBytes(): Long {
        val storageStatsManager = context.getSystemService(StorageStatsManager::class.java)
        val storageUuid = storageStatsManager.getUuidForPath(context.filesDir)
        val stats = storageStatsManager.queryStatsForPackage(storageUuid, context.packageName, context.user)
        return stats.cacheBytes
    }
}
