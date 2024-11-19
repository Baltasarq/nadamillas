// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import com.devbaltasarq.nadamillas.core.StringUtil;
import com.devbaltasarq.nadamillas.core.session.Distance;
import com.devbaltasarq.nadamillas.core.session.Speed;
import com.devbaltasarq.nadamillas.core.session.Temperature;
import com.devbaltasarq.nadamillas.core.settings.PoolLength;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.session.Duration;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.session.Date;
import com.devbaltasarq.nadamillas.core.storage.SessionStorage;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;


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
        this.date = new Date();
        this.duration = new Duration( 0 );
        this.place = this.notes = "";
        this.temperature = Temperature.PREDETERMINED;
        this.isCompetition = false;

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
                    this.temperature = SESSION.getTemperature();
                    this.isCompetition = SESSION.isCompetition();
                }
            } else {
                long instant = DATA.getLong( SessionStorage.FIELD_DATE, this.date.getTimeInMillis() );
                this.date = Date.from( instant );
            }
        }

        // Assign members
        if ( !isEdit ) {
            this.atPool = true;
            this.distance = 0;
            LBL_TITLE.setText( R.string.title_activity_new_session);
        }

        // Update view
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
        final RadioGroup GRD_SESSION_TYPE = this.findViewById( R.id.grdSessionType );
        final RadioButton RBT_POOL = this.findViewById( R.id.rbtPool );
        final RadioButton RBT_OWS = this.findViewById( R.id.rbtOWS);
        final ImageButton BT_SAVE = this.findViewById( R.id.btSaveSession );
        final ImageButton BT_SCRSHOT = this.findViewById( R.id.btTakeScrshotForEditSession );
        final EditText ED_LAPS = this.findViewById( R.id.edLaps );
        final Spinner CB_POOL_LENGTH = this.findViewById( R.id.cbPoolLength );
        final TextView LBL_LENGTH1 = this.findViewById( R.id.lblLength1 );
        final TextView LBL_LENGTH2 = this.findViewById( R.id.lblLength2 );
        final TextView ED_TEMPERATURE = this.findViewById( R.id.edTemperature );
        final RadioButton RBT_TRAINING = this.findViewById( R.id.rbtTraining );
        final RadioButton RBT_COMPETITION = this.findViewById( R.id.rbtCompetition);

        // Prepares the label for distance
        if ( settings.getDistanceUnits() == Distance.Units.mi ) {
            LBL_LENGTH1.setText( R.string.label_yard );
            LBL_LENGTH2.setText( R.string.label_yard );
        }

        // Prepares pool length & laps spinner
        final ArrayAdapter<String> POOL_LENGTH_ADAPTER =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        PoolLength.toStringList() );

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

        CB_POOL_LENGTH.setSelection( settings.getPoolLength().ordinal(), false );

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
        ED_DATE.setText( this.date.toShortDateString() );
        ED_TEMPERATURE.setText( String.format( Locale.getDefault(), "%04.2f", this.temperature ) );

        if ( this.atPool ) {
            GRD_WATER_TYPES.check( RBT_POOL.getId() );
        } else {
            GRD_WATER_TYPES.check( RBT_OWS.getId() );
        }

        if ( this.isCompetition ) {
            GRD_SESSION_TYPE.check( RBT_COMPETITION.getId() );
        } else {
            GRD_SESSION_TYPE.check( RBT_TRAINING.getId() );
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
        RBT_POOL.setOnCheckedChangeListener( (bt, isChecked) -> this.chkAtPool() );

        RBT_OWS.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_sea, 0 );
        RBT_OWS.setOnCheckedChangeListener( (bt, isChecked) -> this.chkAtPool() );

        RBT_TRAINING.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_training, 0 );
        RBT_TRAINING.setOnCheckedChangeListener( (bt, isChecked) -> this.chkIsCompetition() );

        RBT_COMPETITION.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_competition, 0 );
        RBT_COMPETITION.setOnCheckedChangeListener( (bt, isChecked) -> this.chkIsCompetition() );

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
                EditSessionActivity.this.place = StringUtil.capitalize( s.toString() );
            }
        });

        ED_TEMPERATURE.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s)
            {
                EditSessionActivity.this.storeTemperature();
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
                EditSessionActivity.this.notes = StringUtil.capitalize( s.toString() );
            }
        });

        this.blockListeners = false;

        // Prepare
        this.chkAtPool();
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
        final int[] DATA_DATE = Date.dataFromDate( Calendar.getInstance().getTime() );
        final DatePickerDialog dlg = new DatePickerDialog(
                this,
                R.style.DialogTheme,
                (dp, y, m, d) -> this.setDate( Date.from( y, m, d ) ),
                DATA_DATE[ 0 ],
                DATA_DATE[ 1 ],
                DATA_DATE[ 2 ]
        );

        dlg.getDatePicker()
                .setFirstDayOfWeek(
                        settings.getFirstDayOfWeek().getCalendarValue() );

        dlg.show();
    }

    /** Checks whether we are in a session at the pool or not. */
    private void chkAtPool()
    {
        final View LY_POOL_LAPS = this.findViewById( R.id.lyPoolLaps );
        final RadioButton RBT_POOL = this.findViewById( R.id.rbtPool );

        this.atPool = RBT_POOL.isChecked();
        LY_POOL_LAPS.setVisibility( this.atPool ? View.VISIBLE : View.GONE );
    }

    /** Checks whether we are in a session which is a competition or not. */
    private void chkIsCompetition()
    {
        final RadioButton RBT_COMPETITION = this.findViewById( R.id.rbtCompetition );

        this.isCompetition = RBT_COMPETITION.isChecked();
    }

    /** Updates the date info, and reflects the change in the view. */
    private void setDate(Date d)
    {
        final EditText ED_DATE = this.findViewById( R.id.edDate );

        this.date = d;
        ED_DATE.setText( this.date.toShortDateString() );

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

    /** Stores the temperature. */
    private void storeTemperature()
    {
        final EditText ED_TEMPERATURE = this.findViewById( R.id.edTemperature );
        final String STR_TEMPERATURE = ED_TEMPERATURE.getText().toString().trim();

        if ( !STR_TEMPERATURE.isEmpty() ) {
            try {
                final NumberFormat NF = NumberFormat.getInstance( Locale.getDefault() );
                this.temperature = NF.parse( STR_TEMPERATURE ).doubleValue();
            } catch(ParseException exc) {
                Log.d( LOG_TAG, "temperature parsing exception: " + exc.getMessage() );
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
            final EditText ED_POOL_LAPS = this.findViewById( R.id.edLaps );
            int poolLength = this.getPoolLength();
            int numLaps = 0;
            String strNumLaps = "";

            if ( poolLength > 0 ) {
                int modLaps = this.distance % poolLength;
                numLaps = this.distance / poolLength;

                if ( modLaps != 0 ) {
                    ++numLaps;
                }
            }

            strNumLaps += numLaps;

            this.blockListeners = true;
            ED_POOL_LAPS.setText( strNumLaps );
            this.blockListeners = false;
        }

        return;
    }

    /** Calculates and shows the mean speed as data is entered. */
    private void calculateMeanSpeed()
    {
        final TextView LBL_SPEED = this.findViewById( R.id.lblSpeed );
        final Distance.Units UNITS = settings.getDistanceUnits();
        final Speed SPEED = new Speed(
                                new Distance( this.distance, UNITS ),
                                this.duration );
        final String TEXT = SPEED.getSpeedPerHourAsString() + " - " + SPEED;

        LBL_SPEED.setText( TEXT );
    }

    /** Calculates the distance given the laps. */
    private void calculateDistanceByPoolLength()
    {
        if ( !this.blockListeners ) {
            final EditText ED_DISTANCE = this.findViewById( R.id.edDistance );
            int laps = this.getNumLaps();
            int poolLength = this.getPoolLength();
            final String STR_DIST_BY_LAPS = "" + ( laps * poolLength );

            this.blockListeners = true;
            ED_DISTANCE.setText( STR_DIST_BY_LAPS );
            this.blockListeners = false;
        }

        return;
    }

    /** Share the text summary of this session. */
    private void shareSessionSummary()
    {
        this.share(
                Session.summaryFromSessionData(
                        this,
                        settings,
                        this.date,
                        this.atPool,
                        this.place,
                        this.distance ));
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
                            this.isCompetition,
                            this.temperature,
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
    private double temperature;
    private boolean blockListeners;
    private boolean atPool;
    private boolean isCompetition;
}
