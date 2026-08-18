package com.king0929zion.openarchive.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Thread-safe formatters reused across list items and search indexing. */
object ArchiveFormatters {
    private val zone: ZoneId = ZoneId.systemDefault()
    private val feedDayFormatter = DateTimeFormatter.ofPattern("dd", Locale.US)
    private val feedMonthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.US)
    private val detailDateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA)
    private val detailTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)
    private val resultFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 · HH:mm", Locale.CHINA)
    private val searchFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd yyyy年M月d日", Locale.CHINA)

    fun feedDay(timestamp: Long): String = atZone(timestamp).format(feedDayFormatter)
    fun feedMonth(timestamp: Long): String = atZone(timestamp).format(feedMonthFormatter).uppercase(Locale.US)
    fun detailDate(timestamp: Long): String = atZone(timestamp).format(detailDateFormatter)
    fun detailTime(timestamp: Long): String = atZone(timestamp).format(detailTimeFormatter)
    fun searchDate(timestamp: Long): String = atZone(timestamp).format(searchFormatter)
    fun resultDateTime(timestamp: Long): String = atZone(timestamp).format(resultFormatter)

    private fun atZone(timestamp: Long) = Instant.ofEpochMilli(timestamp).atZone(zone)
}
