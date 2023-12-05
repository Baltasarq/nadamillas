// NadaMillas (c) 2019/22 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core;

import android.util.Log;

import androidx.core.util.Pools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;


/** Represents the possible settings of the app. */
public class Settings {
    public static final String LOG_TAG = Settings.class.getSimpleName();

    public enum PoolLength {
        P25(25), P50(50), P100(100);

        PoolLength(int d)
        {
            this.distance = d;
        }

        /** @return the length corresponding to this enum constant. */
        public int getLength()
        {
            return this.distance;
        }

        @Override
        public String toString()
        {
            return String.valueOf( this.distance );
        }

        /** Converts an int to its corresponding PoolLength.
          * @param length the length, as an int.
          * @return the corresponding PoolLength object.
          */
        public static PoolLength fromLength(int length)
        {
            PoolLength toret = PoolLength.P25;

            switch( length ) {
                case 25:
                    toret = PoolLength.P25;
                    break;
                case 50:
                    toret = PoolLength.P50;
                    break;
                case 100:
                    toret = PoolLength.P100;
                    break;
                default:
                    Log.e( LOG_TAG, "no PoolLength for: " + length );
            }

            return toret;
        }

        /** @return a collection with the values of PoolLength, as string. */
        public static List<String> toStringList()
        {
            if ( stringList == null ) {
                final ArrayList<String> toret =
                        new ArrayList<>( PoolLength.values().length );

                for(PoolLength pl: values()) {
                    toret.add( pl.toString() );
                }

                stringList = toret;
            }


            return stringList;
        }

        private final int distance;
        private static List<String> stringList = null;
    }

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
      * @param defaultPoolLength the preferred length of the pool.
      */
    private Settings(DistanceUnits units, FirstDayOfWeek firstDayOfWeek, PoolLength defaultPoolLength)
    {
        this.units = units;
        this.firstDayOfWeek = firstDayOfWeek;
        this.defaultPoolLength = defaultPoolLength;
    }

    /** @return the distance units to be used. */
    public DistanceUnits getDistanceUnits()
    {
        return this.units;
    }

    /** @return the default or favourite pool length */
    public PoolLength getDefaultPoolLength()
    {
        return this.defaultPoolLength;
    }

    /** Sets the default or favourite pool length.
      * @param poolLength the new pool length;
      */
    public void setDefaultPoolLength(PoolLength poolLength)
    {
        this.defaultPoolLength = poolLength;
    }

    /** Changes the units for distance.
      * @param distanceUnits the new units to use.
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
                                "units: %s, first day of week: %s, default pool length: %s",
                                this.getDistanceUnits(),
                                this.getFirstDayOfWeek().toString(),
                                this.getDefaultPoolLength().toString() );
    }

    public static Settings get()
    {
        if ( settings == null ) {
            Log.e( LOG_TAG, "tried to get unbuilt settings!!" );
            System.exit( -1 );
        }

        return settings;
    }

    public static Settings createFrom(
                                        DistanceUnits distanceUnits,
                                        FirstDayOfWeek firstDayOfWeek,
                                        PoolLength defaultPoolLength)
    {
        settings = new Settings( distanceUnits, firstDayOfWeek, defaultPoolLength );
        return settings;
    }

    private DistanceUnits units;
    private FirstDayOfWeek firstDayOfWeek;
    private PoolLength defaultPoolLength;
    private static Settings settings;
}
