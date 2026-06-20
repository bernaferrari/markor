package com.bernaferrari.remarkor.util

private data class UtcDateTime(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
)

private val monthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

@Suppress("MagicNumber")
private fun epochMillisToUtc(epochMillis: Long): UtcDateTime {
    var remaining = epochMillis / 1000
    val second = (remaining % 60).toInt()
    remaining /= 60
    val minute = (remaining % 60).toInt()
    remaining /= 60
    val hour = (remaining % 24).toInt()
    var days = remaining / 24

    var year = 1970
    while (true) {
        val daysInYear = if (isLeapYear(year)) 366 else 365
        if (days < daysInYear) break
        days -= daysInYear
        year++
    }

    val monthLengths = if (isLeapYear(year)) {
        intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    } else {
        intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    }

    var month = 1
    for (length in monthLengths) {
        if (days < length) break
        days -= length
        month++
    }

    return UtcDateTime(
        year = year,
        month = month,
        day = days.toInt() + 1,
        hour = hour,
        minute = minute,
        second = second,
    )
}

private fun isLeapYear(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

private fun Int.twoDigits(): String = toString().padStart(2, '0')

fun formatTrashTimestamp(epochMillis: Long): String {
    val utc = epochMillisToUtc(epochMillis)
    return "${utc.year}" +
        utc.month.twoDigits() +
        utc.day.twoDigits() + "_" +
        utc.hour.twoDigits() +
        utc.minute.twoDigits() +
        utc.second.twoDigits()
}

fun formatExportTimestamp(epochMillis: Long = nowMillis()): String {
    val utc = epochMillisToUtc(epochMillis)
    val monthName = monthNames.getOrElse(utc.month - 1) { "Jan" }.take(3)
    return "$monthName ${utc.day}, ${utc.year} at ${utc.hour.twoDigits()}:${utc.minute.twoDigits()}"
}