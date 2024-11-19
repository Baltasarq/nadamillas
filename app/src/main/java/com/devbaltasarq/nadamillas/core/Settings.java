// NadaMillas (c) 2019-2024-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core;


import android.util.Log;

import com.devbaltasarq.nadamillas.core.session.Distance;
import com.devbaltasarq.nadamillas.core.settings.FirstDayOfWeek;
import com.devbaltasarq.nadamillas.core.settings.PoolLength;

import java.util.Locale;


/** Represents the possible settings of the app. */
public class Settings {
    private static final String LOG_TAG = Settings.class.getSimpleName();

    /** Creates a new settings object with the given values.
      * @param units the DistanceUnits to be used.
      * @param firstDayOfWeek the first day of week, as a Calendar constant.
      * @param defaultPoolLength the preferred length of the pool.
      */
    private Settings(Distance.Units units, FirstDayOfWeek firstDayOfWeek, PoolLength defaultPoolLength)
    {
        this.units = units;
        this.firstDayOfWeek = firstDayOfWeek;
        this.defaultPoolLength = defaultPoolLength;
    }

    /** @return the distance units to be used. */
    public Distance.Units getDistanceUnits()
    {
        return this.units;
    }

    /** Changes the units for distance.
     * @param distanceUnits the new units to use.
     */
    public void setDistanceUnits(Distance.Units distanceUnits)
    {
        this.units = distanceUnits;
    }

    /** @return the default or favourite pool length */
    public PoolLength getPoolLength()
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
                                this.getPoolLength().toString() );
    }

    public static Settings createFrom(
                                        Distance.Units distanceUnits,
                                        FirstDayOfWeek firstDayOfWeek,
                                        PoolLength poolLength)
    {
        if ( distanceUnits == null ) {
            distanceUnits = Distance.Units.getDefault();
            Log.e( LOG_TAG, "creating Settings: distanceUnits was null" );
        }

        if ( firstDayOfWeek == null ) {
            firstDayOfWeek = FirstDayOfWeek.getDefault();
            Log.e( LOG_TAG, "creating Settings: firstDayOfWeek was null" );
        }

        if ( poolLength == null ) {
            poolLength = PoolLength.getDefault();
            Log.e( LOG_TAG, "creating Settings: poolLength was null" );
        }

        return new Settings( distanceUnits, firstDayOfWeek, poolLength );
    }

    private Distance.Units units;
    private FirstDayOfWeek firstDayOfWeek;
    private PoolLength defaultPoolLength;
}
