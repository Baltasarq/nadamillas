// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.core.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.devbaltasarq.nadamillas.core.YearInfo;

import java.io.IOException;

/** Represents a YearInfo in storage. */
public class YearInfoStorage {
    public static final String FIELD_YEAR = "_id";
    public static final String FIELD_TARGET = "target";
    public static final String FIELD_TOTAL = "total";
    public static final String FIELD_TOTAL_POOL = "total_pool";

    /** Creates a new wrapper.
      * @param yinfo the object YearInfo being wrapped.
      */
    public YearInfoStorage(YearInfo yinfo)
    {
        this.yearInfo = yinfo;
    }

    /** @return the YearInfo object begin wrapped. */
    public YearInfo getYearInfo()
    {
        return this.yearInfo;
    }

    /** @return a corresponding ContentValues object. */
    public ContentValues toValues()
    {
        final ContentValues toret = new ContentValues();

        toret.put( FIELD_YEAR, this.getYearInfo().getYear() );
        toret.put( FIELD_TOTAL, this.getYearInfo().getTotal() );
        toret.put( FIELD_TARGET, this.getYearInfo().getTarget() );
        toret.put( FIELD_TOTAL_POOL, this.getYearInfo().getTotalPool() );

        return toret;
    }

    /** Stores the info in JSON.
      * @param jsonWriter the stream to write to.
      * @throws IOException if something goes really wrong.
      */
    public void toJSON(JsonWriter jsonWriter) throws IOException
    {
        jsonWriter.beginObject();
        jsonWriter.name( FIELD_YEAR ).value( this.getYearInfo().getYear() );
        jsonWriter.name( FIELD_TOTAL ).value( this.getYearInfo().getTotal() );
        jsonWriter.name( FIELD_TARGET ).value( this.getYearInfo().getTarget() );
        jsonWriter.name( FIELD_TOTAL_POOL ).value( this.getYearInfo().getTotalPool() );
        jsonWriter.endObject();
    }

    /** Reads a YearInfo object from JSON.
      * @param jsonReader the stream to read from.
      * @return a YearInfo object reflecting the data.
      * @throws IOException if something goes really wrong.
      */
    public static YearInfo createFrom(JsonReader jsonReader) throws IOException
    {
        int year = -1;
        int target = -1;
        int total = -1;
        int poolTotal = -1;

        jsonReader.beginObject();

        while( jsonReader.hasNext() ) {
            final String NAME = jsonReader.nextName();

            if ( NAME.equals( FIELD_YEAR ) ) {
                year = jsonReader.nextInt();
            }
            else
            if ( NAME.equals( FIELD_TARGET ) ) {
                target = jsonReader.nextInt();
            }
            else
            if ( NAME.equals( FIELD_TOTAL ) ) {
                total = jsonReader.nextInt();
            }
            else
            if ( NAME.equals( FIELD_TOTAL_POOL ) ) {
                poolTotal = jsonReader.nextInt();
            } else {
                jsonReader.skipValue();
            }
        }

        jsonReader.endObject();

        if ( year == -1
          || target == -1
          || total == -1
          || poolTotal == -1 )
        {
            throw new IOException( "reading YearInfo from JSON: missing data" );
        }

        return new YearInfo( year, target, total, poolTotal );
    }

    /** Creates a new YearInfo object from the info stored in the Cursor.
      * @param cursor the database cursor object.
      * @return a new YearInfo object.
      */
    public static YearInfo createFrom(Cursor cursor)
    {
        int year = cursor.getInt( cursor.getColumnIndexOrThrow( YearInfoStorage.FIELD_YEAR ) );
        int target = cursor.getInt( cursor.getColumnIndexOrThrow( YearInfoStorage.FIELD_TARGET ) );
        int total = cursor.getInt( cursor.getColumnIndexOrThrow( YearInfoStorage.FIELD_TOTAL ) );
        int totalPool = cursor.getInt( cursor.getColumnIndexOrThrow( YearInfoStorage.FIELD_TOTAL_POOL ) );

        return new YearInfo( year, target, total, totalPool );
    }

    private YearInfo yearInfo;
}
