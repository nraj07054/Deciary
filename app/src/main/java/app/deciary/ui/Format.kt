package app.deciary.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val dayMonth = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
private val dayMonthYear = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
private val longDay = DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.getDefault())
private val fullDay = DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())
private val monthName = DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())
private val monthYear = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

/** "3 Nov", or "2 Jan 2027" when it's not this year. */
fun LocalDate.short(today: LocalDate = LocalDate.now()): String =
    format(if (year == today.year) dayMonth else dayMonthYear)

fun LocalDate.long(): String = format(longDay)

/** "23 April", with the year when it's not this year. */
fun LocalDate.friendly(today: LocalDate = LocalDate.now()): String =
    if (year == today.year) format(fullDay) else format(fullDay) + " " + year

fun LocalDate.monthLabel(today: LocalDate = LocalDate.now()): String =
    format(if (year == today.year) monthName else monthYear)

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

/** "today", "yesterday", "12 days ago", "3 months ago". */
fun daysAgo(at: Long, today: LocalDate = LocalDate.now()): String {
    val days = ChronoUnit.DAYS.between(at.toLocalDate(), today)
    return when {
        days <= 0 -> "today"
        days == 1L -> "yesterday"
        days < 60 -> "$days days ago"
        else -> "${days / 30} months ago"
    }
}

/** "in 3 months", "tomorrow", "in 12 days". */
fun untilText(date: LocalDate, today: LocalDate = LocalDate.now()): String {
    val days = ChronoUnit.DAYS.between(today, date)
    return when {
        days <= 0 -> "today"
        days == 1L -> "tomorrow"
        days < 45 -> "in $days days"
        days < 365 -> "in ${(days + 15) / 30} months"
        else -> "in about ${(days + 180) / 365} year" + if ((days + 180) / 365 > 1) "s" else ""
    }
}
