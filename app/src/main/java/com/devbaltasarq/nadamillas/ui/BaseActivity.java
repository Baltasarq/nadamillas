package com.devbaltasarq.nadamillas.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUriExposedException;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.core.storage.SessionStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class BaseActivity extends AppCompatActivity {
    protected final static int RC_NEW_SESSION = 999;
    public static final int RC_EDIT_SESSION = 554;

    /** Launches the session editor with the "add" profile. */
    protected void launchNewSessionEdit()
    {
        this.launchNewSessionEdit( null );
    }

    /** Launches the session editor with the "add" profile. */
    protected void launchNewSessionEdit(Date date)
    {
        final Intent NEW_SESSION_DATA = new Intent( this, EditSessionActivity.class );

        if ( date != null ) {
            final Bundle BUNDLE = new Bundle();
            new SessionStorage( new Session( date, 0, true ) ).toBundle( BUNDLE );
            NEW_SESSION_DATA.putExtras( BUNDLE );
        }

        this.startActivityForResult( NEW_SESSION_DATA, RC_NEW_SESSION );
    }

    /** Launches the session editor with the "edit existing" profile. */
    protected void launchSessionEdit(Session session)
    {
        final Intent EDIT_SESSION_DATA = new Intent( this, EditSessionActivity.class );
        final Bundle BUNDLE = new Bundle();

        this.lastEditedSessionId = session.getId();

        BUNDLE.putBoolean( EditSessionActivity.IS_EDIT, true );
        new SessionStorage( session ).toBundle( BUNDLE );
        EDIT_SESSION_DATA.putExtras( BUNDLE );

        this.startActivityForResult( EDIT_SESSION_DATA, RC_EDIT_SESSION );
    }

    /** Stores a new session in the data store. */
    protected void storeNewSession(@NonNull DataStore dataStore, Intent intentData)
    {
        if ( intentData != null ) {
            final Bundle DATA = intentData.getExtras();

            if ( DATA != null ) {
                this.storeNewSession( dataStore, DATA );
            } else {
                this.showInternalError();
            }
        } else {
            this.showInternalError();
        }

        return;
    }

    /** Stores a new session in the data store. */
    protected void storeNewSession(@NonNull DataStore dataStore, Bundle data)
    {
        if ( data != null ) {
            dataStore.add( SessionStorage.createFrom( data ) );
        } else {
            this.showInternalError();
        }
    }

    /** Updates and existing session. */
    protected void updateSession(@NonNull DataStore dataStore, Session session)
    {
        if ( session != null ) {
            dataStore.modify( session.copyWithId( this.lastEditedSessionId ) );
            Toast.makeText( this, R.string.message_finished, Toast.LENGTH_SHORT ).show();
        } else {
            this.showInternalError();
        }

        return;
    }

    /** Deletes a given session. */
    protected void deleteSession(@NonNull DataStore dataStore, Session session)
    {
        if ( session != null ) {
            dataStore.delete( session );
            Toast.makeText( this, R.string.message_finished, Toast.LENGTH_SHORT ).show();
        } else {
            this.showInternalError();
        }

        return;
    }

    /** Shows a toast with the "internal error" message. */
    protected void showInternalError()
    {
        Toast.makeText( this, R.string.message_internal_error, Toast.LENGTH_LONG ).show();
    }

    /** Shows an info status on screen. */
    protected void showStatus(final String LOG_TAG, final String MSG)
    {
        Log.d( LOG_TAG, MSG );

        BaseActivity.this.runOnUiThread(
                new Runnable() {
                   @Override
                   public void run() {
                       Snackbar.make(
                               findViewById( android.R.id.content ), MSG, Snackbar.LENGTH_SHORT )
                               .show();
                   }
                });

        return;
    }

    protected void share(String logTag, File f)
    {
        try {
            final Intent INTENT = new Intent( Intent.ACTION_SEND );
            final Uri SCRSHOT_URI = FileProvider.getUriForFile( this.getApplicationContext(), getPackageName() + ".fileprovider", f );

            INTENT.setType( "image/*" );
            INTENT.setAction( Intent.ACTION_SEND );
            INTENT.putExtra( Intent.EXTRA_STREAM, SCRSHOT_URI );

            this.startActivity(
                    Intent.createChooser(
                            INTENT, this.getString( R.string.action_share ) ) );
        } catch(Exception exc) {
            this.showStatus( logTag, this.getString( R.string.message_io_error ) );
            Log.e( logTag, exc.getMessage() );
        }

        return;
    }

    /** @return a screenshot image file in the private storage. */
    protected File takeScreenshot(String logTag, DataStore dataStore)
    {
        return this.extractBitmap( logTag, this.getWindow().getDecorView().getRootView(), dataStore );
    }

    /** @return an image file in the private storage. */
    protected File extractBitmap(String logTag, final View V1, DataStore dataStore)
    {
        final String FMT_DATE = Util.getISODate()
                + "-" + Util.getTimeAsString().replace( ':', '_' );
        File toret = null;

        try {
            // create bitmap screen capture
            V1.setDrawingCacheEnabled( true );
            final Bitmap BITMAP = Bitmap.createBitmap( V1.getDrawingCache() );
            V1.setDrawingCacheEnabled( false );

            // Scale bitmap
            final Bitmap SCALED_BITMAP =
                    Bitmap.createScaledBitmap( BITMAP,
                                        (int) Math.round( BITMAP.getWidth() / 2.5 ),
                                        (int) Math.round( BITMAP.getHeight() / 2.5 ), true );

            // Save it
            final File TEMP_FILE = dataStore.createTempFile(
                    "scrshot", FMT_DATE + ".jpg" );
            final FileOutputStream OUTPUT = new FileOutputStream( TEMP_FILE );
            SCALED_BITMAP.compress( Bitmap.CompressFormat.JPEG, 90, OUTPUT );
            OUTPUT.flush();
            OUTPUT.close();
            toret = TEMP_FILE;
        } catch (IOException exc) {
            this.showStatus( logTag, this.getString( R.string.message_io_error ) );
        }

        return toret;
    }

    private int lastEditedSessionId;
}
