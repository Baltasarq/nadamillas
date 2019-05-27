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
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.core.storage.SessionStorage;
import com.devbaltasarq.nadamillas.ui.adapters.ListViewSessionArrayAdapter;
import com.devbaltasarq.nadamillas.ui.adapters.SessionCursorAdapter;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.util.Date;

public class BrowseActivity extends BaseActivity {
    private static final String LOG_TAG = BrowseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        this.setContentView( R.layout.activity_browse );
        final Toolbar TOOLBAR = this.findViewById( R.id.toolbar );
        this.setSupportActionBar( TOOLBAR );

        final ImageButton BT_SHARE = this.findViewById( R.id.btShareDay );
        final ImageButton BT_SCRSHOT = this.findViewById( R.id.btTakeScrshotForBrowse );
        final ImageButton BT_BACK = this.findViewById( R.id.btCloseBrowse );
        final FloatingActionButton FB_NEW = this.findViewById( R.id.fbNew );
        final CompactCalendarView CV_CALENDAR = this.findViewById( R.id.cvCalendar );
        final ImageButton BT_PREVIOUS = this.findViewById( R.id.btPreviousMonth );
        final ImageButton BT_NEXT = this.findViewById( R.id.btNextMonth );

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

        CV_CALENDAR.setFirstDayOfWeek( settings.getFirstDayOfWeek().getCalendarValue() );
        CV_CALENDAR.setUseThreeLetterAbbreviation( true );
        CV_CALENDAR.shouldDrawIndicatorsBelowSelectedDays( true );

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

                SELF.share( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
            }
        });

        BT_SCRSHOT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BrowseActivity SELF = BrowseActivity.this;

                SELF.save( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
            }
        });

        BT_PREVIOUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CV_CALENDAR.scrollLeft();
            }
        });

        BT_NEXT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CV_CALENDAR.scrollRight();
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        final CompactCalendarView CV_CALENDAR = this.findViewById( R.id.cvCalendar );

        this.currentDate = Util.getDate().getTime();
        CV_CALENDAR.setCurrentDate( this.currentDate );

        this.onDateChanged();
        this.update();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult( requestCode, resultCode, data );

        if ( resultCode == Activity.RESULT_OK ) {
            switch( requestCode ) {
                case RC_NEW_SESSION:
                    this.storeNewSession( data );
                    break;
                case RC_EDIT_SESSION:
                    final Session SESSION = SessionStorage.createFrom( data );

                    this.updateSession( SESSION );
                    break;
            }

            this.update();
        }

        return;
    }

    private void update()
    {
        this.updateSessions();
        this.highlightDays();
    }

    private void highlightDays()
    {
        this.highlightDays( this.currentDate );
    }

    private void updateCalendarDate(Date date)
    {
        final TextView LBL_YEARMONTH = this.findViewById( R.id.lblYearMonth );
        String isoCalendarDate = Util.getISODate( date );

        // Remove the day
        int posLastDelimiter = isoCalendarDate.lastIndexOf( '-' );

        if ( posLastDelimiter >= 0 ) {
            isoCalendarDate = isoCalendarDate.substring( 0, posLastDelimiter );
        }

        LBL_YEARMONTH.setText( isoCalendarDate );
    }

    private void highlightDays(Date firstDayOfMonth)
    {
        final CompactCalendarView CV_CALENDAR = this.findViewById( R.id.cvCalendar );
        final Session[] SESSIONS = dataStore.getSessionsForMonth( firstDayOfMonth );

        CV_CALENDAR.removeAllEvents();
        this.updateCalendarDate( firstDayOfMonth );

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
        LBL_SELECTED_DAY.setText( Util.getFullDate( date, null ) );
        this.updateCalendarDate( date );
        this.updateSessions();
    }

    /** Handle the ops menu event. */
    public void onEntryOpsMenu()
    {
        final IconListAlertDialog DLG = new IconListAlertDialog( this,
                R.drawable.ic_swimming_figure,
                R.string.title_activity_edit_session,
                new int[]{
                        R.drawable.btn_pencil,
                        R.drawable.btn_delete
                },
                new int[]{
                        R.string.action_modify,
                        R.string.action_delete
                } );

        DLG.setItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                final Session SESSION = ListViewSessionArrayAdapter.selectedSession;
                final boolean isModify = ( position == 0 );

                DLG.hide();
                DLG.dismiss();

                if ( isModify ) {
                    BrowseActivity.this.onEditSession( SESSION );
                } else {
                    BrowseActivity.this.onDeleteSession( SESSION );
                }
            }
        });

        DLG.show();
    }

    /** Handler of the edit session event. */
    public void onEditSession(Session session)
    {
        this.launchSessionEdit( session );
    }

    /** Handler of the delete session event. */
    public void onDeleteSession(Session session)
    {
        this.deleteSession( session );

        this.update();
    }

    private Date currentDate;
}
