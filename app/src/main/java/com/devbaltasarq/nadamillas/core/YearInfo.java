// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.core;

import java.util.Calendar;
import java.util.Locale;

/** Represents the accumulated distances. */
public class YearInfo {
    private static final int DEFAULT_TARGET = 200000;
    public static final String NOT_APPLYABLE = "N/A";
    public enum SwimKind { POOL, OWS, TOTAL }

    /** Creates a default instance for a given year.
      * @param year the year for this info.
      */
    public YearInfo(int year)
    {
        this( year, DEFAULT_TARGET, 0, 0, 0 );
    }

    /** Creates a new instance (meters/yards).
      * @param year The year this distances pertain to.
      * @param targetTotal The target distance.
      * @param distanceTotal Total distance.
      * @param distancePool Total distance at the pool.
      * @param targetPool Target distance at the pool.
      */
    public YearInfo(
            int year,
            int targetTotal,
            int distanceTotal,
            int distancePool,
            int targetPool)
    {
        this.year = year;
        this.targetTotal = targetTotal;
        this.distanceTotal = distanceTotal;
        this.distancePool = distancePool;
        this.targetPool = targetPool;
    }

    /** @return the year this distances pertain to. */
    public int getYear()
    {
        return year;
    }

    /** @return the year this distances pertain to, as a string. */
    public String getYearAsString()
    {
        final int YEAR = this.getYear();
        String toret = NOT_APPLYABLE;

        if ( YEAR >= 0 ) {
            toret = Integer.toString( YEAR );
        }

        return toret;
    }

    /** Gets the distance swum.
     * @param swimKind the swimming kind data to get.
     * @return the distance for a given target.
     */
    public int getDistance(SwimKind swimKind)
    {
        int toret = -1;

        switch( swimKind ) {
            case POOL -> toret = this.getDistancePool();
            case OWS -> toret = this.getDistanceOWS();
            case TOTAL-> toret = this.getDistanceTotal();
            default -> throw new Error(
                    "getTarget(): no target found for: " + swimKind );
        }

        return toret;
    }

    /** Calculate the progress made.
      * @param swimKind the swimming kind to calculate the progress for.
      * @return the progress made up until now, between 0 - 100.
      */
    public double getProgress(SwimKind swimKind)
    {
        double toret = -1.0;

        switch( swimKind ) {
            case POOL -> toret = this.getProgressPool();
            case OWS -> toret = this.getProgressOWS();
            case TOTAL-> toret = this.getProgressTotal();
            default -> throw new Error(
                    "getTarget(): no target found for: " + swimKind );
        }

        return toret;
    }

    /** Gets the distance for a given target.
     * @param target the target to get.
     * @return the distance for a given target.
     */
    public int getTarget(SwimKind target)
    {
        int toret = -1;

        switch( target ) {
            case POOL -> toret = this.getTargetPool();
            case OWS -> toret = this.getTargetOWS();
            case TOTAL-> toret = this.getTargetTotal();
            default -> throw new Error(
                    "getTarget(): no target found for: " + target );
        }

        return toret;
    }

    /** Changes a given target distance.
      * @param target the target to change.
      * @param newTarget the new distance to change.
      */
    public void setTarget(SwimKind target, int newTarget)
    {
        newTarget = Math.max( 0, newTarget );

        switch( target ) {
            case POOL -> this.setTargetPool( newTarget );
            case OWS ->  {}
            case TOTAL-> this.setTargetTotal( newTarget );
            default -> throw new Error(
                            "setTarget(): no target found for: " + target );
        }

        return;
    }

    /** Calculates the progression given a distance and a target.
      * as in a distance of 40 km., and a target of 100 km., returns 40%.
      * @param distance the given progress.
      * @param target the given target.
      * @return a double in between 0 and 100.
      */
    public double calcProgress(int distance, int target)
    {
        double toret = 0;

        if ( distance >= 0
          && target > 0 )
        {
            toret = ( (double) distance / target ) * 100.0;
        }

        return toret;
    }

    /** Calculates an annual projection for a given distance.
      * as in a distance of 40 km. at the end of march, it returns 120 km.
      * @param distance a given distance.
      * @return a value for the projection of the distance.
      */
    public double calcProjection(int distance)
    {
        final int DAY_OF_YEAR = Calendar.getInstance().get( Calendar.DAY_OF_YEAR );
        final double PROPORTION_OF_YEAR = 365.0 / DAY_OF_YEAR;

        return this.calcProjection( distance, PROPORTION_OF_YEAR );
    }

    /** Calculates an annual projection for a given distance.
      * as in a distance of 40 km. at the end of march, it returns 120 km.
      * @param distance a given distance.
      * @param proportionOfYear the proportion of the year, i.e., 365/day.
      * @return a value for the projection of the distance.
      */
    public double calcProjection(int distance, double proportionOfYear)
    {
        return distance * proportionOfYear;
    }

    /** Updates this year info.
      * @param distance the distance to add (can be negative).
      * @param atPool indicates whether these meters are done at the pool.
      * @return a new YearInfo object.
      */
    public YearInfo addMeters(int distance, boolean atPool)
    {
        int totalPoolDistance = this.getDistance( SwimKind.POOL );

        if ( atPool ) {
            totalPoolDistance += distance;
        }

        return new YearInfo(
                this.getYear(),
                this.getTarget( SwimKind.TOTAL ),
                this.getDistance( SwimKind.TOTAL ) + distance,
                totalPoolDistance,
                this.getTarget( SwimKind.POOL )
        );
    }

    /** @return a string containing a textual representation of the data of this object. */
    @Override
    public String toString()
    {
        return String.format( Locale.getDefault(),
                            "%4d: %6d/%6d - Pool: %6d/%6d Open water: %6d/%6d",
                            this.getYear(),
                            this.getDistance( SwimKind.TOTAL ),
                            this.getTarget( SwimKind.TOTAL ),
                            this.getDistance( SwimKind.POOL ),
                            this.getTarget( SwimKind.POOL ),
                            this.getDistance( SwimKind.OWS ),
                            this.getTarget( SwimKind.OWS ));
    }

    /** @return the total meters/yards swum. */
    private int getDistanceTotal()
    {
        return distanceTotal;
    }

    /** @return the total meters/yards at the pool. */
    private int getDistancePool()
    {
        return distancePool;
    }

    /** @return the total meters/yards in open water. */
    private int getDistanceOWS()
    {
        return this.getDistanceTotal() - this.getDistancePool();
    }

    /** @return the progress made swimming, in total. */
    private double getProgressTotal()
    {
        return this.calcProgress(
                this.getDistance( SwimKind.TOTAL ),
                this.getTarget( SwimKind.TOTAL ) );
    }

    /** @return the OWS progress made up until now, between 0 - 100. */
    private double getProgressOWS()
    {
        return this.calcProgress(
                this.getDistance( SwimKind.OWS ),
                this.getTarget( SwimKind.OWS ) );
    }

    /** @return the progress at the pool made up until now, between 0 - 100. */
    private double getProgressPool()
    {
        return this.calcProgress(
                this.getDistance( SwimKind.POOL ),
                this.getTarget( SwimKind.POOL ) );
    }

    /** @return the target for this year, as meters/yards. */
    private int getTargetTotal()
    {
        return targetTotal;
    }

    /** @return the target distance to swim at the pool. */
    private int getTargetPool()
    {
        return this.targetPool;
    }

    /** @return the target distance to swim in OWS. */
    private int getTargetOWS()
    {
        return this.getTargetTotal() - this.getTargetPool();
    }

    /** Set the target distance at the pool.
      * @param newTargetPool the new target distance swimming at the pool.
      */
    private void setTargetPool(int newTargetPool)
    {
        if ( newTargetPool > this.targetTotal ) {
            this.targetTotal = newTargetPool;
        }

        this.targetPool = newTargetPool;
    }

    /** Change the target for total distance.
      * @param newTargetTotal the new target for the total distance.
      */
    private void setTargetTotal(int newTargetTotal)
    {
        final int OWS = this.getTargetOWS();

        if ( newTargetTotal < OWS ) {
            this.targetPool = 0;
        }

        this.targetTotal = newTargetTotal;
    }

    private int targetTotal;
    private int targetPool;
    private final int year;
    private final int distanceTotal;
    private final int distancePool;
}
