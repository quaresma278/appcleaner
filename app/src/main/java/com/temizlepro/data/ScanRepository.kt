package com.temizlepro.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.temizlepro.util.DuplicateDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class ScanRepository(private val context: Context) {
    fun scan(
        treeUri: Uri,
        includeDuplicates: Boolean,
        largeThresholdBytes: Long
    ): Flow<ScanState> = flow {
        emit(ScanState.Scanning(0, context.getString(com.temizlepro.R.string.scan_progress)))

        val contentResolver = context.contentResolver
        val root = DocumentFile.fromTreeUri(context, treeUri)
            ?: throw IllegalStateException("Klasör okunamadı")

        val items = mutableListOf<CleanItem>()
        val files = mutableListOf<DocumentFile>()
        val stack = ArrayDeque<DocumentFile>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            ensureActive()
            val current = stack.removeFirst()
            current.listFiles().forEach { child ->
                if (child.isDirectory) {
                    stack.add(child)
                } else {
                    files.add(child)
                }
            }
        }

        files.forEachIndexed { index, file ->
            ensureActive()
            val name = file.name ?: "-"
            val size = file.length()
            val lastModified = file.lastModified().takeIf { it > 0 }
            val path = file.uri.toString()
            val uri = file.uri
            val idBase = "${uri}_downloads"
            items.add(
                CleanItem(
                    id = idBase,
                    name = name,
                    sizeBytes = size,
                    lastModified = lastModified,
                    path = path,
                    category = CleanCategory.DOWNLOADS,
                    uri = uri
                )
            )
            if (size >= largeThresholdBytes) {
                items.add(
                    CleanItem(
                        id = "${uri}_large",
                        name = name,
                        sizeBytes = size,
                        lastModified = lastModified,
                        path = path,
                        category = CleanCategory.LARGE_FILES,
                        uri = uri
                    )
                )
            }
            emit(ScanState.Scanning(((index + 1) * 60) / files.size.coerceAtLeast(1), context.getString(com.temizlepro.R.string.scan_progress)))
        }

        if (includeDuplicates) {
            val candidates = files.map { it.uri to it.length() }
            val duplicates = DuplicateDetector.findDuplicates(contentResolver, candidates) { progress, total ->
                emit(ScanState.Scanning(60 + (progress * 30) / total.coerceAtLeast(1), context.getString(com.temizlepro.R.string.scan_progress)))
            }
            duplicates.values.flatten().forEach { uri ->
                val file = DocumentFile.fromSingleUri(context, uri)
                if (file != null) {
                    items.add(
                        CleanItem(
                            id = "${uri}_dup",
                            name = file.name ?: "-",
                            sizeBytes = file.length(),
                            lastModified = file.lastModified().takeIf { it > 0 },
                            path = uri.toString(),
                            category = CleanCategory.DUPLICATES,
                            uri = uri
                        )
                    )
                }
            }
        }

        items.addAll(getAppCacheItems())
        items.addAll(getAppTempItems())

        emit(ScanState.Scanning(95, context.getString(com.temizlepro.R.string.scan_progress)))
        emit(ScanState.Completed(ScanResult(items)))
    }.flowOn(Dispatchers.IO)

    suspend fun deleteItems(items: List<CleanItem>): DeleteResult = withContext(Dispatchers.IO) {
        var successCount = 0
        var failedCount = 0
        val unique = items.filter { it.deletable }.distinctBy { it.uri ?: it.path }
        unique.forEach { item ->
            val deleted = when {
                item.uri != null -> {
                    DocumentFile.fromSingleUri(context, item.uri)?.delete() ?: false
                }
                item.path != null -> {
                    File(item.path).delete()
                }
                else -> false
            }
            if (deleted) successCount += 1 else failedCount += 1
        }
        DeleteResult(successCount, failedCount)
    }

    private fun getAppCacheItems(): List<CleanItem> {
        val cacheDir = context.cacheDir
        val files = cacheDir.listFiles().orEmpty()
        return files.map { file ->
            CleanItem(
                id = "${file.absolutePath}_cache",
                name = file.name,
                sizeBytes = file.length(),
                lastModified = file.lastModified(),
                path = file.absolutePath,
                category = CleanCategory.CACHE,
                uri = null,
                deletable = true
            )
        }
    }

    private fun getAppTempItems(): List<CleanItem> {
        val tempDir = File(context.filesDir, "temp")
        if (!tempDir.exists()) return emptyList()
        return tempDir.listFiles().orEmpty().map { file ->
            CleanItem(
                id = "${file.absolutePath}_temp",
                name = file.name,
                sizeBytes = file.length(),
                lastModified = file.lastModified(),
                path = file.absolutePath,
                category = CleanCategory.TEMP,
                uri = null,
                deletable = true
            )
        }
    }
}

sealed class ScanState {
    data class Scanning(val progress: Int, val message: String) : ScanState()
    data class Completed(val result: ScanResult) : ScanState()
    data class Error(val messageRes: Int) : ScanState()
}

data class DeleteResult(val successCount: Int, val failedCount: Int)
