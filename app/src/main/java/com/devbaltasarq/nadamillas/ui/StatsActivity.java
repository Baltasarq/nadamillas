// NadaMillas (c) 2019-2023/24 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;

import java.text.DateFormatSymbols;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.PointF;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.devbaltasarq.nadamillas.core.session.Distance;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.session.Date;
import com.devbaltasarq.nadamillas.core.YearInfo;
import com.devbaltasarq.nadamillas.core.storage.YearInfoStorage;
import com.devbaltasarq.nadamillas.ui.graph.BarChart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class StatsActivity extends BaseActivity {
    private final static int NUM_YEARS_IN_GRAPH = 10;
    private final static int MAX_WEEKS = 6;
    private final static int MAX_DAYS_PER_WEEK = 7;
    private final static String LOG_TAG = StatsActivity.class.getSimpleName();
    private enum GraphTimeType { Daily, Weekly, Monthly, Yearly;
        /** @return the corresponding enum value, given its position. */
        public static GraphTimeType fromOrdinal(int pos)
        {
            return GraphTimeType.values()[ pos ];
        }

        /** @return the corresponding string for each graph type. */
        public String toString(Context ctx)
        {
            return ctx.getResources()
                    .getStringArray( R.array.array_graph_time_types)
                                                        [ this.ordinal() ];
        }
    }
    private enum GraphModeType { Distance, Temperature;
        /**
         * @return the corresponding enum value, given its position.
         */
        public static GraphModeType fromOrdinal(int pos) {
            return GraphModeType.values()[ pos ];
        }
    }

    @Override @SuppressWarnings("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        this.setContentView( R.layout.activity_stats );

        final Toolbar TOOL_BAR = this.findViewById( R.id.toolbar );
        this.setSupportActionBar( TOOL_BAR );

        final Spinner CB_YEARS = this.findViewById( R.id.cbGraphYear );
        final Spinner CB_MONTHS = this.findViewById( R.id.cbGraphMonth );
        final Spinner CB_TIME_SEGMENT = this.findViewById( R.id.cbTimeSegment );
        final Spinner CB_GRAPH_MODE = this.findViewById( R.id.cbGraphMode );
        final ImageButton BT_SCRSHOT = this.findViewById( R.id.btTakeScrshotForStats );
        final ImageButton BT_BACK = this.findViewById( R.id.btCloseStats );
        final ImageButton BT_SHOW_GRAPH = this.findViewById( R.id.btShowGraph );
        final ImageButton BT_SHOW_REPORT = this.findViewById( R.id.btShowReport );

        // Prepares time segment spinner
        final var MODE_SEGMENT_TYPES_ADAPTER = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                this.getResources().getStringArray( R.array.array_graph_mode_types ) );

        CB_GRAPH_MODE.setAdapter( MODE_SEGMENT_TYPES_ADAPTER );
        CB_GRAPH_MODE.setSelection( GraphModeType.Distance.ordinal() );

        // Prepares time time segment spinner
        final var TIME_SEGMENT_TYPES_ADAPTER = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                this.getResources().getStringArray( R.array.array_graph_time_types ) );

        CB_TIME_SEGMENT.setAdapter( TIME_SEGMENT_TYPES_ADAPTER );
        CB_TIME_SEGMENT.setSelection( GraphTimeType.Weekly.ordinal() ); // Week by default

        // Prepare the months spinner
        final var MONTHS_ADAPTER = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new DateFormatSymbols().getMonths() );

        CB_MONTHS.setAdapter( MONTHS_ADAPTER );
        CB_MONTHS.setSelection( Calendar.getInstance().get( Calendar.MONTH ), false );

        // Prepare years spinner
        final var CB_YEARS_ADAPTER = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                this.retrieveExistingYearsAsString()
        );

        CB_YEARS.setAdapter( CB_YEARS_ADAPTER );

        // Chart image viewer
        final var GESTURES = new StandardGestures( this );
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

        CB_GRAPH_MODE.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
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

        BT_SCRSHOT.setOnClickListener( v -> {
                final StatsActivity SELF = StatsActivity.this;

                SELF.shareScreenShot( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
        });

        BT_BACK.setOnClickListener( v ->StatsActivity.this.finish() );
    }

    private String[] retrieveExistingYearsAsString()
    {
        final int CURRENT_YEAR = new Date().getYear();
        var toret = new String[ 0 ];

        try (Cursor cursor = dataStore.getDescendingAllYearInfosCursor())
        {
            final ArrayList<String> YEARS = new ArrayList<>( cursor.getCount() );

            while( cursor.moveToNext() ) {
                final int YEAR = cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            YearInfoStorage.FIELD_YEAR ) );

                if ( YEAR <= CURRENT_YEAR ) {
                    YEARS.add( String.valueOf( YEAR ) );
                }
            }

            toret = YEARS.toArray( new String[ 0 ] );
        } catch(SQLException exc) {
            Log.e( LOG_TAG, "retrieveAllYearsInfo(): " + exc.getMessage() );
        }

        return toret;
    }

    /** calculates the stats for years.
      * @param year the year for reference.
      * @param TXT_REPORT a StringBuffer to be filled with textual data.
      * @param SERIES a list of SeriesInfo objects for the chart.
      */
    private void calculateDataForYearsDistanceStats(
                            int year,
                            /* out */ final StringBuffer TXT_REPORT,
                            /* out */ final List<BarChart.SeriesInfo> SERIES)
    {
        final List<YearInfo> YEAR_DATA = this.retrieveDataForYears( year );
        final BarChart.SeriesInfo TOTAL_SERIE =
                new BarChart.SeriesInfo(
                        this.getString( R.string.label_total ),
                        BarChart.Colors.Navy.get() );
        final BarChart.SeriesInfo OPEN_SERIE =
                new BarChart.SeriesInfo(
                        this.getString( R.string.label_open_waters ),
                        BarChart.Colors.DarkTurquoise.get() );
        final Distance.Units UNITS = settings.getDistanceUnits();
        final String LBL_TOTAL = this.getString( R.string.label_total );
        final String LBL_DISTANCE = this.getString( R.string.label_distance );
        final String LBL_OPEN_WATERS = this.getString( R.string.label_open_waters );

        for(final YearInfo YEAR_INFO: YEAR_DATA) {
            final int DISPLAY_YEAR = YEAR_INFO.getYear() % 1000;
            final int DIST_TOTAL =
                    YEAR_INFO.getDistance( YearInfo.SwimKind.TOTAL );
            final Distance TOTAL_K = new Distance( DIST_TOTAL, UNITS );

            if ( TOTAL_K.getValue() > 0 ) {
                final int DIST_OW = DIST_TOTAL
                        - YEAR_INFO.getDistance( YearInfo.SwimKind.POOL );
                final Distance DIST_TOTAL_OW = new Distance( DIST_OW, UNITS );

                TOTAL_SERIE.add(
                        new BarChart.Point(
                                DISPLAY_YEAR,
                                TOTAL_K.toThousandUnits() ) );
                OPEN_SERIE.add(
                        new BarChart.Point(
                                DISPLAY_YEAR,
                                DIST_TOTAL_OW.toThousandUnits() ) );

                // Report
                TXT_REPORT.append( String.format( Locale.getDefault(),
                                        "%s: %4d\n",
                                        capitalize( LBL_TOTAL ),
                                        YEAR_INFO.getYear() ) );

                TXT_REPORT.append( String.format( Locale.getDefault(),
                                        "\t%s: %s\n",
                                        capitalize( LBL_DISTANCE ),
                                        TOTAL_K ) );

                TXT_REPORT.append( String.format( Locale.getDefault(),
                                        "\t%s: %s\n",
                                        capitalize( LBL_OPEN_WATERS ) + ": ",
                                        DIST_TOTAL_OW ) );

                TXT_REPORT.append( "\n" );
            }
        }

        SERIES.add( TOTAL_SERIE );
        SERIES.add( OPEN_SERIE );
    }

    /** Retrieves data for years.
      * @param year the year for reference, -NUM_YEARS... year... +NUN_YEARS
      */
    private List<YearInfo> retrieveDataForYears(int year)
    {
        final int CURRENT_YEAR = Calendar.getInstance().get( Calendar.YEAR );
        final var TORET = new ArrayList<YearInfo>( NUM_YEARS_IN_GRAPH );
        int yearsRetrieved = 0;

        // Adjust years info
        if ( year != CURRENT_YEAR ) {
            year -= NUM_YEARS_IN_GRAPH / 2;
        } else {
            year -= NUM_YEARS_IN_GRAPH - 1;
        }

        // Retrieve years info
        while( yearsRetrieved < NUM_YEARS_IN_GRAPH ) {
            TORET.add( dataStore.getOrCreateInfoFor( year ) );

            // Next
            ++year;
            ++yearsRetrieved;
        }

        return TORET;
    }

    /** Calculates the distance data for months.
      * @param year the year that contains the months to calculate for.
      * @param TXT_REPORT a StringBuffer to be filled with the textual report.
      * @param SERIES the graph series, to be filled.
      */
    private void calculateDataForMonthsDistanceStats(
            int year,
            /* out */ final StringBuffer TXT_REPORT,
            /* out */ final List<BarChart.SeriesInfo> SERIES)
    {
        final BarChart.SeriesInfo SERIE_TOTAL =
                new BarChart.SeriesInfo(
                        this.getString( R.string.label_total ),
                        BarChart.Colors.Navy.get() );
        final BarChart.SeriesInfo SERIE_OPEN =
                new BarChart.SeriesInfo(
                        this.getString( R.string.label_open_waters),
                        BarChart.Colors.DarkTurquoise.get() );
        final Distance.Units UNITS = settings.getDistanceUnits();
        final String LBL_TOTAL = this.getString( R.string.label_total );
        final String LBL_DISTANCE = this.getString( R.string.label_distance );
        final String LBL_OPEN_WATERS = this.getString( R.string.label_open_waters );
        final int[] MONTHS = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
        final Map<Integer, List<Session>> SESSIONS_PER_MONTH =
                                    this.retrieveDataForMonthsIn( year );
        final int MAX_DAYS = 31;

        for(int month: MONTHS) {
            final List<Session> SESSIONS = new ArrayList<>( MAX_DAYS );
            final List<Session> SESSIONS_THIS_MONTH = SESSIONS_PER_MONTH.get( month );
            int totalMeters = 0;
            int totalOpenWaterMeters = 0;

            // Normalize access to sessions when there are none
            if ( SESSIONS_THIS_MONTH != null ) {
                SESSIONS.addAll( SESSIONS_THIS_MONTH );
            }

            // Add up distance data
            for(final Session SESSION: SESSIONS) {
                if ( !SESSION.isAtPool() ) {
                    totalOpenWaterMeters += SESSION.getDistance();
                }

                totalMeters += SESSION.getDistance();
            }

            // Calculate the stats for this month
            final Distance TOTAL_K = new Distance( totalMeters, UNITS );
            final Distance TOTAL_OW = new Distance( totalOpenWaterMeters, UNITS );

            // Graph
            SERIE_TOTAL.add( new BarChart.Point( month + 1, TOTAL_K.toThousandUnits() ));
            SERIE_OPEN.add( new BarChart.Point( month + 1, TOTAL_OW.toThousandUnits() ));

            // Report
            TXT_REPORT.append( String.format( Locale.getDefault(),
                                        "%s: %d - %2d\n",
                                        capitalize( LBL_TOTAL ),
                                        year,
                                        month + 1 ) );
            TXT_REPORT.append( String.format( Locale.getDefault(),
                                        "\t%s: %s\n",
                                        capitalize( LBL_DISTANCE ),
                                        TOTAL_K ) );
            TXT_REPORT.append( String.format( Locale.getDefault(),
                                        "\t%s: %s\n\n",
                                        capitalize( LBL_OPEN_WATERS ),
                                        TOTAL_OW ) );
        }

        SERIES.add( SERIE_TOTAL );
        SERIES.add( SERIE_OPEN );
    }

    /** Calculates the temperature data for months.
     * @param year the year that contains the months to calculate for.
     * @param TXT_REPORT a StringBuffer to be filled with the textual report.
     * @param SERIES the graph series, to be filled.
     */
    private void calculateDataForMonthsTemperatureStats(
            int year,
            /* out */ final StringBuffer TXT_REPORT,
            /* out */ final List<BarChart.SeriesInfo> SERIES)
    {
        final String LBL_POOL = this.getString( R.string.label_pool );
        final String LBL_OPEN_WATERS = this.getString( R.string.label_open_waters );
        final String LBL_TEMPERATURE = this.getString( R.string.label_temperature );
        final BarChart.SeriesInfo SERIE_POOL =
                new BarChart.SeriesInfo(
                        LBL_POOL,
                        BarChart.Colors.Navy.get() );
        final BarChart.SeriesInfo SERIE_OPEN =
                new BarChart.SeriesInfo(
                        LBL_OPEN_WATERS,
                        BarChart.Colors.DarkTurquoise.get() );
        final int[] MONTHS = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
        final Map<Integer, List<Session>> SESSIONS_PER_MONTH =
                this.retrieveDataForMonthsIn( year );
        final List<Double> OWS_TEMPERATURES_PER_MONTH = new ArrayList<>( MONTHS.length );
        final List<Double> POOL_TEMPERATURES_PER_MONTH = new ArrayList<>( MONTHS.length );
        double poolAvgTemperatureBefore = 0.0;
        double owsAvgTemperatureBefore = 0.0;

        for(int month: MONTHS) {
            final List<Session> SESSIONS_THIS_MONTH = SESSIONS_PER_MONTH.get( month );
            double poolAvgTemperature = 0.0;
            double owsAvgTemperature = 0.0;
            int numPoolTemperatures = 0;
            int numOWSTemperatures = 0;

            // Add up distance data
            for(final Session SESSION: SESSIONS_THIS_MONTH) {
                if ( !SESSION.isAtPool() ) {
                    owsAvgTemperature += SESSION.getTemperature();
                    numOWSTemperatures += 1;
                } else {
                    poolAvgTemperature += SESSION.getTemperature();
                    numPoolTemperatures += 1;
                }
            }

            if ( numOWSTemperatures < 1 ) {
                owsAvgTemperature = owsAvgTemperatureBefore;
                numOWSTemperatures = 1;
            }

            if ( numPoolTemperatures < 1 ) {
                poolAvgTemperature = poolAvgTemperatureBefore;
                numPoolTemperatures = 1;
            }

            poolAvgTemperature /= numPoolTemperatures;
            owsAvgTemperature /= numOWSTemperatures;

            OWS_TEMPERATURES_PER_MONTH.add( owsAvgTemperature );
            POOL_TEMPERATURES_PER_MONTH.add( poolAvgTemperature );
            poolAvgTemperatureBefore = poolAvgTemperature;
            owsAvgTemperatureBefore = owsAvgTemperature;

            // Graph
            SERIE_POOL.add(
                        new BarChart.Point( month + 1,
                                        POOL_TEMPERATURES_PER_MONTH.get( month ) ));
            SERIE_OPEN.add(
                        new BarChart.Point( month + 1,
                                        OWS_TEMPERATURES_PER_MONTH.get( month ) ));

            // Report
            TXT_REPORT.append( String.format( Locale.getDefault(),
                    "%s: %d - %2d\n",
                    capitalize( LBL_TEMPERATURE ),
                    year,
                    month + 1 ) );
            TXT_REPORT.append( String.format( Locale.getDefault(),
                    "\t%s: %sº\n",
                    capitalize( LBL_POOL ),
                    POOL_TEMPERATURES_PER_MONTH.get( month ) ) );
            TXT_REPORT.append( String.format( Locale.getDefault(),
                    "\t%s: %sº\n\n",
                    capitalize( LBL_OPEN_WATERS ),
                    OWS_TEMPERATURES_PER_MONTH.get( month ) ) );
        }

        SERIES.add( SERIE_POOL );
        SERIES.add( SERIE_OPEN );
    }

    /** Retrieves the data (sessions) for the months in a given year.
      * @param year the year to retrieve the sessions from.
      * @return a Map between the index for each month and its list of sessions.
      */
    private Map<Integer, List<Session>> retrieveDataForMonthsIn(int year)
    {
        final int NUM_COLUMNS_IN_MONTHLY_GRAPH = 12;
        final var TORET = new HashMap<Integer, List<Session>>( NUM_COLUMNS_IN_MONTHLY_GRAPH );

        for(int month = 0; month < NUM_COLUMNS_IN_MONTHLY_GRAPH; ++month) {
            TORET.put(
                    month,
                    Arrays.asList(
                            dataStore.getSessionsForMonth( year, month ) ) );
        }

        return TORET;
    }

    /** Calculates the distance stats data for each week.
      * @param year the year the month to deal with is in.
      * @param month the month the weeks to deal with are in.
      * @param TXT_REPORT the StringBuffer to be filled in.
      * @param SERIES the series to be filled up with the data for the graph.
      */
    private void calculateDataForWeeksDistanceStats(
                        int year,
                        int month,
                        /* out */ final StringBuffer TXT_REPORT,
                        /* out */ final ArrayList<BarChart.SeriesInfo> SERIES)
    {
        final BarChart.SeriesInfo SERIE_TOTAL = new BarChart.SeriesInfo(
                        this.getString( R.string.label_total ),
                        BarChart.Colors.Navy.get() );
        final BarChart.SeriesInfo SERIE_OPEN = new BarChart.SeriesInfo(
                        this.getString( R.string.label_open_waters ),
                        BarChart.Colors.DarkTurquoise.get() );
        final Distance.Units UNITS = settings.getDistanceUnits();
        final List<Integer> metersTotalPerWeek = new ArrayList<>( MAX_WEEKS );
        final List<Integer> metersOpenPerWeek = new ArrayList<>( MAX_WEEKS );
        final String LBL_DISTANCE = this.getString( R.string.label_distance );
        final String LBL_OPEN_WATERS = this.getString( R.string.label_open_waters );
        final String LBL_TOTAL = this.getString( R.string.label_total );
        final String LBL_WEEK = this.getString( R.string.label_week );
        final String LBL_MONTH = this.getString( R.string.label_month );
        final Map<Integer, List<Session>> SESSIONS_PER_WEEK =
                                    this.retrieveDataForWeeksIn( year, month );
        final List<Integer> WEEKS =
                        SESSIONS_PER_WEEK.keySet()
                                            .stream().sorted()
                                            .collect( Collectors.toList() );

        // Prepare report
        TXT_REPORT.append( String.format( Locale.getDefault(),
                "%s: %4d-%2d (/%s)\n\n",
                capitalize( LBL_MONTH ),
                year, month + 1, LBL_WEEK.toLowerCase() ) );

        // Prepare week stats
        int totalMeters = 0;
        int owMeters = 0;

        // Prepare data retrieved
        for(int weekIndex: WEEKS) {
            final List<Session> SESSIONS = SESSIONS_PER_WEEK.get( weekIndex );

            metersTotalPerWeek.add( 0 );
            metersOpenPerWeek.add( 0 );

            for(Session session: SESSIONS) {
                totalMeters += session.getDistance();
                metersTotalPerWeek.set( weekIndex - 1,
                        metersTotalPerWeek.get( weekIndex - 1 )
                                + session.getDistance() );

                if ( !session.isAtPool() ) {
                    owMeters += session.getDistance();
                    metersOpenPerWeek.set( weekIndex - 1,
                            metersOpenPerWeek.get( weekIndex - 1 )
                                    + session.getDistance() );
                }
            }
        }

        // Prepare points for both series
        for(int i = 0; i < metersTotalPerWeek.size(); ++i) {
            final double TOTAL_PER_WEEK_K = new Distance(
                                                    metersTotalPerWeek.get( i ),
                                                    UNITS )
                                            .toThousandUnits();

            SERIE_TOTAL.add(
                    new BarChart.Point( i + 1, TOTAL_PER_WEEK_K ) );
        }

        for(int i = 0; i < metersOpenPerWeek.size(); ++i) {
            final double TOTAL_OW_PER_WEEK_K = new Distance(
                                                    metersOpenPerWeek.get( i ),
                                                    UNITS )
                                            .toThousandUnits();

            SERIE_OPEN.add(
                    new BarChart.Point( i + 1, TOTAL_OW_PER_WEEK_K ) );
        }

        // Report
        for(int i = 0; i < metersTotalPerWeek.size(); ++i) {
            final String STR_TOTAL_PER_WEEK_K = Distance.Fmt.format(
                                                    metersTotalPerWeek.get( i ),
                                                    UNITS );
            final String STR_TOTAL_OW_PER_WEEK_K = Distance.Fmt.format(
                                                    metersOpenPerWeek.get( i ),
                                                    UNITS );

            TXT_REPORT.append( String.format( Locale.getDefault(),
                                    "%s %d\n",
                                    capitalize( LBL_WEEK ),
                                    i + 1 ) );
            TXT_REPORT.append( String.format( Locale.getDefault(),
                                    "\t%s: %s\n",
                                    capitalize( LBL_DISTANCE ),
                                    STR_TOTAL_PER_WEEK_K ) );
            TXT_REPORT.append( String.format( Locale.getDefault(),
                                    "\t%s: %s\n\n",
                                    capitalize( LBL_OPEN_WATERS ),
                                    STR_TOTAL_OW_PER_WEEK_K ) );
        }

        TXT_REPORT.append( String.format( Locale.getDefault(),
                                "%s: %s\n",
                                capitalize( LBL_TOTAL ),
                                Distance.Fmt.format( totalMeters, UNITS ) ) );

        TXT_REPORT.append( String.format( Locale.getDefault(),
                                "%s (%s): %s\n",
                                capitalize( LBL_TOTAL ),
                                LBL_OPEN_WATERS,
                                Distance.Fmt.format( owMeters, UNITS ) ) );

        SERIES.add( SERIE_TOTAL );
        SERIES.add( SERIE_OPEN );
    }

    /** Calculates the temperature stats data for each week.
     * @param year the year the month to deal with is in.
     * @param month the month the weeks to deal with are in.
     * @param TXT_REPORT the StringBuffer to be filled in.
     * @param SERIES the series to be filled up with the data for the graph.
     */
    private void calculateDataForWeeksTemperatureStats(
            int year,
            int month,
            /* out */ final StringBuffer TXT_REPORT,
            /* out */ final ArrayList<BarChart.SeriesInfo> SERIES)
    {
        final BarChart.SeriesInfo SERIE_POOL = new BarChart.SeriesInfo(
                this.getString( R.string.label_pool ),
                BarChart.Colors.Navy.get() );
        final BarChart.SeriesInfo SERIE_OPEN = new BarChart.SeriesInfo(
                this.getString( R.string.label_open_waters ),
                BarChart.Colors.DarkTurquoise.get() );
        final List<Double> temperaturePoolPerWeek = new ArrayList<>( MAX_WEEKS );
        final List<Double> temperatureOWSPerWeek = new ArrayList<>( MAX_WEEKS );
        final String LBL_TEMPERATURE = this.getString( R.string.label_temperature );
        final String LBL_OPEN_WATERS = this.getString( R.string.label_open_waters );
        final String LBL_POOL = this.getString( R.string.label_pool );
        final String LBL_WEEK = this.getString( R.string.label_week );
        final String LBL_MONTH = this.getString( R.string.label_month );
        final Map<Integer, List<Session>> SESSIONS_PER_WEEK =
                this.retrieveDataForWeeksIn( year, month );
        final List<Integer> WEEKS =
                SESSIONS_PER_WEEK.keySet()
                        .stream().sorted()
                        .collect( Collectors.toList() );

        // Prepare report
        TXT_REPORT.append( String.format( Locale.getDefault(),
                "%s: %4d-%2d (/%s)\n\n",
                capitalize( LBL_MONTH ),
                year, month + 1, LBL_WEEK.toLowerCase() ) );

        double poolAvgTemperatureBefore = 0.0;
        double owsAvgTemperatureBefore = 0.0;

        // Prepare data retrieved
        for(int weekIndex: WEEKS) {
            final List<Session> SESSIONS =
                                    Objects.requireNonNull(
                                            SESSIONS_PER_WEEK.get( weekIndex ) );

            double poolAvgTemperature = 0.0;
            double owsAvgTemperature = 0.0;
            int numPoolTemperatures = 0;
            int numOWSTemperatures = 0;

            for(Session session: SESSIONS) {
                if ( !session.isAtPool() ) {
                    owsAvgTemperature += session.getTemperature();
                    numOWSTemperatures += 1;
                } else {
                    poolAvgTemperature += session.getTemperature();
                    numPoolTemperatures += 1;
                }
            }

            if ( numPoolTemperatures < 1 ) {
                numPoolTemperatures = 1;
                poolAvgTemperature = poolAvgTemperatureBefore;
            }

            if ( numOWSTemperatures < 1 ) {
                numOWSTemperatures = 1;
                owsAvgTemperature = owsAvgTemperatureBefore;
            }

            temperaturePoolPerWeek.add( poolAvgTemperature / numPoolTemperatures );
            temperatureOWSPerWeek.add( owsAvgTemperature / numOWSTemperatures );
            owsAvgTemperatureBefore = owsAvgTemperature;
            poolAvgTemperatureBefore = poolAvgTemperature;
        }

        // Prepare points for both series
        for(int i = 0; i < temperaturePoolPerWeek.size(); ++i) {
            SERIE_POOL.add(
                    new BarChart.Point( i + 1,
                                    temperaturePoolPerWeek.get( i ) ) );
            SERIE_OPEN.add(
                    new BarChart.Point( i + 1,
                                    temperatureOWSPerWeek.get( i ) ) );
        }

        // Report
        for(int i = 0; i < temperaturePoolPerWeek.size(); ++i) {
            TXT_REPORT.append( String.format( Locale.getDefault(),
                    "%s %d\n",
                    capitalize( LBL_WEEK ),
                    i + 1 ) );
            TXT_REPORT.append( String.format( Locale.getDefault(),
                    "\t%s (%s): %sº\n",
                    capitalize( LBL_TEMPERATURE ),
                    LBL_POOL,
                    temperaturePoolPerWeek.get( i ) ) );
            TXT_REPORT.append( String.format( Locale.getDefault(),
                    "\t%s (%s): %sº\n\n",
                    capitalize( LBL_TEMPERATURE ),
                    LBL_OPEN_WATERS,
                    temperatureOWSPerWeek.get( i ) ) );
        }

        SERIES.add( SERIE_POOL );
        SERIES.add( SERIE_OPEN );
    }

    /** Retrieves the data relative for a given month, for each week.
      * @param year the year of the month to retrieve the data for each week.
      * @param month the month to retrieve the data for each week.
      * @return a Map between the week number [1.. 5] and a List of Sessions.
      */
    private Map<Integer, List<Session>> retrieveDataForWeeksIn(int year, int month)
    {
        final Date DATE = Date.from( year, month, 1 );
        final int LAST_DAY_OF_MONTH = DATE.getLastDayOfMonth();
        final var TORET = new HashMap<Integer, List<Session>>( MAX_WEEKS );
        final int FIRST_DAY_OF_WEEK = settings.getFirstDayOfWeek().getCalendarValue();
        final Consumer<Integer> CHANGE_WEEK = (wi) ->
                        TORET.put( wi, new ArrayList<>( MAX_DAYS_PER_WEEK ) );
        int weekIndex = 1;

        // Prepare the first week
        CHANGE_WEEK.accept( weekIndex );
        var currentWeekSessionList = TORET.get( weekIndex );

        // Run all over the dates of that month
        for(int i = 1; i <= LAST_DAY_OF_MONTH; ++i) {
            Date date = Date.from( year, month, i );

            // Change of week?
            if ( i != 1
              && date.getWeekDay() == FIRST_DAY_OF_WEEK )
            {
                ++weekIndex;
                CHANGE_WEEK.accept( weekIndex );
                currentWeekSessionList = TORET.get( weekIndex );
            }

            // Retrieve the sessions for this date
            final var SESSIONS = Arrays.asList( dataStore.getSessionsForDay( date ) );
            currentWeekSessionList.addAll( SESSIONS );
        }

        return TORET;
    }

    /** Calculates the distance data stats for each day of the month.
      * @param year the year the days to deal with are in.
      * @param month the month the days to deal with are in.
      * @param TXT_REPORT a StringBuffer to be filled in with the report.
      * @param SERIES the series to be filled in for the graph.
      */
    private void calculateDataForDaysDistanceStats(
                        int year,
                        int month,
                        /* out */ final StringBuffer TXT_REPORT,
                        /* out */ final ArrayList<BarChart.SeriesInfo> SERIES)
    {
        final BarChart.SeriesInfo SERIE_TOTAL = new BarChart.SeriesInfo(
                this.getString( R.string.label_total ),
                BarChart.Colors.Navy.get() );
        final BarChart.SeriesInfo SERIE_OPEN = new BarChart.SeriesInfo(
                this.getString( R.string.label_open_waters ),
                BarChart.Colors.DarkTurquoise.get() );
        final Distance.Units UNITS = settings.getDistanceUnits();
        final String LBL_DISTANCE = this.getString( R.string.label_distance );
        final String LBL_OPEN_WATERS = this.getString( R.string.label_open_waters );
        final String LBL_TOTAL = capitalize( this.getString( R.string.label_total ) );
        final String LBL_MONTH = this.getString( R.string.label_month );
        final Map<Integer, List<Session>> SESSIOMS_PER_DAY =
                                            this.retrieveDataForDaysIn( year, month );
        final List<Integer> DAYS = SESSIOMS_PER_DAY.keySet()
                                            .stream().sorted()
                                            .collect( Collectors.toList() );

        // Prepare report
        TXT_REPORT.append( String.format( Locale.getDefault(),
                "%s: %d-%d\n\n",
                capitalize( LBL_MONTH ),
                year, month + 1 ) );

        // Prepare series
        int totalDist = 0;
        int totalOWDist = 0;

        for(int i: DAYS) {
            final var SESSIONS = SESSIOMS_PER_DAY.get( i );
            int dayTotalMeters = 0;
            int dayTotalOWS = 0;

            for(Session session: SESSIONS) {
                dayTotalMeters += session.getDistance();

                if ( !session.isAtPool() ) {
                    dayTotalOWS += session.getDistance();
                }
            }

            if ( dayTotalMeters > 0 ) {
                TXT_REPORT.append( String.format( Locale.getDefault(),
                        "%s\n\t%s: %s\n\t%s: %s\n\n",
                        Date.from( year, month, i ).toShortDateString(),
                        LBL_DISTANCE,
                        Distance.Fmt.format( dayTotalMeters, UNITS ),
                        capitalize( LBL_OPEN_WATERS ),
                        Distance.Fmt.format( dayTotalOWS, UNITS ) ) );
            }

            final double DAY_TOTAL_K =
                    new Distance( dayTotalMeters, UNITS ).toThousandUnits();
            final double DAY_OWS_K =
                    new Distance( dayTotalOWS, UNITS ).toThousandUnits();

            totalDist += dayTotalMeters;
            totalOWDist += dayTotalOWS;
            SERIE_TOTAL.add( new BarChart.Point( i, DAY_TOTAL_K ) );
            SERIE_OPEN.add( new BarChart.Point( i, DAY_OWS_K ) );
        }

        // Report summary
        TXT_REPORT.append( String.format( Locale.getDefault(),
                                    "%s: %s\n",
                                    LBL_TOTAL,
                                    Distance.Fmt.format( totalDist, UNITS ) ) );

        TXT_REPORT.append( String.format( Locale.getDefault(),
                                "%s (%s): %s\n",
                                LBL_TOTAL,
                                LBL_OPEN_WATERS,
                                Distance.Fmt.format( totalOWDist, UNITS ) ));

        // Finish series
        SERIES.add( SERIE_TOTAL );
        SERIES.add( SERIE_OPEN );
    }

    private void calculateDataForDaysTemperatureStats(
            int year,
            int month,
            /* out */ final StringBuffer TXT_REPORT,
            /* out */ final ArrayList<BarChart.SeriesInfo> SERIES)
    {
        final BarChart.SeriesInfo SERIE_POOL = new BarChart.SeriesInfo(
                this.getString( R.string.label_pool ),
                BarChart.Colors.Navy.get() );
        final BarChart.SeriesInfo SERIE_OPEN = new BarChart.SeriesInfo(
                this.getString( R.string.label_open_waters ),
                BarChart.Colors.DarkTurquoise.get() );
        final String LBL_TEMPERATURE = this.getString( R.string.label_temperature );
        final String LBL_OPEN_WATERS = this.getString( R.string.label_open_waters );
        final String LBL_POOL = this.getString( R.string.label_pool );
        final String LBL_MONTH = this.getString( R.string.label_month );
        final Map<Integer, List<Session>> SESSIOMS_PER_DAY =
                this.retrieveDataForDaysIn( year, month );
        final List<Integer> DAYS = SESSIOMS_PER_DAY.keySet()
                .stream().sorted()
                .collect( Collectors.toList() );

        // Prepare report
        TXT_REPORT.append( String.format( Locale.getDefault(),
                "%s: %d-%d\n\n",
                capitalize( LBL_MONTH ),
                year, month + 1 ) );

        double dayBeforeAvgPool = 0;
        double dayBeforeAvgOWS = 0;

        for(int i: DAYS) {
            final var SESSIONS = SESSIOMS_PER_DAY.get( i );
            int numPoolInSession = 0;
            int numOWSInSession = 0;
            double dayAvgPool = 0;
            double dayAvgOWS = 0;

            for(Session session: SESSIONS) {
                if ( !session.isAtPool() ) {
                    dayAvgOWS += session.getTemperature();
                    ++numOWSInSession;
                    dayAvgPool = dayBeforeAvgPool;
                } else {
                    dayAvgPool = session.getTemperature();
                    ++numPoolInSession;
                    dayAvgOWS = dayBeforeAvgOWS;
                }
            }

            if ( numPoolInSession > 0 ) {
                dayAvgPool /= numPoolInSession;
                TXT_REPORT.append( String.format( Locale.getDefault(),
                        "%s %s\n\t%s: %5.2fº\n",
                        Date.from( year, month, i ).toShortDateString(),
                        LBL_TEMPERATURE,
                        LBL_POOL,
                        dayAvgPool ) );
            }
            else
            if ( numOWSInSession > 0 ) {
                dayAvgOWS /= numOWSInSession;
                TXT_REPORT.append( String.format( Locale.getDefault(),
                        "%s %s\n\t%s: %5.2fº\n",
                        Date.from( year, month, i ).toShortDateString(),
                        LBL_TEMPERATURE,
                        LBL_OPEN_WATERS,
                        dayAvgOWS ) );
            }

            if ( ( numPoolInSession + numOWSInSession ) > 0 ) {
                TXT_REPORT.append( '\n' );
            }

            SERIE_POOL.add( new BarChart.Point( i, dayAvgPool ) );
            SERIE_OPEN.add( new BarChart.Point( i, dayAvgOWS ) );
            dayBeforeAvgPool = dayAvgPool;
            dayBeforeAvgOWS = dayAvgOWS;
        }

        // Finish series
        SERIES.add( SERIE_POOL );
        SERIES.add( SERIE_OPEN );
    }

    /** Retrieve the data for the days in a given month.
      * @param year the year the days to deal with are in.
      * @param month the month the days to deal with are in.
      * @return a map between day and a list of sessions.
      */
    private Map<Integer, List<Session>> retrieveDataForDaysIn(int year, int month)
    {
        // Prepare date
        final Date FIRST_DAY_DATE = Date.from( year, month, 1 );
        final int LAST_DAY_OF_MONTH = FIRST_DAY_DATE.getLastDayOfMonth();
        final var TORET = new HashMap<Integer, List<Session>>();

        for(int i = 1; i <= LAST_DAY_OF_MONTH; ++i) {
            final var DAY_SESSIONS = new ArrayList<Session>();
            final Session[] SESSIONS = dataStore.getSessionsForDay(
                                                Date.from( year, month, i ) );

            DAY_SESSIONS.addAll( Arrays.asList( SESSIONS ) );
            TORET.put( i, DAY_SESSIONS );
        }

        return TORET;
    }

    /** Sets the configuration for the graph from the selections. */
    private void setUISelections()
    {
        final Spinner CB_YEARS = this.findViewById( R.id.cbGraphYear );
        final Spinner CB_MONTHS = this.findViewById( R.id.cbGraphMonth );
        final Spinner CB_TIME_SEGMENT = this.findViewById( R.id.cbTimeSegment );
        final Spinner CB_MODE_TYPE = this.findViewById( R.id.cbGraphMode );

        this.graphTime = GraphTimeType.fromOrdinal( CB_TIME_SEGMENT.getSelectedItemPosition() );
        this.selectedMonth = CB_MONTHS.getSelectedItemPosition();
        this.selectedYear = -1;
        this.graphMode = GraphModeType.fromOrdinal( CB_MODE_TYPE.getSelectedItemPosition() );

        if ( CB_YEARS.getSelectedItemPosition() >= 0 ) {
            this.selectedYear = Integer.parseInt( (String) CB_YEARS.getSelectedItem() );
        }

        Log.d( LOG_TAG, String.format( "selected year: %d, month %d, graphtime: %d, graphtype: %d",
                this.selectedYear,
                this.selectedMonth,
                this.graphTime.ordinal(),
                this.graphMode.ordinal()));
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
                final var TXT_REPORT = new StringBuffer();
                final var TV_REPORT = (TextView) SELF.findViewById( R.id.txtReport );

                SELF.setUISelections();

                SELF.runOnUiThread( () -> {
                    switch( SELF.graphTime) {
                        case Yearly ->
                            SELF.calculateDataForYearsDistanceStats(
                                    SELF.selectedYear,
                                    TXT_REPORT,
                                    SERIES );
                        case Monthly -> {
                                if ( SELF.graphMode == GraphModeType.Distance ) {
                                    SELF.calculateDataForMonthsDistanceStats(
                                            SELF.selectedYear,
                                            TXT_REPORT,
                                            SERIES );
                                } else {
                                    SELF.calculateDataForMonthsTemperatureStats(
                                            SELF.selectedYear,
                                            TXT_REPORT,
                                            SERIES );
                                }
                        }
                        case Weekly -> {
                            if ( SELF.graphMode == GraphModeType.Distance ) {
                                SELF.calculateDataForWeeksDistanceStats(
                                        SELF.selectedYear,
                                        SELF.selectedMonth,
                                        TXT_REPORT,
                                        SERIES );
                            } else {
                                SELF.calculateDataForWeeksTemperatureStats(
                                        SELF.selectedYear,
                                        SELF.selectedMonth,
                                        TXT_REPORT,
                                        SERIES );
                            }
                        }
                        case Daily -> {
                            if ( SELF.graphMode == GraphModeType.Distance ) {
                                SELF.calculateDataForDaysDistanceStats(
                                        SELF.selectedYear,
                                        SELF.selectedMonth,
                                        TXT_REPORT,
                                        SERIES );
                            } else {
                                SELF.calculateDataForDaysTemperatureStats(
                                        SELF.selectedYear,
                                        SELF.selectedMonth,
                                        TXT_REPORT,
                                        SERIES );
                            }
                        }
                        default -> {
                            Log.e( LOG_TAG, "unsupported graph type" );
                            System.exit( -1 );
                        }
                    }

                    final BarChart CHART = new BarChart( DENSITY, SERIES );
                    String legendY = settings.getDistanceUnits().toString();

                    CHART.setLegendX( SELF.graphTime.toString( SELF ) );

                    if ( SELF.graphMode == GraphModeType.Temperature ) {
                        legendY = SELF.getString( R.string.label_temperature );
                    }

                    CHART.setLegendY( legendY );
                    CHART.setShowLabels( true );

                    TV_REPORT.setText( TXT_REPORT.toString() );
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
    private GraphTimeType graphTime;
    private GraphModeType graphMode;
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
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN -> {
                        this.position.x = event.getX();
                        this.position.y = event.getY();
                    }
                    case MotionEvent.ACTION_MOVE -> {
                        curX = event.getX();
                        curY = event.getY();
                        this.view.scrollBy((int) (this.position.x - curX), (int) (this.position.y - curY));
                        this.position.x = curX;
                        this.position.y = curY;
                    }
                    case MotionEvent.ACTION_UP -> {
                        curX = event.getX();
                        curY = event.getY();
                        this.view.scrollBy((int) (this.position.x - curX), (int) (this.position.y - curY));
                    }
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
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector)
        {
            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector)
        {
        }

        private View view;
        private float scaleFactor = 1;
        private final PointF position;
        private final ScaleGestureDetector gestureScale;
    }
}
