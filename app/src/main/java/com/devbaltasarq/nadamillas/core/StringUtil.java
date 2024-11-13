// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core;


public final class StringUtil {
    /** Capitalize a string, e.g. "capital city" -> "Capital city"
     * @param s the string to capitalize.
     * @return a string with the upper first letter and the rest in lower case.
     */
    public static String capitalize(String s)
    {
        String toret = s;

        // Test for null
        if ( toret == null ) {
            toret = "";
        } else {
            toret = toret.trim();
        }

        // Capitalize it
        if ( !toret.isEmpty() ) {
            toret = toret.substring( 0, 1 ).toUpperCase()
                    + toret.substring( 1 ).toLowerCase();
        }

        return toret;
    }
}
