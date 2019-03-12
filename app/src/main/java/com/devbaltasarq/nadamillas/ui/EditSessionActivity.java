// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.core.storage.SessionStorage;

import java.util.Calendar;
import java.util.Date;

public class EditSessionActivity extends AppCompatActivity {
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

        if ( DATA != null ) {
            final Session SESSION = SessionStorage.createFrom( DATA );

            if ( SESSION != null ) {
                this.date = SESSION.getDate();
                isEdit = DATA.getBoolean( IS_EDIT, false );

                if ( isEdit ) {
                    this.atPool = SESSION.isAtPool();
                    this.distance = SESSION.getDistance();
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
        final EditText ED_DISTANCE = this.findViewById( R.id.edDistance );
        final ImageButton BT_DATE = this.findViewById( R.id.btDate );
        final CheckBox CHK_POOL = this.findViewById( R.id.chkPool );
        final FloatingActionButton FB_SAVE = this.findViewById( R.id.fbSaveSession );

        ED_DATE.setText( Util.getShortDate( this.date, null ) );
        CHK_POOL.setChecked( true );

        if ( this.distance > 0 ) {
            ED_DISTANCE.setText( Integer.toString( this.distance ) );
        }

        // Set listeners
        BT_BACK.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditSessionActivity.this.finish();
            }
        });

        FB_SAVE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditSessionActivity.this.save();
            }
        });

        BT_DATE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditSessionActivity.this.chooseDate();
            }
        });

        CHK_POOL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EditSessionActivity.this.setAtPool();
            }
        });

        ED_DISTANCE.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                EditSessionActivity.this.storeDistance( s );
            }
        });
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

    /** Update whether the session was done at the pool. */
    private void setAtPool()
    {
        final CheckBox CHK_POOL = this.findViewById( R.id.chkPool );

        this.atPool = CHK_POOL.isChecked();
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

    /** Saves the data and returns. */
    private void save()
    {
        // Store data
        final Intent RET_DATA = new Intent();
        final Bundle DATA = new Bundle();

        new SessionStorage( new Session( this.date, this.distance, this.atPool ) ).toBundle( DATA );
        RET_DATA.putExtras( DATA );

        // Finish
        this.setResult( Activity.RESULT_OK, RET_DATA );
        this.finish();
    }

    private Date date;
    private int distance;
    private boolean atPool;
}
