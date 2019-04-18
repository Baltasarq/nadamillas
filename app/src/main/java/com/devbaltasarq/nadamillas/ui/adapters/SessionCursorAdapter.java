package com.devbaltasarq.nadamillas.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Settings;
import com.devbaltasarq.nadamillas.core.Util;
import com.devbaltasarq.nadamillas.core.storage.SessionStorage;
import com.devbaltasarq.nadamillas.ui.HistoryActivity;

import java.util.Locale;


public class SessionCursorAdapter extends CursorAdapter {
    public SessionCursorAdapter(Context context, Settings cfg)
    {
        super( context, null, false );

        settings = cfg;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        final LayoutInflater INFLATER = LayoutInflater.from( context );

        return INFLATER.inflate( R.layout.listview_session_entry, null );
    }

    @Override
    public void bindView(final View VIEW, Context context, final Cursor CURSOR)
    {
        final HistoryActivity ACTIVITY = (HistoryActivity) context;
        final ImageButton BT_DELETE = VIEW.findViewById( R.id.btDelete );
        final ImageButton BT_EDIT = VIEW.findViewById( R.id.btEdit );
        final Session SESSION = SessionStorage.createFrom( CURSOR );

        historyActivity = ACTIVITY;

        updateViewWith( VIEW, SESSION );

        // Set button listeners
        BT_EDIT.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                editedView = VIEW;
                historyActivity = ACTIVITY;
                ACTIVITY.onEditSession( SESSION );
            }
        });

        BT_DELETE.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ACTIVITY.onDeleteSession( SESSION );
            }
        });
    }

    public static void updateViewWith(@NonNull Session session)
    {
        updateViewWith( editedView, session );
        editedView = null;
    }

    private static void updateViewWith(@NonNull View view, @NonNull Session session)
    {
        final Locale LOCALE = Locale.getDefault();
        final TextView LBL_DATA = view.findViewById( R.id.lblData );
        final TextView LBL_SPEED = view.findViewById( R.id.lblSpeed );
        final ImageView IV_LOGO = view.findViewById( R.id.ivLogo );
        final String DATE = Util.getShortDate( session.getDate(), null );
        final String FORMATTED_DISTANCE = String.format( LOCALE, "%6d", session.getDistance() );
        final String FORMATTED_DATE = String.format( LOCALE, "%10s", DATE );
        int units = R.string.label_meter;

        if ( settings.getDistanceUnits() == Settings.DistanceUnits.mi ) {
            units = R.string.label_yard;
        }

        if ( session.getDistance() > 0
          && session.getDuration().getTimeInSeconds() > 0 )
        {
            LBL_SPEED.setText( session.getSpeedAsString( HistoryActivity.settings )
                    + " - " + session.getMeanTimeAsString( HistoryActivity.settings ) );
        }

        // Basic data
        LBL_DATA.setText( FORMATTED_DATE
                            + " " + FORMATTED_DISTANCE
                            + historyActivity.getString( units ) );

        // Logo
        int drawableId = R.drawable.ic_sea;

        if ( session.isAtPool() ) {
            drawableId = R.drawable.ic_pool;
        }

        IV_LOGO.setImageDrawable( AppCompatResources.getDrawable( view.getContext(), drawableId ) );
        historyActivity = null;
    }

    private static View editedView;
    private static Settings settings;
    private static HistoryActivity historyActivity;
}
