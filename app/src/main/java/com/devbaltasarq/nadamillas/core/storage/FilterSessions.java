// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core.storage;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class FilterSessions {
    public enum Field {
        Distance, Temperature, Date, Time;

        public static Field fromString(String s)
        {
            return Field.values()[ FromStringer.fromString( s, STR_FIELDS ) ];
        }

        @Override
        public String toString()
        {
            return STR_FIELDS.get( this.ordinal() );
        }

        public final static List<String> STR_FIELDS = new ArrayList<>(
                Arrays.asList( "distance", "temperature", "date", "time" )
        );
    }

    public enum Operator {
        Equal, NotEqual, Less, More;

        public static Operator fromString(String s)
        {
            return Operator.values()[ FromStringer.fromString( s, STR_OPERATORS ) ];
        }

        @Override
        public String toString()
        {
            return STR_OPERATORS.get( this.ordinal() );
        }

        public final static List<String> STR_OPERATORS = new ArrayList<>(
                Arrays.asList( "=", "!=", "<", ">" )
        );
    }

    /** Create a new filter for sessions.
      * @param f the field from which for filter.
      * @param opr the operator to compare the field with the value.
      * @param value the value to compare the field with.
      * @see Field
      * @see Operator
      */
    public FilterSessions(Field f, Operator opr, String value)
    {
        this.field = f;
        this.opr = opr;
        this.value = value;
    }

    /** @return the field. */
    public Field getField()
    {
        return this.field;
    }

    /** @return the operator. */
    public Operator getOperator()
    {
        return this.opr;
    }

    /** @return the value. */
    public String getValue()
    {
        return this.value;
    }

    /** Convert the info in the class to a SQL condition,
      * such as "temperature < 17"
      * @return a string with the SQL condition.
      */
    public String toSQLCondition()
    {
        return this.field
                + " " + this.opr
                + " ?";
    }

    @Override
    public String toString()
    {
        return "[" + this.field + " " + this.opr + " " + this.value + "]";
    }

    private final Field field;
    private final Operator opr;
    private final String value;


    /** Utiliy class for looking for a string in a vector. */
    private static class FromStringer {
        /**
         * @param s the value to look for
         * @param VALUES the collection to look for the value in.
         * @return the position of s in VALUES.
         */
        public static int fromString(String s, final Collection<String> VALUES)
        {
            int toret = -1;

            s = s.toLowerCase();

            int i = 0;
            for(String v: VALUES) {
                if ( s.equals( v ) ) {
                    toret = i;
                    break;
                }

                ++i;
            }

            if ( toret < 0 ) {
                throw new IllegalArgumentException( s );
            }

            return toret;
        }
    }
}
