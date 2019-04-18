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
import com.devbaltasarq.nadamillas.ui.BrowseActivity;


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

            rowView = LAYOUT_INFLATER.inflate( R.layout.listview_session_in_day_entry, null );

            final ImageButton BT_EDIT = rowView.findViewById( R.id.btEdit );
            final ImageButton BT_DELETE = rowView.findViewById( R.id.btDelete );

            BT_EDIT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    ACTIVITY.onEditSession( SESSION );
                }
            });

            BT_DELETE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    ACTIVITY.onDeleteSession( SESSION );
                }
            });
        }

        final ImageView IV_LOGO = rowView.findViewById( R.id.ivLogo );
        final TextView LBL_DISTANCE = rowView.findViewById( R.id.lblDistance );
        final TextView LBL_SPEED = rowView.findViewById( R.id.lblSpeed );

        // Set appropriate icon
        int drawableId = R.drawable.ic_sea;

        if ( SESSION.isAtPool() ) {
            drawableId = R.drawable.ic_pool;
        }

        IV_LOGO.setImageDrawable( AppCompatResources.getDrawable( ACTIVITY, drawableId ) );

        // Set data
        LBL_DISTANCE.setText( Integer.toString( SESSION.getDistance() ) );

        if ( SESSION.getDistance() > 0
          && SESSION.getDuration().getTimeInSeconds() > 0 )
        {
            LBL_SPEED.setText( SESSION.getMeanTimeAsString( BrowseActivity.settings ) );
        }

        return rowView;
    }
}
