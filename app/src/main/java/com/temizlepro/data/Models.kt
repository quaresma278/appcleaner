package com.temizlepro.data

import android.net.Uri

enum class CleanCategory {
    DOWNLOADS,
    CACHE,
    LARGE_FILES,
    DUPLICATES,
    TEMP
}

data class CleanItem(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long?,
    val path: String?,
    val category: CleanCategory,
    val uri: Uri? = null,
    val deletable: Boolean = true
)

data class StorageSummary(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long
)

data class ScanResult(
    val items: List<CleanItem>
)
