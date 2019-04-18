// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.core.storage;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Util;

import java.io.IOException;
import java.util.Date;

/** Represents a session stored in the database. */
public class SessionStorage {
    public static final String LOG_TAG = SessionStorage.class.getSimpleName();
    public static final String FIELD_SESSION_ID = "_id";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_YEAR = "year";
    public static final String FIELD_MONTH = "month";
    public static final String FIELD_DAY = "day";
    public static final String FIELD_DISTANCE = "distance";
    public static final String FIELD_SECONDS = "seconds_used";
    public static final String FIELD_AT_POOL = "pool";

    /** Create a new wrapper for the Session. */
    public SessionStorage(Session session)
    {
        this.session = session;
    }

    /** Creates a ContentValues of object from a Session.
     * @return a Session object.
     */
    public ContentValues toValues()
    {
        final ContentValues toret = new ContentValues();
        final int[] DATE_DATA = Util.dataFromDate( this.session.getDate() );

        toret.put( FIELD_SESSION_ID, this.session.getId() );
        toret.put( FIELD_YEAR, DATE_DATA[ 0 ] );
        toret.put( FIELD_MONTH, DATE_DATA[ 1 ] );
        toret.put( FIELD_DAY, DATE_DATA[ 2 ] );
        toret.put( FIELD_DISTANCE, this.session.getDistance() );
        toret.put( FIELD_AT_POOL, this.session.isAtPool() );
        toret.put( FIELD_SECONDS, this.session.getDuration().getTimeInSeconds() );

        return toret;
    }

    /** Stores the info of the session to JSON.
      * @param jsonWriter the stream to write to.
      * @throws IOException if something goes really bad.
      */
    public void toJSON(JsonWriter jsonWriter) throws IOException
    {
        final int[] DATE_DATA = Util.dataFromDate( this.session.getDate() );

        jsonWriter.beginObject();
        jsonWriter.name( FIELD_SESSION_ID ).value( this.session.getId() );
        jsonWriter.name( FIELD_YEAR ).value( DATE_DATA[ 0 ] );
        jsonWriter.name( FIELD_MONTH ).value( DATE_DATA[ 1 ] );
        jsonWriter.name( FIELD_DAY ).value( DATE_DATA[ 2 ] );
        jsonWriter.name( FIELD_DISTANCE ).value( this.session.getDistance() );
        jsonWriter.name( FIELD_SECONDS ).value( this.session.getDuration().getTimeInSeconds() );
        jsonWriter.name( FIELD_AT_POOL ).value( this.session.isAtPool() );
        jsonWriter.endObject();
    }

    /** Reads a Session object from JSON.
     * @param jsonReader the stream to read from.
     * @return a Session object reflecting the data.
     * @throws IOException if something goes really wrong.
     */
    public static Session createFrom(JsonReader jsonReader) throws IOException
    {
        int id = -1;
        int year = -1;
        int month = -1;
        int day = -1;
        int distance = -1;
        int secs = 0;
        boolean atPool = false;

        jsonReader.beginObject();

        while( jsonReader.hasNext() ) {
            final String NAME = jsonReader.nextName();

            if ( NAME.equals( FIELD_SESSION_ID ) ) {
                id = jsonReader.nextInt();
            }
            else
            if ( NAME.equals( FIELD_YEAR ) ) {
                year = jsonReader.nextInt();
            }
            else
            if ( NAME.equals( FIELD_MONTH ) ) {
                month = jsonReader.nextInt();
            }
            else
            if ( NAME.equals( FIELD_DAY ) ) {
                day = jsonReader.nextInt();
            }
            else
            if ( NAME.equals( FIELD_DISTANCE ) ) {
                distance = jsonReader.nextInt();
            }
            else
            if ( NAME.equals( FIELD_AT_POOL ) ) {
                atPool = jsonReader.nextBoolean();
            }
            else
            if ( NAME.equals( FIELD_SECONDS ) ) {
                secs = jsonReader.nextInt();
            } else {
                jsonReader.skipValue();
            }
        }

        jsonReader.endObject();

        if ( id == -1
          || year == -1
          || month == -1
          || day == -1
          || distance == -1 )
        {
            throw new IOException( "reading YearInfo from JSON: missing data" );
        }

        return new Session( id, Util.dateFromData( year, month, day ), distance, secs, atPool );
    }

    /** Stores the Session's data in the given Bundle.
      * @param bundle the bundle to store the data in.
      */
    public void toBundle(Bundle bundle)
    {
        final Session SESSION = this.getSession();

        bundle.putLong( FIELD_DATE, SESSION.getDate().getTime() );
        bundle.putBoolean( FIELD_AT_POOL, SESSION.isAtPool() );
        bundle.putInt( FIELD_DISTANCE, SESSION.getDistance() );
        bundle.putInt( FIELD_SECONDS, SESSION.getDuration().getTimeInSeconds() );
    }

    /** @return the session wrapped. */
    public Session getSession()
    {
        return this.session;
    }

    /** Creates a Session of object from a Cursor.
      * @param c A Cursor to extract the data from.
      * @return a Session object.
      */
    public static Session createFrom(Cursor c)
    {
        // Extract data from cursor
        final int ID = c.getInt( c.getColumnIndexOrThrow( FIELD_SESSION_ID ) );
        final int DISTANCE = c.getInt( c.getColumnIndexOrThrow( FIELD_DISTANCE ) );
        final boolean AT_POOL = c.getInt( c.getColumnIndexOrThrow( FIELD_AT_POOL ) ) != 0;
        final int DAY = c.getInt( c.getColumnIndexOrThrow( FIELD_DAY ) );
        final int MONTH = c.getInt( c.getColumnIndexOrThrow( FIELD_MONTH ) );
        final int YEAR = c.getInt( c.getColumnIndexOrThrow( FIELD_YEAR ) );
        int secsColumn = c.getColumnIndex( FIELD_SECONDS );
        int secs = 0;

        // Fetch the seconds, if available
        if ( secsColumn >= 0 ) {
            secs = c.getInt( secsColumn );
        }

        return new Session( ID, Util.dateFromData( YEAR, MONTH, DAY ), DISTANCE, secs, AT_POOL );
    }

    /** Creates a Session object from a Bundle.
     * @param data the bundle to build the Session from.
     * @return a new Session object.
     */
    public static Session createFrom(Intent data)
    {
        Session toret = null;

        if ( data != null ) {
            toret = createFrom( data.getExtras() );
        } else {
            Log.e( LOG_TAG, "ERROR: null intent to create session from!!!" );
            System.exit( -1 );
        }

        return toret;
    }

    /** Creates a Session object from a Bundle.
      * @param extras the bundle to build the Session from.
      * @return a new Session object.
      */
    public static Session createFrom(Bundle extras)
    {
        Session toret = null;

        if ( extras != null ) {
            final long TODAY = Util.getDate().getTimeInMillis();

            toret = new Session(
                    new Date( extras.getLong( FIELD_DATE, TODAY ) ),
                    extras.getInt( FIELD_DISTANCE, 0 ),
                    extras.getInt( FIELD_SECONDS, 0 ),
                    extras.getBoolean( FIELD_AT_POOL, true ) );
        }

        return toret;
    }

    private Session session;
}
