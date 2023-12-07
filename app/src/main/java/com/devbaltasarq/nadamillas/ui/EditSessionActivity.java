// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import com.devbaltasarq.nadamillas.core.Settings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.Duration;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.core.storage.SessionStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;


public class EditSessionActivity extends BaseActivity {
    private static final String LOG_TAG = EditSessionActivity.class.getSimpleName();
    public static final String IS_EDIT = "is_edit";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView( R.layout.activity_edit_session);
        final Toolbar toolbar = findViewById( R.id.toolbar );
        this.setSupportActionBar( toolbar );

        // Pick up session date and other info
        final Bundle DATA = this.getIntent().getExtras();
        final TextView LBL_TITLE = this.findViewById( R.id.lblTitle );
        boolean isEdit = false;

        this.blockListeners = true;
        this.date = Util.getDate().getTime();
        this.duration = new Duration( 0 );
        this.place = this.notes = "";

        if ( DATA != null ) {
            final Session SESSION = SessionStorage.createFrom( DATA );

            if ( SESSION != null ) {
                this.date = SESSION.getDate();
                isEdit = DATA.getBoolean( IS_EDIT, false );

                if ( isEdit ) {
                    this.atPool = SESSION.isAtPool();
                    this.distance = SESSION.getDistance();
                    this.duration = SESSION.getDuration();
                    this.place = SESSION.getPlace();
                    this.notes = SESSION.getNotes();
                }
            } else {
                long instant = DATA.getLong( SessionStorage.FIELD_DATE, this.date.getTime() );
                this.date = new Date( instant );
            }
        }

        // Assign members
        if ( !isEdit ) {
            this.atPool = true;
            this.distance = 0;
            LBL_TITLE.setText( R.string.title_activity_new_session);
        }

        // Update view
        final LinearLayout LY_POOL_LAPS = this.findViewById( R.id.lyPoolLaps );
        final ImageButton BT_BACK = this.findViewById( R.id.btCloseEditSession );
        final FloatingActionButton FB_SAVE = this.findViewById( R.id.fbSaveSession );
        final EditText ED_DATE = this.findViewById( R.id.edDate );
        final EditText ED_HOURS = this.findViewById( R.id.edHours );
        final EditText ED_MINUTES = this.findViewById( R.id.edMinutes );
        final EditText ED_SECONDS = this.findViewById( R.id.edSeconds );
        final EditText ED_DISTANCE = this.findViewById( R.id.edDistance );
        final EditText ED_PLACE = this.findViewById( R.id.edPlace );
        final EditText ED_NOTES = this.findViewById( R.id.edNotes );
        final ImageButton BT_DATE = this.findViewById( R.id.btDate );
        final ImageButton BT_SHARE = this.findViewById( R.id.btShareEditSession );
        final RadioGroup GRD_WATER_TYPES = this.findViewById( R.id.grdWaters );
        final RadioButton RBT_POOL = this.findViewById( R.id.rbtPool );
        final RadioButton RBT_OWS = this.findViewById( R.id.rbtOpen );
        final ImageButton BT_SAVE = this.findViewById( R.id.btSaveSession );
        final ImageButton BT_SCRSHOT = this.findViewById( R.id.btTakeScrshotForEditSession );
        final EditText ED_LAPS = this.findViewById( R.id.edLaps );
        final Spinner CB_POOL_LENGTH = this.findViewById( R.id.cbPoolLength );
        final TextView LBL_LENGTH1 = this.findViewById( R.id.lblLength1 );
        final TextView LBL_LENGTH2 = this.findViewById( R.id.lblLength2 );

        // Prepares the label for distance
        if ( settings.getDistanceUnits() == Settings.DistanceUnits.mi ) {
            LBL_LENGTH1.setText( R.string.label_yard );
            LBL_LENGTH2.setText( R.string.label_yard );
        }

        // Prepares pool length & laps spinner
        final ArrayAdapter<String> POOL_LENGTH_ADAPTER =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        Settings.PoolLength.toStringList() );

        CB_POOL_LENGTH.setAdapter( POOL_LENGTH_ADAPTER );
        CB_POOL_LENGTH.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                EditSessionActivity.this.calculatePoolLaps();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        CB_POOL_LENGTH.setSelection( settings.getDefaultPoolLength().ordinal(), false );

        // Prepares the laps editor
        ED_LAPS.setText( "0" );

        ED_LAPS.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                EditSessionActivity.this.calculateDistanceByPoolLength();
            }
        });

        // Prepare remaining widgets
        ED_DATE.setText( Util.getShortDate( this.date, null ) );

        if ( this.atPool ) {
            GRD_WATER_TYPES.check( RBT_POOL.getId() );
        } else {
            GRD_WATER_TYPES.check( RBT_OWS.getId() );
        }

        if ( this.distance > 0 ) {
            ED_DISTANCE.setText( String.format( Locale.getDefault(), "%d", this.distance ) );
        }

        if ( this.duration.getTimeInSeconds() > 0 ) {
            ED_HOURS.setText( String.valueOf( this.duration.getHours() ) );
            ED_MINUTES.setText( String.valueOf( this.duration.getMinutes() ) );
            ED_SECONDS.setText( String.valueOf( this.duration.getSeconds() ) );
        }

        RBT_POOL.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_pool, 0 );
        RBT_POOL.setOnCheckedChangeListener( (bt, isChecked) -> {
            if ( isChecked ) {
                LY_POOL_LAPS.setVisibility( View.VISIBLE );
            }
        });
        RBT_OWS.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_sea, 0 );
        RBT_OWS.setOnCheckedChangeListener( (bt, isChecked) -> {
            if ( isChecked ) {
                LY_POOL_LAPS.setVisibility( View.GONE );
            }
        });

        // Set listeners
        BT_BACK.setOnClickListener( v -> EditSessionActivity.this.finish() );

        BT_SCRSHOT.setOnClickListener( v -> {
            final EditSessionActivity SELF = EditSessionActivity.this;

            SELF.shareScreenShot( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
        });

        BT_SAVE.setOnClickListener( v -> EditSessionActivity.this.save() );
        FB_SAVE.setOnClickListener( v -> EditSessionActivity.this.save() );
        BT_DATE.setOnClickListener( v -> EditSessionActivity.this.chooseDate() );
        BT_SHARE.setOnClickListener( v -> EditSessionActivity.this.shareSessionSummary() );

        GRD_WATER_TYPES.setOnCheckedChangeListener( (grp, id) -> {
            int pos = ( id == RBT_POOL.getId() ) ? 0 : 1;

            EditSessionActivity.this.setAtPool( pos );
        });

        ED_DISTANCE.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                EditSessionActivity.this.storeDistance( s );
                EditSessionActivity.this.recalculate();
            }
        });

        ED_HOURS.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                EditSessionActivity.this.storeDuration();
                EditSessionActivity.this.recalculate();
            }
        });

        ED_MINUTES.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                EditSessionActivity.this.storeDuration();
                EditSessionActivity.this.recalculate();
            }
        });

        ED_SECONDS.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                EditSessionActivity.this.storeDuration();
                EditSessionActivity.this.recalculate();
            }
        });

        ED_PLACE.setText( this.place );
        ED_PLACE.addTextChangedListener(  new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                EditSessionActivity.this.place = Util.capitalize( s.toString() );
            }
        });

        ED_NOTES.setText( this.notes );
        ED_NOTES.addTextChangedListener(  new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                EditSessionActivity.this.notes = Util.capitalize( s.toString() );
            }
        });

        this.blockListeners = false;

        // Set focus
        ED_DISTANCE.requestFocus();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        this.recalculate();
    }

    /** Launch a date picker dialog. */
    private void chooseDate()
    {
        final int[] DATA_DATE = Util.dataFromDate( Calendar.getInstance().getTime() );
        final DatePickerDialog dlg = new DatePickerDialog(
                this,
                R.style.DialogTheme,
                (dp, y, m, d) ->
                        EditSessionActivity.this.setDate(
                                Util.dateFromData( y, m ,d ) ),
                DATA_DATE[ 0 ],
                DATA_DATE[ 1 ],
                DATA_DATE[ 2 ]
        );

        dlg.getDatePicker()
                .setFirstDayOfWeek(
                        settings.getFirstDayOfWeek().getCalendarValue() );

        dlg.show();
    }

    /** Update whether the session was done at the pool.
      * pool == 0, open waters == 1
      */
    private void setAtPool(int pos)
    {
        this.atPool = ( pos == 0 );
    }

    /** Updates the date info, and reflects the change in the view. */
    private void setDate(Date d)
    {
        final EditText ED_DATE = this.findViewById( R.id.edDate );

        this.date = d;
        ED_DATE.setText( Util.getShortDate( this.date, null ) );

    }

    /** Stores the current value for the distance. */
    private void storeDistance(Editable s)
    {
        final String contents = s.toString();

        if ( !contents.isEmpty() ) {
            try {
                this.distance = Integer.parseInt( contents );
            } catch(Exception exc) {
                Log.e(
                        LOG_TAG,
                        "unable to convert to int: " + contents
                        + " still " + this.distance
                );
            }
        }

        return;
    }

    /** Stores the duration. */
    private void storeDuration()
    {
        final EditText ED_HOURS = this.findViewById( R.id.edHours );
        final EditText ED_MINUTES = this.findViewById( R.id.edMinutes );
        final EditText ED_SECONDS = this.findViewById( R.id.edSeconds );
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        try {
            hours = Integer.parseInt( ED_HOURS.getText().toString() );
        } catch(NumberFormatException exc) {
            Log.d( LOG_TAG, "hours parsing exception: " + exc.getMessage() );
        }

        try {
            minutes = Integer.parseInt( ED_MINUTES.getText().toString() );
        } catch(NumberFormatException exc) {
            Log.d( LOG_TAG, "minutes parsing exception: " + exc.getMessage() );
        }

        try {
            seconds = Integer.parseInt( ED_SECONDS.getText().toString() );
        } catch(NumberFormatException exc) {
            Log.d( LOG_TAG, "seconds parsing exception: " + exc.getMessage() );
        }

        this.duration = new Duration( hours, minutes, seconds );
    }

    /** Recalculate */
    private void recalculate()
    {
        this.calculatePoolLaps();
        this.calculateMeanSpeed();
    }

    /** Gets the value of the pool length spinner. */
    private int getPoolLength()
    {
        final Spinner CB_POOL_LENGTH = this.findViewById( R.id.cbPoolLength );
        final String POOL_LENGTH = (String) CB_POOL_LENGTH.getSelectedItem();
        int toret = 25;

        if ( POOL_LENGTH != null ) {
            try {
                toret = Integer.parseInt( POOL_LENGTH );
            } catch(NumberFormatException exc) {
                Log.e( LOG_TAG, "Pool length incorrect: " + POOL_LENGTH );
            }
        }

        return toret;
    }

    /** Gets the value of the number of laps editor. */
    private int getNumLaps()
    {
        final EditText ED_LAPS = this.findViewById( R.id.edLaps );
        final String STR_LAPS = ED_LAPS.getText().toString();
        int toret = 0;

        try {
            toret = Integer.parseInt( STR_LAPS );
        } catch(NumberFormatException exc) {
            Log.e( LOG_TAG, "Num laps incorrect: " + STR_LAPS );
        }

        return toret;
    }

    /** Calculates the number of pool laps needed for the distance. */
    private void calculatePoolLaps()
    {
        if ( !this.blockListeners ) {
            final Session FAKE_SESSION = new Session(
                                                this.date,
                                                this.distance,
                                                this.duration,
                                                this.atPool,
                                                this.place,
                                                this.notes );
            final EditText ED_POOL_LAPS = this.findViewById( R.id.edLaps );
            int poolLength = this.getPoolLength();
            int numLaps = 0;

            if ( poolLength > 0 ) {
                int modLaps = FAKE_SESSION.getDistance() % poolLength;
                numLaps = FAKE_SESSION.getDistance() / poolLength;

                if ( modLaps != 0 ) {
                    ++numLaps;
                }
            }

            this.blockListeners = true;
            ED_POOL_LAPS.setText( Integer.toString( numLaps ) );
            this.blockListeners = false;
        }

        return;
    }

    /** Calculates and shows the mean speed as data is entered. */
    private void calculateMeanSpeed()
    {
        final TextView LBL_SPEED = this.findViewById( R.id.lblSpeed );
        final Session FAKE_SESSION = new Session(
                                            this.date,
                                            this.distance,
                                            this.duration,
                                            this.atPool,
                                            this.place,
                                            this.notes );

        LBL_SPEED.setText( FAKE_SESSION.getSpeedAsString( settings )
                            + " - "
                            + FAKE_SESSION.getMeanTimeAsString( settings.getDistanceUnits() ) );
    }

    /** Calculates the distance given the laps. */
    private void calculateDistanceByPoolLength()
    {
        if ( !this.blockListeners ) {
            final EditText ED_DISTANCE = this.findViewById( R.id.edDistance );
            int laps = this.getNumLaps();
            int poolLength = this.getPoolLength();

            this.blockListeners = true;
            ED_DISTANCE.setText( "" + ( laps * poolLength ) );
            this.blockListeners = false;
        }

        return;
    }

    /** Share the text summary of this session. */
    private void shareSessionSummary()
    {
        final Session FAKE_SESSION = new Session(
                                        this.date,
                                        this.distance,
                                        this.duration,
                                        this.atPool,
                                        this.place,
                                        this.notes );

        this.share( FAKE_SESSION.toHumanReadableString( this, settings ) );
    }

    @Override
    protected void update()
    {
        this.recalculate();
    }

    /** Saves the data and returns. */
    private void save()
    {
        // Store data
        final Intent RET_DATA = new Intent();
        final Bundle DATA = new Bundle();

        new SessionStorage(
                new Session(
                            this.date,
                            this.distance,
                            this.duration,
                            this.atPool,
                            this.place,
                            this.notes ) ).toBundle( DATA );
        RET_DATA.putExtras( DATA );

        // Finish
        this.setResult( Activity.RESULT_OK, RET_DATA );
        this.finish();
    }

    private Date date;
    private int distance;
    private Duration duration;
    private String place;
    private String notes;
    private boolean blockListeners;
    private boolean atPool;
}
