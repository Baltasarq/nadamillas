// NadaMillas (c) 2019-2023/24 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import com.devbaltasarq.nadamillas.core.session.Distance;
import com.devbaltasarq.nadamillas.core.session.Distance.Fmt.UnitsUse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.AppInfo;
import com.devbaltasarq.nadamillas.core.session.Date;
import com.devbaltasarq.nadamillas.core.YearInfo;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.storage.SettingsStorage;

import java.util.Locale;


public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        this.setContentView( R.layout.activity_main );
        final Toolbar TOOL_BAR = this.findViewById( R.id.toolbar );
        this.setSupportActionBar( TOOL_BAR );

        final ImageButton BT_SCRSHOT = this.findViewById( R.id.btTakeScrshotForSummary );
        final FloatingActionButton FB_NEW = this.findViewById( R.id.fbNew );
        final FloatingActionButton FB_BROWSE = this.findViewById( R.id.fbBrowse );
        final FloatingActionButton FB_STATS = this.findViewById( R.id.fbStats );
        final TextView LBL_TITLE = this.findViewById( R.id.lblTitle );

        this.setTitle( "" );

        BT_SCRSHOT.setOnClickListener( v -> {
            final MainActivity SELF = MainActivity.this;

            SELF.shareScreenShot( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
        });

        LBL_TITLE.setOnClickListener( v -> MainActivity.this.showStatus( LOG_TAG, AppInfo.getCompleteAuthoringMessage() ));

        FB_NEW.setOnClickListener( v -> MainActivity.this.onNewSession() );
        FB_BROWSE.setOnClickListener( v -> MainActivity.this.onBrowse() );
        FB_STATS.setOnClickListener( v -> MainActivity.this.onStats() );

        final ProgressView PROGRESS = this.findViewById( R.id.pvProgress );
        final DrawerLayout DRAWER = this.findViewById( R.id.drawer_layout );
        final ActionBarDrawerToggle TOGGLE = new ActionBarDrawerToggle(
                this, DRAWER, TOOL_BAR, R.string.navigation_drawer_open, R.string.navigation_drawer_close );

        PROGRESS.setColor( Color.parseColor( "#ff008b8b" ) );
        DRAWER.addDrawerListener( TOGGLE );
        TOGGLE.syncState();

        final NavigationView NAVIGATION_VIEW = this.findViewById( R.id.nav_view );
        NAVIGATION_VIEW.setNavigationItemSelectedListener( this );

        // Back
        this.getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback( true ) {
                    @Override
                    public void handleOnBackPressed() {
                        MainActivity.this.onBack();
                    }
                }
        );
    }

    @Override
    public void onResume()
    {
        super.onResume();

        final Context APP_CONTEXT = this.getApplicationContext();
        final Date DATE = new Date();

        dataStore = DataStore.createFor( APP_CONTEXT );
        settings = SettingsStorage.restore( APP_CONTEXT );

        if ( DATE.getWeekDay() == DATE.getMondayWeekDayCode() ) {
            Thread backupThread = new Thread() {
                @Override
                public void run() {
                    dataStore.backup();
                }
            };

            backupThread.start();
        }

        this.update();
    }

    private void onBack()
    {
        final DrawerLayout drawer = this.findViewById( R.id.drawer_layout );

        if ( drawer.isDrawerOpen( GravityCompat.START ) ) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            this.finish();
        }

        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if ( this.go( item.getItemId() ) ) {
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        final DrawerLayout drawer = this.findViewById( R.id.drawer_layout );

        this.go( item.getItemId() );

        drawer.closeDrawer( GravityCompat.START );
        return true;
    }

    /** Starts the appropriate action.
      * @param actionId Typically an R.id.nav_* or R.id.action_*
      * @return true if an action was triggered, false otherwise.
      */
    private boolean go(int actionId)
    {
        boolean toret = false;

        if ( actionId == R.id.nav_new
          || actionId == R.id.action_new )
        {
            toret = true;
            this.onNewSession();
        }
        else
        if ( actionId == R.id.action_browse
          || actionId == R.id.nav_browse )
        {
            toret = true;
            this.onBrowse();
        }
        else
        if ( actionId == R.id.action_stats
          || actionId == R.id.nav_stats )
        {
            this.onStats();
            toret = true;
        }
        else
        if ( actionId == R.id.action_achievements
          || actionId == R.id.nav_achievements )
        {
            this.onAchievements();
            toret = true;
        }
        else
        if ( actionId == R.id.action_history
          || actionId == R.id.nav_history )
        {
            this.onHistory();
            toret = true;
        }
        else
        if ( actionId == R.id.action_settings
          || actionId == R.id.nav_settings )
        {
            this.onSettings();
            toret = true;
        }

        return toret;
    }

    /** Updates the on-screen totals. */
    @Override
    protected void update()
    {
        final TextView LBL_WEEKDAY_NAME = this.findViewById( R.id.lblWeekDayName );
        final TextView LBL_DATE = this.findViewById( R.id.lblDate );
        final YearInfo INFO = dataStore.getCurrentYearInfo();
        final Date DATE = new Date();

        LBL_WEEKDAY_NAME.setText( DATE.getWeekDayName() );
        LBL_DATE.setText( DATE.toSemiFullDateString() );

        this.showProgressForTotals( INFO );
        this.showProgressForOWS( INFO );
        this.showProgressForPool( INFO );

        this.showUnits();
    }

    private void showProgressForTotals(final YearInfo INFO)
    {
        final TextView LBL_TOTAL = this.findViewById( R.id.lblTotal );
        final TextView LBL_PROJECTION = this.findViewById( R.id.lblProjection );
        final TextView LBL_TARGET = this.findViewById( R.id.lblTarget );
        final ProgressView PROGRESS_VIEW = this.findViewById( R.id.pvProgress );
        final Distance.Units UNITS = settings.getDistanceUnits();

        String total = "0";
        String progress = "0";
        String target = YearInfo.NOT_APPLYABLE;
        String projection = YearInfo.NOT_APPLYABLE;
        String projectionPercentage = YearInfo.NOT_APPLYABLE;

        if ( INFO != null ) {
            final int TOTAL = INFO.getDistance( YearInfo.SwimKind.TOTAL );
            final int TARGET = INFO.getTarget( YearInfo.SwimKind.TOTAL );
            final int PROGRESS = (int) INFO.getProgress( YearInfo.SwimKind.TOTAL );
            final int PROJECTED = (int) INFO.calcProjection( TOTAL );
            final int PROJECTED_PERCENTAGE = (int) INFO.calcProgress( PROJECTED, TARGET );
            final Thread SHOW_PROGRESS_THREAD = new Thread() {
                @Override
                public void run()
                {
                    try {
                        for(int i = 0; i < ( PROGRESS - 1 ); ++i) {
                            final int POS = i;

                            MainActivity.this.runOnUiThread(
                                    () -> PROGRESS_VIEW.setProgress( POS )
                            );
                            Thread.sleep( 10 );
                        }
                    } catch(InterruptedException exc) {
                        Log.d( LOG_TAG, "interrupted: " + exc.getMessage() );
                    }

                    MainActivity.this.runOnUiThread(
                                () -> PROGRESS_VIEW.setProgress( PROGRESS ) );
                }
            };

            total = Distance.Fmt.format(
                                INFO.getDistance( YearInfo.SwimKind.TOTAL ),
                                UNITS,
                                UnitsUse.NO_UNITS );
            projection = Distance.Fmt.format( PROJECTED, UNITS, UnitsUse.NO_UNITS );
            projectionPercentage = PROJECTED_PERCENTAGE + "%";
            target = Distance.Fmt.format(
                                INFO.getTarget( YearInfo.SwimKind.TOTAL ),
                                UNITS, UnitsUse.NO_UNITS );
            progress = PROGRESS + "%";
            SHOW_PROGRESS_THREAD.start();
        }

        LBL_TOTAL.setText(
                String.format( Locale.getDefault(),
                        "%s (%s)",
                        total,
                        progress ) );
        LBL_PROJECTION.setText(
                String.format( Locale.getDefault(),
                        "%s (%s)",
                        projection,
                        projectionPercentage ) );
        LBL_TARGET.setText( target );
    }

    private void showProgressForOWS(final YearInfo INFO)
    {
        final TextView LBL_OPEN_WATERS = this.findViewById( R.id.lblOpenWaters );
        final TextView LBL_PROJECTION = this.findViewById( R.id.lblProjectionOWS );
        final TextView LBL_TARGET = this.findViewById( R.id.lblTargetOWS );
        final Distance.Units UNITS = settings.getDistanceUnits();
        String totalOpenWaters = "0";
        String target = YearInfo.NOT_APPLYABLE;
        String projection = YearInfo.NOT_APPLYABLE;
        String projectionPercentage = YearInfo.NOT_APPLYABLE;
        String progress = "0";

        if ( INFO != null ) {
            final int TOTAL = INFO.getDistance( YearInfo.SwimKind.OWS );
            final int TARGET = INFO.getTarget( YearInfo.SwimKind.OWS );
            final int PROGRESS = (int) INFO.getProgress( YearInfo.SwimKind.OWS );
            final int PROJECTED = (int) INFO.calcProjection( TOTAL );
            final int PROJECTED_PERCENTAGE = (int) INFO.calcProgress( PROJECTED, TARGET );

            totalOpenWaters = Distance.Fmt.format( TOTAL, UNITS, UnitsUse.NO_UNITS );
            projection = Distance.Fmt.format( PROJECTED, UNITS, UnitsUse.NO_UNITS );
            projectionPercentage = PROJECTED_PERCENTAGE + "%";
            target = Distance.Fmt.format( TARGET, UNITS, UnitsUse.NO_UNITS );
            progress = PROGRESS + "%";
        }

        LBL_PROJECTION.setText(
                String.format( Locale.getDefault(),
                                "%s (%s)",
                                projection,
                                projectionPercentage ) );
        LBL_OPEN_WATERS.setText(
                String.format( Locale.getDefault(),
                        "%s (%s)",
                        totalOpenWaters,
                        progress ) );;
        LBL_TARGET.setText( target );
    }

    private void showProgressForPool(final YearInfo INFO)
    {
        final TextView LBL_POOL = this.findViewById( R.id.lblPool );
        final TextView LBL_PROJECTION = this.findViewById( R.id.lblProjectionPool );
        final TextView LBL_TARGET = this.findViewById( R.id.lblTargetPool );
        String totalPool = "0";
        String target = YearInfo.NOT_APPLYABLE;
        String projection = YearInfo.NOT_APPLYABLE;
        String projectionPercentage = YearInfo.NOT_APPLYABLE;
        String progress = "0";

        if ( INFO != null ) {
            final Distance.Units UNITS = settings.getDistanceUnits();
            final int TOTAL = INFO.getDistance( YearInfo.SwimKind.POOL );
            final int TARGET = INFO.getTarget( YearInfo.SwimKind.POOL );
            final int PROGRESS = (int) INFO.getProgress( YearInfo.SwimKind.POOL );
            final int PROJECTED = (int) INFO.calcProjection( TOTAL );
            final int PROJECTED_PERCENTAGE = (int) INFO.calcProgress( PROJECTED, TARGET );

            totalPool = Distance.Fmt.format( TOTAL, UNITS, UnitsUse.NO_UNITS );
            projection = Distance.Fmt.format( PROJECTED, UNITS, UnitsUse.NO_UNITS );
            projectionPercentage = PROJECTED_PERCENTAGE + "%";
            target = Distance.Fmt.format( TARGET, UNITS, UnitsUse.NO_UNITS );
            progress = PROGRESS + "%";
        }

        LBL_PROJECTION.setText(
                String.format( Locale.getDefault(),
                        "%s (%s)",
                        projection,
                        projectionPercentage ) );
        LBL_POOL.setText(
                String.format( Locale.getDefault(),
                        "%s (%s)",
                        totalPool,
                        progress ) );;
        LBL_TARGET.setText( target );
    }

    private void showUnits()
    {
        final TextView LBL_UNITS = this.findViewById( R.id.lblUnits );

        LBL_UNITS.setText( settings.getDistanceUnits().toString() );
    }

    /** The new session handler. */
    private void onNewSession()
    {
        this.launchNewSessionEdit();
    }

    private void onHistory()
    {
        final Intent DATA = new Intent( this, HistoryActivity.class );

        this.LAUNCH_THEN_UPDATE.launch( DATA );
    }

    /** The settings handler. */
    private void onSettings()
    {
        final Intent DATA = new Intent( this, SettingsActivity.class );

        this.LAUNCH_THEN_UPDATE.launch( DATA );
    }

    /** The browse sessions handler. */
    private void onBrowse()
    {
        final Intent DATA = new Intent( this, BrowseActivity.class );

        this.LAUNCH_THEN_UPDATE.launch( DATA );
    }

    /** The browse sessions handler. */
    private void onStats()
    {
        this.startActivity( new Intent( this, StatsActivity.class ) );
    }

    /** The achievements handler. */
    private void onAchievements()
    {
        this.startActivity( new Intent( this, AchievementsActivity.class ) );
    }

    private final ActivityResultLauncher<Intent> LAUNCH_THEN_UPDATE =
            this.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if ( result.getResultCode() == RESULT_OK ) {
                            this.update();
                        }
                    });
}
