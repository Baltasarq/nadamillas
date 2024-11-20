// NadaMillas (c) 2019-2024-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core.storage;


import android.content.Context;
import android.content.SharedPreferences;


import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.session.Distance;
import com.devbaltasarq.nadamillas.core.Settings;
import com.devbaltasarq.nadamillas.core.settings.FirstDayOfWeek;
import com.devbaltasarq.nadamillas.core.settings.PoolLength;

import java.util.Calendar;


/** Represents the Settings object in storage. */
public class SettingsStorage {
    private static final String TAG_DISTANCE_UNITS = Distance.Units.class.getSimpleName();
    private static final String TAG_POOL_LENGTH = PoolLength.class.getSimpleName();
    private static final String TAG_FIRST_DAY_OF_WEEK = FirstDayOfWeek.class.getSimpleName();

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
        int poolLength = this.getSettings().getPoolLength().getLength();

        editor.putInt( TAG_DISTANCE_UNITS,
                       this.getSettings().getDistanceUnits().ordinal() );
        editor.putInt( TAG_FIRST_DAY_OF_WEEK,
                        this.getSettings().getFirstDayOfWeek().ordinal() );
        editor.putInt( TAG_POOL_LENGTH,
                        poolLength );
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
        final int DEFAULT_POOL_LENGTH = PoolLength.getDefault().getLength();
        final Distance.Units DIST_UNITS =
                Distance.Units.fromOrdinal(
                        PREFS.getInt( TAG_DISTANCE_UNITS, 0 ) );

        int posFdow = PREFS.getInt( TAG_FIRST_DAY_OF_WEEK, -1 );
        FirstDayOfWeek fDoW = FirstDayOfWeek.getDefault();

        if ( posFdow < 0
          || posFdow >= FirstDayOfWeek.values().length )
        {
            final int FIRST_DAY_OF_WEEK = Calendar.getInstance().getFirstDayOfWeek();

            fDoW = FirstDayOfWeek.fromCalendarValue( FIRST_DAY_OF_WEEK );
        } else {
            fDoW = FirstDayOfWeek.fromOrdinal( posFdow );
        }

        final PoolLength POOL_LENGTH =
                PoolLength.fromLength(
                        PREFS.getInt( TAG_POOL_LENGTH, DEFAULT_POOL_LENGTH ) );

        return Settings.createFrom( DIST_UNITS, fDoW, POOL_LENGTH );
    }

    private final Settings settings;
    private final Context applicationContext;
}
