package com.example.pfsm.data.datautil


import com.example.pfsm.data.entities.DailyLimitEntity
import java.time.LocalDate

fun resolveDailyLimitForDate(history: List<DailyLimitEntity>, date: LocalDate): Double {
    val dateStr = date.toString()
    return history
        .filter { it.effectiveFrom <= dateStr }
        .sortedWith(compareBy<DailyLimitEntity> { it.effectiveFrom }.thenBy { it.id })
        .lastOrNull()
        ?.amount
        ?: 0.0
}