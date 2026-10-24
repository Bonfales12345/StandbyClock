package com.standby.clock.data

data class AlarmData(
    val hasAlarm: Boolean = true,
    val timeFormatted: String = "6:30 AM",
    val timeCompact: String = "6:30AM"
)
