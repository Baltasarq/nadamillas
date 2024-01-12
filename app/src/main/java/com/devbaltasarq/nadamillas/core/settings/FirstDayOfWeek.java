// NadaMillas (c) 2019-2024-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core.settings;


import java.util.Calendar;


public enum FirstDayOfWeek {
    MONDAY(Calendar.MONDAY), SUNDAY(Calendar.SUNDAY);

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
        return FirstDayOfWeek.values()[ pos ];
    }

    /** Builds a new FirstDayOfWeek from a calendar value.
     * @param value a Calendar constant, such as Calendar.MONDAY.
     * @return the corresponding enum value, honoring the given calendar value.
     */
    public static FirstDayOfWeek fromCalendarValue(int value)
    {
        FirstDayOfWeek toret = FirstDayOfWeek.MONDAY;

        if ( value == Calendar.SUNDAY ) {
            toret = FirstDayOfWeek.SUNDAY;
        }

        return toret;
    }

    private final int value;
}
