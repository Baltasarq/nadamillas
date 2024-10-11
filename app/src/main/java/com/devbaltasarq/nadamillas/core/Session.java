// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core;


import android.content.Context;

import com.devbaltasarq.nadamillas.R;

import java.util.Date;
import java.util.Locale;


public class Session {
    public static final int FAKE_ID = -1;

    /** Creates a fake training session. */
    public Session(Date date)
    {
        this( FAKE_ID,
                date,
                0,
                new Duration( 0 ),
                false,
                false,
                18,
                "",
                ""
        );
    }

    /** Creates a new training session.
      * @param date the date this session happened on.
      * @param distance the distance for this session.
      * @param duration the duration of the session.
      * @param atPool whether this session was done at the pool or not.
      * @param place the place the swim happened at.
      * @param isCompetition was it a competition or training?
      * @param temperature the temperature of the water
      * @param notes any remarks.
      */
    public Session(Date date,
                   int distance,
                   Duration duration,
                   boolean atPool,
                   boolean isCompetition,
                   double temperature,
                   String place,
                   String notes)
    {
        this( FAKE_ID,
                date,
                distance,
                duration,
                atPool,
                isCompetition,
                temperature,
                place,
                notes );
    }

    /** Creates a new training session.
     * @param id the unique id for this session.
     * @param date the date this session happened on.
     * @param distance the distance for this session.
     * @param duration the duration of the session.
     * @param atPool whether this session was done at the pool or not.
     * @param place the place the swim happened at.
     * @param isCompetition was it a competition or training?
     * @param temperature the temperature of the water
     * @param notes any remarks.
     */
    public Session(int id,
                   Date date,
                   int distance,
                   Duration duration,
                   boolean atPool,
                   boolean isCompetition,
                   double temperature,
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

        this.isCompetition = isCompetition;
        this.temperature = temperature;
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

    /** @return whether this session was a competition or not. */
    public boolean isCompetition()
    {
        return this.isCompetition;
    }

    /** @return the temperature of the water. */
    public double getTemperature()
    {
        return this.temperature;
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

    /** @return the mean time for each 100m. */
    public Duration getMeanTime(Distance.Units units)
    {
        return new Speed(
                        new Distance( this.getDistance(), units ),
                        this.getDuration() ).getMeanTime();
    }

    /** @return the mean time, as a string. */
    public String getMeanTimeAsString(Distance.Units du)
    {
        return new Speed(
                new Distance( this.getDistance(), du ),
                this.getDuration() ).getMeanTimeAsStr();
    }

    /** Get the mean velocity for this session.
      * @param units the units to use.
      * @return the mean velocity for this session.
      */
    public double getSpeed(Distance.Units units)
    {
        return new Speed(
                    new Distance( this.getDistance(), units ),
                    this.getDuration() ).getValue();
    }

    /** @return the mean velocity for this session, as a string. */
    public String getSpeedAsString(Settings settings)
    {
        final Distance.Units UNITS = settings.getDistanceUnits();
        final Distance DIST = new Distance( this.getDistance(), UNITS );

        return new Speed( DIST, this.getDuration() ).toString();
    }

    /** @return get the whole speed information, as a formatted string. */
    public String getWholeSpeedFormattedString(Settings settings)
    {
        final Distance.Units UNITS = settings.getDistanceUnits();
        final Speed SPEED = new Speed(
                                    new Distance( this.getDistance(), UNITS ),
                                    this.getDuration() );

        return this.getDuration() + " - " + SPEED;
    }

    /** @return get the time and the whole speed information, as a formatted string. */
    public String getTimeAndWholeSpeedFormattedString(Settings settings)
    {
        return this.getDuration().toString()
                + "\n" + this.getWholeSpeedFormattedString( settings );
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
                            this.isCompetition(),
                            this.getTemperature(),
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
                + Boolean.hashCode( this.isAtPool() )
                + Double.hashCode( this.getTemperature() )
                + this.getPlace().hashCode()
                + this.getNotes().hashCode() );
    }

    @Override
    public boolean equals(Object other)
    {
        boolean toret = false;

        if ( other instanceof final Session OTHER_SESSION ) {
            toret = this.getDate().equals( OTHER_SESSION.getDate() )
                    && this.getDistance() == OTHER_SESSION.getDistance()
                    && this.getDuration().equals( OTHER_SESSION.getDuration() )
                    && this.isAtPool() == OTHER_SESSION.isAtPool()
                    && this.isCompetition() == OTHER_SESSION.isCompetition()
                    && this.getTemperature() == OTHER_SESSION.getTemperature()
                    && this.getPlace().equals( OTHER_SESSION.getPlace() )
                    && this.getNotes().equals( OTHER_SESSION.getNotes() );
        }

        return toret;
    }

    @Override
    public String toString()
    {
        return String.format( Locale.getDefault(),
                                "%03d: %s: %7dm (%s) %5.2fº %s %s - %s (%s)",
                                this.getId(),
                                Util.getShortDate( this.getDate(), null ),
                                this.getDistance(),
                                this.getDuration().toString(),
                                this.getTemperature(),
                                this.isCompetition() ? "race" : "training",
                                this.isAtPool() ? "at pool" : "open water",
                                this.getPlace(),
                                this.getNotes() );
    }

    public static String summaryFromSessionData(
            Context ctx,
            Settings settings,
            Date date,
            boolean isAtPool,
            String place,
            int distance)
    {
        String fmt = ctx.getString( R.string.fmt_human_readable_info );
        final Distance.Units UNITS = settings.getDistanceUnits();

        if ( place.isEmpty() ) {
            place = ctx.getString( R.string.label_pool );
        }

        if ( !isAtPool ) {
            place = ctx.getString( R.string.label_open_waters );
        }

        place = " (" + place.toLowerCase() + ")";
        return fmt.replace( "$date",
                        Util.getShortDate( date, null ) )
                .replace( "$distance",
                        Distance.format( distance, UNITS ) )
                .replace( "$place", place );
    }

    private final int id;
    private final Date date;
    private final int distance;
    private final Duration duration;
    private final boolean atPool;
    private final boolean isCompetition;
    private final double temperature;
    private final String place;
    private final String notes;
}
