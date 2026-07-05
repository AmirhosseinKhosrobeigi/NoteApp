package io.github.amirhosseinkhosrobeigi.notes.utils

import java.util.*

class PersianDate {

    var strWeekDay: String = ""
    var strMonth: String = ""
    var day: Int = 0
    var month: Int = 0
    var year: Int = 0
    var hour: Int = 0
    var min: Int = 0
    var second: Int = 0

    init {
        setDateCalendar()
    }

    private fun setDateCalendar() {
        val calendar = GregorianCalendar(TimeZone.getTimeZone("GMT+03:30"))
        calendar.time = Date()

        hour = calendar.get(Calendar.HOUR_OF_DAY)
        min = calendar.get(Calendar.MINUTE)
        second = calendar.get(Calendar.SECOND)

        val persianYear = calendar.get(Calendar.YEAR)
        val persianMonth = calendar.get(Calendar.MONTH) + 1
        val persianDate = calendar.get(Calendar.DATE)
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)

        // تبدیل میلادی به شمسی
        val gDays = if (persianYear % 4 == 0) {
            intArrayOf(0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335)
        } else {
            intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        }

        var dayOfYear = gDays[persianMonth - 1] + persianDate

        if (dayOfYear > (if (persianYear % 4 == 0) 80 else 79)) {
            dayOfYear -= if (persianYear % 4 == 0) 80 else 79
            if (dayOfYear <= 186) {
                month = (dayOfYear - 1) / 31 + 1
                day = (dayOfYear - 1) % 31 + 1
            } else {
                dayOfYear -= 186
                month = (dayOfYear - 1) / 30 + 7
                day = (dayOfYear - 1) % 30 + 1
            }
            year = persianYear - 621
        } else {
            dayOfYear += 10
            month = (dayOfYear - 1) / 30 + 10
            day = (dayOfYear - 1) % 30 + 1
            year = persianYear - 622
        }

        // ماه
        strMonth = when (month) {
            1 -> "فروردین"
            2 -> "اردیبهشت"
            3 -> "خرداد"
            4 -> "تیر"
            5 -> "مرداد"
            6 -> "شهریور"
            7 -> "مهر"
            8 -> "آبان"
            9 -> "آذر"
            10 -> "دی"
            11 -> "بهمن"
            12 -> "اسفند"
            else -> ""
        }

        // روز هفته
        strWeekDay = when (weekDay) {
            Calendar.SATURDAY -> "شنبه"
            Calendar.SUNDAY -> "یکشنبه"
            Calendar.MONDAY -> "دوشنبه"
            Calendar.TUESDAY -> "سه‌شنبه"
            Calendar.WEDNESDAY -> "چهارشنبه"
            Calendar.THURSDAY -> "پنج‌شنبه"
            Calendar.FRIDAY -> "جمعه"
            else -> ""
        }
    }

    override fun toString(): String {
        return "$strWeekDay $day $strMonth $year - $hour:$min:$second"
    }
}
