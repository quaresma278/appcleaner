package com.temizlepro.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

object FormatUtils {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR"))

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        return String.format(Locale("tr", "TR"), "%.1f %s", value, units[digitGroups - 1])
    }

    fun formatDate(timestamp: Long?): String {
        return timestamp?.let { dateFormat.format(Date(it)) } ?: "-"
    }
}
