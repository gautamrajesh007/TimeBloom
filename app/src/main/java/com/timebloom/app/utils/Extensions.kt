package com.timebloom.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toFormattedDateTime(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.daysAgo(): Int {
    val diff = System.currentTimeMillis() - this
    return (diff / (1000 * 60 * 60 * 24)).toInt()
}