// NadaMillas (c) 2019/22 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core;

import android.util.Log;

import java.util.Calendar;
import java.util.Locale;


/** Represents the possible settings of the app. */
public class Settings {
    public static final String LOG_TAG = Settings.class.getSimpleName();
    public enum DistanceUnits { km, mi;

        /** @return the corresponding enum value, given its position. */
        public static DistanceUnits fromOrdinal(int pos)
        {
            return DistanceUnits.values()[ pos ];
        }
    }

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

    /** Creates a new settings object with the given values.
      * @param units the DistanceUnits to be used.
      * @param firstDayOfWeek the first day of week, as a Calendar constant.
      */
    private Settings(DistanceUnits units, FirstDayOfWeek firstDayOfWeek)
    {
        this.units = units;
        this.firstDayOfWeek = firstDayOfWeek;
    }

    /** @return get the distance units to be used. */
    public DistanceUnits getDistanceUnits()
    {
        return this.units;
    }

    /** Changes the units for distance.
      * @param distanceUnits
      */
    public void setDistanceUnits(DistanceUnits distanceUnits)
    {
        this.units = distanceUnits;
    }

    /** @return the given meters as kilometers. */
    public double toKm(int meters)
    {
        return (double) meters / 1000f;
    }

    /** @return the given yards as nautical miles. */
    public double toMi(int yards)
    {
        return (double) yards / 1650f;
    }

    /** @param d a given distance (as meters or yards)
      * @return the same distance in km or mi, depending on settings.
      */
    public double toUnits(int d)
    {
        double toret = this.toKm( d );

        if ( this.getDistanceUnits() == Settings.DistanceUnits.mi ) {
            toret = this.toMi( d );
        }

        return toret;
    }

    /** @param d a given distance (as meters or yards)
      * @return the same distance in km or mi, depending on settings, as a string.
      */
    public String toUnitsAsString(int d)
    {
        double value = this.toUnits( d );

        return String.format( Locale.getDefault(), "%7.2f", value );
    }

    /** @return the first day of week. */
    public FirstDayOfWeek getFirstDayOfWeek()
    {
        return this.firstDayOfWeek;
    }

    /** Changes the first day of week.
      * @param firstDayOfWeek the first day of week, as a Calendar constant.
      */
    public void setFirstDayOfWeek(FirstDayOfWeek firstDayOfWeek)
    {
        this.firstDayOfWeek = firstDayOfWeek;
    }

    @Override
    public String toString()
    {
        return String.format( Locale.getDefault(),
                                "units: %s",
                                this.getDistanceUnits() );
    }

    public static Settings get()
    {
        if ( settings == null ) {
            Log.e( LOG_TAG, "tried to get unbuilt settings!!" );
            System.exit( -1 );
        }

        return settings;
    }

    public static Settings createFrom(DistanceUnits distanceUnits, FirstDayOfWeek firstDayOfWeek)
    {
        settings = new Settings( distanceUnits, firstDayOfWeek );
        return settings;
    }

    private DistanceUnits units;
    private static Settings settings;
    private FirstDayOfWeek firstDayOfWeek;
}
