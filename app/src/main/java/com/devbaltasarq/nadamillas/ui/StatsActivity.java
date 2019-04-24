// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.ui;

import java.text.DateFormatSymbols;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.YearInfo;
import com.devbaltasarq.nadamillas.core.storage.YearInfoStorage;
import com.devbaltasarq.nadamillas.ui.graph.BarChart;

import java.util.ArrayList;
import java.util.Calendar;

public class StatsActivity extends BaseActivity {
    private int NUM_COLUMNS_IN_GRAPH = 10;
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
        final Spinner CB_MONTHS = this.findViewById( R.id.cbGrapMonth );
        final Spinner CB_TIME_SEGMENT = this.findViewById( R.id.cbTimeSegment );
        final ImageButton BT_SHARE = this.findViewById( R.id.btShareStats);
        final ImageButton BT_SCRSHOT = this.findViewById( R.id.btTakeScrshotForStats );
        final ImageButton BT_BACK = this.findViewById( R.id.btCloseStats );

        // Prepares time segment spinner
        this.segmentAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        this.getString( R.string.label_week ),
                        this.getString( R.string.label_month ),
                        this.getString( R.string.label_year )
        });

        CB_TIME_SEGMENT.setAdapter( this.segmentAdapter );

        // Prepare the months spinner
        this.monthsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new DateFormatSymbols().getMonths() );

        CB_MONTHS.setAdapter( this.monthsAdapter );
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
                StatsActivity.this.plotChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CB_MONTHS.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StatsActivity.this.plotChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CB_TIME_SEGMENT.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StatsActivity.this.plotChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        BT_SHARE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final StatsActivity SELF = StatsActivity.this;

                SELF.share( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
            }
        });

        BT_SCRSHOT.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final StatsActivity SELF = StatsActivity.this;

                SELF.save( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
            }
        });

        BT_BACK.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatsActivity.this.finish();
            }
        });
    }

    private String[] retrieveAllYearInfos()
    {
        Cursor cursor = null;
        String[] toret = null;

        try {
            cursor = dataStore.getDescendingAllYearInfosCursor();

            toret = new String[ cursor.getCount() ];

            int i = 0;
            while( cursor.moveToNext() ) {
                final int YEAR = cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            YearInfoStorage.FIELD_YEAR ) );

                toret[ i ] = String.valueOf( YEAR );
                ++i;
            }
        } catch(SQLException exc) {
            Log.e( LOG_TAG, exc.getMessage() );
        } finally {
            DataStore.close( cursor );
        }

        return toret;
    }

    private void loadDataForYearsGraph(int year, final ArrayList<BarChart.SeriesInfo> SERIES)
    {
        final BarChart.SeriesInfo TOTAL_SERIE =
                            new BarChart.SeriesInfo(
                                this.getString( R.string.label_total ), Color.BLUE );
        final BarChart.SeriesInfo OPEN_SERIE =
                new BarChart.SeriesInfo(
                        this.getString( R.string.label_open_waters ), Color.CYAN );
        final Calendar DATE = Calendar.getInstance();
        final int CURRENT_YEAR = DATE.get( Calendar.YEAR );
        int yearsRetrieved = 0;

        // Adjust years info
        if ( year != CURRENT_YEAR ) {
            year -= NUM_COLUMNS_IN_GRAPH / 2;
        } else {
            year -= NUM_COLUMNS_IN_GRAPH - 1;
        }

        while( yearsRetrieved < NUM_COLUMNS_IN_GRAPH ) {
            final int DISPLAY_YEAR = year % 1000;
            YearInfo yinfo = dataStore.getInfoFor( year );

            if ( yinfo == null ) {
                yinfo = new YearInfo( year, 0, 0 );
            }

            final int OPEN_METERS = yinfo.getTotal() - yinfo.getTotalPool();


            TOTAL_SERIE.add( new BarChart.Point( DISPLAY_YEAR, yinfo.getTotal() / 1000 ) );
            OPEN_SERIE.add( new BarChart.Point( DISPLAY_YEAR, OPEN_METERS / 1000 ) );
            ++year;
            ++yearsRetrieved;
        }

        SERIES.add( TOTAL_SERIE );
        SERIES.add( OPEN_SERIE );
    }

    private void loadDataForMonthsGraph(int year, final ArrayList<BarChart.SeriesInfo> SERIES)
    {
        final BarChart.SeriesInfo SERIE_TOTAL =
                new BarChart.SeriesInfo(
                        this.getString( R.string.label_total ), Color.BLUE );
        final BarChart.SeriesInfo SERIE_OPEN =
                new BarChart.SeriesInfo(
                        this.getString( R.string.label_open_waters), Color.CYAN );
        final int NUM_COLUMNS_IN_MONTHLY_GRAPH = 12;
        int month = 0;

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

            SERIE_TOTAL.add( new BarChart.Point( month + 1, totalMeters / 1000 ) );
            SERIE_OPEN.add( new BarChart.Point( month + 1, totalOpenWaterMeters / 1000 ) );
            ++month;
        }

        SERIES.add( SERIE_TOTAL );
        SERIES.add( SERIE_OPEN );
    }

    private void loadDataForWeeksGraph(int year, int month, final ArrayList<BarChart.SeriesInfo> SERIES)
    {
        final BarChart.SeriesInfo SERIE_TOTAL = new BarChart.SeriesInfo(
                        this.getString( R.string.label_total ), Color.BLUE );
        final BarChart.SeriesInfo SERIE_OPEN = new BarChart.SeriesInfo(
                        this.getString( R.string.label_open_waters ), Color.CYAN );
        final Calendar DATE = Calendar.getInstance();
        final ArrayList<Integer> metersTotalPerWeek = new ArrayList<>( 6 );
        final ArrayList<Integer> metersOpenPerWeek = new ArrayList<>( 6 );
        final int LAST_DAY_OF_MONTH = DATE.getActualMaximum( Calendar.DAY_OF_MONTH );
        int firstDayOfWeek = settings.getFirstDayOfWeek().getCalendarValue();
        int weekIndex = 0;

        // Prepare initial week
        metersTotalPerWeek.add( 0 );
        metersOpenPerWeek.add( 0 );

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
                metersTotalPerWeek.set( weekIndex,
                                        metersTotalPerWeek.get( weekIndex )
                                        + session.getDistance() );

                if ( !session.isAtPool() ) {
                    metersOpenPerWeek.set( weekIndex,
                                           metersOpenPerWeek.get( weekIndex )
                                         + session.getDistance() );
                }
            }
        }

        // Pass sesssions to points
        for(int i = 0; i < metersTotalPerWeek.size(); ++i) {
            SERIE_TOTAL.add(
                    new BarChart.Point( i + 1, metersTotalPerWeek.get( i ) / 1000 ) );
        }

        for(int i = 0; i < metersOpenPerWeek.size(); ++i) {
            SERIE_OPEN.add(
                    new BarChart.Point( i + 1, metersOpenPerWeek.get( i ) / 1000 ) );
        }

        SERIES.add( SERIE_TOTAL );
        SERIES.add( SERIE_OPEN );
    }

    private void getSelections()
    {
        final Spinner CB_YEARS = this.findViewById( R.id.cbGraphYear );
        final Spinner CB_MONTHS = this.findViewById( R.id.cbGrapMonth );
        final Spinner CB_TIME_SEGMENT = this.findViewById( R.id.cbTimeSegment );

        this.graphType = GraphType.fromOrdinal( CB_TIME_SEGMENT.getSelectedItemPosition() );
        this.selectedMonth = CB_MONTHS.getSelectedItemPosition();
        this.selectedYear = Integer.valueOf( (String) CB_YEARS.getSelectedItem() );

        Log.d( LOG_TAG, String.format( "selected year: %d, month %d, graphtype: %d",
                                        this.selectedYear,
                                        this.selectedMonth,
                                        this.graphType.ordinal() ));
    }

    /** Plots the chart in a drawable and shows it. */
    private void plotChart()
    {
        final double DENSITY = this.getResources().getDisplayMetrics().scaledDensity;
        final ArrayList<BarChart.SeriesInfo> SERIES = new ArrayList<>();

        final Thread LOAD_GRAPH = new Thread() {
            @Override
            public void run() {
                final StatsActivity SELF = StatsActivity.this;
                int idForLegendX = R.string.label_year;

                SELF.getSelections();

                switch( SELF.graphType ) {
                    case Yearly:
                        SELF.loadDataForYearsGraph( SELF.selectedYear, SERIES );
                        break;
                    case Monthly:
                        SELF.loadDataForMonthsGraph( SELF.selectedYear, SERIES );
                        idForLegendX = R.string.label_month;
                        break;
                    case Weekly:
                        SELF.loadDataForWeeksGraph(
                                SELF.selectedYear, SELF.selectedMonth, SERIES );
                        idForLegendX = R.string.label_week;
                        break;
                    default:
                        Log.e( LOG_TAG, "unsupported graph type" );
                }

                final String LEGEND_X = SELF.getString( idForLegendX );

                SELF.runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        final BarChart CHART = new BarChart( DENSITY, SERIES );

                        CHART.setLegendX( LEGEND_X );
                        CHART.setLegendY( SELF.getString( R.string.label_meter ) );
                        CHART.setShowLabels( true );
                        SELF.chartView.setScaleType( ImageView.ScaleType.MATRIX );
                        SELF.chartView.setImageDrawable( CHART );
                    }
                });
            }
        };

        LOAD_GRAPH.start();
    }

    private int selectedYear;
    private int selectedMonth;
    private GraphType graphType;
    private ImageView chartView;
    private ArrayAdapter<String> segmentAdapter;
    private ArrayAdapter<String> monthsAdapter;

    /** Manages gestures. */
    public class StandardGestures implements View.OnTouchListener,
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

        private PointF position;
        private View view;
        private ScaleGestureDetector gestureScale;
        private float scaleFactor = 1;
    }
}
