package com.devbaltasarq.nadamillas.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.Settings;

import java.util.Calendar;

/** Represents the Settings object in storage. */
public class SettingsStorage {
    private static final String TAG_DISTANCE_UNITS = Settings.DistanceUnits.class.getSimpleName();
    private static final String TAG_FIRST_DAY_OF_WEEK = "first_day_of_week";

    /** Creates a new storage wrapper for a given settings object. */
    public SettingsStorage(Context applicationContext, Settings settings)
    {
        this.settings = settings;
        this.applicationContext = applicationContext;
    }

    /** Stores the settings in the private storage. */
    public void store()
    {
        final SharedPreferences PREFS = openPreferences( this.applicationContext );
        final SharedPreferences.Editor editor = PREFS.edit();

        editor.putInt( TAG_DISTANCE_UNITS,
                       this.getSettings().getDistanceUnits().ordinal());
        editor.putInt( TAG_FIRST_DAY_OF_WEEK, this.getSettings().getFirstDayOfWeek().ordinal() );
        editor.apply();
    }

    /** @return the settings object being wrapped. */
    public Settings getSettings()
    {
        return this.settings;
    }

    /** @return the storage's name. */
    public static String getStorageName(Context appContext)
    {
        return appContext.getString( R.string.app_name );
    }

    /** Opens the preferences storage. */
    private static SharedPreferences openPreferences(Context appContext)
    {
        return appContext.getSharedPreferences( getStorageName( appContext ), Context.MODE_PRIVATE );
    }

    /** @return the settings object restored from the private storage. */
    public static Settings restore(Context appContext)
    {
        final SharedPreferences PREFS = openPreferences( appContext );
        final int POS_DIST_UNITS = PREFS.getInt( TAG_DISTANCE_UNITS, 0 );
        final Settings.DistanceUnits DIST_UNITS =
                Settings.DistanceUnits.fromOrdinal( POS_DIST_UNITS );

        int posFdow = PREFS.getInt( TAG_FIRST_DAY_OF_WEEK, -1 );
        Settings.FirstDayOfWeek fDoW;

        if ( posFdow < 0 ) {
            final int FIRST_DAY_OF_WEEK = Calendar.getInstance().getFirstDayOfWeek();

            fDoW = Settings.FirstDayOfWeek.fromCalendarValue( FIRST_DAY_OF_WEEK );
        } else {
            fDoW = Settings.FirstDayOfWeek.fromOrdinal( posFdow );
        }

        return Settings.createFrom( DIST_UNITS, fDoW );
    }

    private Settings settings;
    private Context applicationContext;
}
