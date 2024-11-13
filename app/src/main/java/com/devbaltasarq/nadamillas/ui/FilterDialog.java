// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;


import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.storage.FilterSessions;

import java.util.List;


public class FilterDialog extends AlertDialog {
    public FilterDialog(Activity act)
    {
        super( act, true, null );
    }

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate( savedInstance );

        this.setTitle( R.string.label_filter );
        this.setContentView( R.layout.dlg_prefs );

        final Spinner SP_FIELD = this.findViewById( R.id.spField );
        final Spinner SP_OPERATOR = this.findViewById( R.id.spOperator );
        final Button BT_OK = this.findViewById( R.id.btOk );
        final Button BT_CANCEL = this.findViewById( R.id.btCancel );

        final ArrayAdapter<String> ADAP_FIELD =
                new ArrayAdapter<>(
                        this.getContext(),
                        android.R.layout.simple_spinner_item,
                        FIELDS );
        ADAP_FIELD.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

        final ArrayAdapter<String> ADAP_OPERATOR =
                new ArrayAdapter<>(
                        this.getContext(),
                        android.R.layout.simple_spinner_item,
                        OPERATORS );
        ADAP_OPERATOR.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

        SP_FIELD.setAdapter( ADAP_FIELD );
        SP_OPERATOR.setAdapter( ADAP_OPERATOR );

        BT_OK.setOnClickListener( (v) -> {} );
        BT_CANCEL.setOnClickListener( (v) -> { this.dismiss(); this.hide(); } );
        this.setCancelable( true );
    }

    private static final List<String> FIELDS = FilterSessions.Field.STR_FIELDS;
    private static final List<String> OPERATORS = FilterSessions.Operator.STR_OPERATORS;
}
