// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.core.storage.SessionStorage;
import com.devbaltasarq.nadamillas.ui.adapters.ListViewSessionArrayAdapter;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.util.Date;

public class BrowseActivity extends BaseActivity {
    private static final String LOG_TAG = BrowseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_browse);
        final Toolbar TOOLBAR = findViewById(R.id.toolbar);
        this.setSupportActionBar( TOOLBAR );

        final ImageButton BT_SHARE = this.findViewById( R.id.btShareDay );
        final ImageButton BT_BACK = this.findViewById( R.id.btCloseBrowse );
        final FloatingActionButton FB_NEW = this.findViewById( R.id.fbNew );
        final CompactCalendarView CV_CALENDAR = this.findViewById( R.id.cvCalendar );

        // Initialize widgets
        this.currentDate = Util.getDate().getTime();
        this.onDateChanged();
        this.highlightDays();

        // Connect listeners
        CV_CALENDAR.setListener( new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                BrowseActivity.this.onDateChanged( dateClicked );
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                BrowseActivity.this.highlightDays( firstDayOfNewMonth );
            }
        });

        FB_NEW.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BrowseActivity.this.onNewSession();
            }
        } );

        BT_BACK.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowseActivity.this.finish();
            }
        });

        BT_SHARE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BrowseActivity SELF = BrowseActivity.this;

                SELF.share( SELF.takeScreenshot( LOG_TAG, dataStore ) );
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult( requestCode, resultCode, data );

        if ( resultCode == Activity.RESULT_OK ) {
            switch( requestCode ) {
                case RC_NEW_SESSION:
                    this.storeNewSession( dataStore, data );
                    break;
                case RC_EDIT_SESSION:
                    final Session SESSION = SessionStorage.createFrom( data );

                    this.updateSession( dataStore, SESSION );
                    break;
            }

            this.highlightDays();
            this.updateSessions();
        }

        return;
    }

    private void highlightDays()
    {
        this.highlightDays( this.currentDate );
    }

    private void highlightDays(Date firstDayOfMonth)
    {
        final CompactCalendarView CV_CALENDAR = this.findViewById( R.id.cvCalendar );
        final Session[] SESSIONS = dataStore.getSessionsForMonth( firstDayOfMonth );

        CV_CALENDAR.removeAllEvents();

        for(Session session: SESSIONS) {
            int indicatorColor = Color.GREEN;

            if ( session.isAtPool() ) {
                indicatorColor = Color.CYAN;
            }

            Event ev1 = new Event( indicatorColor, session.getDate().getTime(), "extra" );
            CV_CALENDAR.addEvent( ev1 );
        }

        return;
    }

    /** Update sessions in the list view. */
    private void updateSessions()
    {
        final ListView LV_SESSIONS = this.findViewById( R.id.lvSessionsPerDay );
        final Session[] SESSIONS = dataStore.getSessionsForDay( this.currentDate );

        LV_SESSIONS.setAdapter( new ListViewSessionArrayAdapter( this, SESSIONS ) );
    }

    /** Handler of the new session event. */
    private void onNewSession()
    {
        this.launchNewSessionEdit( this.currentDate );
    }

    /** Handler of the date changed event. */
    private void onDateChanged()
    {
        this.onDateChanged( this.currentDate );
    }

    private void onDateChanged(Date date)
    {
        final TextView LBL_SELECTED_DAY = this.findViewById( R.id.lblSelectedDay );

        this.currentDate = date;
        LBL_SELECTED_DAY.setText(Util.getFullDate( date, null ) );
        this.updateSessions();
    }

    /** Handler of the edit session event. */
    public void onEditSession(Session session)
    {
        this.dateOfLastEditedSession = session.getDate();
        this.launchSessionEdit( session );
    }

    /** Handler of the delete session event. */
    public void onDeleteSession(Session session)
    {
        this.deleteSession( dataStore, session );

        this.updateSessions();
        this.highlightDays();
    }

    private Date currentDate;
    private Date dateOfLastEditedSession;
    public static DataStore dataStore;
}
