// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.core.session;

import java.util.Locale;

/** Represents duration in minutes and seconds. */
public class Duration {
    public enum TimeUnit { Seconds, Minutes;

        /** @return the corresponding enum value, given its position. */
        public static TimeUnit fromOrdinal(int pos)
        {
            return TimeUnit.values()[ pos ];
        }
    }

    /** Creates a new duration, given time in minutes and seconds.
      * @param hrs The duration time, in hours.
      * @param min The duration time, in minutes.
      * @param secs The duration time, in seconds.
      */
    public Duration(int hrs, int min, int secs)
    {
        this( ( hrs * 3600 ) + ( min * 60 ) + secs );
    }

    /** Creates a new duration, given time in minutes and seconds.
      * @param min The duration time, in minutes.
      * @param secs The duration time, in seconds.
      */
    public Duration(int min, int secs)
    {
        this( ( min * 60 ) + secs );
    }

    /** Creates a new duration, given time in seconds.
     * @param secs The duration time, in seconds.
     */
    public Duration(int secs)
    {
        this.secs = secs;
    }


    /** @return the same duration in here, but in another object. */
    public Duration copy()
    {
        return new Duration( this.secs );
    }

    /** Creates a new duration, given time in minutes and seconds.
      * @param min The duration time, in minutes.
      */
    public Duration(float min)
    {
        this.secs = ( (int) ( 60.0 * min ) );
    }

    @Override
    public int hashCode()
    {
        return Integer.valueOf( this.secs ).hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        boolean toret = false;

        if ( o instanceof final Duration DOT) {
            toret = ( this.getTimeInSeconds() == DOT.getTimeInSeconds() );
        }

        return toret;
    }

    /** Adds a number of seconds to this duration.
      * @param secs the number of seconds to add.
      * @return itself.
      */
    public Duration add(int secs)
    {
        this.secs += secs;
        return this;
    }

    /** @return The whole time in seconds. */
    public int getTimeInSeconds()
    {
        return this.secs;
    }

    /** @return The amount of minutes in this duration. */
    public int getTimeInMinutes()
    {
        return this.secs / 60;
    }

    /** @return The hours part of this duration. */
    public int getHours()
    {
        return this.secs / 3600;
    }

    /** @return The minutes part of this duration. */
    public int getMinutes()
    {
        int hourSeconds = this.getHours() * 3600;

        return ( this.secs - hourSeconds ) / 60;
    }

    /** @return The seconds part of this duration. */
    public int getSeconds()
    {
        int hourSeconds = this.secs - ( this.getHours() * 3600 );
        int minutesSeconds = hourSeconds - ( this.getMinutes() * 60 );

        return minutesSeconds % 60;
    }

    /** Parses the time as the user enters it.
      * @param mode the corresponding enumeration value for minutes and seconds.
      * @param txt the text for the value in seconds or minutes.
      * @return the new value of this.secs.
      * @see TimeUnit
      */
    public int parse(TimeUnit mode, String txt) throws NumberFormatException
    {
        float timeValue = Float.parseFloat( txt );

        if ( mode == TimeUnit.Seconds ) {
            this.secs = (int) timeValue;
        }
        else
        if ( mode == TimeUnit.Minutes ) {
            this.secs = ( (int) (60 * timeValue) );
        }

        return this.secs;
    }

    @Override
    public String toString()
    {
        final Locale DEF_LOC = Locale.getDefault();
        final int MINUTES = this.getMinutes();
        final int HOURS = this.getHours();
        String toret;

        if ( HOURS == 0
          && MINUTES == 0 )
        {
            toret = String.format( DEF_LOC, "%2d\"", this.getTimeInSeconds() );
        } else {
            final int SECONDS = this.getSeconds();
            toret = "";

            if ( HOURS > 0 ) {
                toret = String.format( DEF_LOC, "%02dh", HOURS );
            }

            toret += String.format( DEF_LOC, "%02d'%02d\"", MINUTES, SECONDS );
        }

        return toret;
    }

    /** @return zero duration, zero seconds. */
    public static Duration Zero()
    {
        if ( ZERO == null ) {
            ZERO = new Duration( 0 );
        }

        return ZERO;
    }

    private int secs;
    private static Duration ZERO = null;
}
