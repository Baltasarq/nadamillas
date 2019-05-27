// NadaMillas (c) 2019 Baltasar MIT License <baltasarq@gmail.com>

package com.devbaltasarq.nadamillas.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Settings;
import com.devbaltasarq.nadamillas.ui.BrowseActivity;

import java.util.Locale;

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

        // Set appropriate icon
        int drawableId = R.drawable.ic_sea;

        if ( SESSION.isAtPool() ) {
            drawableId = R.drawable.ic_pool;
        }

        IV_LOGO.setImageDrawable( AppCompatResources.getDrawable( ACTIVITY, drawableId ) );

        // Set data
        LBL_DATA.setText( SESSION.getFormattedDistance( ACTIVITY, settings ) );

        if ( SESSION.getDistance() > 0
          && SESSION.getDuration().getTimeInSeconds() > 0 )
        {
            LBL_SPEED.setVisibility( View.VISIBLE );
            LBL_SPEED.setText( SESSION.getWholeSpeedFormattedString( settings ) );
        } else {
            LBL_SPEED.setVisibility( View.GONE );
        }

        return rowView;
    }

    public static Session selectedSession;
}
