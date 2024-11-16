// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core.session;


import android.util.Log;

import java.util.Locale;

public final class Distance {
    /** Distance units. */
    public enum Units { km, mi;
        private static final String LOG_TAG = Units.class.getSimpleName();

        /** @return the corresponding enum value, given its position. */
        public static Units fromOrdinal(int pos)
        {
            final Units[] ALL_UNITS = Units.values();
            Units toret = getDefault();

            if ( pos >= 0
              && pos < ALL_UNITS.length )
            {
                toret = Units.values()[ pos ];
            } else {
                Log.e( LOG_TAG, "Units.fromOrdinal(): out of bounds: " + pos );
            }

            return toret;
        }

        /** Converts a distance in meters to kilometers.
         * @param meters the meters to convert.
         * @return the given meters as kilometers.
         */
        public static double kmFromM(int meters)
        {
            return (double) meters / 1000f;
        }

        /** Converts a distance in kilometers to meters.
         * @param km the meters to convert.
         * @return the given kilometers as meters.
         */
        public static int mFromKm(int km)
        {
            return km * 1000;
        }

        /** Converts yards to miles.
         * @param yards the yards to convert.
         * @return the given yards as nautical miles.
         */
        public static double miFromYd(int yards)
        {
            return (double) yards / 1650f;
        }

        /** Converts miles to yards.
         * @param yards the miles to convert.
         * @return the given nautical miles as yards.
         */
        public static int ydFromMi(int yards)
        {
            return yards * 1650;
        }

        /** @return the default unit. */
        public static Units getDefault()
        {
            return km;
        }
    }

    /** Create a new distance.
      * @param distanceInBasicUnits a distance in basic units.
      */
    public Distance(int distanceInBasicUnits, Units units)
    {
        this.distanceInBasicUnits = distanceInBasicUnits;
        this.units = units;
    }

    /** @return the actual value (basic units). */
    public int getValue()
    {
        return this.distanceInBasicUnits;
    }

    /** @return the units for this distance. */
    public Units getUnits()
    {
        return this.units;
    }

    /** Converts the distance to the distance in the thousands.
      * @return the same distance in km or mi, depending on settings.
      */
    public double toThousandUnits()
    {
        double toret = Units.kmFromM( this.getValue() );

        if ( this.getUnits() == Units.mi ) {
            toret = Units.miFromYd( this.getValue() );
        }

        return toret;
    }

    /** Converts the distance to the distance in the thousands.
      * @return the same distance in km or mi, depending on settings, as String.
      */
    public String toThousandUnitsAsString()
    {
        return String.format( Locale.getDefault(),
                            "%04.2f", this.toThousandUnits() );
    }

    @Override
    public String toString()
    {
        return this.toThousandUnitsAsString()
                + " " + this.getUnits().toString() + ".";
    }

    /** Creates a new distance object, from a distance in thousands.
      * @param distanceInThousandUnits an integer, for instance, 1.5.
      * @param units a given unit, for instance meters/km.
      * @return a new Distance object, following the example, of 1500m.
      */
    public static Distance fromThousandUnits(int distanceInThousandUnits, Units units)
    {
        int d = Units.mFromKm( distanceInThousandUnits );

        if ( units == Units.mi ) {
            d = Units.ydFromMi( distanceInThousandUnits );
        }

        return new Distance( d, units );
    }

    private final Units units;
    private final int distanceInBasicUnits;


    public static class Fmt {
        /** Used in the format() static methods. */
        public enum UnitsUse {
            NO_UNITS, UNITS
        }

        /** Formats a distance in basic units to a string.
         * @param units the distance units.
         * @param distanceInBasicUnits the given distance.
         * @param unitsUse use of the units, as in 3.5 km.
         * @return a string representing a given distance.
         */
        public static String format(int distanceInBasicUnits, Distance.Units units, UnitsUse unitsUse)
        {
            final Distance DIST = new Distance( distanceInBasicUnits, units );
            String toret = "";

            if ( unitsUse == UnitsUse.UNITS ) {
                toret = DIST.toString();
            }
            else
            if ( unitsUse == UnitsUse.NO_UNITS ) {
                toret = DIST.toThousandUnitsAsString();
            } else {
                throw new IllegalArgumentException( unitsUse + "?? not supported." );
            }

            return toret;
        }

        /** Formats a distance in basic units to a string,
         * WITH UNITS, as in 3500 -> 3.5 km.
         * @param units the distance units.
         * @param distanceInBasicUnits the given distance.
         * @return a string representing a given distance.
         */
        public static String format(int distanceInBasicUnits, Distance.Units units)
        {
            return format( distanceInBasicUnits, units, UnitsUse.UNITS );
        }
    }
}
