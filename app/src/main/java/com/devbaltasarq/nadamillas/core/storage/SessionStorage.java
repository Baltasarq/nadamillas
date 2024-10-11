// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core.storage;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.devbaltasarq.nadamillas.core.Duration;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Temperature;
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
    public static final String FIELD_IS_RACE = "race";
    public static final String FIELD_TEMPERATURE = "temperature";
    public static final String FIELD_PLACE = "place";
    public static final String FIELD_NOTES = "notes";

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
        toret.put( FIELD_PLACE, Util.capitalize( this.session.getPlace() ) );
        toret.put( FIELD_NOTES, Util.capitalize( this.session.getNotes() ) );
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
        jsonWriter.name( FIELD_PLACE ).value( this.session.getPlace() );
        jsonWriter.name( FIELD_NOTES ).value( this.session.getNotes() );
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
        boolean isRace = false;
        double temperature = Temperature.PREDETERMINED;
        String place = "";
        String notes = "";

        jsonReader.beginObject();

        while( jsonReader.hasNext() ) {
            final String NAME = jsonReader.nextName();

            switch (NAME) {
                case FIELD_SESSION_ID -> id = jsonReader.nextInt();
                case FIELD_YEAR -> year = jsonReader.nextInt();
                case FIELD_MONTH -> month = jsonReader.nextInt();
                case FIELD_DAY -> day = jsonReader.nextInt();
                case FIELD_DISTANCE -> distance = jsonReader.nextInt();
                case FIELD_AT_POOL -> atPool = jsonReader.nextBoolean();
                case FIELD_PLACE -> place = jsonReader.nextString();
                case FIELD_NOTES -> notes = jsonReader.nextString();
                case FIELD_SECONDS -> secs = jsonReader.nextInt();
                case FIELD_TEMPERATURE -> temperature = jsonReader.nextDouble();
                case FIELD_IS_RACE -> isRace = jsonReader.nextBoolean();
                default -> jsonReader.skipValue();
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

        return new Session(
                        id,
                        Util.dateFromData( year, month, day ),
                        distance,
                        new Duration( secs ),
                        atPool,
                        isRace,
                        temperature,
                        place,
                        notes );
    }

    /** Stores the Session's data in the given Bundle.
      * @param bundle the bundle to store the data in.
      */
    public void toBundle(Bundle bundle)
    {
        final Session SESSION = this.getSession();

        bundle.putLong( FIELD_DATE, SESSION.getDate().getTime() );
        bundle.putInt( FIELD_DISTANCE, SESSION.getDistance() );
        bundle.putInt( FIELD_SECONDS, SESSION.getDuration().getTimeInSeconds() );
        bundle.putBoolean( FIELD_AT_POOL, SESSION.isAtPool() );
        bundle.putString( FIELD_PLACE, Util.capitalize( SESSION.getPlace() ) );
        bundle.putString( FIELD_NOTES, Util.capitalize( SESSION.getNotes() ) );
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
        final int DAY = c.getInt( c.getColumnIndexOrThrow( FIELD_DAY ) );
        final int MONTH = c.getInt( c.getColumnIndexOrThrow( FIELD_MONTH ) );
        final int YEAR = c.getInt( c.getColumnIndexOrThrow( FIELD_YEAR ) );
        final boolean AT_POOL = c.getInt( c.getColumnIndexOrThrow( FIELD_AT_POOL ) ) != 0;
        final boolean IS_RACE = c.getInt( c.getColumnIndexOrThrow( FIELD_IS_RACE ) ) != 0;
        final double TEMPERATURE = c.getDouble( c.getColumnIndexOrThrow( FIELD_TEMPERATURE ) );
        final String PLACE = c.getString( c.getColumnIndexOrThrow( FIELD_PLACE ) );
        final String NOTES = c.getString( c.getColumnIndexOrThrow( FIELD_NOTES ) );
        int secsColumn = c.getColumnIndex( FIELD_SECONDS );
        int secs = 0;

        // Fetch the seconds, if available
        if ( secsColumn >= 0 ) {
            secs = c.getInt( secsColumn );
        }

        return new Session(
                        ID,
                        Util.dateFromData( YEAR, MONTH, DAY ),
                        DISTANCE,
                        new Duration( secs ),
                        AT_POOL,
                        IS_RACE,
                        TEMPERATURE,
                        PLACE,
                        NOTES );
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
                        new Duration( extras.getInt( FIELD_SECONDS, 0 ) ),
                        extras.getBoolean( FIELD_AT_POOL, true ),
                        extras.getBoolean( FIELD_IS_RACE, false ),
                        extras.getDouble( FIELD_TEMPERATURE, Temperature.PREDETERMINED ),
                        extras.getString( FIELD_PLACE, "" ),
                        extras.getString( FIELD_NOTES, "" ));
        }

        return toret;
    }

    private final Session session;
}
