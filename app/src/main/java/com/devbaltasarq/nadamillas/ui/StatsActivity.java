// NadaMillas (c) 2019/22 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;

import java.text.DateFormatSymbols;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.YearInfo;
import com.devbaltasarq.nadamillas.core.storage.YearInfoStorage;
import com.devbaltasarq.nadamillas.ui.graph.BarChart;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class StatsActivity extends BaseActivity {
    private final int NUM_YEARS_IN_GRAPH = 10;
    private final static String LOG_TAG = StatsActivity.class.getSimpleName();
    private enum GraphType { Weekly, Monthly, Yearly;

        /** @return the corresponding enum value, given its position. */
        public static GraphType fromOrdinal(int pos)
        {
            return GraphType.values()[ pos ];
        }
    }

    @Override @SuppressWarnings("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView( R.layout.activity_stats );

        final Toolbar TOOL_BAR = this.findViewById( R.id.toolbar );
        this.setSupportActionBar( TOOL_BAR );

        final Spinner CB_YEARS = this.findViewById( R.id.cbGraphYear );
        final Spinner CB_MONTHS = this.findViewById( R.id.cbGraphMonth );
        final Spinner CB_TIME_SEGMENT = this.findViewById( R.id.cbTimeSegment );
        final ImageButton BT_SHARE = this.findViewById( R.id.btShareStats);
        final ImageButton BT_SCRSHOT = this.findViewById( R.id.btTakeScrshotForStats );
        final ImageButton BT_BACK = this.findViewById( R.id.btCloseStats );
        final ImageButton BT_SHOW_GRAPH = this.findViewById( R.id.btShowGraph );
        final ImageButton BT_SHOW_REPORT = this.findViewById( R.id.btShowReport );

        // Prepares time segment spinner
        final ArrayAdapter<String> SEGMENTS_ADAPTER = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        this.getString( R.string.label_week ),
                        this.getString( R.string.label_month ),
                        this.getString( R.string.label_year )
        });

        CB_TIME_SEGMENT.setAdapter( SEGMENTS_ADAPTER );

        // Prepare the months spinner
        final ArrayAdapter<String> MONTHS_ADAPTER = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new DateFormatSymbols().getMonths() );

        CB_MONTHS.setAdapter( MONTHS_ADAPTER );
        CB_MONTHS.setSelection( Calendar.getInstance().get( Calendar.MONTH ), false );

        // Prepare years spinner
        final ArrayAdapter<String> CB_YEARS_ADAPTER = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                this.retrieveAllYearInfos()
        );

        CB_YEARS.setAdapter( CB_YEARS_ADAPTER );

        // Chart image viewer
        final StandardGestures GESTURES = new StandardGestures( this );
        this.chartView = findViewById( R.id.ivChartViewer );
        this.chartView.setOnTouchListener( GESTURES );

        // Listeners
        CB_YEARS.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StatsActivity.this.update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CB_MONTHS.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StatsActivity.this.update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CB_TIME_SEGMENT.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StatsActivity.this.update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // View choice listener
        BT_SHOW_GRAPH.setOnClickListener( v -> this.chooseViewGraph() );
        BT_SHOW_REPORT.setOnClickListener( v -> this.chooseViewReport() );
        this.chooseViewGraph();

        // Main activity buttons
        BT_SHARE.setOnClickListener( v -> {
                final StatsActivity SELF = StatsActivity.this;

                SELF.share( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
        });

        BT_SCRSHOT.setOnClickListener( v -> {
                final StatsActivity SELF = StatsActivity.this;

                SELF.saveScreenShotToDownloads( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
        });

        BT_BACK.setOnClickListener( v ->StatsActivity.this.finish() );
    }

    private String[] retrieveAllYearInfos()
    {
        final int CURRENT_YEAR = Calendar.getInstance().get( Calendar.YEAR );
        Cursor cursor = null;
        String[] toret = null;

        try {
            cursor = dataStore.getDescendingAllYearInfosCursor();
            final ArrayList<String> YEARS = new ArrayList<>( cursor.getCount() );

            while( cursor.moveToNext() ) {
                final int YEAR = cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            YearInfoStorage.FIELD_YEAR ) );

                if ( YEAR <= CURRENT_YEAR ) {
                    YEARS.add( String.valueOf( YEAR ) );
                }
            }

            toret = YEARS.toArray( new String[0] );
        } catch(SQLException exc) {
            Log.e( LOG_TAG, exc.getMessage() );
        } finally {
            DataStore.close( cursor );
        }

        return toret;
    }

    private void calculateDataForYearsStats(int year, final ArrayList<BarChart.SeriesInfo> SERIES)
    {
        final BarChart.SeriesInfo TOTAL_SERIE =
                            new BarChart.SeriesInfo(
                                this.getString( R.string.label_total ), Color.BLUE );
        final BarChart.SeriesInfo OPEN_SERIE =
                new BarChart.SeriesInfo(
                        this.getString( R.string.label_open_waters ), Color.CYAN );
        final int CURRENT_YEAR = Calendar.getInstance().get( Calendar.YEAR );
        final TextView TXT_REPORT = this.findViewById( R.id.txtReport );
        final String STR_UNITS = settings.getDistanceUnits().toString();
        final String LBL_TOTAL = this.getString( R.string.label_total );
        final String LBL_DISTANCE = this.getString( R.string.label_distance );
        final String LBL_OPEN_WATERS = this.getString( R.string.label_open_waters );
        int yearsRetrieved = 0;

        // Adjust years info
        if ( year != CURRENT_YEAR ) {
            year -= NUM_YEARS_IN_GRAPH / 2;
        } else {
            year -= NUM_YEARS_IN_GRAPH - 1;
        }

        TXT_REPORT.setText( "" );
        while( yearsRetrieved < NUM_YEARS_IN_GRAPH ) {
            final int DISPLAY_YEAR = year % 1000;
            final YearInfo YEAR_INFO = dataStore.getInfoFor( year );

            // Graph
            final int TOTAL_K = YEAR_INFO.getTotal();
            final int TOTAL_OW = YEAR_INFO.getTotal() - YEAR_INFO.getTotalPool();
            final String STR_TOTAL_K = settings.toUnitsAsString( TOTAL_K ) + STR_UNITS;
            final String STR_TOTAL_OW = settings.toUnitsAsString( TOTAL_OW ) + STR_UNITS;

            TOTAL_SERIE.add( new BarChart.Point( DISPLAY_YEAR, TOTAL_K ) );
            OPEN_SERIE.add( new BarChart.Point( DISPLAY_YEAR, TOTAL_OW ) );

            // Report
            TXT_REPORT.append( capitalize( LBL_TOTAL ) + ": "
                                + YEAR_INFO.getYearAsString() + '\n' );
            TXT_REPORT.append( capitalize( LBL_DISTANCE ) + ": "
                                + STR_TOTAL_K + '\n' );
            TXT_REPORT.append( capitalize( LBL_OPEN_WATERS ) + ": "
                                + STR_TOTAL_OW + '\n' );
            TXT_REPORT.append( "\n" );

            // Next
            ++year;
            ++yearsRetrieved;
        }

        SERIES.add( TOTAL_SERIE );
        SERIES.add( OPEN_SERIE );
    }

    private void calculateDataForMonthsStats(int year, final ArrayList<BarChart.SeriesInfo> SERIES)
    {
        final BarChart.SeriesInfo SERIE_TOTAL =
                new BarChart.SeriesInfo(
                        this.getString( R.string.label_total ), Color.BLUE );
        final BarChart.SeriesInfo SERIE_OPEN =
                new BarChart.SeriesInfo(
                        this.getString( R.string.label_open_waters), Color.CYAN );
        final int NUM_COLUMNS_IN_MONTHLY_GRAPH = 12;
        final String STR_UNITS = settings.getDistanceUnits().toString();
        final String LBL_TOTAL = this.getString( R.string.label_total );
        final String LBL_DISTANCE = this.getString( R.string.label_distance );
        final String LBL_OPEN_WATERS = this.getString( R.string.label_open_waters );
        final TextView TXT_REPORT = this.findViewById( R.id.txtReport );
        int month = 0;

        TXT_REPORT.setText( "" );
        while( month < NUM_COLUMNS_IN_MONTHLY_GRAPH ) {
            final Session[] SESSIONS = dataStore.getSessionsForMonth( year, month );
            int totalMeters = 0;
            int totalOpenWaterMeters = 0;

            for(Session session: SESSIONS) {
                if ( !session.isAtPool() ) {
                    totalOpenWaterMeters += session.getDistance();
                }

                totalMeters += session.getDistance();
            }

            final double TOTAL_K = settings.toUnits( totalMeters );
            final double TOTAL_OW = settings.toUnits( totalOpenWaterMeters );
            final String STR_TOTAL_K = settings.toUnitsAsString( totalMeters ) + STR_UNITS;
            final String STR_TOTAL_OW = settings.toUnitsAsString( totalOpenWaterMeters ) + STR_UNITS;

            // Graph
            SERIE_TOTAL.add( new BarChart.Point( month + 1, TOTAL_K ) );
            SERIE_OPEN.add( new BarChart.Point( month + 1, TOTAL_OW ) );

            // Report
            TXT_REPORT.append( capitalize( LBL_TOTAL ) + ": "
                                + year + "-" + ( month + 1) + '\n' );
            TXT_REPORT.append( capitalize( LBL_DISTANCE ) + ": "
                                + STR_TOTAL_K + '\n' );
            TXT_REPORT.append( capitalize( LBL_OPEN_WATERS ) + ": "
                                + STR_TOTAL_OW + '\n' );
            TXT_REPORT.append( "\n" );

            // Next
            ++month;
        }

        SERIES.add( SERIE_TOTAL );
        SERIES.add( SERIE_OPEN );
    }

    private void calculateDataForWeeksStats(int year, int month, final ArrayList<BarChart.SeriesInfo> SERIES)
    {
        final BarChart.SeriesInfo SERIE_TOTAL = new BarChart.SeriesInfo(
                        this.getString( R.string.label_total ), Color.BLUE );
        final BarChart.SeriesInfo SERIE_OPEN = new BarChart.SeriesInfo(
                        this.getString( R.string.label_open_waters ), Color.CYAN );
        final Calendar DATE = Calendar.getInstance();
        final TextView TXT_REPORT = this.findViewById( R.id.txtReport );
        final ArrayList<Integer> metersTotalPerWeek = new ArrayList<>( 6 );
        final ArrayList<Integer> metersOpenPerWeek = new ArrayList<>( 6 );
        final int LAST_DAY_OF_MONTH = DATE.getActualMaximum( Calendar.DAY_OF_MONTH );
        final String LBL_DISTANCE = this.getString( R.string.label_distance );
        final String LBL_OPEN_WATERS = this.getString( R.string.label_open_waters );
        final String LBL_TOTAL = this.getString( R.string.label_total );
        final String LBL_WEEK = this.getString( R.string.label_week );
        final String LBL_MONTH = this.getString( R.string.label_month );
        final String STR_UNITS = settings.getDistanceUnits().toString();
        int firstDayOfWeek = settings.getFirstDayOfWeek().getCalendarValue();
        int weekIndex = 0;

        // Prepare report
        TXT_REPORT.setText( String.format( Locale.getDefault(),
                      "%s: %d-%d (/%s)\n\n",
                             capitalize( LBL_MONTH ),
                             year, month + 1, LBL_WEEK.toLowerCase() ) );

        // Prepare initial week
        metersTotalPerWeek.add( 0 );
        metersOpenPerWeek.add( 0 );
        int totalMeters = 0;
        int owMeters = 0;

        // Run all over the dates of that month
        for(int i = 1; i <= LAST_DAY_OF_MONTH; ++i) {
            DATE.set( year, month, i );

            // Change of week ?
            if ( DATE.get( Calendar.DAY_OF_WEEK ) == firstDayOfWeek
              && i != 1 )
            {
                metersTotalPerWeek.add( 0 );
                metersOpenPerWeek.add( 0 );
                ++weekIndex;
            }

            // Retrieve the sessions for this date
            final Session[] SESSIONS = dataStore.getSessionsForDay( DATE.getTime() );

            for(Session session: SESSIONS) {
                totalMeters += session.getDistance();
                metersTotalPerWeek.set( weekIndex,
                                        metersTotalPerWeek.get( weekIndex )
                                        + session.getDistance() );

                if ( !session.isAtPool() ) {
                    owMeters += session.getDistance();
                    metersOpenPerWeek.set( weekIndex,
                                           metersOpenPerWeek.get( weekIndex )
                                         + session.getDistance() );
                }
            }
        }

        // Pass sesssions to points
        for(int i = 0; i < metersTotalPerWeek.size(); ++i) {
            final double TOTAL_PER_WEEK_K = settings.toUnits( metersTotalPerWeek.get( i ) );

            SERIE_TOTAL.add(
                    new BarChart.Point( i + 1, TOTAL_PER_WEEK_K ) );
        }

        for(int i = 0; i < metersOpenPerWeek.size(); ++i) {
            final double TOTAL_OW_PER_WEEK_K = settings.toUnits( metersOpenPerWeek.get( i ) );

            SERIE_OPEN.add(
                    new BarChart.Point( i + 1, TOTAL_OW_PER_WEEK_K ) );
        }

        // Report
        for(int i = 0; i < metersTotalPerWeek.size(); ++i) {
            final String STR_TOTAL_PER_WEEK_K = settings.toUnitsAsString( metersTotalPerWeek.get( i ) );
            final String STR_TOTAL_OW_PER_WEEK_K = settings.toUnitsAsString( metersOpenPerWeek.get( i ) );

            TXT_REPORT.append( capitalize( LBL_WEEK ) + " " + ( i + 1 ) + '\n' );
            TXT_REPORT.append( capitalize( LBL_DISTANCE ) + ": " + STR_TOTAL_PER_WEEK_K + STR_UNITS + '\n' );
            TXT_REPORT.append( capitalize( LBL_OPEN_WATERS ) + ": " + STR_TOTAL_OW_PER_WEEK_K + STR_UNITS + '\n' );
            TXT_REPORT.append( "\n" );
        }

        TXT_REPORT.append( capitalize( LBL_TOTAL ) + ": "
                           + settings.toUnitsAsString( totalMeters )
                           + STR_UNITS + "\n" );

        TXT_REPORT.append( capitalize( LBL_TOTAL )
                + " (" + LBL_OPEN_WATERS + "): "
                + settings.toUnitsAsString( owMeters )
                + STR_UNITS + "\n" );

        SERIES.add( SERIE_TOTAL );
        SERIES.add( SERIE_OPEN );
    }

    private void getSelections()
    {
        final Spinner CB_YEARS = this.findViewById( R.id.cbGraphYear );
        final Spinner CB_MONTHS = this.findViewById( R.id.cbGraphMonth );
        final Spinner CB_TIME_SEGMENT = this.findViewById( R.id.cbTimeSegment );

        this.graphType = GraphType.fromOrdinal( CB_TIME_SEGMENT.getSelectedItemPosition() );
        this.selectedMonth = CB_MONTHS.getSelectedItemPosition();
        this.selectedYear = -1;

        if ( CB_YEARS.getSelectedItemPosition() >= 0 ) {
            this.selectedYear = Integer.parseInt( (String) CB_YEARS.getSelectedItem() );
        }

        Log.d( LOG_TAG, String.format( "selected year: %d, month %d, graphtype: %d",
                this.selectedYear,
                this.selectedMonth,
                this.graphType.ordinal() ));
    }

    /** Plots the chart in a drawable and shows it. */
    @Override
    protected void update()
    {
        final double DENSITY = this.getResources().getDisplayMetrics().scaledDensity;
        final ArrayList<BarChart.SeriesInfo> SERIES = new ArrayList<>();

        final Thread LOAD_GRAPH = new Thread() {
            @Override
            public void run() {
                final StatsActivity SELF = StatsActivity.this;
                int idForLegendX = R.string.label_week;

                SELF.getSelections();

                if( SELF.graphType == GraphType.Yearly ) {
                    idForLegendX = R.string.label_year;
                }
                else
                if( SELF.graphType == GraphType.Monthly ) {
                    idForLegendX = R.string.label_month;
                }

                final String LEGEND_X = SELF.getString( idForLegendX );

                SELF.runOnUiThread( () -> {
                    switch( SELF.graphType ) {
                        case Yearly:
                            SELF.calculateDataForYearsStats( SELF.selectedYear, SERIES );
                            break;
                        case Monthly:
                            SELF.calculateDataForMonthsStats( SELF.selectedYear, SERIES );
                            break;
                        case Weekly:
                            SELF.calculateDataForWeeksStats(
                                    SELF.selectedYear, SELF.selectedMonth, SERIES );
                            break;
                        default:
                            Log.e( LOG_TAG, "unsupported graph type" );
                    }

                    final BarChart CHART = new BarChart( DENSITY, SERIES );

                    CHART.setLegendX( LEGEND_X );
                    CHART.setLegendY( settings.getDistanceUnits().toString() );
                    CHART.setShowLabels( true );
                    SELF.chartView.setScaleType( ImageView.ScaleType.MATRIX );
                    SELF.chartView.setImageDrawable( CHART );
                });
            }
        };

        LOAD_GRAPH.start();
    }

    private static String capitalize(String s)
    {
        return s.substring( 0, 1 ).toUpperCase()
                + s.substring( 1 ).toLowerCase();
    }

    private void chooseViewReport()
    {
        final LinearLayout LY_GRAPH = this.findViewById( R.id.lyGraph );
        final LinearLayout LY_REPORT = this.findViewById( R.id.lyReport );

        this.chooseView( LY_GRAPH, LY_REPORT );
    }

    private void chooseViewGraph()
    {
        final LinearLayout LY_GRAPH = this.findViewById( R.id.lyGraph );
        final LinearLayout LY_REPORT = this.findViewById( R.id.lyReport );

        this.chooseView( LY_REPORT, LY_GRAPH );
    }

    private void chooseView(final LinearLayout SHOWN, final LinearLayout HIDDEN)
    {
        // Hide the one shown and vice-versa
        SHOWN.setVisibility( View.GONE );
        HIDDEN.setVisibility( View.VISIBLE );
    }

    private int selectedYear;
    private int selectedMonth;
    private GraphType graphType;
    private ImageView chartView;

    /** Manages gestures. */
    public static class StandardGestures implements View.OnTouchListener,
            ScaleGestureDetector.OnScaleGestureListener
    {
        public StandardGestures(Context c)
        {
            this.gestureScale = new ScaleGestureDetector( c, this );
            this.position = new PointF( 0, 0);
        }

        @Override @SuppressWarnings("ClickableViewAccessibility")
        public boolean onTouch(View view, MotionEvent event)
        {
            float curX;
            float curY;

            this.view = view;
            this.gestureScale.onTouchEvent( event );

            if ( !this.gestureScale.isInProgress() ) {
                switch ( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        this.position.x = event.getX();
                        this.position.y = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        curX = event.getX();
                        curY = event.getY();
                        this.view.scrollBy( (int) ( this.position.x - curX ), (int) ( this.position.y - curY ) );
                        this.position.x = curX;
                        this.position.y = curY;
                        break;
                    case MotionEvent.ACTION_UP:
                        curX = event.getX();
                        curY = event.getY();
                        this.view.scrollBy( (int) ( this.position.x - curX ), (int) ( this.position.y - curY ) );
                        break;
                }
            }

            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            this.scaleFactor *= detector.getScaleFactor();

            // Prevent view from becoming too small
            this.scaleFactor = ( this.scaleFactor < 1 ? 1 : this.scaleFactor );

            // Change precision to help with jitter when user just rests their fingers
            this.scaleFactor = ( (float) ( (int) ( this.scaleFactor * 100 ) ) ) / 100;
            this.view.setScaleX( this.scaleFactor );
            this.view.setScaleY( this.scaleFactor) ;

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector)
        {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector)
        {
        }

        private View view;
        private float scaleFactor = 1;
        private final PointF position;
        private final ScaleGestureDetector gestureScale;
    }
}
