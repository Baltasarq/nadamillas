// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.core;

import java.util.Date;
import java.util.Locale;

public class Session {
    public static final int FAKE_ID = -1;

    /** Creates a new training session.
      * @param date the date this session happened on.
      * @param distance the distance for this session.
      * @param atPool whether this session was done at the pool or not.
      */
    public Session(Date date, int distance, boolean atPool)
    {
        this( FAKE_ID, date, distance, atPool );
    }

    /** Creates a new training session.
     * @param id the unique id for this session.
     * @param date the date this session happened on.
     * @param distance the distance for this session.
     * @param atPool whether this session was done at the pool or not.
     */
    public Session(int id, Date date, int distance, boolean atPool)
    {
        this.id = id;
        this.date = date;
        this.distance = distance;
        this.atPool = atPool;
    }

    /** @return the id for this session. -1 if fake. */
    public int getId()
    {
        return this.id;
    }

    /** @return the date for this session. */
    public Date getDate()
    {
        return date;
    }

    /** @return the distance in this session. */
    public int getDistance()
    {
        return distance;
    }

    /** @return whether this session was done at the pool or not. */
    public boolean isAtPool()
    {
        return atPool;
    }

    /** Creates a new session with a given id and session number.
      * @param id the new id for the session.
      * @return a new Session.
      */
    public Session copyWithId(int id)
    {
        return new Session( id, this.getDate(), this.getDistance(), this.isAtPool() );
    }

    @Override
    public String toString()
    {
        return String.format( Locale.getDefault(),
                         "%03d: %s: %7dm %s",
                                this.getId(),
                                Util.getShortDate( this.getDate(), null ),
                                this.getDistance(),
                                this.isAtPool() ? "at pool" : "open water" );
    }

    private int id;
    private Date date;
    private int distance;
    private boolean atPool;
}
