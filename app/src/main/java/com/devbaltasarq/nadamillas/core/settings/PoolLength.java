// NadaMillas (c) 2019-2023/24 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core.settings;


import android.util.Log;

import androidx.core.util.Pools;

import java.util.ArrayList;
import java.util.List;


public enum PoolLength {
    P25(25), P50(50), P100(100);

    private static final String LOG_TAG = PoolLength.class.getSimpleName();

    PoolLength(int d)
    {
        this.distance = d;
    }

    /** @return the length corresponding to this enum constant. */
    public int getLength()
    {
        return this.distance;
    }

    @Override
    public String toString()
    {
        return String.valueOf( this.distance );
    }

    /** Converts an int to its corresponding PoolLength.
     * @param length the length, as an int.
     * @return the corresponding PoolLength object.
     */
    public static PoolLength fromLength(int length)
    {
        final PoolLength[] VALUES = PoolLength.values();
        PoolLength toret = getDefault();

        for (PoolLength value : VALUES) {
            if ( value.getLength() == length ) {
                toret = value;
                break;
            }
        }

        return toret;
    }

    /** @return a collection with the values of PoolLength, as string. */
    public static List<String> toStringList()
    {
        if ( stringList == null ) {
            final ArrayList<String> toret =
                    new ArrayList<>( PoolLength.values().length );

            for(PoolLength pl: values()) {
                toret.add( pl.toString() );
            }

            stringList = toret;
        }


        return stringList;
    }

    /** @return the default pool length. */
    public static PoolLength getDefault()
    {
        return P25;
    }

    private final int distance;
    private static List<String> stringList = null;
}
