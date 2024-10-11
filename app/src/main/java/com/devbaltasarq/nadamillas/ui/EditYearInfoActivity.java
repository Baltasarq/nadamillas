// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.Distance;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.core.YearInfo;
import com.devbaltasarq.nadamillas.core.Speed;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class EditYearInfoActivity extends BaseActivity {
    private final String LOG_TAG = EditYearInfoActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        this.setContentView( R.layout.activity_edit_year_info );

        final TextView LBL_TITLE = this.findViewById( R.id.lblTitle );
        final TextView LBL_YEAR = this.findViewById( R.id.lblYearToEdit );
        final TextView LBL_YEAR_INFO = this.findViewById( R.id.lblYearInfoToEdit );
        final Distance.Units UNITS = settings.getDistanceUnits();
        final String STR_YEAR = "" + yearInfo.getYear();
        final String TITLE = this.getString( R.string.action_modify )
                + " "
                + this.getString( R.string.label_target );

        LBL_TITLE.setText( TITLE );
        LBL_YEAR.setText( STR_YEAR );
        LBL_YEAR_INFO.setText( String.format( Locale.getDefault(),
                                        "%s: %s %s.",
                                        this.getString( R.string.label_total_distance ),
                                        Distance.format(
                                            yearInfo.getDistance( YearInfo.SwimKind.TOTAL ),
                                            UNITS ),
                                        UNITS.toString() ));

        this.setButtonListeners();
        this.update();
    }

    private void setButtonListeners()
    {
        final ImageButton BT_BACK = this.findViewById( R.id.btCloseEditYearInfo );
        final FloatingActionButton BT_SAVE = this.findViewById( R.id.fbSaveYearInfo );
        final List<ImageButton> BT_EDITS = this.lookForViews( new int[] {
                R.id.btEditTargetPool, R.id.btEditTargetTotal
        });

        // Insert a blank imposting BT_EDIT_TARGET_OWS
        BT_EDITS.add( 1, null );

        final ImageButton BT_EDIT_TARGET_POOL = BT_EDITS.get( YearInfo.SwimKind.POOL.ordinal() );
        final ImageButton BT_EDIT_TARGET_TOTAL = BT_EDITS.get( YearInfo.SwimKind.TOTAL.ordinal() );

        BT_EDIT_TARGET_POOL.setOnClickListener( v -> this.onEdit( YearInfo.SwimKind.POOL ) );
        BT_EDIT_TARGET_TOTAL.setOnClickListener( v -> this.onEdit( YearInfo.SwimKind.TOTAL ) );
        BT_BACK.setOnClickListener( v -> this.finish() );
        BT_SAVE.setOnClickListener( v -> this.store() );
    }

    /** When an edit button is clicked...
      * @param TARGET the target distance to edit: OWS or Total.
      * @see YearInfo.SwimKind
      */
    private void onEdit(final YearInfo.SwimKind TARGET)
    {
        final AlertDialog.Builder DLG = new AlertDialog.Builder( this );
        final Distance.Units UNITS = settings.getDistanceUnits();
        int titleHandler = R.string.label_pool;
        int distance = (int) new Distance(
                                yearInfo.getTarget( YearInfo.SwimKind.POOL ),
                                UNITS ).toThousandUnits();

        // Deduce title
        if ( TARGET == YearInfo.SwimKind.OWS ) {
            titleHandler = R.string.label_open_waters;
            distance = (int) new Distance(
                                yearInfo.getTarget( YearInfo.SwimKind.OWS ),
                                UNITS ).toThousandUnits();
        }
        else
        if ( TARGET == YearInfo.SwimKind.TOTAL ) {
            titleHandler = R.string.label_total;
            distance = (int) new Distance(
                                yearInfo.getTarget( YearInfo.SwimKind.TOTAL ),
                                UNITS ).toThousandUnits();
        }

        // Prepare input
        final EditText ED_DISTANCE = new EditText( this );
        final String STR_DISTANCE = "" + distance;

        ED_DISTANCE.setText( STR_DISTANCE );
        ED_DISTANCE.setInputType( InputType.TYPE_CLASS_NUMBER );
        ED_DISTANCE.setTextAlignment( View.TEXT_ALIGNMENT_TEXT_END );

        // Show
        DLG.setTitle( titleHandler );
        DLG.setView( ED_DISTANCE );
        DLG.setPositiveButton(
                R.string.action_modify,
                (intf, which) -> this.updateYearInfoFromDialog(
                                            TARGET,
                                            ED_DISTANCE.getText().toString() ) );
        DLG.setNegativeButton( R.string.label_cancel, null );
        DLG.create().show();
    }

    /** Ask for the new target distance: total or OWS. */
    private void updateYearInfoFromDialog(final YearInfo.SwimKind TARGET, String strNewDistance)
    {
        final Distance.Units UNITS = settings.getDistanceUnits();

        // Get the new target distance
        int distance = 0;

        try {
            distance = Integer.parseInt( strNewDistance );
        } catch(NumberFormatException exc) {
            this.showStatus(
                    LOG_TAG,
                    this.getString( R.string.message_no_number_error )
                        + strNewDistance );
        }

        // Store the result
        yearInfo.setTarget(
                        TARGET,
                        Distance.fromThousandUnits( distance, UNITS ).getValue() );
        this.update();
    }

    /** Passes the data in the yearInfo object to the widgets. */
    @Override
    protected void update()
    {
        final Distance.Units UNITS = settings.getDistanceUnits();
        final TextView LBL_OWS = this.findViewById( R.id.lblTargetOWS );
        final TextView LBL_POOL = this.findViewById( R.id.lblTargetPool );
        final TextView LBL_TOTAL = this.findViewById( R.id.lblTargetTotal );
        final String TARGET_OWS = Distance.format(
                                            yearInfo.getTarget( YearInfo.SwimKind.OWS ),
                                            UNITS );

        final String TARGET_POOL = Distance.format(
                                            yearInfo.getTarget( YearInfo.SwimKind.POOL ),
                                            UNITS );
        final String TARGET_TOTAL = Distance.format(
                                            yearInfo.getTarget( YearInfo.SwimKind.TOTAL ),
                                            UNITS );

        LBL_OWS.setText( TARGET_OWS );
        LBL_POOL.setText( TARGET_POOL );
        LBL_TOTAL.setText( TARGET_TOTAL );
    }

    /** Store the yearInfo object and finish. */
    private void store()
    {
        if ( Util.getDate().get( Calendar.MONTH ) > 9 ) {
            final AlertDialog.Builder DLG = new AlertDialog.Builder( this );

            DLG.setMessage( R.string.message_objectives_not_editable_now );
            DLG.setPositiveButton( R.string.label_ok, (DialogInterface dlg, int op) -> {
                dlg.dismiss();
                EditYearInfoActivity.this.finish();
            });

            DLG.create().show();
        } else {
            dataStore.add( yearInfo );
            this.finish();
        }
    }

    public static YearInfo yearInfo;
}
