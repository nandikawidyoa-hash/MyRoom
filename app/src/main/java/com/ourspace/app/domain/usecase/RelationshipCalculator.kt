package com.ourspace.app.domain.usecase

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class RelationshipStats(
    val totalDaysTogether: Long,
    val yearsTogether: Long,
    val monthsTogether: Long,
    val daysUntilNextAnniversary: Long,
    val nextAnniversaryNumber: Long
)

object RelationshipCalculator {

    private val curatedQuestions = listOf(
        "Momen kecil apa dari minggu ini yang membuatmu merasa paling disayang olehku?",
        "Jika kita bisa teleportasi ke satu tempat malam ini hanya berdua, ke mana kita akan pergi?",
        "Apa hal pertama yang kamu pikirkan saat pertama kali kita bertemu?",
        "Lagu apa yang menurutmu paling menggambarkan cerita kita saat ini?",
        "Kebiasaan kecilku apa yang diam-diam paling kamu sukai?",
        "Apa satu impian bersama yang ingin kita wujudkan dalam 1 tahun ke depan?",
        "Bagaimana cara terbaikku menenangkanmu saat kamu sedang lelah atau stres?",
        "Kenangan perjalanan atau kencan mana yang paling ingin kamu ulang kembali?",
        "Apa arti 'rumah' bagimu ketika kita sedang bersama?",
        "Satu hal baru apa yang ingin kita pelajari atau coba bersama bulan ini?"
    )

    fun calculateStats(
        anniversaryEpochMillis: Long,
        nowEpochMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): RelationshipStats {
        val startDate = Instant.ofEpochMilli(anniversaryEpochMillis).atZone(zoneId).toLocalDate()
        val today = Instant.ofEpochMilli(nowEpochMillis).atZone(zoneId).toLocalDate()

        val totalDays = ChronoUnit.DAYS.between(startDate, today).coerceAtLeast(0L) + 1L
        val years = ChronoUnit.YEARS.between(startDate, today).coerceAtLeast(0L)
        val months = ChronoUnit.MONTHS.between(startDate, today).coerceAtLeast(0L)

        var nextAnniversary = startDate.plusYears(years + 1L)
        if (nextAnniversary.isBefore(today)) {
            nextAnniversary = nextAnniversary.plusYears(1L)
        }
        val daysUntilNext = ChronoUnit.DAYS.between(today, nextAnniversary).coerceAtLeast(0L)
        val nextNumber = ChronoUnit.YEARS.between(startDate, nextAnniversary).coerceAtLeast(1L)

        return RelationshipStats(
            totalDaysTogether = totalDays,
            yearsTogether = years,
            monthsTogether = months,
            daysUntilNextAnniversary = daysUntilNext,
            nextAnniversaryNumber = nextNumber
        )
    }

    fun getDailyQuestionForDate(date: LocalDate = LocalDate.now()): String {
        val idx = (date.dayOfYear + date.year) % curatedQuestions.size
        return curatedQuestions[idx]
    }

    fun isUnlockedByServerTime(
        unlockAtServerMillis: Long,
        estimatedServerNowMillis: Long
    ): Boolean {
        return estimatedServerNowMillis >= unlockAtServerMillis
    }
}
