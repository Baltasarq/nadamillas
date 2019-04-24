// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.AppInfo;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.core.YearInfo;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.storage.SettingsStorage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;


public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int RC_PICK_FILE = 0x813;
    public static final int RC_ASK_WRITE_EXTERNAL_STORAGE_PERMISSION_FOR_EXPORT = 111;

    private static String LOG_TAG = MainActivity.class.getSimpleName();
    private final static int RC_SETTINGS = 998;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView( R.layout.activity_main );
        final Toolbar TOOL_BAR = this.findViewById( R.id.toolbar );
        this.setSupportActionBar( TOOL_BAR );

        final ImageButton BT_SHARE = this.findViewById( R.id.btShareSummary );
        final ImageButton BT_SCRSHOT = this.findViewById( R.id.btTakeScrshotForSummary );
        final FloatingActionButton FB_NEW = this.findViewById( R.id.fbNew );
        final FloatingActionButton FB_BROWSE = this.findViewById( R.id.fbBrowse );
        final FloatingActionButton FB_STATS = this.findViewById( R.id.fbStats );
        final TextView LBL_TITLE = this.findViewById( R.id.lblTitle );

        this.setTitle( "" );

        BT_SHARE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            final MainActivity SELF = MainActivity.this;

            SELF.share( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
            }
        });

        BT_SCRSHOT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MainActivity SELF = MainActivity.this;

                SELF.save( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
            }
        });

        LBL_TITLE.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.showStatus( LOG_TAG, AppInfo.getCompleteAuthoringMessage() );
            }
        });

        FB_NEW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.onNewSession();
            }
        });
        FB_BROWSE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.onBrowse();
            }
        });
        FB_STATS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.onStats();
            }
        });

        final DrawerLayout DRAWER = this.findViewById( R.id.drawer_layout );
        final ActionBarDrawerToggle TOGGLE = new ActionBarDrawerToggle(
                this, DRAWER, TOOL_BAR, R.string.navigation_drawer_open, R.string.navigation_drawer_close );

        DRAWER.addDrawerListener( TOGGLE );
        TOGGLE.syncState();

        final NavigationView NAVIGATION_VIEW = this.findViewById( R.id.nav_view );
        NAVIGATION_VIEW.setNavigationItemSelectedListener( this );
        this.createNotificationChannel();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        final Context APP_CONTEXT = this.getApplicationContext();
        final Calendar DATE = Util.getDate();

        dataStore = DataStore.createFor( APP_CONTEXT );
        settings = SettingsStorage.restore( APP_CONTEXT );

        if ( DATE.get( Calendar.DAY_OF_WEEK ) == Calendar.MONDAY ) {
            Thread backupThread = new Thread() {
                @Override
                public void run() {
                    dataStore.backup();
                }
            };

            backupThread.run();
        }

        return;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        this.updateTotals();
    }

    @Override
    public void onBackPressed()
    {
        final DrawerLayout drawer = this.findViewById( R.id.drawer_layout );

        if (drawer.isDrawerOpen( GravityCompat.START ) ) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if ( this.go( item.getItemId() ) ) {
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        final DrawerLayout drawer = this.findViewById( R.id.drawer_layout );

        this.go( item.getItemId() );

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /** Starts the appropriate action.
      * @param actionId Typically an R.id.nav_* or R.id.action_*
      * @return true if an action was triggered, false otherwise.
      */
    private boolean go(int actionId)
    {
        boolean toret = false;

        switch( actionId ) {
            case R.id.nav_new:
            case R.id.action_new:
                toret = true;
                this.onNewSession();
                break;
            case R.id.action_browse:
            case R.id.nav_browse:
                toret = true;
                this.onBrowse();
                break;
            case R.id.action_stats:
            case R.id.nav_stats:
                this.onStats();
                toret = true;
                break;
            case R.id.action_history:
            case R.id.nav_history:
                this.onHistory();
                toret = true;
                break;
            case R.id.action_import:
            case R.id.nav_import:
                this.pickFile();
                toret = true;
                break;
            case R.id.action_export:
            case R.id.nav_export:
                this.onExport();
                toret = true;
                break;
            case R.id.action_settings:
            case R.id.nav_settings:
                this.onSettings();
                toret = true;
                break;
        }

        return toret;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult( requestCode, resultCode, data );

        switch( requestCode ) {
            case RC_NEW_SESSION:
                if ( resultCode == RESULT_OK ) {
                    this.storeNewSession( data );
                }
                break;
            case RC_SETTINGS:
                this.updateTotals();
                break;
            case RC_PICK_FILE:
                if ( resultCode == RESULT_OK ) {
                    this.onImport( data );
                }
                break;
            default:
                final String ERR_NO_HANDLER = "sub activity without handler";

                AlertDialog.Builder dlg = new AlertDialog.Builder( this );
                dlg.setMessage( "sub activity without handler" );
                dlg.create().show();
                Log.e( LOG_TAG, ERR_NO_HANDLER );
                break;
        }

        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );

        switch ( requestCode ) {
            case RC_ASK_WRITE_EXTERNAL_STORAGE_PERMISSION_FOR_EXPORT: {
                if ( grantResults.length > 0
                  && grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED )
                {
                    this.doExport();
                }
            }
        }

        return;
    }

    /** Updates the on-screen totals. */
    private void updateTotals()
    {
        final TextView LBL_UNITS = this.findViewById( R.id.lblUnits );
        final TextView LBL_TOTAL = this.findViewById( R.id.lblTotal );
        final TextView LBL_TARGET = this.findViewById( R.id.lblTarget );
        final TextView LBL_POOL = this.findViewById( R.id.lblPool );
        final TextView LBL_OPEN_WATERS = this.findViewById( R.id.lblOpenWaters );
        final TextView LBL_DATE = this.findViewById( R.id.lblDate );
        final TextView LBL_PROGRESS = this.findViewById( R.id.lblProgress );
        final YearInfo INFO = dataStore.getCurrentYearInfo();
        String total = "0";
        String target = YearInfo.NOT_APPLYABLE;
        String totalPool = "0";
        String totalOpenWaters = "0";
        String progress = YearInfo.NOT_APPLYABLE;

        if ( INFO != null ) {
            total = INFO.getTotalAsString( settings );
            target = INFO.getTargetAsString( settings );
            progress = INFO.getProgressAsString();
            totalPool = INFO.getTotalPoolAsString( settings );
            totalOpenWaters = INFO.getTotalOpenWaterAsString( settings );
        }

        LBL_TOTAL.setText( total );
        LBL_TARGET.setText( target );
        LBL_POOL.setText( totalPool  );
        LBL_OPEN_WATERS.setText( totalOpenWaters );
        LBL_PROGRESS.setText( progress );
        LBL_DATE.setText( Util.getFullDate() );
        LBL_UNITS.setText( settings.getDistanceUnits().toString() );
    }

    /** The new session handler. */
    private void onNewSession()
    {
        this.launchNewSessionEdit();
    }

    private void onHistory()
    {
        this.startActivity( new Intent( this, HistoryActivity.class ) );
    }

    /** The settings handler. */
    private void onSettings()
    {
        final Intent SETTINGS_DATA = new Intent( this, SettingsActivity.class );

        this.startActivityForResult( SETTINGS_DATA, RC_SETTINGS );
    }

    /** The browse sessions handler. */
    private void onBrowse()
    {
        this.startActivity( new Intent( this, BrowseActivity.class ) );
    }

    /** The browse sessions handler. */
    private void onStats()
    {
        this.startActivity( new Intent( this, StatsActivity.class ) );
    }

    /** Lets the user choose a backup file for importing. */
    private void pickFile()
    {
        final Intent intent = new Intent();

        // Launch
        intent.setType( "*/*" );
        intent.setAction( Intent.ACTION_GET_CONTENT );

        this.startActivityForResult(
                Intent.createChooser( intent, this.getString( R.string.label_import ) ),
                RC_PICK_FILE );
    }

    /** The import event handler. */
    private void onImport(Intent data)
    {
        final Uri uri = data.getData();

        if ( uri != null ) {
            final String FILE_EXTENSION = MimeTypeMap.getFileExtensionFromUrl( uri
                    .toString().toLowerCase() );
            final String RES_FILE_EXT = DataStore.EXT_BACKUP_FILE.toLowerCase();

            if ( ( uri.getScheme().equals( ContentResolver.SCHEME_FILE )
                && FILE_EXTENSION.equals( RES_FILE_EXT ) )
              || uri.getScheme().equals( ContentResolver.SCHEME_CONTENT ) )
            {
                final AlertDialog.Builder DLG = new AlertDialog.Builder( this );

                DLG.setTitle( R.string.action_import );
                DLG.setItems( R.array.array_import_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ( which < 2 ) {
                            MainActivity.this.importFile( uri, which == 0 );
                        } else {
                            dialog.dismiss();
                        }
                    }
                });

                DLG.create().show();
            } else {
                this.showStatus( LOG_TAG, this.getString( R.string.message_unsupported_file_type_error ) );
            }
        } else {
            this.showStatus( LOG_TAG, this.getString( R.string.message_file_not_found ) );
        }

        return;
    }

    /** Import a given json file. */
    private void importFile(final Uri uri, final boolean fromScratch)
    {
        final Thread IMPORT_THREAD = new Thread() {
            @Override
            public void run()
            {
                final MainActivity SELF = MainActivity.this;
                final ProgressBar PROGRESS_BAR = SELF.findViewById( R.id.pbProgressMain );

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PROGRESS_BAR.setVisibility( View.VISIBLE );
                        PROGRESS_BAR.setIndeterminate( true );
                    }
                });

                if ( uri != null
                  && uri.getScheme() != null
                  && ( uri.getScheme().equals( ContentResolver.SCHEME_CONTENT )
                   ||  uri.getScheme().equals( ContentResolver.SCHEME_FILE ) ) )
                {
                    try {
                        final InputStream IN = SELF.getContentResolver().openInputStream( uri );

                        dataStore.importFrom( IN, fromScratch );
                        SELF.showStatus( LOG_TAG, SELF.getString( R.string.message_finished ) );
                    } catch(IOException exc) {
                        SELF.showStatus( LOG_TAG, SELF.getString( R.string.message_io_error ) );
                        Log.e( LOG_TAG, exc.getMessage() );
                    }

                    SELF.runOnUiThread( new Runnable() {
                        @Override
                        public void run() {
                        dataStore.recalculate();
                        SELF.updateTotals();
                        }
                    });
                } else {
                    SELF.showStatus( LOG_TAG, SELF.getString( R.string.message_unsupported_file_type_error ) );
                }

                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        PROGRESS_BAR.setVisibility( View.GONE );
                    }
                });

                return;
            }
        };

        IMPORT_THREAD.start();
    }

    /** The export handler. */
    private void onExport()
    {
        final String PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        final int RESULT_REQUEST = ContextCompat.checkSelfPermission( this, PERMISSION );

        if ( RESULT_REQUEST != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this,
                    new String[]{ PERMISSION },
                    RC_ASK_WRITE_EXTERNAL_STORAGE_PERMISSION_FOR_EXPORT );
        } else {
            this.doExport();
        }

        return;
    }

    /** It just exports, asking permission is assumed to have been asked elsewhere. */
    private void doExport()
    {
        final Thread EXPORT_THREAD = new Thread() {
            @Override
            public void run()
            {
                final MainActivity SELF = MainActivity.this;

                try {
                    dataStore.exportTo( null );
                    SELF.showStatus( LOG_TAG, SELF.getString( R.string.message_finished ) );
                } catch(IOException exc) {
                    SELF.showStatus( LOG_TAG,
                            SELF.getString( R.string.message_io_error )
                                    + ": " + exc.getMessage() );
                }
            }
        };

        EXPORT_THREAD.start();
    }
}
