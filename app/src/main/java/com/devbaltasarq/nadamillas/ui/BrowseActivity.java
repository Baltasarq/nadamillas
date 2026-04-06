// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;

import android.os.Bundle;

import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener;
import com.devbaltasarq.nadamillas.core.settings.FirstDayOfWeek;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.session.Date;
import com.devbaltasarq.nadamillas.ui.adapters.ListViewSessionArrayAdapter;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.CalendarWeekDay;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


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
        final FrameLayout frmCalendarFrame = this.findViewById( R.id.frmCalendarFrame );

        // Compose
        frmCalendarFrame.addView( this.buildCalendar() );

        // Handlers
        FB_NEW.setOnClickListener( v -> BrowseActivity.this.onNewSession() );
        BT_BACK.setOnClickListener( v -> BrowseActivity.this.finish() );
        BT_SCRSHOT.setOnClickListener( v -> {
            final BrowseActivity SELF = BrowseActivity.this;

            SELF.shareScreenShot( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
        });
    }

    private CalendarView buildCalendar()
    {
        final Calendar MAX = Calendar.getInstance();
        final Calendar MIN = Calendar.getInstance();
        final int DELTA_MONTH = 12;
        var firstDayOfWeek = CalendarWeekDay.MONDAY;

        this.cvCalendar = new CalendarView( this );

        // First day of week
        if ( settings.getFirstDayOfWeek() == FirstDayOfWeek.SUNDAY ) {
            firstDayOfWeek = CalendarWeekDay.SUNDAY;
        }

        this.cvCalendar.setFirstDayOfWeek( firstDayOfWeek );
        this.cvCalendar.setHeaderColor( R.color.design_default_color_primary );

        // Min and max date
        MIN.add( Calendar.MONTH, -DELTA_MONTH );
        MAX.add( Calendar.MONTH, DELTA_MONTH );

        this.cvCalendar.setMinimumDate( MIN );
        this.cvCalendar.setMaximumDate( MAX );

        // Handlers
        this.cvCalendar.setOnCalendarDayClickListener( eventDay -> {
            Date clickedDay = Date.from( eventDay.getCalendar().getTimeInMillis() );

            this.onDateChanged( clickedDay );
        });

        final OnCalendarPageChangeListener PAGE_CHANGED_HANDLER = () -> {
            Date newDate = Date.from(
                    this.cvCalendar.getCurrentPageDate().getTimeInMillis() );
            this.onDateChanged( newDate );
        };

        this.cvCalendar.setOnPreviousPageChangeListener( PAGE_CHANGED_HANDLER );
        this.cvCalendar.setOnForwardPageChangeListener( PAGE_CHANGED_HANDLER );

        this.addedMonths = new HashSet<>();
        this.currentDate = new Date();
        return this.cvCalendar;
    }

    /** Adds various event days.
     * @param sessions a list of sessions for the event days.
     */
    private void addAllEventDaysIn(List<Session> sessions)
    {
        final var DOT = ContextCompat.getDrawable(
                this.cvCalendar.getContext(),
                R.drawable.ic_dot );

        final var CAL_DAYS = Objects.requireNonNull( sessions )
                .stream().map( (session) -> {
                    var calDay = new CalendarDay( session.getDate().toCalendar() );
                    calDay.setBackgroundDrawable( DOT );
                    return calDay;
                }).collect( Collectors.toList() );

        // Apply to calendar
        this.cvCalendar.setCalendarDays( CAL_DAYS );
    }

    @Override
    public void onResume()
    {
        super.onResume();

        this.currentDate = new Date();
        this.onDateChanged();
        this.update();
    }

    @Override
    protected void update()
    {
        this.updateSessions();
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
        var monthDate = Date.from(
                this.currentDate.getYear(),
                this.currentDate.getMonth(),
                1 );

        this.addedMonths.remove( monthDate );
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
        LBL_SELECTED_DAY.setText( date.toFullDateString() );
        this.updateSessions();
        this.highlightDays();
    }

    private void highlightDays()
    {
        var monthDate = Date.from(
                                this.currentDate.getYear(),
                                this.currentDate.getMonth(),
                            1 );

        if ( !this.addedMonths.contains( monthDate ) ) {
            this.addedMonths.add( monthDate );
            this.addAllEventDaysIn( Arrays.asList(
                    Objects.requireNonNull(
                            dataStore.getSessionsForMonth( monthDate ) ) ) );
        }
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
    private Set<Date> addedMonths;
    private CalendarView cvCalendar;
}
