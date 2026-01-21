package com.temizlepro.util

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.security.MessageDigest

object DuplicateDetector {
    suspend fun hashForUri(contentResolver: ContentResolver, uri: Uri): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        contentResolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArray(8192)
            var read = input.read(buffer)
            while (read > 0) {
                ensureActive()
                digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        } ?: throw IllegalStateException("Dosya açılamadı")
        digest.digest().joinToString("") { "%02x".format(it) }
    }

    suspend fun findDuplicates(
        contentResolver: ContentResolver,
        entries: List<Pair<Uri, Long>>,
        onProgress: suspend (Int, Int) -> Unit
    ): Map<String, List<Uri>> = withContext(Dispatchers.IO) {
        val grouped = entries.groupBy { it.second }.filter { it.value.size > 1 }
        val result = mutableMapOf<String, MutableList<Uri>>()
        val candidates = grouped.values.flatten()
        val total = candidates.size
        var index = 0
        for (entry in candidates) {
            ensureActive()
            val hash = hashForUri(contentResolver, entry.first)
            result.getOrPut(hash) { mutableListOf() }.add(entry.first)
            index += 1
            onProgress(index, total)
        }
        result.filterValues { it.size > 1 }
    }
}
