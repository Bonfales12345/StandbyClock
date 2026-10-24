package com.standby.clock.services

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.AlarmClock
import androidx.core.content.ContextCompat
import com.standby.clock.data.AlarmData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmRepository(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private val _alarmData = MutableStateFlow(fetchNextAlarm())
    val alarmData: StateFlow<AlarmData> = _alarmData.asStateFlow()

    private val alarmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshAlarm()
        }
    }

    init {
        try {
            val filter = IntentFilter(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.registerReceiver(
                    context,
                    alarmReceiver,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(alarmReceiver, filter)
            }
        } catch (e: Throwable) {

        }
    }

    fun refreshAlarm() {
        _alarmData.value = fetchNextAlarm()
    }

    fun openSystemClock() {
        try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Throwable) {
            try {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory("android.intent.category.APP_CLOCK")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Throwable) {

            }
        }
    }

    private fun fetchNextAlarm(): AlarmData {
        try {
            val nextAlarm = alarmManager?.nextAlarmClock
            if (nextAlarm != null) {
                val triggerTime = nextAlarm.triggerTime
                if (triggerTime > System.currentTimeMillis()) {
                    val date = Date(triggerTime)
                    val is24 = android.text.format.DateFormat.is24HourFormat(context)
                    val standardFormat = if (is24) {
                        SimpleDateFormat("HH:mm", Locale.getDefault())
                    } else {
                        SimpleDateFormat("h:mm a", Locale.US)
                    }
                    val compactFormat = if (is24) {
                        SimpleDateFormat("HH:mm", Locale.getDefault())
                    } else {
                        SimpleDateFormat("h:mma", Locale.US)
                    }
                    return AlarmData(
                        hasAlarm = true,
                        timeFormatted = standardFormat.format(date),
                        timeCompact = compactFormat.format(date)
                    )
                }
            }
        } catch (e: Throwable) {

        }
        return AlarmData(
            hasAlarm = false,
            timeFormatted = "",
            timeCompact = ""
        )
    }
}
