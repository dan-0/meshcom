package me.danlowe.meshcommunicator.util.helper.time

import android.content.Context
import android.text.format.DateFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.*

class TimeFormatter(
    @ApplicationContext private val context: Context
) {

    /**
     * Note, this optimizes setting the pattern, but won't update 24 hour time format with
     * context changes while the app is running.
     */
    private val formatter: DateTimeFormatter
        get() = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
            FormatStyle.MEDIUM,
            FormatStyle.MEDIUM,
            IsoChronology.INSTANCE,
            Locale.getDefault()
        ).let { format ->

            val pattern = if (DateFormat.is24HourFormat(context)) {
                // remove/replace non-24 hour formatting
                format.replace("h", "H")
                    .replace("a", "").trim()
            } else {
                format
            }

            DateTimeFormatter.ofPattern(pattern)
        }

    fun instantToMediumLocalizedDateTime(instant: Instant): String {
        return formatter.format(
            LocalDateTime.ofInstant(
                instant, ZoneId.systemDefault()
            )
        )
    }

}