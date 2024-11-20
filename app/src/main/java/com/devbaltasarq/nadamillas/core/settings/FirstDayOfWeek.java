// NadaMillas (c) 2019-2023/24 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core.settings;


import android.util.Log;

import java.util.Calendar;


public enum FirstDayOfWeek {
    MONDAY(Calendar.MONDAY), SUNDAY(Calendar.SUNDAY);

    private static String LOG_TAG = FirstDayOfWeek.class.getSimpleName();

    FirstDayOfWeek(int value)
    {
        this.value = value;
    }

    /** @return the equivalent value of the java.util.Calendar. */
    public int getCalendarValue()
    {
        return this.value;
    }

    /** @return the corresponding enum value, given its position. */
    public static FirstDayOfWeek fromOrdinal(int pos)
    {
        final FirstDayOfWeek[] ALL_DAYS = FirstDayOfWeek.values();
        FirstDayOfWeek toret = getDefault();

        if ( pos >= 0
          && pos < ALL_DAYS.length )
        {
            toret = ALL_DAYS[ pos ];
        } else {
            Log.e( LOG_TAG, "FirstDayOfWeek.fromOrdinal(): out of bounds: " + pos );
        }

        return toret;
    }

    /** Builds a new FirstDayOfWeek from a calendar value.
     * @param value a Calendar constant, such as Calendar.MONDAY.
     * @return the corresponding enum value, honoring the given calendar value.
     */
    public static FirstDayOfWeek fromCalendarValue(int value)
    {
        FirstDayOfWeek toret = FirstDayOfWeek.getDefault();

        if ( value == Calendar.SUNDAY ) {
            toret = FirstDayOfWeek.SUNDAY;
        }

        return toret;
    }

    /** @return the default value. */
    public static FirstDayOfWeek getDefault()
    {
        return MONDAY;
    }

    private final int value;
}
