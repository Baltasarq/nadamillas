// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;


import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.YearInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class EditYearInfo extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        this.setContentView( R.layout.activity_edit_year_info );

        this.setButtonListeners();
    }

    private void setButtonListeners()
    {
        final ImageButton BT_BACK = this.findViewById( R.id.btCloseEditYearInfo );
        final FloatingActionButton BT_SAVE = this.findViewById( R.id.fbSaveYearInfo );

        // Buttons
        BT_BACK.setOnClickListener( (v) -> this.finish() );
        BT_SAVE.setOnClickListener( (v) -> this.store() );
    }

    @Override
    protected void update()
    {
        final EditText ED_OWS = this.findViewById( R.id.edDistanceOWS );
        final EditText ED_POOL = this.findViewById( R.id.edDistancePool );
        final EditText ED_TOTAL = this.findViewById( R.id.edDistanceTotal );

        ED_OWS.setText( "" + yearInfo.getTargetOpenWater() );
        ED_POOL.setText( "" + yearInfo.getTargetPool() );
        ED_TOTAL.setText( "" + yearInfo.getTarget() );
    }

    protected void store()
    {
        final EditText ED_OWS = this.findViewById( R.id.edDistanceOWS );
        final EditText ED_POOL = this.findViewById( R.id.edDistancePool );
        final EditText ED_TOTAL = this.findViewById( R.id.edDistanceTotal );
        int targetPool = 0;
        int targetOWS = 0;
        int total = 0;

        // Target Pool
        try {
            targetPool = Integer.parseInt( ED_POOL.getText().toString() );
        } catch(NumberFormatException exc) {
            ED_POOL.setText( "0" );
        }

        // Target OWS
        try {
            targetOWS = Integer.parseInt( ED_OWS.getText().toString() );
        } catch(NumberFormatException exc) {
            ED_OWS.setText( "0" );
        }

        // Global target
        try {
            total = Integer.parseInt( ED_TOTAL.getText().toString() );
        } catch(NumberFormatException exc) {
            ED_TOTAL.setText( "0" );
        }

        total = targetPool + targetOWS;
        yearInfo.setTarget( total );
        yearInfo.setTargetPool( targetPool );
    }

    public static YearInfo yearInfo;
}
