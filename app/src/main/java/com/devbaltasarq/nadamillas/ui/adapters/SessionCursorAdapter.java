package com.devbaltasarq.nadamillas.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.session.Distance;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Settings;
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
        final ImageButton BT_MENU = VIEW.findViewById( R.id.btEntryOpsMenu );
        final Session SESSION = SessionStorage.createFrom( CURSOR );

        updateViewWith( VIEW, SESSION );

        // Set entry menu listener
        BT_MENU.setOnClickListener( (v) -> {
            selectedSession = SESSION;
            ACTIVITY.onEntryOpsMenu();
        });
    }

    private static void updateViewWith(@NonNull View view, @NonNull Session session)
    {
        final Locale LOCALE = Locale.getDefault();
        final TextView LBL_DATA = view.findViewById( R.id.lblData );
        final TextView LBL_SPEED = view.findViewById( R.id.lblSpeed );
        final ImageView IV_LOGO = view.findViewById( R.id.ivLogo );
        final String DATE = session.getDate().toShortDateString();
        final Distance.Units UNITS = settings.getDistanceUnits();

        if ( session.getDistance() > 0
          && session.getDuration().getTimeInSeconds() > 0 )
        {
            LBL_SPEED.setVisibility( View.VISIBLE );
            LBL_SPEED.setText( session.getTimeAndWholeSpeedFormattedString( settings ) );
        } else {
            LBL_SPEED.setVisibility( View.GONE );
        }

        // Basic data
        final String BASIC_DATA = String.format( LOCALE, "%10s", DATE )
                                    + " "
                                    + Distance.Fmt.format( session.getDistance(), UNITS );

        LBL_DATA.setText( BASIC_DATA );

        // Logo
        int drawableId = R.drawable.ic_sea;

        if ( session.isAtPool() ) {
            drawableId = R.drawable.ic_pool;
        }

        IV_LOGO.setImageDrawable( AppCompatResources.getDrawable( view.getContext(), drawableId ) );
    }

    public static Session selectedSession;
    private static Settings settings;
}
