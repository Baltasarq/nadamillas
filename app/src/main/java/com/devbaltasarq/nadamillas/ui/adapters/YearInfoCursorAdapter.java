// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui.adapters;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.Settings;
import com.devbaltasarq.nadamillas.core.YearInfo;
import com.devbaltasarq.nadamillas.core.settings.DistanceUtils;
import com.devbaltasarq.nadamillas.core.storage.YearInfoStorage;
import com.devbaltasarq.nadamillas.ui.AchievementsActivity;

import java.util.Locale;


public class YearInfoCursorAdapter extends CursorAdapter {
    public YearInfoCursorAdapter(Context context, Settings cfg)
    {
        super( context, null, false );

        settings = cfg;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        final LayoutInflater INFLATER = LayoutInflater.from( context );

        return INFLATER.inflate( R.layout.listview_year_info_entry, null );
    }

    @Override
    public void bindView(final View VIEW, Context context, final Cursor CURSOR)
    {
        final AchievementsActivity ACTIVITY = (AchievementsActivity) context;
        final YearInfo YEAR_INFO = YearInfoStorage.createFrom( CURSOR );

        achievementsActivity = ACTIVITY;

        updateViewWith( VIEW, YEAR_INFO );
    }

    private static void updateViewWith(@NonNull View view, @NonNull YearInfo yearInfo)
    {
        final Locale LOCALE = Locale.getDefault();
        final DistanceUtils DISTANCE_UTILS = settings.getDistanceUtils();
        final TextView LBL_MAIN_DATA = view.findViewById( R.id.lblMainProgress );
        final TextView LBL_AUX_DATA = view.findViewById( R.id.lblSecondaryProgress );
        final TextView LBL_YEAR = view.findViewById( R.id.lblYear );
        final ImageView IV_ACHIEVEMENT = view.findViewById( R.id.ivAchievement );
        final String STR_UNITS = settings.getDistanceUnits().toString();
        final String STR_OWS = achievementsActivity.getString( R.string.label_abbrev_open_waters );
        final String STR_POOL = achievementsActivity.getString( R.string.label_pool );
        final int PROGRESS = (int) yearInfo.getProgress( YearInfo.SwimKind.TOTAL );
        final String STR_MAIN_DATA = String.format( LOCALE,
                "%s (%d%%) %s.",
                DISTANCE_UTILS.toString( yearInfo.getDistance( YearInfo.SwimKind.TOTAL ) ),
                PROGRESS,
                STR_UNITS );
        final String STR_AUX_DATA = String.format( LOCALE,
                                    "%s %s (%d%%) - "
                                    + "%s %s (%d%%)",
                                    STR_OWS,
                                    DISTANCE_UTILS.toString( yearInfo.getDistance( YearInfo.SwimKind.OWS ) ),
                                    (int) yearInfo.getProgress( YearInfo.SwimKind.OWS ),
                                    STR_POOL,
                                    DISTANCE_UTILS.toString( yearInfo.getDistance( YearInfo.SwimKind.POOL ) ),
                                    (int) yearInfo.getProgress( YearInfo.SwimKind.POOL ),
                                    STR_UNITS );

        LBL_YEAR.setText( Integer.toString( yearInfo.getYear() ) );
        LBL_MAIN_DATA.setText( STR_MAIN_DATA );
        LBL_AUX_DATA.setText( STR_AUX_DATA );

        // Achievement image
        if ( PROGRESS >= 100 ) {
            IV_ACHIEVEMENT.setImageDrawable(
                    AppCompatResources.getDrawable(
                                            view.getContext(),
                                            R.drawable.ic_achievement ) );
        }

        achievementsActivity = null;
    }

    private static Settings settings;
    private static AchievementsActivity achievementsActivity;
}
