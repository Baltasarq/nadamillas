// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.core;

import java.util.Locale;

/** Represents the accumulated distances. */
public class YearInfo {
    private static final int DEFAULT_TARGET = 200000;
    public static final String NOT_APPLYABLE = "N/A";

    /** Creates a default year info.
      * @param year the year to create the YearInfo for.
      */
    public YearInfo(int year, int distance, int poolDistance)
    {
        this( year, DEFAULT_TARGET, distance, poolDistance );
    }

    /** Creates a new instance (meters/yards).
      * @param year The year this distances pertain to.
      * @param target The target distance.
      * @param t Total distance.
      * @param tp Total distance in the pool.
      */
    public YearInfo(int year, int target, int t, int tp)
    {
        this.year = year;
        this.target = target;
        this.total = t;
        this.totalPool = tp;
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

    /** @return the target for this year, as meters/yards. */
    public int getTarget()
    {
        return target;
    }

    /** Change the target.
      * @param target the new target
      */
    public void setTarget(int target)
    {
        this.target = target;
    }

    /** @return the target for this year, as a string. */
    public String getTargetAsString(Settings settings)
    {
        final int TARGET = this.getTarget();
        String toret = NOT_APPLYABLE;

        if ( TARGET >= 0 ) {
            toret = settings.toUnitsAsString( TARGET );
        }

        return toret;
    }

    /** @return the total meters/yards. */
    public int getTotal()
    {
        return total;
    }

    /** @return the total meters, as a string. */
    public String getTotalAsString(Settings settings)
    {
        return settings.toUnitsAsString( this.getTotal() );
    }

    /** @return the total meters/yards at the pool. */
    public int getTotalPool()
    {
        return totalPool;
    }

    /** @return the total meters/yards at the pool, as a string. */
    public String getTotalPoolAsString(Settings settings)
    {
        return settings.toUnitsAsString( this.getTotalPool() );
    }

    /** @return the total meters/yards in open water. */
    public int getTotalOpenWater()
    {
        return this.getTotal() - this.getTotalPool();
    }

    /** @return the total meters/yards in open waters, as a string. */
    public String getTotalOpenWaterAsString(Settings settings)
    {
        return settings.toUnitsAsString( this.getTotalOpenWater() );
    }

    /** @return the progress made up until now, between 0 - 100. */
    public double getProgress()
    {
        final int TOTAL = this.getTotal();
        final int TARGET = this.getTarget();
        double toret = 0;

        if ( TOTAL >= 0
          && TARGET > 0 )
        {
            toret = ( (double) this.getTotal() / this.getTarget() ) * 100;
        }

        return toret;
    }

    /** @return the progress made, as a string (without an ending '%'). */
    public String getProgressAsString()
    {
        double progress = this.getProgress();
        String toret = NOT_APPLYABLE;

        if ( progress > -1 ) {
            toret = String.format( Locale.getDefault(), "%6.2f", progress );
        }

        return toret;
    }

    /** Updates this year info.
      * @param distance the distance to add (can be negative).
      * @param atPool indicates whether these meters are done at the pool.
      * @return a new YearInfo object.
      */
    public YearInfo addMeters(int distance, boolean atPool)
    {
        int totalPoolDistance = this.getTotalPool();

        if ( atPool ) {
            totalPoolDistance += distance;
        }

        return new YearInfo(
                this.getYear(),
                this.getTarget(),
                this.getTotal() + distance,
                totalPoolDistance
        );
    }

    /** @return a string containing a textual representation of the data of this object. */
    @Override
    public String toString()
    {
        return String.format( Locale.getDefault(),
                            "%4d: %6d/%6d - Pool: %6d Open water: %6d",
                            this.getYear(),
                            this.getTotal(),
                            this.getTarget(),
                            this.getTotalPool(),
                            this.getTotalOpenWater() );
    }

    private int target;
    private final int year;
    private final int total;
    private final int totalPool;
}
