// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.devbaltasarq.nadamillas.core.storage.SessionStorage;
import com.devbaltasarq.nadamillas.core.storage.YearInfoStorage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;


public class DataStore extends SQLiteOpenHelper {
    private static final String LOG_TAG = DataStore.class.getSimpleName();
    public static final String DIR_BACKUP_NAME = "backup";
    public static final String EXT_BACKUP_FILE = "json";
    private static final int VERSION = 4;
    private static final String NAME = "swimming_workouts";
    private static final String TABLE_YEARS = "years";
    private static final String TABLE_SESSIONS = "workouts";

    private DataStore(Context context)
    {
        super( context, NAME, null, VERSION );

        this.context = context;
        Log.d( LOG_TAG, "database opened");

        DIR_DOWNLOADS = null;
        if ( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q )
        {
            DIR_DOWNLOADS = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS );
        }

        DIR_TEMP = context.getCacheDir();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.d( LOG_TAG, "creating tables");

        try{
            db.beginTransaction();

            createYearsInfoTable( db );
            createSessionsTable( db );

            db.setTransactionSuccessful();
            Log.d( LOG_TAG, "tables created");
        } catch(SQLException exc) {
            Log.e( LOG_TAG, "error creating tables: " + exc.getMessage() );
        } finally {
            db.endTransaction();
        }

        return;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.d( LOG_TAG, "upgrading database.");

        try{
            db.beginTransaction();

            createYearsInfoTable( db );
            createSessionsTable( db );
            updateSessionsTable( db, oldVersion, newVersion );

            db.setTransactionSuccessful();
            Log.d( LOG_TAG, "tables updated");
        } catch(SQLException exc) {
            Log.e( LOG_TAG, "error upgrading: updating tables: " + exc.getMessage() );
        } finally {
            db.endTransaction();
        }

        this.onCreate( db );
    }

    private static void removeSessionsTable(SQLiteDatabase db)
    {
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_SESSIONS );
    }

    private static void createSessionsTable(SQLiteDatabase db)
    {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_SESSIONS + "("
                        + SessionStorage.FIELD_SESSION_ID + " integer PRIMARY KEY,"
                        + SessionStorage.FIELD_YEAR + " integer NOT NULL,"
                        + SessionStorage.FIELD_MONTH + " integer NOT NULL,"
                        + SessionStorage.FIELD_DAY + " integer NOT NULL,"
                        + SessionStorage.FIELD_DISTANCE + " integer NOT NULL,"
                        + SessionStorage.FIELD_AT_POOL + " boolean NOT NULL,"
                        + SessionStorage.FIELD_SECONDS + " integer NOT NULL)"
        );
    }

    private static void createYearsInfoTable(SQLiteDatabase db)
    {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_YEARS + "("
                        + YearInfoStorage.FIELD_YEAR + " integer PRIMARY KEY,"
                        + YearInfoStorage.FIELD_TARGET + " integer NOT NULL,"
                        + YearInfoStorage.FIELD_TOTAL + " integer NOT NULL,"
                        + YearInfoStorage.FIELD_TOTAL_POOL + " integer NOT NULL)"
        );
    }

    private static void updateSessionsTable(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if ( newVersion > oldVersion ) {
            createSessionsTable( db );

            if ( oldVersion <= 3 ) {
                // Add the time to the sessions table.
                db.execSQL( "ALTER TABLE " + TABLE_SESSIONS
                        + " ADD COLUMN " +  SessionStorage.FIELD_SECONDS
                        + " integer NOT NULL DEFAULT 0;" );
            }
        } else {
            // We know nothing about how to update, remove everything
            removeSessionsTable( db );
        }

        return;
    }

    private File getBackupDir()
    {
        return this.context.getDir( DIR_BACKUP_NAME,  Context.MODE_PRIVATE );
    }

    /** Retrives the accumulated distances from the database.
      * @return a YearInfo object.
      */
    public YearInfo getCurrentYearInfo()
    {
        final Calendar TODAY = Calendar.getInstance();

        return this.getInfoFor( TODAY.get( Calendar.YEAR ) );
    }

    /** Retrieves the accumulated distances from the database.
      * @param d the Date object from which the year number will be extracted.
      * @return a YearInfo object.
      */
    public YearInfo getInfoFor(Date d)
    {
        return getInfoFor( Util.getYearFrom( d ) );
    }

    /** Retrieves the accumulated distances from the database.
      * @param year the year for the accumulated distances, as an int.
      * @return a YearInfo object.
      */
    public YearInfo getInfoFor(int year)
    {
        YearInfo toret = null;
        final Cursor CURSOR = this.getAllYearInfosCursorWith(
                                        YearInfoStorage.FIELD_YEAR + "=?",
                                        new String[]{ Integer.toString( year ) } );

        if ( CURSOR.moveToFirst() ) {
            toret = YearInfoStorage.createFrom( CURSOR );
        } else {
            Log.e( LOG_TAG, "records found for " + year + ": " + CURSOR.getCount() );
        }

        return toret;
    }

    public Cursor getDescendingAllYearInfosCursor()
    {
        return this.getAllYearInfosCursorWith( null, null,
                                               YearInfoStorage.FIELD_YEAR + " DESC" );
    }

    private Cursor getAllYearInfosCursorWith(String query, String[] queryArgs)
    {
        return this.getAllYearInfosCursorWith( query, queryArgs, null );
    }

    private Cursor getAllYearInfosCursorWith(String query, String[] queryArgs, String orderBy)
    {
        final SQLiteDatabase DB = this.getReadableDatabase();

        return DB.query( TABLE_YEARS,
                null,
                query,
                queryArgs,
                null, null, orderBy );
    }

    /** Creates a YearInfo object for the given data.
     * @param year the year of interest.
     * @param distance the distance covered
     * @param atPool is this distance at the pool?
     */
    public void createYearInfoFor(int year, int distance, boolean atPool)
    {
        int poolDistance = 0;

        if ( atPool ) {
            poolDistance = distance;
        }

        this.add( new YearInfo( year, distance, poolDistance ) );
    }

    /** Adds a new YearInfo object to the data store.
      * @param yinfo the info to store.
      */
    public void add(YearInfo yinfo)
    {
        final ContentValues VALUES = new YearInfoStorage( yinfo ).toValues();
        final String STR_YEAR = Integer.toString( yinfo.getYear() );
        final SQLiteDatabase DB = this.getWritableDatabase();

        Log.d( LOG_TAG, "storing year info record: " + yinfo );

        final Cursor CURSOR = this.getAllYearInfosCursorWith(
                                        YearInfoStorage.FIELD_YEAR + "=?",
                                        new String[]{ STR_YEAR } );

        // Found? Delete it.
        if ( CURSOR.moveToFirst() ) {
            Log.d( LOG_TAG, "erasing existing year info record" );
            DB.delete( TABLE_YEARS, YearInfoStorage.FIELD_YEAR + "=?", new String[]{ STR_YEAR } );
        }

        try {
            Log.d( LOG_TAG, "inserting year info record" );
            DB.beginTransaction();
            DB.insert( TABLE_YEARS, null, VALUES );
            DB.setTransactionSuccessful();
            Log.d( LOG_TAG, "inserted year info record" );
        } catch(SQLException exc) {
            Log.e( LOG_TAG, "creating year info: " + yinfo + ": " + exc.getMessage() );
        } finally {
            DB.endTransaction();
        }

        return;
    }

    /** Updates the year info.
      * @param date the date for this update.
      * @param distance the integer for updating the totals (can be negative).
      * @param atPool a boolean indicating whether these meters were at the pool.
      */
    private void updateYearInfo(Date date, int distance, boolean atPool)
    {
        final YearInfo YEAR_INFO = this.getInfoFor( date );
        YearInfo NEW_YEAR_INFO;

        if ( YEAR_INFO != null ) {
            NEW_YEAR_INFO = YEAR_INFO.addMeters( distance, atPool );
            Log.d( LOG_TAG,"Updating year info, adding:" + distance + ", at pool: " + atPool );
        } else {
            NEW_YEAR_INFO = new YearInfo( 2019, distance, atPool? distance : 0 );
        }

        this.updateYearInfo( NEW_YEAR_INFO );
    }

    private void updateYearInfo(Date date, int distToSubstract, boolean atPool, int distToSum)
    {
        final YearInfo YEAR_INFO = this.getInfoFor( date );

        Log.d( LOG_TAG,"Updating year info, adding:"
                            + distToSum + ", at pool: " + atPool
                            + ", removing: " + distToSubstract + ", at pool: " + !atPool );

        // Prepare new year info
        YearInfo newYearInfo = YEAR_INFO.addMeters( distToSubstract * -1, atPool );
        newYearInfo = newYearInfo.addMeters( distToSum, !atPool );

        this.updateYearInfo( newYearInfo );
    }

    private void updateYearInfo(YearInfo yinfo)
    {
        final int YEAR = yinfo.getYear();
        final SQLiteDatabase DB = this.getWritableDatabase();
        final YearInfo YEAR_INFO = this.getInfoFor( yinfo.getYear() );

        Log.d( LOG_TAG,"updating year info: " + yinfo );

        if ( YEAR_INFO == null ) {
            Log.d( LOG_TAG, "detected the need to create the year info" );
            final YearInfo CREATED_INFO = new YearInfo( YEAR, 0, 0 );
            Log.d( LOG_TAG, "storing year info: " + CREATED_INFO );
            this.createYearInfoFor( YEAR, 0, true );
        }

        try {
            DB.beginTransaction();
            DB.update( TABLE_YEARS,
                    new YearInfoStorage( yinfo ).toValues(),
                    YearInfoStorage.FIELD_YEAR + "=?",
                    new String[]{ yinfo.getYearAsString() } );
            DB.setTransactionSuccessful();
        } catch(SQLException exc) {
            Log.e( LOG_TAG, "error updating year info: " + exc.getMessage() );
        } finally {
            DB.endTransaction();
        }

        return;
    }

    /** Get all training sessions for a given month.
     * @param d the date to look for sessions.
     * @return an array of Session.
     */
    public Session[] getSessionsForMonth(Date d)
    {
        final int[] YMD = Util.dataFromDate( d );

        return getSessionsForMonth( YMD[ 0 ], YMD[ 1 ] );
    }

    /** Retrieves all sessions for a given month.
     * @param year the given year.
     * @param month the given month.
     * @return An array of Session.
     */
    public Session[] getSessionsForMonth(int year, int month)
    {
        final String QUERY = SessionStorage.FIELD_YEAR + "=? and "
                            + SessionStorage.FIELD_MONTH + "=?";
        final String[] QUERY_ARGS = new String[] {
                Integer.toString( year ),
                Integer.toString( month )
        };

        return retrieveSessionsWith( QUERY, QUERY_ARGS );
    }

    /** Get all training sessions for a given date.
     * @param d the date to look for sessions.
     * @return an array of Session.
     */
    public Session[] getSessionsForDay(Date d)
    {
        final int[] YMD = Util.dataFromDate( d );

        return getSessionsForDay( YMD[ 0 ], YMD[ 1 ], YMD[ 2 ] );
    }

    /** Retrieves a particular session from the data store.
     * @param id the id of the session to retrieve.
     * @return a Session object, or null if not found.
     */
    public Session getSessionFor(int id)
    {
        final String QUERY = SessionStorage.FIELD_SESSION_ID + "=?";
        final String[] QUERY_ARGS = new String[] { Integer.toString( id ) };
        final Session[] SESSIONS = this.retrieveSessionsWith( QUERY, QUERY_ARGS );
        Session toret = null;

        if ( SESSIONS.length > 0 ) {
            toret = SESSIONS[ 0 ];
        }

        return toret;
    }

    /** Retrieves all sessions for a given year and month.
     * @param year the given year.
     * @param month the given month.
     * @param day the given day of the month.
     * @return An array of Session.
     */
    public Session[] getSessionsForDay(int year, int month, int day)
    {
        final String QUERY = SessionStorage.FIELD_YEAR + "=? and "
                + SessionStorage.FIELD_MONTH + "=? and "
                + SessionStorage.FIELD_DAY + "=?";
        final String[] QUERY_ARGS = new String[] {
                Integer.toString( year ),
                Integer.toString( month ),
                Integer.toString( day )
        };

        return retrieveSessionsWith( QUERY, QUERY_ARGS );
    }

    /** Converts a query to a string, only for debugging purposes.
      * @param query the query, such as 'NAME=?'
      * @param queryArgs the args, such as 'baltasar'
      * @return a string such as 'NAME=baltasar'
      */
    private String stringFromQuery(String query, String[] queryArgs)
    {
        StringBuilder toret = new StringBuilder();

        toret.append( "WHERE " );

        if ( query != null ) {
            int i = 0;
            int oldPos = 0;
            int pos = query.indexOf( '?' );

            while( pos >= 0 ) {
                toret.append( query.substring( oldPos, pos ) );
                toret.append( queryArgs[ i ] );
                ++i;

                oldPos = pos + 1;
                pos = query.indexOf( '?', oldPos );
            }
        } else {
            toret.append( "ALL" );
        }

        return toret.toString();
    }

    /** Retrieves sessions giving a query and its arguments.
      * This method returns all sessions, don't use it for a year of sessions.
      * @param query the query string, such as NAME=?
      * @param queryArgs the query args, such as [ "Baltasar" ]
      * @return an array of Session.
      */
    private Session[] retrieveSessionsWith(String query, String[] queryArgs)
    {
        Cursor cursor = null;
        Session[] toret = null;
        int numSessions = 0;

        Log.d( LOG_TAG, "Retrieve with query: " + stringFromQuery( query, queryArgs ) );

        try {
            // Build query
            cursor = this.getAllSessionsCursorWith( query, queryArgs );
            toret = new Session[ cursor.getCount() ];

            // Retrieve sessions
            if ( cursor.moveToFirst() ) {
                int pos = 0;

                do {
                    toret[ pos ] = SessionStorage.createFrom( cursor );
                    ++pos;
                } while( cursor.moveToNext() );
            }

            numSessions = toret.length;
        } catch(SQLException exc) {
            Log.e( LOG_TAG, exc.getMessage() );
        } finally {
            close( cursor );
        }

        Log.d( LOG_TAG, "retrieved " + numSessions + " sessions" );
        return toret;
    }

    public Cursor getAllSessionsCursorForCurrentYear()
    {
        return this.getAllSessionsCursorFor( Util.getYearFrom( Util.getDate().getTime() ) );
    }

    public Cursor getAllSessionsCursorFor(int year)
    {
        return this.getAllSessionsCursorWith(
                SessionStorage.FIELD_YEAR + "=?",
                new String[]{ Integer.toString( year ) } );
    }

    public Cursor getAllSessionsCursor()
    {
        return this.getAllSessionsCursorWith( null, null, null );
    }

    public Cursor getAllDescendingSessionsCursor()
    {
        return this.getAllSessionsCursorWith( null, null,
                                            SessionStorage.FIELD_YEAR + " DESC, "
                                                    + SessionStorage.FIELD_MONTH + " DESC, "
                                                    + SessionStorage.FIELD_DAY + " DESC" );
    }

    private Cursor getAllSessionsCursorWith(String query, String[] queryArgs)
    {
        return this.getAllSessionsCursorWith( query, queryArgs, null );
    }

    private Cursor getAllSessionsCursorWith(String query, String[] queryArgs, String orderBy)
    {
        final SQLiteDatabase DB = this.getReadableDatabase();

        return DB.query(
                TABLE_SESSIONS,
                null,
                query,
                queryArgs,
                null, null,
                orderBy );
    }

    public void recalculateAll()
    {
        final Cursor YEAR_INFO_CURSOR = this.getDescendingAllYearInfosCursor();

        if ( YEAR_INFO_CURSOR.moveToFirst() ) {
            do {
                this.recalculate(
                        YEAR_INFO_CURSOR.getInt(
                            YEAR_INFO_CURSOR.getColumnIndexOrThrow(
                                    YearInfoStorage.FIELD_YEAR
                            )
                        )
                );
            } while( YEAR_INFO_CURSOR.moveToNext() );
        }

        return;
    }

    public void recalculateCurrentYear()
    {
        final Calendar CALENDAR = Calendar.getInstance();

        this.recalculate( CALENDAR.get( Calendar.YEAR ) );
    }

    public void recalculate(int year)
    {
        Cursor cursor = null;
        YearInfo info = null;

        int target = 0;
        int totalMeters = 0;
        int totalMetersPool = 0;

        try {
            cursor = this.getAllSessionsCursorFor( year );
            info = this.getInfoFor( year );

            if ( cursor.moveToFirst() ) {
                do {
                    int distance = cursor.getInt(
                                        cursor.getColumnIndexOrThrow( SessionStorage.FIELD_DISTANCE ) );
                    boolean atPool = cursor.getInt(
                                        cursor.getColumnIndexOrThrow( SessionStorage.FIELD_AT_POOL) ) > 0;

                    totalMeters += distance;

                    if ( atPool ) {
                        totalMetersPool += distance;
                    }
                } while( cursor.moveToNext() );
            }
        } catch(SQLException exc) {
            Log.e( LOG_TAG, exc.getMessage() );
        } finally {
            close( cursor );
        }

        // Build & store the target
        if ( info != null ) {
            target = info.getTarget();
        }

        final YearInfo TORET = new YearInfo( year, totalMeters, totalMetersPool );
        TORET.setTarget( target );
        this.add( TORET );
    }

    /** Adds a new session to the database.
      * @param session the session to store.
      */
    public void add(Session session)
    {
        if ( session != null ) {
            final SQLiteDatabase DB = this.getWritableDatabase();
            boolean failed = false;

            Log.d( LOG_TAG, "Add session: " + session );

            // Create the new record
            try {
                final ContentValues VALUES = new SessionStorage( session ).toValues();
                VALUES.putNull( SessionStorage.FIELD_SESSION_ID);

                DB.beginTransaction();
                DB.insert( TABLE_SESSIONS, null, VALUES );
                DB.setTransactionSuccessful();
                Log.d( LOG_TAG, "Inserted session." );
            } catch(SQLException exc) {
                failed = true;
                Log.e( LOG_TAG, "unable to create: " + session + ": " + exc.getMessage() );
            } finally {
                DB.endTransaction();
            }

            if ( !failed ) {
                this.updateYearInfo( session.getDate(), session.getDistance(), session.isAtPool() );
            }
        } else {
            Log.e( LOG_TAG, "Session to add cannot be null." );
        }

        return;
    }

    /** Deletes a given session from the data store.
      * @param session the Session object to delete.
      */
    public void delete(Session session)
    {
        final SQLiteDatabase DB = this.getWritableDatabase();
        boolean failed = false;

        Log.d( LOG_TAG, "deleting: " + session );

        if ( session != null ) {
            try {
                DB.beginTransaction();
                DB.delete( TABLE_SESSIONS,
                        SessionStorage.FIELD_SESSION_ID + "=?",
                        new String[] { Integer.toString( session.getId() ) } );
                DB.setTransactionSuccessful();
                Log.d( LOG_TAG, "finished deleting session id: " + session.getId() );
            } catch(SQLException exc) {
                failed = true;
                Log.e( LOG_TAG, "error deleting session id:"
                                            + session.getId() + ": " + exc.getMessage() );
            } finally {
                DB.endTransaction();
            }

            if ( !failed ) {
                this.updateYearInfo( session.getDate(), -1 * session.getDistance(), session.isAtPool() );
            }
        } else {
            Log.e( LOG_TAG, "session to delete cannot be null" );
        }

        return;
    }

    /** Modifies the given session in the data store.
      * @param session the session to update.
      */
    public void modify(Session session)
    {
        if ( session != null ) {
            final Session OLD_SESSION = this.getSessionFor( session.getId() );

            if ( OLD_SESSION != null ) {
                final String ID = Integer.toString( session.getId() );
                final ContentValues VALUES = new SessionStorage( session ).toValues();
                final SQLiteDatabase DB = this.getWritableDatabase();
                boolean failed = false;

                Log.d( LOG_TAG, "updating: " + session );

                try {
                    DB.beginTransaction();
                    DB.update( TABLE_SESSIONS, VALUES,
                                SessionStorage.FIELD_SESSION_ID + "=?",
                                new String[]{ ID } );
                    DB.setTransactionSuccessful();
                    Log.d( LOG_TAG, "updated session id: " + ID );
                } catch(SQLException exc) {
                    failed = true;
                    Log.e( LOG_TAG, "error updating session id: " + ID + ": " + exc.getMessage() );
                } finally {
                    DB.endTransaction();
                }

                if ( !failed ) {
                    if ( OLD_SESSION.isAtPool() == session.isAtPool() ) {
                        this.updateYearInfo( session.getDate(),
                                             session.getDistance() - OLD_SESSION.getDistance(),
                                             session.isAtPool() );
                    } else {
                        this.updateYearInfo( session.getDate(),
                                                OLD_SESSION.getDistance(), OLD_SESSION.isAtPool(),
                                                session.getDistance() );
                    }
                }
            } else {
                Log.e( LOG_TAG, "error: old session missing in store, id: " + session.getId() );
            }
        } else {
            Log.e( LOG_TAG, "session to update cannot be null" );
        }

        return;
    }

    public void toJSON(Writer writer) throws IOException
    {
        final JsonWriter jsonWriter = new JsonWriter( writer );

        jsonWriter.beginObject();
        jsonWriter.name( TABLE_YEARS );
        this.allYearInfosTo( jsonWriter );

        jsonWriter.name( TABLE_SESSIONS );
        this.allSessionsTo( jsonWriter );
        jsonWriter.endObject();
    }

    private void allYearInfosTo(JsonWriter jsonWriter) throws IOException
    {
        Cursor cursorYearInfo = null;

        jsonWriter.beginArray();

        try {
            cursorYearInfo = this.getAllYearInfosCursorWith( null, null );

            while ( cursorYearInfo.moveToNext() ) {
                new YearInfoStorage(
                        YearInfoStorage.createFrom( cursorYearInfo ) ).toJSON( jsonWriter );
            }
        } catch(SQLException exc) {
            Log.e( LOG_TAG, exc.getMessage() );
        } finally {
            close( cursorYearInfo );
        }

        jsonWriter.endArray();
    }

    private void allSessionsTo(JsonWriter jsonWriter) throws IOException
    {
        Cursor cursorSessions = null;

        jsonWriter.beginArray();

        try {
            cursorSessions = this.getAllSessionsCursor();

            while ( cursorSessions.moveToNext() ) {
                new SessionStorage(
                        SessionStorage.createFrom( cursorSessions ) ).toJSON( jsonWriter );
            }
        } catch(SQLException exc) {
            Log.e( LOG_TAG, exc.getMessage() );
        } finally {
            close( cursorSessions );
        }

        jsonWriter.endArray();
    }

    private String createExportFileName()
    {
        return NAME + "-" + Util.getISODate() + "." + EXT_BACKUP_FILE;
    }

    public void backup()
    {
        try {
            this.saveTo( this.getBackupDir() );
        } catch(IOException exc)
        {
            Log.e( LOG_TAG, "unable to create backup." );
        }
    }

    public File saveTo(File dir) throws IOException
    {
        File toret = null;
        final String EXPT_FILE_NAME = this.createExportFileName();

        if ( dir == null ) {
            if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q ) {
                throw new IOException( "unable to save directly to /Downloads" );
            }

            dir = DIR_DOWNLOADS;
        }

        try {
            File tempFile = this.createTempFile( NAME, Long.toString( Util.getDate().getTimeInMillis() ) );

            try (Writer tempStream = openWriterFor( tempFile )) {
                this.toJSON( tempStream );
            }

            toret = new File( dir, EXPT_FILE_NAME );
            copyFile( tempFile, toret );
            if ( !tempFile.delete() ) {
                throw new IOException( "unable to delete: " + tempFile.getName() );
            }
        } catch(IOException exc)
        {
            throw new IOException(
                    "exporting: '"
                            + EXPT_FILE_NAME
                            + "' to '" + dir
                            + "': " + exc.getMessage() );
        }

        return toret;
    }

    public void saveToDownloads(String fn, String mimeType) throws IOException
    {
        if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q )
        {
            final File INPUT_FILE = new File( fn );
            final ContentValues VALUES = new ContentValues();
            final ContentResolver FINDER = this.context.getContentResolver();

            VALUES.put( MediaStore.MediaColumns.DISPLAY_NAME, INPUT_FILE.getName() );
            VALUES.put( MediaStore.MediaColumns.MIME_TYPE, mimeType );
            VALUES.put( MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS );

            final Uri URI = FINDER.insert( MediaStore.Downloads.EXTERNAL_CONTENT_URI, VALUES );

            if ( URI != null ) {
                final InputStream IN = INPUT_FILE.toURI().toURL().openStream();
                final OutputStream OUT = FINDER.openOutputStream( URI );

                copyStream( IN, OUT );
            } else {
                throw new IOException( "unable to save to /Downloads" );
            }
        } else {
            copyFile( new File( fn ), new File( DIR_DOWNLOADS, fn ) );
        }

        return;
    }

    public void fromJSON(Reader reader) throws IOException
    {
        final JsonReader jsonReader = new JsonReader( reader );

        jsonReader.beginObject();

        while( jsonReader.hasNext() ) {
            final String NAME = jsonReader.nextName();

            if ( NAME.equals( TABLE_YEARS ) ) {
                this.importYearInfosFrom( jsonReader );
            }
            else
            if ( NAME.equals( TABLE_SESSIONS ) ) {
                this.importSessionsFrom( jsonReader );
            } else {
                jsonReader.skipValue();
            }
        }

        jsonReader.endObject();
    }

    private void importYearInfosFrom(JsonReader jsonReader) throws IOException
    {
        jsonReader.beginArray();

        while( jsonReader.hasNext() ) {
            this.add( YearInfoStorage.createFrom( jsonReader ) );
        }

        jsonReader.endArray();
    }

    private void importSessionsFrom(JsonReader jsonReader) throws IOException
    {
        jsonReader.beginArray();

        while( jsonReader.hasNext() ) {
            this.add( SessionStorage.createFrom( jsonReader ) );
        }

        jsonReader.endArray();
    }

    public void importFrom(InputStream in, boolean fromScratch) throws IOException
    {
        if ( fromScratch ) {
            this.deleteSessions( null, null );
            this.deleteYearInfos( null, null );
        }

        this.fromJSON( openReaderFor( in ) );
    }

    private void deleteSessions(String query, String[] queryArgs)
    {
        final SQLiteDatabase DB = this.getWritableDatabase();

        Log.d( LOG_TAG, "start deleting sessions: " + this.stringFromQuery( query, queryArgs ) );

        try {
            DB.beginTransaction();
            DB.delete( TABLE_SESSIONS, query, queryArgs );
            DB.setTransactionSuccessful();
            Log.d( LOG_TAG, "finished deleting sessions." );
        } catch(Exception exc) {
            Log.e( LOG_TAG, "error deleting sessions: " + exc.getMessage() );
        } finally {
            DB.endTransaction();
        }

        return;
    }

    private void deleteYearInfos(String query, String[] queryArgs)
    {
        final SQLiteDatabase DB = this.getWritableDatabase();

        Log.d( LOG_TAG, "start deleting year info's: " + this.stringFromQuery( query, queryArgs ) );

        try {
            DB.beginTransaction();
            DB.delete( TABLE_YEARS, query, queryArgs );
            DB.setTransactionSuccessful();
            Log.d( LOG_TAG, "finished deleting year info's." );
        } catch(Exception exc) {
            Log.e( LOG_TAG, "error deleting year info's: " + exc.getMessage() );
        } finally {
            DB.endTransaction();
        }

        return;
    }

    public static Writer openWriterFor(File f) throws IOException
    {
        BufferedWriter toret;

        try {
            final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    new FileOutputStream( f ),
                    StandardCharsets.UTF_8.newEncoder() );

            toret = new BufferedWriter( outputStreamWriter );
        } catch (IOException exc) {
            Log.e( LOG_TAG,"Error creating writer for file: " + f );
            throw exc;
        }

        return toret;
    }

    public static BufferedReader openReaderFor(File f) throws IOException
    {
        BufferedReader toret;

        try {
            toret = openReaderFor( new FileInputStream( f ) );
        } catch (IOException exc) {
            Log.e( LOG_TAG,"Error creating reader for file: " + f.getName() );
            throw exc;
        }

        return toret;
    }

    private static BufferedReader openReaderFor(InputStream inStream)
    {
        final InputStreamReader inputStreamReader = new InputStreamReader(
                inStream,
                StandardCharsets.UTF_8.newDecoder() );

        return new BufferedReader( inputStreamReader );
    }

    /** Closes a writer stream. */
    public static void close(Writer writer)
    {
        try {
            if ( writer != null ) {
                writer.close();
            }
        } catch(IOException exc)
        {
            Log.e( LOG_TAG, "closing writer: " + exc.getMessage() );
        }
    }

    /** Closes a reader stream. */
    public static void close(Reader reader)
    {
        try {
            if ( reader != null ) {
                reader.close();
            }
        } catch(IOException exc)
        {
            Log.e( LOG_TAG, "closing reader: " + exc.getMessage() );
        }
    }

    /** Closes a JSONReader stream. */
    private static void close(JsonReader jsonReader)
    {
        try {
            if ( jsonReader != null ) {
                jsonReader.close();
            }
        } catch(IOException exc)
        {
            Log.e( LOG_TAG, "closing json reader: " + exc.getMessage() );
        }
    }

    /** @return a newly created temp file. */
    public File createTempFile(String prefix, String suffix) throws IOException
    {
        return File.createTempFile( prefix, suffix, this.context.getCacheDir() );
    }

    /** Copies a given file to a destination, overwriting if necessary.
     * @param source The File object of the source file.
     * @param dest The File object of the destination file.
     * @throws IOException if something goes wrong while copying.
     */
    private static void copyFile(File source, File dest) throws IOException
    {
        final String errorMsg = "error copying: " + source + " to: " + dest + ": ";
        InputStream is;
        OutputStream os;

        try {
            is = new FileInputStream( source );
            os = new FileOutputStream( dest );

            copyStream( is, os );
        } catch(IOException exc)
        {
            Log.e( LOG_TAG, errorMsg + exc.getMessage() );
            throw new IOException( errorMsg );
        }

        return;
    }

    /** Copies from a stream to another one.
     * @param is The input stream object to copy from.
     * @param os The output stream object of the destination.
     * @throws IOException if something goes wrong while copying.
     */
    public static void copyStream(InputStream is, OutputStream os) throws IOException
    {
        final byte[] buffer = new byte[ 1024 ];
        int length;

        try {
            while ( ( length = is.read( buffer ) ) > 0 ) {
                os.write( buffer, 0, length );
            }
        } finally {
            try {
                if ( is != null ) {
                    is.close();
                }

                if ( os != null ) {
                    os.close();
                }
            } catch(IOException exc) {
                Log.e( LOG_TAG, "Copying file: error closing streams: " + exc.getMessage() );
            }
        }

        return;
    }

    public static void close(Cursor cursor)
    {
        if ( cursor != null ) {
            cursor.close();
        } else {
            Log.e( LOG_TAG, "cursor to close was null" );
        }

        return;
    }

    /** Creates a single copy of the data store, no matter how many times is called.
     * @param context the application context for the database.
     * @return a DataStore singleton.
     */
    public static DataStore createFor(Context context)
    {
        if ( dataStore == null ) {
            dataStore = new DataStore( context );
        }

        return dataStore;
    }

    /** @return the DataStore singleton. */
    public static DataStore get()
    {
        if ( dataStore == null ) {
            Log.e( LOG_TAG, "ERROR: trying to get an unbuilt data store!!!" );
            System.exit( -1 );
        }

        return dataStore;
    }

    private final Context context;
    private static DataStore dataStore;
    private static File DIR_DOWNLOADS;
    public static File DIR_TEMP;
}
