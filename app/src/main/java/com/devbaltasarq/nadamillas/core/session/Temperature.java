// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core.session;


import java.util.Locale;


/** Represents a given temperature. */
public final class Temperature {
    public static final double PREDETERMINED = 18.0;

    public Temperature(double temperature)
    {
        this.temperature = temperature;
    }

    /** @return the actual value of the temperature. */
    public double getValue()
    {
        return this.temperature;
    }

    @Override
    public String toString()
    {
        return String.format( Locale.getDefault(),
                "%5.2fº",
                this.getValue() );
    }

    private final double temperature;

    public static Temperature predetermined()
    {
        if ( predetermined == null ) {
            predetermined = new Temperature( PREDETERMINED );
        }

        return predetermined;
    }

    private static Temperature predetermined = null;
}
