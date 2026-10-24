package com.standby.clock.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

private val DAYS = arrayOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
private val MONTHS = arrayOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

data class TimeState(
    val hour12: Int = 12,
    val hour24: Int = 12,
    val minute: Int = 30,
    val second: Int = 0,
    val millis: Long = 0L,
    val isAm: Boolean = true,
    val dayOfWeekUpper: String = "WED",
    val dayOfMonth: String = "23",
    val monthUpper: String = "OCT",
    val hourString12: String = "12",
    val hourString24: String = "12",
    val minuteString: String = "30",
    val secondSweepFraction: Float = 0f
)

class TimeManager(private val scope: CoroutineScope) {
    private val _timeState = MutableStateFlow(createTimeState())
    val timeState: StateFlow<TimeState> = _timeState.asStateFlow()

    init {
        scope.launch(Dispatchers.Default) {
            while (true) {
                try {
                    _timeState.value = createTimeState()
                } catch (e: Throwable) {

                }
                delay(50)
            }
        }
    }

    private fun createTimeState(): TimeState {
        val calendar = Calendar.getInstance()
        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val hour12Raw = calendar.get(Calendar.HOUR)
        val hour12 = if (hour12Raw == 0) 12 else hour12Raw
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val millisInSecond = calendar.get(Calendar.MILLISECOND)
        val isAm = calendar.get(Calendar.AM_PM) == Calendar.AM

        val dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val dayOfWeek = if (dayIndex in DAYS.indices) DAYS[dayIndex] else "WED"
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val monthIndex = calendar.get(Calendar.MONTH)
        val month = if (monthIndex in MONTHS.indices) MONTHS[monthIndex] else "OCT"

        val sweepFraction = (second * 1000 + millisInSecond) / 60000f

        return TimeState(
            hour12 = hour12,
            hour24 = hour24,
            minute = minute,
            second = second,
            millis = calendar.timeInMillis,
            isAm = isAm,
            dayOfWeekUpper = dayOfWeek,
            dayOfMonth = dayOfMonth,
            monthUpper = month,
            hourString12 = hour12.toString(),
            hourString24 = String.format(Locale.US, "%02d", hour24),
            minuteString = String.format(Locale.US, "%02d", minute),
            secondSweepFraction = sweepFraction
        )
    }
}
