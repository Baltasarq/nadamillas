// NadaMillas (c) 2019/22 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;


import android.content.ContentResolver;
import android.database.Cursor;
import android.database.SQLException;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.AppInfo;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.Settings;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.core.YearInfo;
import com.devbaltasarq.nadamillas.core.storage.SettingsStorage;
import com.devbaltasarq.nadamillas.core.storage.YearInfoStorage;


public class SettingsActivity extends BaseActivity {
    public final static String LOG_TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        this.setContentView( R.layout.activity_settings );

        final Toolbar TOOL_BAR = findViewById( R.id.toolbar );
        this.setSupportActionBar( TOOL_BAR );

        final ImageButton BT_BACK = this.findViewById( R.id.btCloseSettings );
        final Spinner CB_UNITS = this.findViewById( R.id.cbUnits );
        final ImageButton BT_RECALCULATE = this.findViewById( R.id.btRecalculate );
        final ImageButton BT_IMPORT = this.findViewById( R.id.btImport );
        final ImageButton BT_EXPORT = this.findViewById( R.id.btExport );
        final Spinner CB_YEARS = this.findViewById( R.id.cbYears );
        final Spinner CB_FDoW = this.findViewById( R.id.cbFirstDayOfWeek );
        final ImageButton BT_EDIT_YEAR = this.findViewById( R.id.btEditYear );
        final ImageButton BT_NEW_YEAR = this.findViewById( R.id.btNewYear );
        final TextView LBL_ABOUT = this.findViewById( R.id.lblAbout );

        // Prepare the "about" label
        LBL_ABOUT.setText( AppInfo.getAuthoringMessage() );

        // Prepare units spinner
        final ArrayAdapter<Settings.DistanceUnits> UNITS_ADAPTER = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Settings.DistanceUnits.values() );

        CB_UNITS.setAdapter( UNITS_ADAPTER );
        CB_UNITS.setSelection( settings.getDistanceUnits().ordinal() );

        // Prepare first day of week spinner
        final ArrayAdapter<CharSequence> FDoW_ADAPTER = ArrayAdapter.createFromResource(
                    this,
                     R.array.array_first_day_of_week,
                     android.R.layout.simple_spinner_item
        );

        CB_FDoW.setAdapter( FDoW_ADAPTER );
        CB_FDoW.setSelection( settings.getFirstDayOfWeek().ordinal() );

        // Prepare years spinner
        this.yearsAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_item,
                null,
                new String[]{ YearInfoStorage.FIELD_YEAR },
                new int[]{ android.R.id.text1 },
                0 );

        CB_YEARS.setAdapter( this.yearsAdapter );

        // Set the initial values
        this.working = false;

        // Set the listeners
        CB_UNITS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                SettingsActivity.this.onUnitsChangedTo( position );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        BT_RECALCULATE.setOnClickListener( v -> this.onRecalculate() );
        BT_EDIT_YEAR.setOnClickListener( v -> this.onEditTarget() );
        BT_NEW_YEAR.setOnClickListener( v -> this.onNewYear() );
        BT_BACK.setOnClickListener( v -> this.onBackPressed() );
        BT_IMPORT.setOnClickListener( v -> this.onImport() );
        BT_EXPORT.setOnClickListener( v -> this.onExport() );

        CB_YEARS.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SettingsActivity.this.updateTarget();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CB_FDoW.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SettingsActivity.this.updateFirstDayOfWeek();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        this.update();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        DataStore.close( this.yearsAdapter.getCursor() );
    }

    @Override
    public void onBackPressed()
    {
        if ( !this.working) {
            new SettingsStorage( this.getApplicationContext(), settings ).store();

            super.onBackPressed();
            this.finish();
        }

        return;
    }

    /** Listener for the recalculate button. */
    private void onRecalculate()
    {
        final ProgressBar PROGRESS = this.findViewById( R.id.pbRecalculationProgress );
        final ImageButton BT_RECALCULATE = this.findViewById( R.id.btRecalculate );

        Thread recalculationThread = new Thread() {
            @Override
            public void run() {
                runOnUiThread( () -> {
                    PROGRESS.setVisibility( View.VISIBLE );
                    BT_RECALCULATE.setVisibility( View.GONE );
                });

                dataStore.recalculateAll();

                runOnUiThread( () -> {
                    PROGRESS.setVisibility( View.GONE );
                    BT_RECALCULATE.setVisibility( View.VISIBLE );
                    SettingsActivity.this.update();
                    Toast.makeText( SettingsActivity.this,
                            R.string.message_finished, Toast.LENGTH_LONG ).show();
                });
                SettingsActivity.this.working = false;
            }
        };

        this.working = true;
        recalculationThread.start();
    }

    /** Handler for the edit target event. */
    private void onEditTarget()
    {
        final Spinner CB_YEARS = this.findViewById( R.id.cbYears );
        final TextView ED_TARGET = this.findViewById( R.id.edTarget );
        final Cursor SELECTED_YEAR_CURSOR = (Cursor) CB_YEARS.getSelectedItem();

        if ( SELECTED_YEAR_CURSOR != null ) {
            final int SELECTED_YEAR = SELECTED_YEAR_CURSOR.getInt(
                    SELECTED_YEAR_CURSOR.getColumnIndexOrThrow(
                            YearInfoStorage.FIELD_YEAR ) );
            final YearInfo INFO = dataStore.getInfoFor( SELECTED_YEAR );
            final String STR_TARGET = ED_TARGET.getText().toString();

            // Convert to integer
            int target = 0;

            try {
                target = Integer.parseInt( STR_TARGET ) * 1000;
            } catch(NumberFormatException exc) {
                Log.d( LOG_TAG, "error converting target: " + STR_TARGET );
            }

            INFO.setTarget( target );
            dataStore.add( INFO );
            Toast.makeText( this, R.string.message_target_updated, Toast.LENGTH_LONG ).show();
        } else {
            Toast.makeText( this, this.getString( R.string.label_year ) + "?",
                            Toast.LENGTH_LONG ).show();
        }
    }

    /** Handler for the create new year event. */
    private void onNewYear()
    {
        final int MARGIN_FOR_YEARS = 20;
        final AlertDialog.Builder DLG = new AlertDialog.Builder( this );

        // Create years
        final int YEAR = Util.getYearFrom( Util.getDate().getTime() );
        final String[] YEARS = new String[ MARGIN_FOR_YEARS * 2 ];

        final ArrayAdapter<String> YEARS_ADAPTER = new ArrayAdapter<>( this,
                                                        android.R.layout.simple_spinner_item,
                                                        YEARS );

        for(int i = YEAR - MARGIN_FOR_YEARS; i < YEAR + MARGIN_FOR_YEARS; ++i ) {
            YEARS[ i - ( YEAR - MARGIN_FOR_YEARS ) ] = Integer.toString( i );
        }


        DLG.setTitle( R.string.title_activity_new_year );

        final LayoutInflater INFLATER = LayoutInflater.from( this );
        final View VIEW = INFLATER.inflate( R.layout.dlg_date_spinner, null );

        DLG.setView( VIEW );

        final Spinner CB_YEAR = VIEW.findViewById( R.id.cbYears );
        CB_YEAR.setAdapter( YEARS_ADAPTER );

        DLG.setPositiveButton( R.string.label_ok, (dialog, which) -> {
            final String SELECTED_YEAR = (String) CB_YEAR.getSelectedItem();
            final int THE_YEAR = Integer.parseInt( SELECTED_YEAR );

            dataStore.add( new YearInfo( THE_YEAR, 0, 0 ) );
            Toast.makeText( SettingsActivity.this, R.string.message_year_info_created, Toast.LENGTH_LONG ).show();
            SettingsActivity.this.update();
        });

        DLG.setNegativeButton( R.string.label_cancel, null );
        DLG.create().show();
    }

    /** Handler for the units changed event. */
    private void onUnitsChangedTo(int pos)
    {
        settings.setDistanceUnits( Settings.DistanceUnits.fromOrdinal( pos ) );
        this.updateTarget();
    }

    /** Updates the spinners info. */
    @Override
    protected void update()
    {
        final Spinner CB_YEARS = this.findViewById( R.id.cbYears );
        final Cursor CURSOR = dataStore.getDescendingAllYearInfosCursor();

        // Set the selection
        this.yearsAdapter.changeCursor( CURSOR );
        CB_YEARS.setSelection( 0, false );

        this.updateTarget();
    }

    private void updateTarget()
    {
        final TextView LBL_TOTAL_DISTANCE = this.findViewById( R.id.lblTotalDistance );
        final TextView LBL_UNITS_TOTAL_DISTANCE = this.findViewById( R.id.lblUnitsTotalDistance );
        final Spinner CB_YEARS = this.findViewById( R.id.cbYears );
        final TextView ED_TARGET = this.findViewById( R.id.edTarget );
        final Cursor SELECTED_YEAR_CURSOR = (Cursor) CB_YEARS.getSelectedItem();
        Cursor cursor = null;

        if ( SELECTED_YEAR_CURSOR != null ) {
            try {
                cursor = dataStore.getDescendingAllYearInfosCursor();

                final int SELECTED_YEAR = SELECTED_YEAR_CURSOR.getInt(
                        cursor.getColumnIndexOrThrow(
                                YearInfoStorage.FIELD_YEAR ) );
                final YearInfo INFO = dataStore.getInfoFor( SELECTED_YEAR );

                if ( INFO != null ) {
                    final String TARGET = Integer.toString( INFO.getTarget() / 1000 );
                    int displayUnits = R.string.label_km;

                    if ( settings.getDistanceUnits() == Settings.DistanceUnits.mi ) {
                        displayUnits = R.string.label_mi;
                    }

                    ED_TARGET.setText( TARGET );
                    LBL_TOTAL_DISTANCE.setText( INFO.getTotalAsString( settings ) );
                    LBL_UNITS_TOTAL_DISTANCE.setText( displayUnits );
                } else {
                    Log.e( LOG_TAG, "no info for year: " + SELECTED_YEAR );
                }
            } catch(SQLException exc) {
                Log.e( LOG_TAG, exc.getMessage() );
            } finally {
                DataStore.close( cursor );
            }
        } else {
            Log.d( LOG_TAG, "unable to update the target");
        }

        return;
    }

    /** Changes the first day of week. */
    private void updateFirstDayOfWeek()
    {
        final Spinner CB_FDoW = this.findViewById( R.id.cbFirstDayOfWeek );
        final Settings.FirstDayOfWeek FIRST_DAY_OF_WEEK =
                Settings.FirstDayOfWeek.fromOrdinal( CB_FDoW.getSelectedItemPosition() );

        settings.setFirstDayOfWeek( FIRST_DAY_OF_WEEK );
    }

    /** Lets the user choose a backup file for importing. */
    private void pickBackupFileForImporting()
    {
        this.SELECT_MEDIA.launch( "*/*" );
    }

    /** The import event handler. */
    private void onImport()
    {
        final File BKUP_FILE = dataStore.findBackup();

        if ( BKUP_FILE != null ) {
            final android.app.AlertDialog.Builder DLG = new android.app.AlertDialog.Builder( this );
            DLG.setTitle( R.string.action_import );
            DLG.setItems( R.array.array_import_options, (dialog, which ) -> {
                if ( which == 0 ) {
                    this.importFile( Uri.fromFile( BKUP_FILE ) );
                }
                else
                if ( which == 1 ) {
                    this.pickBackupFileForImporting();
                } else {
                    dialog.dismiss();
                }
            });
            DLG.create().show();
        } else {
            this.pickBackupFileForImporting();
        }

        return;
    }

    /** Import a given file. */
    private void importFile(Uri uri)
    {
        if ( uri != null ) {
            final String FILE_EXTENSION = MimeTypeMap.getFileExtensionFromUrl( uri
                    .toString().toLowerCase() );
            final String BCKUP_FILE_EXT = DataStore.EXT_BACKUP_FILE.toLowerCase();

            if ( ( uri.getScheme().equals( ContentResolver.SCHEME_FILE )
                    && FILE_EXTENSION.equals( BCKUP_FILE_EXT ) )
                || uri.getScheme().equals( ContentResolver.SCHEME_CONTENT ) )
            {
                final android.app.AlertDialog.Builder DLG = new android.app.AlertDialog.Builder( this );

                DLG.setTitle( R.string.action_import );
                DLG.setItems( R.array.array_how_to_import_options, (dialog, which ) -> {
                    if ( which < 2 ) {
                        SettingsActivity.this.importFile( uri, which == 0 );
                    } else {
                        dialog.dismiss();
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
                final SettingsActivity SELF = SettingsActivity.this;
                final ProgressBar PROGRESS_BAR = SELF.findViewById( R.id.pbImportProgress );
                final ImageButton BT_IMPORT = SELF.findViewById( R.id.btImport );

                SELF.working = true;

                runOnUiThread( () -> {
                    BT_IMPORT.setVisibility( View.GONE );
                    PROGRESS_BAR.setVisibility( View.VISIBLE );
                    PROGRESS_BAR.setIndeterminate( true );
                });

                if ( uri != null
                        && uri.getScheme() != null
                        && ( uri.getScheme().equals( ContentResolver.SCHEME_CONTENT )
                        ||  uri.getScheme().equals( ContentResolver.SCHEME_FILE ) ) )
                {
                    try {
                        final InputStream IN = SELF.getContentResolver().openInputStream( uri );

                        dataStore.importFrom( IN, fromScratch );
                        dataStore.recalculateAll();
                        SELF.showStatus( LOG_TAG, SELF.getString( R.string.message_finished ) );
                    } catch(IOException exc) {
                        SELF.showStatus( LOG_TAG, SELF.getString( R.string.message_io_error ) );
                        Log.e( LOG_TAG, exc.getMessage() );
                    }

                    SELF.runOnUiThread( SELF::update );
                } else {
                    SELF.showStatus( LOG_TAG, SELF.getString( R.string.message_unsupported_file_type_error ) );
                }

                runOnUiThread( () -> {
                    PROGRESS_BAR.setVisibility( View.GONE );
                    BT_IMPORT.setVisibility( View.VISIBLE );
                });

                SELF.working = false;
            }
        };

        IMPORT_THREAD.start();
    }

    /** The export handler. */
    private void onExport()
    {
        final Thread EXPORT_THREAD = new Thread() {
            @Override
            public void run()
            {
                final SettingsActivity SELF = SettingsActivity.this;
                final ProgressBar PROGRESS_BAR = SELF.findViewById( R.id.pbExportProgress );
                final ImageButton BT_EXPORT = SELF.findViewById( R.id.btExport );

                SELF.working = true;

                runOnUiThread( () -> {
                    PROGRESS_BAR.setVisibility( View.VISIBLE );
                    BT_EXPORT.setVisibility( View.GONE );
                });

                try {
                    // Create backup
                    final File BKUP_FILE = dataStore.saveTo( dataStore.getBackupDir() );

                    SELF.share( LOG_TAG, "application/json", BKUP_FILE );
                } catch(IOException exc) {
                    SELF.showStatus( LOG_TAG,
                            SELF.getString( R.string.message_io_error )
                                    + ": " + exc.getMessage() );
                }

                runOnUiThread( () -> {
                    PROGRESS_BAR.setVisibility( View.GONE );
                    BT_EXPORT.setVisibility( View.VISIBLE );
                });

                SELF.working = false;
            };
        };

        EXPORT_THREAD.start();
    }

    private final ActivityResultLauncher<String> SELECT_MEDIA = this.registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if ( uri != null ) {
                    this.importFile( uri );
                } else {
                    this.showStatus( LOG_TAG, this.getString( R.string.message_io_error) );
                }
            });

    private boolean working;
    private CursorAdapter yearsAdapter;
}
