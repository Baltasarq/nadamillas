// NadaMillas (c) 2019-2024-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core.session;


import java.util.Locale;


/** Represents the relation between a distance dnd a duration. */
public final class Speed {
    private static String MPH = "mph";
    private static String KMH = "km/h";

    public Speed(Distance dist, Duration duration)
    {
        this.distance = dist;
        this.duration = duration;
    }

    /** @return the distance for this speed. */
    public Distance getDistance()
    {
        return this.distance;
    }

    /** @return the duration for this speed. */
    public Duration getDuration()
    {
        return this.duration;
    }

    /** Get the mean time by each one hundred meters.
      * @return a new Duration / 100m
      */
    public Duration getMeanTime()
    {
        final double HUNDREDS = (double) this.getDistance().getValue() / 100;
        int secs = 0;

        if ( HUNDREDS > 0 ) {
            secs = (int) Math.round( this.getDuration().getTimeInSeconds() / HUNDREDS );
        }

        return new Duration( secs );
    }

    /** @return the mean velocity for this session, km/h. */
    public double getSpeedPerHour()
    {
        final double DISTANCE = this.getDistance().toThousandUnits();
        final double TIME = (double) this.getDuration().getTimeInSeconds() / 3600;
        double toret = 0;

        if ( TIME > 0 ) {
            toret = DISTANCE / TIME;
        }

        return toret;
    }

    public String getSpeedPerHourAsString()
    {
        String units = KMH;

        if ( this.getDistance().getUnits() == Distance.Units.mi ) {
            units = MPH;
        }

        return String.format( Locale.getDefault(),
                "%5.2f %s",
                this.getSpeedPerHour(),
                units );
    }

    @Override
    public String toString()
    {
        String distanceUnits = "m";

        // Set distance units
        if ( this.getDistance().getUnits() == Distance.Units.mi ) {
            distanceUnits = "y";
        }

        return this.getMeanTime() + "/100 " + distanceUnits + ".";
    }

    private final Distance distance;
    private final Duration duration;
}
