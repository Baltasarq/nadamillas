// NadaMillas (c) 2019-2024-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;


import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.OnBackPressedCallback;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import java.util.Locale;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.AppInfo;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.session.Distance;
import com.devbaltasarq.nadamillas.core.session.Date;
import com.devbaltasarq.nadamillas.core.YearInfo;
import com.devbaltasarq.nadamillas.core.settings.FirstDayOfWeek;
import com.devbaltasarq.nadamillas.core.settings.PoolLength;
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

        final Spinner CB_UNITS = this.findViewById( R.id.cbUnits );
        final Spinner CB_YEARS = this.findViewById( R.id.cbYears );
        final Spinner CB_FDoW = this.findViewById( R.id.cbFirstDayOfWeek );
        final Spinner CB_DEFAULT_POOL_LENGTH = this.findViewById( R.id.cbDefaultPoolLength );
        final TextView LBL_ABOUT = this.findViewById( R.id.lblAbout );

        // Prepare the "about" label
        LBL_ABOUT.setText( AppInfo.getAuthoringMessage() );

        // Prepare the default pool length
        final ArrayAdapter<PoolLength> POOL_LENGTH_ADAPTER = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                PoolLength.values()
        );

        CB_DEFAULT_POOL_LENGTH.setAdapter( POOL_LENGTH_ADAPTER );
        CB_DEFAULT_POOL_LENGTH.setSelection( settings.getPoolLength().ordinal() );

        // Prepare units spinner
        final ArrayAdapter<Distance.Units> UNITS_ADAPTER = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Distance.Units.values() );

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

        CB_DEFAULT_POOL_LENGTH.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                settings.setDefaultPoolLength( PoolLength.values()[ position ] );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        this.setButtonsListeners();
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

    private void onBack()
    {
        if ( !this.working) {
            new SettingsStorage( this.getApplicationContext(), settings ).store();

            this.finish();
        }

        return;
    }

    private void setButtonsListeners()
    {
        final ImageButton BT_BACK = this.findViewById( R.id.btCloseSettings );
        final ImageButton BT_RECALCULATE = this.findViewById( R.id.btRecalculate );
        final ImageButton BT_IMPORT = this.findViewById( R.id.btImport );
        final ImageButton BT_EXPORT = this.findViewById( R.id.btExport );
        final ImageButton BT_EDIT_YEAR = this.findViewById( R.id.btEditYear );
        final ImageButton BT_NEW_YEAR = this.findViewById( R.id.btNewYear );

        // Back
        this.getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback( true ) {
                    @Override
                    public void handleOnBackPressed() {
                        SettingsActivity.this.onBack();
                    }
                }
        );

        BT_RECALCULATE.setOnClickListener( v -> this.onRecalculate() );
        BT_NEW_YEAR.setOnClickListener( v -> this.onNewYear() );
        BT_BACK.setOnClickListener( v -> this.onBack() );
        BT_IMPORT.setOnClickListener( v -> this.onImport() );
        BT_EXPORT.setOnClickListener( v -> this.onExport() );
        BT_EDIT_YEAR.setOnClickListener( v -> this.onEditYearInfo() );
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

    private void onEditYearInfo()
    {
        EditYearInfoActivity.yearInfo = this.getSelectedYearInfo();
        this.startActivity( new Intent( this, EditYearInfoActivity.class ) );
    }

    /** Handler for the create new year event. */
    private void onNewYear()
    {
        final int MARGIN_FOR_YEARS = 20;
        final AlertDialog.Builder DLG = new AlertDialog.Builder( this );

        // Create years
        final int YEAR = new Date().getYear();
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
            final String STR_SELECTED_YEAR = (String) CB_YEAR.getSelectedItem();
            final int SELECTED_YEAR = Integer.parseInt( STR_SELECTED_YEAR );

            dataStore.add( new YearInfo( SELECTED_YEAR ) );
            Toast.makeText( SettingsActivity.this, R.string.message_year_info_created, Toast.LENGTH_LONG ).show();
            SettingsActivity.this.update();
        });

        DLG.setNegativeButton( R.string.label_cancel, null );
        DLG.create().show();
    }

    /** Handler for the units changed event. */
    private void onUnitsChangedTo(int pos)
    {
        settings.setDistanceUnits( Distance.Units.fromOrdinal( pos ) );
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

    /** @return the current year info selected by the user, or null if impossible. */
    private YearInfo getSelectedYearInfo()
    {
        final Spinner CB_YEARS = this.findViewById( R.id.cbYears );
        final Cursor SELECTED_YEAR_CURSOR = (Cursor) CB_YEARS.getSelectedItem();
        YearInfo toret = null;

        if ( SELECTED_YEAR_CURSOR != null ) {
            try (Cursor cursor = dataStore.getDescendingAllYearInfosCursor()) {
                final int SELECTED_YEAR = SELECTED_YEAR_CURSOR.getInt(
                                            cursor.getColumnIndexOrThrow(
                                                YearInfoStorage.FIELD_YEAR ) );
                final YearInfo INFO = dataStore.getOrCreateInfoFor( SELECTED_YEAR );

                if ( INFO != null ) {
                    toret = INFO;
                } else {
                    final String MSG = "no info for year: " + SELECTED_YEAR;
                    Log.e( LOG_TAG, MSG );
                    throw new Error( MSG );
                }
            } catch(SQLException exc) {
                Log.e( LOG_TAG, "" + exc.getMessage() );
            }
        } else {
            Log.d( LOG_TAG, "unable to update the target");
        }

        return toret;
    }

    private void updateTarget()
    {
        final TextView LBL_DISTANCE = this.findViewById( R.id.lblDistanceInfo );
        final TextView LBL_TARGET_TOTAL = this.findViewById( R.id.lblTargetTotal );
        final TextView LBL_INFO = this.findViewById( R.id.lblTargetInfo );
        final YearInfo INFO = this.getSelectedYearInfo();
        final Distance.Units UNITS = settings.getDistanceUnits();

        if ( INFO != null ) {
            // Target & distance info
            LBL_DISTANCE.setText(
                    Distance.Fmt.format(
                        INFO.getDistance( YearInfo.SwimKind.TOTAL ), UNITS ));
            LBL_TARGET_TOTAL.setText(
                    Distance.Fmt.format(
                        INFO.getTarget( YearInfo.SwimKind.TOTAL ), UNITS ) );
            LBL_INFO.setText( String.format(
                                Locale.getDefault(),
                                "%s %s, %s %s",
                                Distance.Fmt.format(
                                        INFO.getTarget( YearInfo.SwimKind.OWS ), UNITS ),
                                this.getString( R.string.label_abbrev_open_waters ),
                                Distance.Fmt.format(
                                        INFO.getTarget( YearInfo.SwimKind.POOL ), UNITS ),
                                this.getString( R.string.label_pool ) ));
        }

        return;
    }

    /** Changes the first day of week. */
    private void updateFirstDayOfWeek()
    {
        final Spinner CB_FDoW = this.findViewById( R.id.cbFirstDayOfWeek );
        final int POS = CB_FDoW.getSelectedItemPosition();

        if ( POS != AdapterView.INVALID_POSITION ) {
            settings.setFirstDayOfWeek( FirstDayOfWeek.fromOrdinal( POS  ) );
        }

        return;
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
            final String SCHEME = uri.getScheme();

            if ( SCHEME != null
                && ( SCHEME.equals( ContentResolver.SCHEME_FILE )
                  || SCHEME.equals( ContentResolver.SCHEME_CONTENT ) ) )
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
    private void importFile(final Uri URI, final boolean FROM_SCRATCH)
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

                if ( URI != null ) {
                    final String SCHEME = URI.getScheme();

                    if ( SCHEME != null
                      && ( URI.getScheme().equals( ContentResolver.SCHEME_CONTENT )
                       ||  URI.getScheme().equals( ContentResolver.SCHEME_FILE ) ) )
                    {
                        try {
                            final InputStream IN = SELF.getContentResolver().openInputStream( URI );

                            dataStore.importFrom( IN, FROM_SCRATCH );
                            dataStore.recalculateAll();
                            SELF.showStatus( LOG_TAG, SELF.getString( R.string.message_finished ) );
                        } catch(IOException exc) {
                            SELF.showStatus( LOG_TAG, SELF.getString( R.string.message_io_error ) );
                            Log.e( LOG_TAG, "" + exc.getMessage() );
                        }

                        SELF.runOnUiThread( SELF::update );
                    } else {
                        SELF.showStatus( LOG_TAG, SELF.getString( R.string.message_unsupported_file_type_error ) );
                    }
                } else {
                    SELF.showStatus( LOG_TAG, SELF.getString( R.string.message_file_not_found ) );
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
