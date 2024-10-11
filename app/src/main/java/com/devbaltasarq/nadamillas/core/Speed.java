// NadaMillas (c) 2019-2024-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core;


import java.util.Locale;


/** Represents the relation between a distance dnd a duration. */
public final class Speed {
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

    public String getMeanTimeAsStr()
    {
        String distanceUnits = "m";

        // Set distance units
        if ( this.getDistance().getUnits() == Distance.Units.mi ) {
            distanceUnits = "y";
        }

        return this.getMeanTime() + "/100" + distanceUnits;
    }

    /** @return the mean velocity for this session, km/h. */
    public double getValue()
    {
        final double DISTANCE = this.getDistance().toThousandUnits();
        final double TIME = (double) this.getDuration().getTimeInSeconds() / 3600;
        double toret = 0;

        if ( TIME > 0 ) {
            toret = DISTANCE / TIME;
        }

        return toret;
    }

    @Override
    public String toString()
    {
        return String.format( Locale.getDefault(),
                "%5.2f%s",
                this.getValue(),
                this.getDistance().getUnits().toString() );
    }

    private final Distance distance;
    private final Duration duration;
}
