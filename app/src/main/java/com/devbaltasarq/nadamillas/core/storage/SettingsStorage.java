package com.devbaltasarq.nadamillas.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.Settings;

import java.util.Calendar;

/** Represents the Settings object in storage. */
public class SettingsStorage {
    private static final String TAG_DISTANCE_UNITS = Settings.DistanceUnits.class.getSimpleName();
    private static final String TAG_POOL_LENGTH = Settings.PoolLength.class.getSimpleName();
    private static final String TAG_FIRST_DAY_OF_WEEK = Settings.FirstDayOfWeek.class.getSimpleName();

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
                       this.getSettings().getDistanceUnits().ordinal() );
        editor.putInt( TAG_FIRST_DAY_OF_WEEK,
                        this.getSettings().getFirstDayOfWeek().ordinal() );
        editor.putInt( TAG_POOL_LENGTH,
                        this.getSettings().getDefaultPoolLength().getLength() );
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
        return appContext.getSharedPreferences( getStorageName( appContext ),
                                                Context.MODE_PRIVATE );
    }

    /** @return the settings object restored from the private storage. */
    public static Settings restore(Context appContext)
    {
        final SharedPreferences PREFS = openPreferences( appContext );
        final Settings.DistanceUnits DIST_UNITS =
                Settings.DistanceUnits.fromOrdinal(
                        PREFS.getInt( TAG_DISTANCE_UNITS, 0 ) );

        int posFdow = PREFS.getInt( TAG_FIRST_DAY_OF_WEEK, -1 );
        Settings.FirstDayOfWeek fDoW;

        if ( posFdow < 0
          || posFdow >= Settings.FirstDayOfWeek.values().length )
        {
            final int FIRST_DAY_OF_WEEK = Calendar.getInstance().getFirstDayOfWeek();

            fDoW = Settings.FirstDayOfWeek.fromCalendarValue( FIRST_DAY_OF_WEEK );
        } else {
            fDoW = Settings.FirstDayOfWeek.fromOrdinal( posFdow );
        }

        final Settings.PoolLength POOL_LENGTH =
                Settings.PoolLength.fromLength(
                        PREFS.getInt( TAG_POOL_LENGTH, 25 ) );

        return Settings.createFrom( DIST_UNITS, fDoW, POOL_LENGTH );
    }

    private final Settings settings;
    private final Context applicationContext;
}
