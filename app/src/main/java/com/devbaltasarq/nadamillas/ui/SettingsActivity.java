// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.ui;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.Settings;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.core.YearInfo;
import com.devbaltasarq.nadamillas.core.storage.SessionStorage;
import com.devbaltasarq.nadamillas.core.storage.SettingsStorage;
import com.devbaltasarq.nadamillas.core.storage.YearInfoStorage;

import java.util.Calendar;

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
        final Spinner CB_YEARS = this.findViewById( R.id.cbYears );
        final Spinner CB_FDoW = this.findViewById( R.id.cbFirstDayOfWeek );
        final ImageButton BT_EDIT_YEAR = this.findViewById( R.id.btEditYear );
        final ImageButton BT_NEW_YEAR = this.findViewById( R.id.btNewYear );

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
                new int[]{ android.R.id.text1 });

        CB_YEARS.setAdapter( this.yearsAdapter );

        // Set the initial values
        this.recalculating = false;

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

        BT_RECALCULATE.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            SettingsActivity.this.onRecalculate();
            }
        });

        BT_EDIT_YEAR.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            SettingsActivity.this.onEditTarget();
            }
        });

        BT_NEW_YEAR.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            SettingsActivity.this.onNewYear();
            }
        });

        CB_YEARS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SettingsActivity.this.updateTarget();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CB_FDoW.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SettingsActivity.this.updateFirstDayOfWeek();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        BT_BACK.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.onBackPressed();
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        this.updateSpinners();
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
        if ( !this.recalculating ) {
            new SettingsStorage( this.getApplicationContext(), settings ).store();

            super.onBackPressed();
            this.finish();
        }

        return;
    }

    /** Listener for the recalculate button. */
    private void onRecalculate()
    {
        final ProgressBar PROGRESS_BAR = this.findViewById( R.id.pbProgress );
        final ImageButton BT_RECALCULATE = this.findViewById( R.id.btRecalculate );

        BT_RECALCULATE.setEnabled( false );

        Thread recalculationThread = new Thread() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PROGRESS_BAR.setVisibility( View.VISIBLE );
                        PROGRESS_BAR.setIndeterminate( true );
                    }
                });

                dataStore.recalculate();

                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        PROGRESS_BAR.setVisibility( View.GONE );
                        BT_RECALCULATE.setEnabled( true );
                        SettingsActivity.this.updateSpinners();
                        Toast.makeText( SettingsActivity.this,
                                R.string.message_finished, Toast.LENGTH_LONG ).show();
                    }
                });
                SettingsActivity.this.recalculating = false;
            }
        };

        this.recalculating = true;
        recalculationThread.start();
    }

    /** Handler for the edit target event. */
    private void onEditTarget()
    {
        final Spinner CB_YEARS = this.findViewById( R.id.cbYears );
        final TextView ED_TARGET = this.findViewById( R.id.edTarget );
        final Cursor SELECTED_YEAR_CURSOR = (Cursor) CB_YEARS.getSelectedItem();
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

        DLG.setPositiveButton( R.string.label_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String SELECTED_YEAR = (String) CB_YEAR.getSelectedItem();
                final int THE_YEAR = Integer.parseInt( SELECTED_YEAR );

                dataStore.add( new YearInfo( THE_YEAR, 0, 0 ) );
                Toast.makeText( SettingsActivity.this, R.string.message_year_info_created, Toast.LENGTH_LONG ).show();
                SettingsActivity.this.updateSpinners();
            }
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
    private void updateSpinners()
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

    private boolean recalculating;
    private CursorAdapter yearsAdapter;
}
