package com.group1.meetme

import java.util.Calendar


object HolidayUtils {

    val holidayDates: List<Calendar> = listOf(
        Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 1) },
        Calendar.getInstance().apply { set(2025, Calendar.MARCH, 21) },
        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 18) },
        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 21) },
        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 27) },

        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 28) },
        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 29) },
        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 30) },
        Calendar.getInstance().apply { set(2025, Calendar.MAY, 1) },
        Calendar.getInstance().apply { set(2025, Calendar.MAY, 2) },

        Calendar.getInstance().apply { set(2025, Calendar.JUNE, 16) },
        Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 9) },

        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 22) },
        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 23) },
        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 24) },
        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 25) },
        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 26) },

        Calendar.getInstance().apply { set(2025, Calendar.DECEMBER, 16) },
        Calendar.getInstance().apply { set(2025, Calendar.DECEMBER, 25) },
        Calendar.getInstance().apply { set(2025, Calendar.DECEMBER, 26) },

        // Saturdays and Sundays
        Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 1) },
        Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 2) },
        Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 8) },
        Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 9) },
        Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 15) },
        Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 16) },
        Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 22) },
        Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 23) },

        Calendar.getInstance().apply { set(2025, Calendar.MARCH, 1) },
        Calendar.getInstance().apply { set(2025, Calendar.MARCH, 2) },
        Calendar.getInstance().apply { set(2025, Calendar.MARCH, 8) },
        Calendar.getInstance().apply { set(2025, Calendar.MARCH, 9) },
        Calendar.getInstance().apply { set(2025, Calendar.MARCH, 15) },
        Calendar.getInstance().apply { set(2025, Calendar.MARCH, 16) },
        Calendar.getInstance().apply { set(2025, Calendar.MARCH, 22) },
        Calendar.getInstance().apply { set(2025, Calendar.MARCH, 23) },
        Calendar.getInstance().apply { set(2025, Calendar.MARCH, 29) },
        Calendar.getInstance().apply { set(2025, Calendar.MARCH, 30) },

        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 5) },
        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 6) },
        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 12) },
        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 13) },
        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 19) },
        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 20) },
        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 26) },
        Calendar.getInstance().apply { set(2025, Calendar.APRIL, 27) },

        Calendar.getInstance().apply { set(2025, Calendar.MAY, 3) },
        Calendar.getInstance().apply { set(2025, Calendar.MAY, 4) },
        Calendar.getInstance().apply { set(2025, Calendar.MAY, 10) },
        Calendar.getInstance().apply { set(2025, Calendar.MAY, 11) },
        Calendar.getInstance().apply { set(2025, Calendar.MAY, 17) },
        Calendar.getInstance().apply { set(2025, Calendar.MAY, 18) },
        Calendar.getInstance().apply { set(2025, Calendar.MAY, 24) },
        Calendar.getInstance().apply { set(2025, Calendar.MAY, 25) },
        Calendar.getInstance().apply { set(2025, Calendar.MAY, 31) },

        Calendar.getInstance().apply { set(2025, Calendar.JUNE, 7) },
        Calendar.getInstance().apply { set(2025, Calendar.JUNE, 8) },
        Calendar.getInstance().apply { set(2025, Calendar.JUNE, 14) },
        Calendar.getInstance().apply { set(2025, Calendar.JUNE, 15) },
        Calendar.getInstance().apply { set(2025, Calendar.JUNE, 21) },
        Calendar.getInstance().apply { set(2025, Calendar.JUNE, 22) },
        Calendar.getInstance().apply { set(2025, Calendar.JUNE, 28) },
        Calendar.getInstance().apply { set(2025, Calendar.JUNE, 29) },

        Calendar.getInstance().apply { set(2025, Calendar.JULY, 5) },
        Calendar.getInstance().apply { set(2025, Calendar.JULY, 6) },
        Calendar.getInstance().apply { set(2025, Calendar.JULY, 12) },
        Calendar.getInstance().apply { set(2025, Calendar.JULY, 13) },
        Calendar.getInstance().apply { set(2025, Calendar.JULY, 19) },
        Calendar.getInstance().apply { set(2025, Calendar.JULY, 20) },
        Calendar.getInstance().apply { set(2025, Calendar.JULY, 26) },
        Calendar.getInstance().apply { set(2025, Calendar.JULY, 27) },

        Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 2) },
        Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 3) },
        Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 9) },
        Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 10) },
        Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 16) },
        Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 17) },
        Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 23) },
        Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 24) },
        Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 30) },
        Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 31) },

        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 6) },
        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 7) },
        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 13) },
        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 14) },
        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 20) },
        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 21) },
        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 27) },
        Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 28) },

        Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 4) },
        Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 5) },
        Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 11) },
        Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 12) },
        Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 18) },
        Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 19) },
        Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 25) },
        Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 26) },

        Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 1) },
        Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 2) },
        Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 8) },
        Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 9) },
        Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 15) },
        Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 16) },
        Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 22) },
        Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 23) },
        Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 29) },
        Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 30) },

        Calendar.getInstance().apply { set(2026, Calendar.JANUARY, 1) }
    )

}
