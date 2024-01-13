// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;


import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.ui.adapters.YearInfoCursorAdapter;


public class AchievementsActivity extends BaseActivity {
    private final String LOG_TAG = AchievementsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        this.setContentView( R.layout.activity_achievements );

        final Toolbar TOOL_BAR = this.findViewById( R.id.toolbarAchievements );
        this.setSupportActionBar( TOOL_BAR );

        final ImageButton BT_BACK = this.findViewById( R.id.btCloseAchievements );
        final ImageButton BT_SCRSHOT = this.findViewById( R.id.btTakeScrshotForAchievements );

        BT_BACK.setOnClickListener( v -> AchievementsActivity.this.finish() );
        BT_SCRSHOT.setOnClickListener( v -> {
            final AchievementsActivity SELF = AchievementsActivity.this;

            SELF.shareScreenShot( LOG_TAG, SELF.takeScreenshot( LOG_TAG ) );
        });

        this.createYearInfoList();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        this.update();
    }

    /** Updates the cursor. */
    private void updateAllSessionsList()
    {
        this.yearInfoCursor.changeCursor( dataStore.getDescendingAllYearInfosCursor() );
    }

    @Override
    protected void update()
    {
        this.updateAllSessionsList();
    }

    private void createYearInfoList()
    {
        final ListView LV_ALL_YEAR_INFOS = this.findViewById( R.id.lvAllYearInfos );

        this.yearInfoCursor = new YearInfoCursorAdapter( this, settings );
        LV_ALL_YEAR_INFOS.setAdapter( this.yearInfoCursor );
    }

    private YearInfoCursorAdapter yearInfoCursor;
}
