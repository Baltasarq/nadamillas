// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui.adapters;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.session.Distance;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.session.Duration;
import com.devbaltasarq.nadamillas.ui.BrowseActivity;

import static com.devbaltasarq.nadamillas.ui.BaseActivity.settings;


/** An array adapter for presenting a customized ListView of Session. */
public class ListViewSessionArrayAdapter extends ArrayAdapter<Session> {

    /** Creates a new ArrayAdapter. */
    public ListViewSessionArrayAdapter(Context cntxt, Session[] entries)
    {
        super( cntxt, 0, entries );
    }

    @Override
    public @NonNull View getView(int position, View rowView, @NonNull ViewGroup parent)
    {
        final Session SESSION = this.getItem( position );
        final BrowseActivity ACTIVITY = (BrowseActivity) this.getContext();
        final Distance.Units UNITS = settings.getDistanceUnits();

        if ( rowView == null ) {
            final LayoutInflater LAYOUT_INFLATER = LayoutInflater.from( this.getContext() );

            rowView = LAYOUT_INFLATER.inflate( R.layout.listview_session_entry, null );

            final ImageButton BT_MENU = rowView.findViewById( R.id.btEntryOpsMenu );


            // Set entry menu listener
            BT_MENU.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListViewSessionArrayAdapter.selectedSession = SESSION;

                    ACTIVITY.onEntryOpsMenu();
                }
            });
        }

        final ImageView IV_LOGO = rowView.findViewById( R.id.ivLogo );
        final TextView LBL_DATA = rowView.findViewById( R.id.lblData );
        final TextView LBL_SPEED = rowView.findViewById( R.id.lblSpeed );

        // Set appropriate icon and default data
        Duration duration = Duration.Zero();
        int drawableId = R.drawable.ic_sea;
        boolean atPool = false;
        String fmtSwimData = "";
        int distance = 0;

        // Set data, provided it is safe
        if ( SESSION != null ) {
            atPool = SESSION.isAtPool();
            distance = SESSION.getDistance();
            duration = SESSION.getDuration();
            fmtSwimData = SESSION.getTimeAndWholeSpeedFormattedString( settings );
        }

        if ( atPool ) {
            drawableId = R.drawable.ic_pool;
        }

        IV_LOGO.setImageDrawable(
                AppCompatResources.getDrawable(
                                        ACTIVITY,
                                        drawableId ) );

        // Set data
        LBL_DATA.setText( Distance.Fmt.format( distance, UNITS ) );

        if ( distance > 0
          && duration.getTimeInSeconds() > 0 )
        {
            LBL_SPEED.setVisibility( View.VISIBLE );
            LBL_SPEED.setText( fmtSwimData );
        } else {
            LBL_SPEED.setVisibility( View.GONE );
        }

        return rowView;
    }

    public static Session selectedSession;
}
