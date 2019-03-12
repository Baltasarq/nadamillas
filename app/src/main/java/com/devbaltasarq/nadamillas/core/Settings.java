// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.core;

import android.util.Log;

import java.util.Locale;

/** Represents the possible settings of the app. */
public class Settings {
    public static final String LOG_TAG = Settings.class.getSimpleName();
    public enum DistanceUnits { km, mi };

    /** Creates a new settings object with the given values.
      * @param units the DistanceUnits to be used.
      */
    private Settings(DistanceUnits units)
    {
        this.units = units;
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
        return (double) meters / 1000;
    }

    /** @return the given yards as nautical miles. */
    public double toMi(int yards)
    {
        return (double) yards / 1852;
    }

    /** @return the given distance in km or mi, depending on settings. */
    public double toUnits(int d)
    {
        double toret = this.toKm( d );

        if ( this.getDistanceUnits() == Settings.DistanceUnits.mi ) {
            toret = this.toMi( d );
        }

        return toret;
    }

    /** @return the given distance as string, as km or mi, depending on settings. */
    public String toUnitsAsString(int d)
    {
        double value = this.toUnits( d );

        return String.format( Locale.getDefault(), "%7.2f", value );
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

    public static Settings createFrom(DistanceUnits distanceUnits)
    {
        settings = new Settings( distanceUnits );
        return settings;
    }

    private DistanceUnits units;
    private static Settings settings;
}
