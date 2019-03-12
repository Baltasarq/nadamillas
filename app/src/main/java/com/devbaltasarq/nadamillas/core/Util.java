// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.core;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Util {
    /** @return the current date with the default locale. */
    public static Calendar getDate()
    {
        return getDate( null );
    }

    /** Gets the current date.
      * @param locale a given Locale, or null for default one.
      * @return the current date with the given locale.
      */
    public static Calendar getDate(Locale locale)
    {
        if ( locale == null ) {
            locale = Locale.getDefault();
        }

        return Calendar.getInstance( locale );
    }

    /** @return the Date object corresponding to that year, month, and day. */
    public static Date dateFromData(int year, int month, int dayOfMonth)
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
    public static int[] dataFromDate(Date d)
    {
        final int[] toret = new int[3];
        final Calendar DATE = Calendar.getInstance();

        DATE.setTime( d );
        toret[ 0 ] = DATE.get( Calendar.YEAR );
        toret[ 1 ] = DATE.get( Calendar.MONTH );
        toret[ 2 ] = DATE.get( Calendar.DAY_OF_MONTH );

        return toret;
    }

    public static String getTimeAsString()
    {
        return getTimeAsString( getDate().getTime() );
    }

    public static String getTimeAsString(Date date)
    {
        final Calendar DATE_TIME = Calendar.getInstance();

        DATE_TIME.setTime( date );
        return String.format( Locale.getDefault(), "%02d:%02d:02d",
                                DATE_TIME.get( Calendar.HOUR ),
                                DATE_TIME.get( Calendar.MINUTE ),
                                DATE_TIME.get( Calendar.SECOND ) );
    }

    /** a year, as an int, from a given Date. */
    public static int getYearFrom(Date date)
    {
        final Calendar DATE = Calendar.getInstance();

        DATE.setTime( date );
        return DATE.get( Calendar.YEAR );
    }

    /** @return the short current date for the current locale. */
    public static String getShortDate()
    {
        return getShortDate( null, Locale.getDefault() );
    }

    /** Gets the current short date for a given locale.
     * @param d a given date. If null, current is assumed.
     * @param locale a given Locale, or null for default one.
     * @return the current date with the given locale.
     */
    public static String getShortDate(Date d, Locale locale)
    {
        if ( locale == null ) {
            locale = Locale.getDefault();
        }

        final DateFormat F = DateFormat.getDateInstance( DateFormat.SHORT, locale );

        if ( d == null ) {
            d = getDate( locale ).getTime();
        }

        return F.format( d );
    }

    /** @return the current full date. */
    public static String getFullDate()
    {
        return getFullDate( null, Locale.getDefault() );
    }

    /** Gets the current full date.
     * @param d a given date. If null, current is assumed.
     * @param locale a given Locale, or null for default one.
     * @return the current date with the given locale.
     */
    public static String getFullDate(Date d, Locale locale)
    {
        if ( locale == null ) {
            locale = Locale.getDefault();
        }

        final DateFormat F = DateFormat.getDateInstance( DateFormat.FULL, locale );

        if ( d == null ) {
            d = getDate( locale ).getTime();
        }

        return F.format( d );
    }

    /** @return the current date as an ISO-formatted date. */
    public static String getISODate()
    {
        return getISODate( getDate().getTime() );
    }

    /** Returns an ISO-formatted string for the given date.
      * @param date the date to format as ISO date.
      * @return the formatted date, as a string.
      */
    public static String getISODate(Date date)
    {
        final int[] DATE_DATA = dataFromDate( date );

        return String.format( "%04d-%02d-%02d", DATE_DATA[ 0 ], DATE_DATA[ 1 ] + 1, DATE_DATA[ 2 ] );
    }
}
