// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core;


import android.content.Context;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.settings.DistanceUtils;

import java.util.Date;
import java.util.Locale;


public class Session {
    public static final int FAKE_ID = -1;

    /** Creates a new training session.
      * @param date the date this session happened on.
      * @param distance the distance for this session.
      * @param atPool whether this session was done at the pool or not.
      */
    public Session(Date date,
                   int distance,
                   Duration duration,
                   boolean atPool,
                   String place,
                   String notes)
    {
        this( FAKE_ID, date, distance, duration, atPool, place, notes );
    }

    /** Creates a new training session.
     * @param date the date this session happened on.
     * @param distance the distance for this session.
     * @param atPool whether this session was done at the pool or not.
     */
    public Session(Date date,
                   int distance,
                   int secs,
                   boolean atPool,
                   String place,
                   String notes)
    {
        this( FAKE_ID, date, distance, new Duration( secs ), atPool, place, notes );
    }

    /** Creates a new training session.
     * @param id the unique id for this session.
     * @param date the date this session happened on.
     * @param distance the distance for this session.
     * @param atPool whether this session was done at the pool or not.
     */
    public Session(int id,
                   Date date,
                   int distance,
                   int secs,
                   boolean atPool,
                   String place,
                   String notes)
    {
        this( id, date, distance, new Duration( secs ), atPool, place, notes );
    }

    /** Creates a new training session.
     * @param id the unique id for this session.
     * @param date the date this session happened on.
     * @param distance the distance for this session.
     * @param atPool whether this session was done at the pool or not.
     */
    public Session(int id,
                   Date date,
                   int distance,
                   Duration duration,
                   boolean atPool,
                   String place,
                   String notes)
    {
        this.id = id;
        this.date = date;
        this.distance = distance;
        this.duration = duration;
        this.atPool = atPool;

        if ( place == null ) {
            place = "";
        }

        if ( notes == null ) {
            notes = "";
        }

        this.place = Util.capitalize( place );
        this.notes = Util.capitalize( notes );
    }

    /** @return the id for this session. Is FAKE_ID if fake. */
    public int getId()
    {
        return this.id;
    }

    /** @return the date for this session. */
    public Date getDate()
    {
        return date;
    }

    /** @return the distance in this session, in meters/yards. */
    public int getDistance()
    {
        return this.distance;
    }

    /** @return the duration of this session. */
    public Duration getDuration()
    {
        return this.duration;
    }

    /** @return whether this session was done at the pool or not. */
    public boolean isAtPool()
    {
        return this.atPool;
    }

    /** @return the place this session happened on. */
    public String getPlace()
    {
        return this.place;
    }

    /** @return the notes related to this session. */
    public String getNotes()
    {
        return this.notes;
    }

    /** @return the formatted distance. */
    public String getFormattedDistance(Context cntx, DistanceUtils.Units du)
    {
        final Locale LOCALE = Locale.getDefault();
        int units = R.string.label_meter;

        if ( du == DistanceUtils.Units.mi ) {
            units = R.string.label_yard;
        }

        return String.format( LOCALE, "%6d%s",
                this.getDistance(),
                cntx.getString( units ) );
    }

    /** @return the mean time for each 100m. */
    public Duration getMeanTime()
    {
        final double HUNDREDS = (double) this.getDistance() / 100;
        int secs = 0;

        if ( HUNDREDS > 0 ) {
            secs = (int) Math.round( this.getDuration().getTimeInSeconds() / HUNDREDS );
        }

        return new Duration( secs );
    }

    /** @return the mean time, as a string. */
    public String getMeanTimeAsString(DistanceUtils.Units du)
    {
        String distanceUnits = "m";

        // Set distance units
        if ( du == DistanceUtils.Units.mi ) {
            distanceUnits = "y";
        }

        return this.getMeanTime().toString() + "/100" + distanceUnits;
    }

    /** @return the mean velocity for this session. */
    public double getSpeed(Settings settings)
    {
        final DistanceUtils DISTANCE_UTIL = settings.getDistanceUtils();
        final double DISTANCE = DISTANCE_UTIL.thousandUnitsFromUnits( this.getDistance() );
        final double TIME = (double) this.getDuration().getTimeInSeconds() / 3600;
        double toret = 0;

        if ( TIME > 0 ) {
            toret = DISTANCE / TIME;
        }

        return toret;
    }

    /** @return the mean velocity for this session, as a string. */
    public String getSpeedAsString(Settings settings)
    {
        final String SPEED_UNITS = settings.getDistanceUnits().toString() + "/h";

        return String.format( Locale.getDefault(), "%5.2f%s",
                              this.getSpeed( settings ), SPEED_UNITS );
    }

    /** @return get the whole speed information, as a formatted string. */
    public String getWholeSpeedFormattedString(Settings settings)
    {
        return this.getSpeedAsString( settings )
                + " - " + this.getMeanTimeAsString( settings.getDistanceUnits() );
    }

    public String getTimeAndWholeSpeedFormattedString(Settings settings)
    {
        return this.getDuration().toString()
                + "\n" + this.getWholeSpeedFormattedString( settings );
    }

    public String toHumanReadableString(Context ctx, Settings settings)
    {
        String fmt = ctx.getString( R.string.fmt_human_readable_info );
        String place = ctx.getString( R.string.label_pool ).toLowerCase();

        if ( !this.isAtPool() ) {
            place = ctx.getString( R.string.label_open_waters ).toLowerCase();
        }

        if ( !this.getPlace().isEmpty() ) {
            place += " (" + this.getPlace() + ")";
        }

        return fmt.replace( "$date", Util.getShortDate( this.getDate(), null ) )
                            .replace( "$distance",
                                            this.getFormattedDistance(
                                                    ctx,
                                                    settings.getDistanceUnits() ) )
                            .replace( "$place", place );
    }

    /** Creates a new session with a given id and session number.
      * @param id the new id for the session.
      * @return a new Session.
      */
    public Session copyWithId(int id)
    {
        return new Session( id,
                            this.getDate(),
                            this.getDistance(),
                            this.getDuration(),
                            this.isAtPool(),
                            this.place,
                            this.notes );
    }

    @Override
    public int hashCode()
    {
        return 11 * (
                this.getId()
                + this.getDate().hashCode()
                + this.getDistance()
                + this.getDuration().hashCode()
                + this.getPlace().hashCode()
                + this.getNotes().hashCode()
                + ( this.isAtPool() ? 31 : 37 )
        );
    }

    @Override
    public boolean equals(Object other)
    {
        boolean toret = false;

        if ( other instanceof final Session OTHER_SESSION ) {
            toret = this.getDate().equals( OTHER_SESSION.getDate() )
                    && this.getDistance() == OTHER_SESSION.getDistance()
                    && this.getDuration().equals( OTHER_SESSION.getDuration() )
                    && this.getPlace().equals( OTHER_SESSION.getPlace() )
                    && this.isAtPool() == OTHER_SESSION.isAtPool();
        }

        return toret;
    }

    @Override
    public String toString()
    {
        return String.format( Locale.getDefault(),
                                "%03d: %s: %7dm (%s) %s - %s (%s)",
                                this.getId(),
                                Util.getShortDate( this.getDate(), null ),
                                this.getDistance(),
                                this.getDuration().toString(),
                                this.isAtPool() ? "at pool" : "open water",
                                this.getPlace(),
                                this.getNotes() );
    }

    private final int id;
    private final Date date;
    private final int distance;
    private final Duration duration;
    private final boolean atPool;
    private final String place;
    private final String notes;
}
