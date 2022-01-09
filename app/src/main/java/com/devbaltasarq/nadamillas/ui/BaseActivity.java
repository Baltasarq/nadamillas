// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Settings;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.core.storage.SessionStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;


public abstract class BaseActivity extends AppCompatActivity {
    public static final int CHANNEL_STR_ID = R.string.app_name;
    private static final int RC_ASK_WRITE_EXTERNAL_STORAGE_PERMISSION_FOR_SAVE = 121;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );

        if ( requestCode == RC_ASK_WRITE_EXTERNAL_STORAGE_PERMISSION_FOR_SAVE
          && grantResults.length > 0
          && grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED )
        {
            final String[] SAVE_OPTS_PARTS = saveOptions.split( "|" );

            this.doSaveScreenShotToDownloads( SAVE_OPTS_PARTS[ 0 ],
                         new File( SAVE_OPTS_PARTS[ 1 ] ) );
        }

        return;
    }

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
            new SessionStorage( new Session( date, 0, 0, true ) ).toBundle( BUNDLE );
            NEW_SESSION_DATA.putExtras( BUNDLE );
        }

        this.LAUNCH_NEW_SESSION.launch( NEW_SESSION_DATA );
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

        this.LAUNCH_EDIT_SESSION.launch( EDIT_SESSION_DATA );
    }

    /** Stores a new session in the data store. */
    protected void storeNewSession(Intent intentData)
    {
        if ( intentData != null ) {
            final Bundle DATA = intentData.getExtras();

            if ( DATA != null ) {
                this.storeNewSession( DATA );
            } else {
                this.showInternalError();
            }
        } else {
            this.showInternalError();
        }

        return;
    }

    /** Stores a new session in the data store. */
    protected void storeNewSession(Bundle data)
    {
        if ( data != null ) {
            dataStore.add( SessionStorage.createFrom( data ) );
        } else {
            this.showInternalError();
        }
    }

    /** Updates and existing session. */
    protected void updateSession(Session session)
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
    protected void deleteSession(Session session)
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
            () -> Snackbar.make(
                    findViewById( android.R.id.content ), MSG, Snackbar.LENGTH_SHORT )
                    .show());

        return;
    }

    protected void createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            final String CHANNEL_ID = this.getString( CHANNEL_STR_ID );
            final CharSequence NAME = getString( R.string.channel_name );
            final String DESCRIPTION = getString( R.string.channel_description );

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            NAME,
                            NotificationManager.IMPORTANCE_DEFAULT );

            channel.setDescription( DESCRIPTION );

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService( NotificationManager.class );
            notificationManager.createNotificationChannel( channel );
        }

        return;
    }

    protected void showNotification(int iconId, String msg, String desc)
    {
        final String CHANNEL_ID = this.getString( CHANNEL_STR_ID );
        final NotificationManagerCompat NOTIFY_MANAGER =
                NotificationManagerCompat.from( this );

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder( this, CHANNEL_ID )
                .setContentTitle( msg )
                .setContentText( desc )
                .setSmallIcon( iconId )
                .setPriority( NotificationCompat.PRIORITY_DEFAULT );

        NOTIFY_MANAGER.notify( notificationId++, notification.build() );
    }

    protected void saveScreenShotToDownloads(String logTag, File f)
    {
        final String PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        final int RESULT_REQUEST = ContextCompat.checkSelfPermission( this, PERMISSION );

        if ( RESULT_REQUEST != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this,
                    new String[]{ PERMISSION },
                    RC_ASK_WRITE_EXTERNAL_STORAGE_PERMISSION_FOR_SAVE );

            saveOptions = logTag + "|" + f.getAbsolutePath();
        } else {
            this.doSaveScreenShotToDownloads( logTag, f );
        }

        return;
    }

    protected void doSaveScreenShotToDownloads(final String LOG_TAG, final File IN_FILE)
    {
        final Thread SAVE_THREAD = new Thread() {
            @Override
            public void run()
            {
                final BaseActivity SELF = BaseActivity.this;

                try {
                    dataStore.saveToDownloads( IN_FILE.getAbsolutePath(),
                                     "image/jpeg" );

                    SELF.showStatus( LOG_TAG,
                            SELF.getString( R.string.message_finished )
                                    + ": " + IN_FILE.getName() );

                    SELF.showNotification( android.R.drawable.stat_sys_download_done,
                            SELF.getString( R.string.message_screenshot ),
                            SELF.getString( R.string.message_screenshot_desc )
                                    + IN_FILE.getName() );
                } catch(IOException exc) {
                    SELF.showStatus( LOG_TAG,
                            SELF.getString( R.string.message_io_error )
                                    + ": " + exc.getMessage() );
                }
            }
        };

        SAVE_THREAD.start();
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
    protected File takeScreenshot(String logTag)
    {
        return this.saveScreenBitmapToTempFile( logTag, this.getWindow().getDecorView().getRootView() );
    }

    /** @return an image file in the private storage. */
    protected File saveScreenBitmapToTempFile(String logTag, final View V1)
    {
        final int WIDTH = V1.getWidth();
        final int HEIGHT = V1.getHeight();
        final String FMT_DATE_TIME = Util.getISODate()
                + "-" + Util.getTimeAsString().replace( ':', '_' );
        File toret = null;

        try {
            // Create canvas
            final Bitmap BITMAP = Bitmap.createBitmap( WIDTH, HEIGHT, Bitmap.Config.ARGB_4444 );
            final Canvas CANVAS = new Canvas( BITMAP );

            // Determine background
            final Resources.Theme THEME = this.getTheme();
            final TypedArray BACKGROUND_STYLED_ATTRS =
                    THEME.obtainStyledAttributes(
                            new int[] { android.R.attr.windowBackground } );
            final int BACKGROUND_RES_ID = BACKGROUND_STYLED_ATTRS.getResourceId( 0, 0 );
            final Drawable BACKGROUND = ContextCompat.getDrawable( this, BACKGROUND_RES_ID );

            if ( BACKGROUND == null ) {
                throw new IOException( "unable to locate background" );
            }

            // Draw the view in the canvas
            BACKGROUND.draw( CANVAS );
            V1.draw( CANVAS );

            // Save it
            final File TEMP_FILE = dataStore.createTempFile(
                    "scrshot", FMT_DATE_TIME + ".jpg" );
            final FileOutputStream OUTPUT = new FileOutputStream( TEMP_FILE );
            BITMAP.compress( Bitmap.CompressFormat.JPEG, 90, OUTPUT );
            OUTPUT.flush();
            OUTPUT.close();
            toret = TEMP_FILE;
        } catch (IOException exc) {
            this.showStatus( logTag, this.getString( R.string.message_io_error ) );
        }

        return toret;
    }

    protected abstract void update();

    private int lastEditedSessionId;

    private final ActivityResultLauncher<Intent> LAUNCH_NEW_SESSION =
            this.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if ( result.getResultCode() == RESULT_OK ) {
                            this.storeNewSession( result.getData() );
                            this.update();
                        }
                    });

    private final ActivityResultLauncher<Intent> LAUNCH_EDIT_SESSION =
            this.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if ( result.getResultCode() == RESULT_OK ) {
                            final Session SESSION = SessionStorage.createFrom( result.getData() );
                            this.updateSession( SESSION );
                            this.update();
                        }
                    });

    private static int notificationId = -1000;
    public static Settings settings;
    public static DataStore dataStore;
    private static String saveOptions;
}
