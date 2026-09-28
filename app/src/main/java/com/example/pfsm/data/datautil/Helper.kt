package com.example.pfsm.data.datautil

data class DailyTotal(
    val transactionDate: String,
    val spent: Double
)

data class MonthTotal(
    val month: String,
    val total: Double
)