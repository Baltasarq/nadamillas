package com.devbaltasarq.nadamillas.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.Session;
import com.devbaltasarq.nadamillas.core.Settings;
import com.devbaltasarq.nadamillas.core.storage.SessionStorage;
import com.devbaltasarq.nadamillas.ui.adapters.SessionCursorAdapter;

public class HistoryActivity extends BaseActivity {
    private static final String LOG_TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        this.setContentView( R.layout.activity_history );

        final Toolbar TOOL_BAR = this.findViewById( R.id.toolbar );
        this.setSupportActionBar( TOOL_BAR );

        final FloatingActionButton FB_NEW = this.findViewById( R.id.fbNew );
        final ImageButton BT_BACK = this.findViewById( R.id.btCloseHistory );
        final ImageButton BT_SHARE = this.findViewById( R.id.btShareHstory );

        FB_NEW.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HistoryActivity.this.onNew();
            }
        });
        BT_BACK.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryActivity.this.finish();
            }
        });

        BT_SHARE.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final HistoryActivity SELF = HistoryActivity.this;

                SELF.share( LOG_TAG, SELF.takeScreenshot( LOG_TAG, dataStore ) );
            }
        });

        this.createAllSessionsList();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        this.updateAllSessionsList();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        this.sessionsCursor.getCursor().close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if ( resultCode == Activity.RESULT_OK ) {
            switch( requestCode ) {
                case RC_NEW_SESSION:
                    this.storeNewSession( dataStore, data );
                    this.updateAllSessionsList();
                    break;
                case RC_EDIT_SESSION:
                    final Session SESSION = SessionStorage.createFrom( data );

                    this.updateSession( dataStore, SESSION );
                    SessionCursorAdapter.updateViewWith( SESSION );
                    break;
            }
        }

        return;
    }

    /** Handle the 'new session' event. */
    private void onNew()
    {
        this.launchNewSessionEdit();
    }

    /** Updates the cursor. */
    private void updateAllSessionsList()
    {
        this.sessionsCursor.changeCursor( dataStore.getAllDescendingSessionsCursor() );
    }

    /** Updates the list view, since it has probably changed. */
    private void createAllSessionsList()
    {
        final ListView LV_ALL_SESSIONS = this.findViewById( R.id.lvAllSessions );

        this.sessionsCursor = new SessionCursorAdapter( this, settings );
        LV_ALL_SESSIONS.setAdapter( this.sessionsCursor );
    }

    /** Listener for the edit session event. */
    public void onEditSession(Session session)
    {
        this.launchSessionEdit( session );
    }

    /** Listener for the delete session event. */
    public void onDeleteSession(Session session)
    {
        this.deleteSession( dataStore, session );
        this.updateAllSessionsList();
    }

    private SessionCursorAdapter sessionsCursor;
    public static DataStore dataStore;
}
