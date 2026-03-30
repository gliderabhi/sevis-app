package com.sevis.app.presentation.util

import kotlin.math.round

/** Format a Double as a decimal string with [decimals] places and thousands separators.
 *  Works in KMP common — avoids String.format which is internal in common stdlib. */
fun Double.fmtRs(decimals: Int = 2): String {
    val neg = this < 0
    val abs = if (neg) -this else this
    val intPart = abs.toLong()
    val frac = round((abs - intPart) * 100).toLong().coerceIn(0, 99)
    val intStr = buildString {
        intPart.toString().reversed().forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append(',')
            append(c)
        }
    }.reversed()
    return buildString {
        if (neg) append('-')
        append(intStr)
        if (decimals > 0) append('.').append(frac.toString().padStart(decimals, '0'))
    }
}
