// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.core.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.devbaltasarq.nadamillas.core.YearInfo;
import com.devbaltasarq.nadamillas.core.settings.DistanceUtils;

import java.io.IOException;
import java.time.Year;

/** Represents a YearInfo in storage. */
public class YearInfoStorage {
    public static final String FIELD_YEAR = "_id";
    public static final String FIELD_TARGET = "target";
    public static final String FIELD_POOL_TARGET = "pool_target";
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
        final YearInfo INFO = this.getYearInfo();

        toret.put( FIELD_YEAR, INFO.getYear() );
        toret.put( FIELD_TOTAL, INFO.getDistance( YearInfo.SwimKind.TOTAL ) );
        toret.put( FIELD_TARGET, INFO.getTarget( YearInfo.SwimKind.TOTAL ) );
        toret.put( FIELD_TOTAL_POOL, INFO.getDistance( YearInfo.SwimKind.POOL ) );
        toret.put( FIELD_POOL_TARGET, INFO.getTarget( YearInfo.SwimKind.POOL ) );

        return toret;
    }

    /** Stores the info in JSON.
      * @param jsonWriter the stream to write to.
      * @throws IOException if something goes really wrong.
      */
    public void toJSON(JsonWriter jsonWriter) throws IOException
    {
        final YearInfo INFO = this.getYearInfo();

        jsonWriter.beginObject();
        jsonWriter.name( FIELD_YEAR )
                        .value( INFO.getYear() );
        jsonWriter.name( FIELD_TOTAL )
                        .value( INFO.getDistance( YearInfo.SwimKind.TOTAL ) );
        jsonWriter.name( FIELD_TARGET )
                        .value( INFO.getTarget( YearInfo.SwimKind.TOTAL ) );
        jsonWriter.name( FIELD_TOTAL_POOL )
                        .value( INFO.getDistance( YearInfo.SwimKind.POOL ) );
        jsonWriter.name( FIELD_POOL_TARGET )
                        .value( INFO.getTarget( YearInfo.SwimKind.POOL ) );
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
        int targetPool = 0;

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
            if ( NAME.equals( FIELD_POOL_TARGET ) ) {
                targetPool = jsonReader.nextInt();
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

        return new YearInfo( year, target, total, poolTotal, targetPool );
    }

    /** Creates a new YearInfo object from the info stored in the Cursor.
      * @param cursor the database cursor object.
      * @return a new YearInfo object.
      */
    public static YearInfo createFrom(Cursor cursor)
    {
        int year = cursor.getInt( cursor.getColumnIndexOrThrow( FIELD_YEAR ) );
        int target = cursor.getInt( cursor.getColumnIndexOrThrow( FIELD_TARGET ) );
        int total = cursor.getInt( cursor.getColumnIndexOrThrow( FIELD_TOTAL ) );
        int totalPool = cursor.getInt( cursor.getColumnIndexOrThrow( FIELD_TOTAL_POOL ) );
        int targetPoolColIndex = cursor.getColumnIndex( FIELD_POOL_TARGET );
        int targetPool = 0;

        if ( targetPoolColIndex > -1 ) {
            targetPool = cursor.getInt( targetPoolColIndex );
        }

        return new YearInfo( year, target, total, totalPool, targetPool );
    }

    private final YearInfo yearInfo;
}
