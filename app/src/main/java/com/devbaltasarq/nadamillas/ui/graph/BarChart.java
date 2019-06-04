package com.devbaltasarq.nadamillas.ui.graph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;


/** A graph created from lines. */
public class BarChart extends Drawable {
    private static String LOG_TAG = BarChart.class.getSimpleName();
    private static double SCALED_DENSITY = 1.0;
    public static int[] COLORS = new int[] {
            0xff0000ff,                                 // blue
            0xffff0000,                                 // red
            0xff8b008b,                                 // magenta
            0xff00ced1,                                 // dark turquoise
            0xffffff00,                                 // yellow
            0xffffa500,                                 // orange
            0xff9acd32,                                 // yellow-green
            0xff87ceeb,                                 // sky blue
            0xff00ff00,                                 // green
            0xff808080,                                 // gray
    };

    /** Represents a single point in the graph. */
    public static class Point {
        public Point(double x, double y)
        {
            this.x = x;
            this.y = y;
        }

        /** @return the x coordinate. */
        public double getX()
        {
            return this.x;
        }

        /** @return the y coordinate. */
        public double getY()
        {
            return this.y;
        }

        @Override
        public String toString()
        {
            return String.format (Locale.getDefault(),
                    "(%06.2f, %06.2f)",
                    this.getX(), this.getY() );
        }

        private double x;
        private double y;
    }

    /** Represents the correspondence between color and tag. */
    public static class SeriesInfo {
        /** Builds an association between color and tag,
         * for explanatory purposes in the chart.
         * @param tag the tag associated with a color
         * @param color the color this tag is associated to, as an int,
         *              format 0xaarrggbb, a: alpha, r: red, g: green, b: blue,
         *              from 00 to ff.
         */
        public SeriesInfo(String tag, int color)
        {
            this.tag = tag;
            this.color = color;
            this.points = new ArrayList<>();
        }

        /** @return the tag associated with a given color. */
        public String getTag()
        {
            return this.tag;
        }

        /** @return the color for this tag. */
        public int getColor()
        {
            return this.color;
        }

        /** Insert a new point. */
        public void add(Point p)
        {
            this.points.add( p );
        }

        /** @return the number of points. */
        public int count()
        {
            return this.points.size();
        }

        private ArrayList<Point> points;
        private int color;
        private String tag;
    }

    /** Constructs a new graph. */
    public BarChart(double scaledDensity, Collection<SeriesInfo> seriesInfo)
    {
        // Check series being the same size
        int numPoints = 0;

        for(SeriesInfo serie: seriesInfo) {
            final int SERIE_NUM_POINTS = serie.count();

            if ( numPoints == 0 ) {
                numPoints = SERIE_NUM_POINTS;
            }

            if ( numPoints != serie.count() ) {
                final String ERROR_MSG = String.format(
                                        "%d series are uneven: calculated %d vs found %d",
                                        seriesInfo.size(),
                                        numPoints,
                                        SERIE_NUM_POINTS );

                Log.e( LOG_TAG, ERROR_MSG );
                throw new ExceptionInInitializerError( ERROR_MSG  );
            }
        }

        // Set up graphics
        SCALED_DENSITY = scaledDensity;
        this.drawGrid = true;
        this.paint = new Paint();
        this.paint.setStrokeWidth( 2 );

        // Store data as plain arrays
        this.points = new Point[ seriesInfo.size() ][ numPoints ];

        int i = 0;
        for(SeriesInfo serie: seriesInfo) {
            System.arraycopy(
                    serie.points.toArray( new Point[ 0 ] ), 0,
                    this.points[ i ], 0,
                    serie.points.size() );

            ++i;
        }
        this.series = seriesInfo.toArray( new SeriesInfo[ 0 ] );

        // Preparation for data normalization
        this.calculateDataMinMax();

        this.showLabels = true;
        this.labelThreshold = 1.0;
    }

    /** Calculates the minimum and maximum values in the data sets.
     * This is stored in the minX, minY, maxX and maxY
     * @see Point
     */
    private void calculateDataMinMax()
    {
        this.minY = 0;
        this.minX = Double.MAX_VALUE;
        this.maxX = this.maxY = Double.MIN_VALUE;

        for(Point[] seriePoints: this.points) {
            for(Point point : seriePoints) {
                final double X = point.getX();
                final double Y = point.getY();

                this.minX = Math.min( this.minX, X );
                this.maxX = Math.max( this.maxX, X );
                this.maxY = Math.max( this.maxY, Y );
            }
        }

        return;
    }

    @Override
    public void setAlpha(int x)
    {
    }

    @Override
    public int getOpacity()
    {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter)
    {
    }

    @Override
    protected boolean onLevelChange(int level)
    {
        super.onLevelChange( level );

        this.invalidateSelf();
        return true;
    }

    /** Sets the legend for the x axis. */
    public void setLegendX(String legendX)
    {
        this.legendX = legendX;
    }

    /** Sets the legend for the y axis. */
    public void setLegendY(String legendY)
    {
        this.legendY = legendY;
    }

    /** @return true if the grid will be drawn, false otherwise. */
    public boolean shouldDrawGrid()
    {
        return this.drawGrid;
    }

    /** @return true if the labels for each point should be shown, false otherwise. */
    public boolean shouldShowLabels()
    {
        return this.showLabels;
    }

    /** Sets the label threshold. */
    public void setLabelThreshold(double threshold)
    {
        this.labelThreshold = threshold;
    }

    /** Shows the labels on the chart or not. */
    public void setShowLabels(boolean showLabels)
    {
        this.showLabels = showLabels;
    }

    /** Changes whether the grid should be drawn or not.
     * @param drawGrid true to draw the grid, false otherwise.
     */
    public void setDrawGrid(boolean drawGrid)
    {
        this.drawGrid = drawGrid;
    }

    @Override
    public void draw(@NonNull Canvas canvas)
    {
        final int LEGEND_SPACE = 50;
        final int CHART_PADDING = 60;
        final int LEGEND_PADDING = 20;
        final float TEXT_SIZE_SP = 10;
        final float TEXT_SIZE = TEXT_SIZE_SP * (float) SCALED_DENSITY;

        // Set up
        this.canvas = canvas;
        this.chartBounds = new Rect( 0,  0, this.canvas.getWidth(), this.canvas.getHeight() );
        this.legendBounds = new Rect( 0, 0, this.canvas.getWidth(), this.canvas.getHeight() );
        this.paint.setTypeface( Typeface.create( "serif", Typeface.NORMAL ) );
        this.paint.setTextSize( TEXT_SIZE );
        this.paint.setAntiAlias( true );

        // Adjust chart bounds
        this.chartBounds.top += CHART_PADDING;
        this.chartBounds.right = ( (int) ( this.getBounds().width() * .75 ) ) - CHART_PADDING;
        this.chartBounds.bottom -= CHART_PADDING + LEGEND_SPACE;
        this.chartBounds.left += CHART_PADDING + LEGEND_SPACE;

        // Adjust legend bounds
        this.legendBounds.top += CHART_PADDING;
        this.legendBounds.right -= LEGEND_PADDING;
        this.legendBounds.bottom -= LEGEND_PADDING;
        this.legendBounds.left = ( (int) ( this.getBounds().width() * .75 ) ) + LEGEND_PADDING;

        // Draw the graph's axis
        this.paint.setStrokeWidth( 6 );
        this.drawAxis();
        this.drawGrid();

        // Draw the data
        this.paint.setStrokeWidth( 4 );
        this.drawData();

        // Draw the legend box
        this.drawLegendBox();
    }

    /** Draws a new line in the canvas.
     * Remember to set the canvas attribute (in BarChart::draw) before using this method.
     * @param x the initial x coordinate.
     * @param y the initial y coordinate.
     * @param x2 the final x coordinate.
     * @param y2 the final y coordinate
     * @param color the color to draw the line with.
     * @see BarChart ::draw
     */
    private void line(int x, int y, int x2, int y2, int color)
    {
        this.paint.setColor( color );
        canvas.drawLine( x, y, x2, y2, this.paint );
    }

    /** Draws a new line in the canvas.
     * Remember to set the canvas attribute (in BarChart::draw) before using this method.
     * @param x the initial x coordinate.
     * @param y the initial y coordinate.
     * @param x2 the final x coordinate.
     * @param y2 the final y coordinate
     * @param color the color to draw the line with.
     * @see BarChart ::draw
     */
    private void rectangle(int x, int y, int x2, int y2, int color)
    {
        this.paint.setColor( color );
        this.paint.setStyle( Paint.Style.FILL );
        canvas.drawRect( x, y, x2, y2, this.paint );
    }

    /** Draws a new bar in the graph.
      * @param height the height of the bar.
      * @param color the color of the bar.
      */
    private void bar(int x, int y, int width, int height, int color)
    {
        this.rectangle( x, y, x + width, y + height, color );
    }

    /** Writes a real value as text.
     * Remember to set the canvas attribute (in BarChart::draw) before using this method.
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     * @param value the value to show.
     */
    private void write(double x, double y, double value)
    {
        if ( value != Math.floor( value ) ) {
            value = Math.round( value );
        }

        this.write( x, y, String.format( Locale.getDefault(), "%2d", (int) value ) );
    }

    /** Writes a real value as text.
     * Remember to set the canvas attribute (in BarChart::draw) before using this method.
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     * @param msg the string to show.
     */
    private void write(double x, double y, String msg)
    {
        final float BEFORE_STROKE_WIDTH = this.paint.getStrokeWidth();
        final int BEFORE_COLOR = this.paint.getColor();

        this.paint.setStrokeWidth( 1 );
        this.paint.setColor( Color.BLACK );
        this.canvas.drawText( msg, (float) x, (float) y, this.paint );

        this.paint.setColor( BEFORE_COLOR );
        this.paint.setStrokeWidth( BEFORE_STROKE_WIDTH );
    }

    /** Draws the grid.
     * @see BarChart ::shouldDrawGrid
     */
    private void drawGrid()
    {
        if ( this.shouldDrawGrid() ) {
            final int COLOR = Color.DKGRAY;
            final int CHART_RIGHT = this.chartBounds.right;
            final int CHART_LEFT = this.chartBounds.left;
            final int CHART_TOP = this.chartBounds.top;
            final int CHART_BOTTOM = this.chartBounds.bottom;
            final int NUM_SLOTS_X = this.getNumBars();
            final int NUM_SLOTS_Y = 10;

            this.paint.setStrokeWidth( 1 );

            // Complete rectangle
            this.line( CHART_LEFT, CHART_TOP, CHART_RIGHT, CHART_TOP, COLOR );
            this.line( CHART_RIGHT, CHART_TOP, CHART_RIGHT, CHART_BOTTOM, COLOR );

            for(int i = 0; i < NUM_SLOTS_X; ++i) {
                final double DATA_X = this.points[ 0 ][ i ].getX();
                final int X = this.translateX( DATA_X );

                this.write( X + this.getBarWidth() / 2, CHART_BOTTOM  + 35, DATA_X );
                this.line( X, CHART_BOTTOM, X, CHART_TOP, COLOR );
            }

            // Intermediate horizontal lines (marking the y segments)
            final double SLOT_DATA_Y = ( this.maxY - this.minY ) / NUM_SLOTS_Y;

            for(int i = 1; i <= NUM_SLOTS_Y; ++i) {
                final double DATA_Y = this.minY + ( i * SLOT_DATA_Y );
                final int Y = this.translateY( DATA_Y );

                this.write( CHART_LEFT - 60, Y, DATA_Y );
                this.line( CHART_LEFT, Y, CHART_RIGHT, Y, COLOR );
            }
        }

        return;
    }

    /** @return the normalized value for x, right for drawing in the screen. */
    private int translateX(double x)
    {
        final double X = Math.max( x, this.minX ) - this.minX;
        final int BAR_WIDTH = this.getBarWidth();

        return (int) Math.round( this.chartBounds.left + ( BAR_WIDTH * X ) );
    }

    /** @return the normalized value for y, right for drawing in the screen. */
    private int translateY(double y)
    {
        final double Y = Math.max( y, this.minY ) - this.minY;
        final int NORM_Y = (int) ( ( Y * this.chartBounds.height() ) / ( this.maxY - this.minY ) );

        return this.chartBounds.bottom - NORM_Y;
    }

    /** Draws the axis of the graph */
    private void drawAxis()
    {
        final int LEFT = this.chartBounds.left;
        final int BOTTOM = this.chartBounds.bottom;
        final int COLOR = Color.BLACK;

        // Horizontal axis
        this.line( LEFT, BOTTOM, this.chartBounds.right, BOTTOM, COLOR );

        // Vertical axis
        this.line( LEFT, this.chartBounds.top, LEFT, BOTTOM, COLOR );

        // Vertical legend
        float textWidthY = this.paint.measureText( this.legendY );
        int centeredLegendY = ( this.chartBounds.height() / 2 ) - ( (int) ( textWidthY / 2 ) );
        int posLegendYX = LEFT - 70;
        int posLegendYY = BOTTOM - centeredLegendY;
        this.canvas.save();
        this.canvas.rotate( -90, posLegendYX, posLegendYY );
        this.write( posLegendYX, posLegendYY, this.legendY );
        this.canvas.restore();

        // Horizontal legend
        float textWidthX = this.paint.measureText( this.legendX );
        int posLegendX = ( this.chartBounds.width() / 2 ) - ( (int) ( textWidthX / 2 ) );
        this.write( LEFT + posLegendX, BOTTOM + 60, this.legendX );
    }

    /** Draws the data in the chart. */
    private void drawData()
    {
        final int COLUMN_WIDTH = ( this.chartBounds.right - this.chartBounds.left ) / ( this.getNumBars() + 1 );
        final int COLUMN_MARGIN = COLUMN_WIDTH / 4;
        final int BAR_WIDTH = (int) Math.round( COLUMN_WIDTH / 1.25 );
        final int BASE_Y = this.translateY( 0 );

        for(int i = 0; i < this.series.length; ++i) {
            final Point[] POINTS = this.points[ i ];

            for(Point point : POINTS) {
                final double DATA_Y = point.getY();
                final int X = this.translateX( point.getX() );
                final int Y = this.translateY( DATA_Y );

                this.bar( X + COLUMN_MARGIN + ( 10 * i ), Y,
                          BAR_WIDTH, BASE_Y - Y,
                          series[ i ].getColor() );

                // Show label only if it's not zero
                if ( this.shouldShowLabels()
                  && DATA_Y != 0 )
                {
                    this.write( X + 10, Y - 10, DATA_Y );
                }
            }
        }

        return;
    }

    /** Draws the box with all legends. */
    private void drawLegendBox()
    {
        final float BEFORE_STROKE_WIDTH = this.paint.getStrokeWidth();
        final double LETTER_WIDTH = this.paint.measureText( "W" );
        final int MAX_LENGTH = (int) ( this.legendBounds.width() / LETTER_WIDTH );

        int y = this.legendBounds.top;

        for(SeriesInfo colorTag: this.series) {
            String tag = colorTag.getTag();
            final double TEXT_WIDTH = this.paint.measureText( tag );


            if ( TEXT_WIDTH > this.legendBounds.width() ) {
                final int LENGTH = Math.max( 0, Math.min( MAX_LENGTH, tag.length() ) );
                tag = tag.substring( 0, LENGTH ) + "...";
            }

            this.paint.setColor( colorTag.getColor() );
            this.paint.setStyle( Paint.Style.FILL );
            this.canvas.drawRect(
                    this.legendBounds.left,
                    y - 20,
                    this.legendBounds.left + 20,
                    y,
                    this.paint );

            this.paint.setStyle( Paint.Style.STROKE );
            this.paint.setColor( Color.BLACK );
            this.write( this.legendBounds.left + 50, y, tag );

            y += 50;
        }

        this.paint.setStrokeWidth( BEFORE_STROKE_WIDTH );
    }

    public int getNumBars()
    {
        return this.points[ 0 ].length;
    }

    public int getBarWidth()
    {
        return this.chartBounds.width() / this.getNumBars();
    }

    private String legendX;
    private String legendY;
    private boolean drawGrid;
    private Rect chartBounds;
    private Rect legendBounds;
    private Paint paint;
    private Canvas canvas;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private Point[][] points;
    private SeriesInfo[] series;
    private boolean showLabels;
    private double labelThreshold;
}
