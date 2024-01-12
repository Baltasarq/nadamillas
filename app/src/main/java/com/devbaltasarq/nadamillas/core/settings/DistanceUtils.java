// NadaMillas (c) 2019-2024-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core.settings;


import java.util.Locale;


public final class DistanceUtils {
    public enum Units { km, mi;

        /** @return the corresponding enum value, given its position. */
        public static Units fromOrdinal(int pos)
        {
            return Units.values()[ pos ];
        }
    }

    /** Creates a new Distance utils, following the selected units.
      * @param units the units to use.
      */
    private DistanceUtils(Units units)
    {
        this.units = units;
    }

    /** @return the units for this distance object. */
    public Units getUnits()
    {
        return this.units;
    }

    /** Converts the distance in the thousand levels to the distance.
      * @param d a given distance (as km or miles)
      * @return the same distance in m or yd, depending on settings.
      */
    public int unitsFromThousandUnits(int d)
    {
        int toret = mFromKm( d );

        if ( this.getUnits() == Units.mi ) {
            toret = ydFromMi( d );
        }

        return toret;
    }

    /** Converts the distance to the distance in the thousand levels.
      * @param d a given distance (as meters or yards)
      * @return the same distance in km or mi, depending on settings.
      */
    public double thousandUnitsFromUnits(int d)
    {
        double toret = kmFromM( d );

        if ( this.getUnits() == Units.mi ) {
            toret = miFromYd( d );
        }

        return toret;
    }

    /** Converts a distance in basic units to a String.
      * @param distanceInBasicUnits the distance in basic units (m, yd).
      * @return the distance as a string.
      */
    public String toString(int distanceInBasicUnits)
    {
        double distance = thousandUnitsFromUnits(
                                this.getUnits(),
                                distanceInBasicUnits );

        return String.format( Locale.getDefault(), "%7.2f", distance );
    }

    private final Units units;

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

    /** Converts the distance in the thousand levels to the distance.
     * @param units a given distance units.
     * @param d a given distance (as km or miles)
     * @return the same distance in m or yd, depending on settings.
     */
    public static int unitsFromThousandUnits(Units units, int d)
    {
        int toret = mFromKm( d );

        if ( units == Units.mi ) {
            toret = ydFromMi( d );
        }

        return toret;
    }

    /** Converts the distance to the distance in the thousand levels.
     * @param units a given distance units.
     * @param d a given distance (as meters or yards)
     * @return the same distance in km or mi, depending on settings.
     */
    public static double thousandUnitsFromUnits(Units units, int d)
    {
        double toret = kmFromM( d );

        if ( units == Units.mi ) {
            toret = miFromYd( d );
        }

        return toret;
    }

    /** Converts a distance in basic units to a String.
      * @param distanceInBasicUnits the distance to convert to String.
      * @return the distance as a string.
      */
    public static String toString(DistanceUtils.Units units, int distanceInBasicUnits)
    {
        double distance = thousandUnitsFromUnits( units, distanceInBasicUnits );

        return String.format( Locale.getDefault(), "%7.2f", distance );
    }

    /** Creates a new Distance utils object. */
    public static DistanceUtils createFor(Units units)
    {
        return new DistanceUtils( units );
    }
}
