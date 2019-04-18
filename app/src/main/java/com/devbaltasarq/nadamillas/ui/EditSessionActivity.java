// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.Duration;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Settings;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.core.storage.SessionStorage;

import java.util.Calendar;
import java.util.Date;
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

        this.date = Util.getDate().getTime();
        this.duration = new Duration( 0 );

        if ( DATA != null ) {
            final Session SESSION = SessionStorage.createFrom( DATA );

            if ( SESSION != null ) {
                this.date = SESSION.getDate();
                isEdit = DATA.getBoolean( IS_EDIT, false );

                if ( isEdit ) {
                    this.atPool = SESSION.isAtPool();
                    this.distance = SESSION.getDistance();
                    this.duration = SESSION.getDuration();
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
        final ImageButton BT_BACK = this.findViewById( R.id.btCloseEditSession );
        final EditText ED_DATE = this.findViewById( R.id.edDate );
        final EditText ED_HOURS = this.findViewById( R.id.edHours );
        final EditText ED_MINUTES = this.findViewById( R.id.edMinutes );
        final EditText ED_SECONDS = this.findViewById( R.id.edSeconds );
        final EditText ED_DISTANCE = this.findViewById( R.id.edDistance );
        final ImageButton BT_DATE = this.findViewById( R.id.btDate );
        final RadioGroup GRD_WATER_TYPES = this.findViewById( R.id.grdWaters );
        final RadioButton RBT_POOL = this.findViewById( R.id.rbtPool );
        final RadioButton RBT_OPEN = this.findViewById( R.id.rbtOpen );
        final ImageButton BT_SAVE = this.findViewById( R.id.btSaveSession );

        ED_DATE.setText( Util.getShortDate( this.date, null ) );

        if ( this.atPool ) {
            GRD_WATER_TYPES.check( RBT_POOL.getId() );
        } else {
            GRD_WATER_TYPES.check( RBT_OPEN.getId() );
        }

        if ( this.distance > 0 ) {
            ED_DISTANCE.setText( String.format( Locale.getDefault(), "%d", this.distance ) );
        }

        if ( this.duration.getTimeInSeconds() > 0 ) {
            ED_HOURS.setText( String.valueOf( this.duration.getHours() ) );
            ED_MINUTES.setText( String.valueOf( this.duration.getMinutes() ) );
            ED_SECONDS.setText( String.valueOf( this.duration.getSeconds() ) );
        }

        // Set listeners
        BT_BACK.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditSessionActivity.this.finish();
            }
        });

        BT_SAVE.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditSessionActivity.this.save();
            }
        });

        BT_DATE.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditSessionActivity.this.chooseDate();
            }
        });

        GRD_WATER_TYPES.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup grp, int id) {
                int pos = ( id == RBT_POOL.getId() ) ? 0 : 1;

                EditSessionActivity.this.setAtPool( pos );
            }
        });

        ED_DISTANCE.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                EditSessionActivity.this.storeDistance( s );
                EditSessionActivity.this.calculateMeanSpeed();
            }
        });

        ED_HOURS.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                EditSessionActivity.this.storeDuration();
                EditSessionActivity.this.calculateMeanSpeed();
            }
        });

        ED_MINUTES.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                EditSessionActivity.this.storeDuration();
                EditSessionActivity.this.calculateMeanSpeed();
            }
        });

        ED_SECONDS.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                EditSessionActivity.this.storeDuration();
                EditSessionActivity.this.calculateMeanSpeed();
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        this.calculateMeanSpeed();
    }

    /** Launch a date picker dialog. */
    private void chooseDate()
    {
        final int[] DATA_DATE = Util.dataFromDate( Calendar.getInstance().getTime() );
        final DatePickerDialog dlg = new DatePickerDialog(
                this,
                R.style.DialogTheme,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker dp, int y, int m, int d) {
                        EditSessionActivity.this.setDate( Util.dateFromData( y, m ,d ) );
                    }
                },
                DATA_DATE[ 0 ],
                DATA_DATE[ 1 ],
                DATA_DATE[ 2 ]
        );
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

    /** Calculates and shows the mean speed as data is entered. */
    private void calculateMeanSpeed()
    {
        final TextView LBL_SPEED = this.findViewById( R.id.lblSpeed );
        final Session FAKE_SESSION = new Session( this.date, this.distance, this.duration, this.atPool );

        LBL_SPEED.setText( FAKE_SESSION.getSpeedAsString( settings )
                            + " - "
                            + FAKE_SESSION.getMeanTimeAsString( settings
        ) );
    }

    /** Saves the data and returns. */
    private void save()
    {
        // Store data
        final Intent RET_DATA = new Intent();
        final Bundle DATA = new Bundle();

        new SessionStorage( new Session( this.date, this.distance, this.duration, this.atPool ) ).toBundle( DATA );
        RET_DATA.putExtras( DATA );

        // Finish
        this.setResult( Activity.RESULT_OK, RET_DATA );
        this.finish();
    }

    private Date date;
    private int distance;
    private Duration duration;
    private boolean atPool;
}
