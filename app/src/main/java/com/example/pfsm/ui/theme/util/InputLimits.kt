package com.example.pfsm.ui.theme.util

const val MAX_TEXT_LENGTH = 15
const val MAX_AMOUNT = 1_000_000_000.0   // 100 Cr. For 10 Cr use 100_000_000.0

fun amountExceedsMax(text: String): Boolean = (text.toDoubleOrNull() ?: 0.0) > MAX_AMOUNT