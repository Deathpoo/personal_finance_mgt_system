package com.example.pfsm.ui.theme.util


import kotlin.math.abs


fun Double.toReadableAmount(): String {
    val absValue = abs(this)
    val sign = if (this < 0) "-" else ""

    val (divided, suffix) = when {
        absValue >= 1_00_00_000.0 -> absValue / 1_00_00_000.0 to "Cr"
        absValue >= 1_00_000.0 -> absValue / 1_00_000.0 to "L"
        absValue >= 1_000.0 -> absValue / 1_000.0 to "K"
        else -> return "$sign${"%,.0f".format(absValue)}"
    }

    val formatted = if (divided % 1.0 == 0.0) {
        "%.0f".format(divided)
    } else {
        "%.2f".format(divided).trimEnd('0').trimEnd('.')
    }

    return "$sign$formatted$suffix"
}

fun Double.toTransactionReadableAmount(): String {
    val absValue = abs(this)
    val sign = if (this < 0) "-" else ""

    val (divided, suffix) = when {
        absValue >= 1_00_00_000.0 -> absValue / 1_00_00_000.0 to "Cr"
        absValue >= 1_00_000.0 -> absValue / 1_00_000.0 to "L"
        else -> return "$sign${"%,.0f".format(absValue)}"
    }

    val formatted = if (divided % 1.0 == 0.0) {
        "%.0f".format(divided)
    } else {
        "%.2f".format(divided).trimEnd('0').trimEnd('.')
    }

    return "$sign$formatted$suffix"
}