// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;


/** A class to display an arc showing a hint of progress */
public class ProgressView extends View {
    private static final int Padding = 35;
    private static final int MinSize = 100;

    public ProgressView(Context context)
    {
        super( context );
        this.init();
    }

    public ProgressView(Context ctx, @Nullable AttributeSet atrSet)
    {
        super( ctx, atrSet );
        this.init();
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super( context, attrs, defStyleAttr );
        this.init();
    }

    private void init()
    {
        this.paint = new Paint( Paint.ANTI_ALIAS_FLAG
                                | Paint.LINEAR_TEXT_FLAG );
        this.paint.setColor( Color.GREEN );
        this.paint.setFakeBoldText( true );
        this.paint.setTypeface( Typeface.DEFAULT_BOLD );

        this.textBounds = new Rect();

        this.setMinimumWidth( MinSize );
        this.setMinimumHeight( MinSize );
        this.color = Color.BLACK;
    }

    /** @return the current progress, a number between 0-100. */
    public int getProgress()
    {
        return progress;
    }

    /** Changes the progress.
      * @param progress the new progress, a number between 0-100.
      */
    public void setProgress(int progress)
    {
        this.progress = progress;
        this.invalidate();
    }

    /** @return the color of the arc denoting the progress.
      * @see android.graphics.Color
      */
    public int getColor()
    {
        return this.color;
    }

    /** Change the color for the arc denoting the process.
      * @param color an int denoting the color.
      * @see android.graphics.Color
      */
    public void setColor(int color)
    {
        this.color = color;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw( canvas );

        this.paint.setColor( this.color );
        this.paint.setStyle( Paint.Style.STROKE );
        this.paint.setStrokeWidth( 30f );

        canvas.drawArc(
                Padding,
                Padding,
                this.getWidth() - Padding,
                this.getHeight() - Padding,
                0.0f,
                (float) ( 3.6 * this.getProgress() ),
                false,
                this.paint
        );

        final String TEXT = this.getProgress() + "%";
        this.paint.setColor( Color.BLACK );
        this.paint.setStyle( Paint.Style.FILL_AND_STROKE );
        this.paint.setStrokeWidth( 1f );
        this.paint.setTextSize( (float) this.getHeight() / 4f );
        this.paint.getTextBounds( TEXT, 0, TEXT.length(), this.textBounds );

        canvas.drawText( TEXT,
                ( (float) this.getWidth() - this.textBounds.width() ) / 2f,
                Padding + ( (float) this.getHeight() - this.textBounds.height() ) / 1.75f,
                   this.paint );
    }

    private int progress;
    private Paint paint;
    private Rect textBounds;
    private int color;
}
