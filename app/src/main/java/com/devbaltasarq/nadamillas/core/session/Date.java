// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core.session;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/** A date, */
public class Date {
    /** Creates a new date with today's data. */
    public Date()
    {
        this( Calendar.getInstance() );
    }

    /** Creates a new date with today's data.
      * @param l the desired locale, or null for default.
      */
    public Date(Locale l)
    {
        this( Calendar.getInstance( l ) );
    }

    public Date(Calendar c)
    {
        this.date = c;
    }

    /** @return a copy of the calendar inside this object. */
    public Calendar getInnerCalendar()
    {
        final Calendar TORET = Calendar.getInstance();

        TORET.setTimeInMillis( this.date.getTimeInMillis() );
        return TORET;
    }

    /** @return the current date in milliseconds. */
    public long getTimeInMillis()
    {
        return this.date.getTimeInMillis();
    }

    /** @return the year of this date. */
    public int getYear()
    {
        return this.date.get( Calendar.YEAR );
    }

    /** @return the month of this date. */
    public int getMonth()
    {
        return this.date.get( Calendar.MONTH );
    }

    /** @return the day of this date. */
    public int getDay()
    {
        return this.date.get( Calendar.DAY_OF_MONTH );
    }

    public int getLastDayOfMonth()
    {
        return this.date.getActualMaximum( Calendar.DAY_OF_MONTH );
    }

    /** @return the day of the week for this date. */
    public int getWeekDay()
    {
        return this.date.get( Calendar.DAY_OF_WEEK );
    }

    /** @return the day of the week code for a specific day. */
    public int getMondayWeekDayCode()
    {
        return this.date.get( Calendar.MONDAY );
    }

    /** @return the day of the week code for a specific day. */
    public int getSundayWeekDayCode()
    {
        return this.date.get( Calendar.SUNDAY );
    }

    /** @return the date as an array: 0 - year, 1 - month, 2 - day. */
    public int[] toData()
    {
        return dataFromDate( this.date.getTime() );
    }

    /** @return the week day name for a given date. */
    public String getWeekDayName()
    {
        return String.format( "%tA", this.date );
    }

    /** @return the current short date, as a string. */
    public String toShortDateString()
    {
        return this.toShortDateString( null );
    }

    /** @return the semi full date with the given locale, as string. */
    public String toSemiFullDateString()
    {
        return this.toSemiFullDateString( null );
    }

    /** Gets the semi full date (no week day).
     * @param locale a given Locale, or null for default one.
     * @return the semi full date with the given locale, as string.
     */
    public String toSemiFullDateString(Locale locale)
    {
        if ( locale == null ) {
            locale = Locale.getDefault();
        }

        final DateFormat F = DateFormat.getDateInstance( DateFormat.LONG, locale );
        return F.format( this.date.getTime() );
    }

    /** Gets the current short date for a given locale.
      * @param locale a given Locale, or null for default one.
      * @return the current short date, as a string.
      */
    public String toShortDateString(Locale locale)
    {
        if ( locale == null ) {
            locale = Locale.getDefault();
        }

        final DateFormat DATE_FORMAT = DateFormat.getDateInstance( DateFormat.SHORT, locale );

        if ( DATE_FORMAT instanceof final SimpleDateFormat SDF ) {
            // To show Locale specific short date expression with full year, month and day
            SDF.applyPattern(
                    SDF.toPattern().replaceAll( "y+", "yyyy" )
                        .replaceAll( "M+", "MM" )
                        .replaceAll( "d+", "dd" ) );
        }

        return DATE_FORMAT.format( this.date.getTime() );
    }

    /** @return the full date, as a string. */
    public String toFullDateString()
    {
        return this.toFullDateString( null );
    }

    /** Gets a full date.
     * @param locale a given Locale.
     * @return the full date with the given locale, as a string.
     */
    public String toFullDateString(Locale locale)
    {
        if ( locale == null ) {
            locale = Locale.getDefault();
        }

        final DateFormat F = DateFormat.getDateInstance( DateFormat.FULL, locale );
        return F.format( this.date.getTime() );
    }

    /** @return the time in this date, as a string. */
    public String toTimeString()
    {
        return String.format( Locale.getDefault(), "%02d:%02d:%02d",
                this.date.get( Calendar.HOUR ),
                this.date.get( Calendar.MINUTE ),
                this.date.get( Calendar.SECOND ) );
    }

    /** @return an ISO-formatted string for the given date. */
    @Override
    public String toString()
    {
        final int[] DATE_DATA = dataFromDate( this.date.getTime() );

        return String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        DATE_DATA[ 0 ], DATE_DATA[ 1 ] + 1, DATE_DATA[ 2 ] );
    }

    /** Creates a new date from int data.
      * @param year the year
      * @param month the month - 1
      * @param day the day
      * @return a corresponding Date object.
      */
    public static Date from(int year, int month, int day)
    {
        final Calendar DATE = Calendar.getInstance();

        DATE.setTime( dateFromData( year, month, day ) );
        return new Date( DATE );
    }

    /** Creates a new date from a time in millis.
      * @param millis the time in millis.
      * @return a corresponding Date object.
      */
    public static Date from(long millis)
    {
        final Calendar DATE = Calendar.getInstance();

        DATE.setTimeInMillis( millis );
        return new Date( DATE );
    }

    /** @return the Date object corresponding to that year, month, and day. */
    public static java.util.Date dateFromData(int year, int month, int dayOfMonth)
    {
        final Calendar DATE = Calendar.getInstance();

        DATE.set( Calendar.YEAR, year );
        DATE.set( Calendar.MONTH, month );
        DATE.set( Calendar.DAY_OF_MONTH, dayOfMonth );

        return DATE.getTime();
    }

    /** Converts a Date object to an array of three positions.
     * @param d a given Date object.
     * @return an array of three positions: 0: year, 1: month, 2: day.
     */
    public static int[] dataFromDate(java.util.Date d)
    {
        final int[] toret = new int[3];
        final Calendar DATE = Calendar.getInstance();

        DATE.setTime( d );
        toret[ 0 ] = DATE.get( Calendar.YEAR );
        toret[ 1 ] = DATE.get( Calendar.MONTH );
        toret[ 2 ] = DATE.get( Calendar.DAY_OF_MONTH );

        return toret;
    }

    private final Calendar date;
}
