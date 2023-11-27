// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;

import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.ui.adapters.ListViewSessionArrayAdapter;
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

        FB_NEW.setOnClickListener( v -> BrowseActivity.this.onNewSession() );
        BT_BACK.setOnClickListener( v -> BrowseActivity.this.finish() );
        BT_PREVIOUS.setOnClickListener( v -> CV_CALENDAR.scrollLeft() );
        BT_NEXT.setOnClickListener( v -> CV_CALENDAR.scrollRight() );
        BT_SCRSHOT.setOnClickListener( v -> {
            final BrowseActivity SELF = BrowseActivity.this;

            SELF.shareScreenShot( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
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
    protected void update()
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

        DLG.setItemClickListener( (parent, view, position, id) -> {
            final Session SESSION = ListViewSessionArrayAdapter.selectedSession;
            final boolean isModify = ( position == 0 );

            DLG.hide();
            DLG.dismiss();

            if ( isModify ) {
                BrowseActivity.this.onEditSession( SESSION );
            } else {
                BrowseActivity.this.onDeleteSession( SESSION );
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
