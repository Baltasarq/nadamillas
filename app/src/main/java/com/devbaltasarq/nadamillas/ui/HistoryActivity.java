// NadaMillas (c) 2019-2024 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.ui;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.devbaltasarq.nadamillas.R;
import com.devbaltasarq.nadamillas.core.DataStore;
import com.devbaltasarq.nadamillas.core.Session;
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
        final ImageButton BT_SCRSHOT = this.findViewById( R.id.btTakeScrshotForHistory );

        FB_NEW.setOnClickListener( v -> this.onNew() );
        BT_BACK.setOnClickListener( v -> this.finish() );
        BT_SCRSHOT.setOnClickListener( v ->
                this.shareScreenShot( LOG_TAG, this.takeScreenshot( LOG_TAG ) )
        );

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

        DataStore.close( this.sessionsCursor.getCursor() );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        /*this.getMenuInflater().inflate( R.menu.history, menu );
        menu.getItem( R.id.menu_history_filter ).setVisible( false );
         */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if ( item.getItemId() == R.id.menu_history_back ) {
            this.finish();
            return true;
        }
        else
        if ( item.getItemId() == R.id.menu_history_filter ) {
            this.onFilter();
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    public void onFilter()
    {
        new FilterDialog( this ).show();
    }

    /** Handle the ops menu event. */
    public void onEntryOpsMenu()
    {
        final IconListAlertDialog DLG = new IconListAlertDialog( this,
                                        R.drawable.ic_swimming_figure,
                                        R.string.title_activity_edit_session,
                                        new int[]{
                                                R.drawable.btn_pencil,
                                                R.drawable.btn_delete
                                        },
                                        new int[]{
                                                R.string.action_modify,
                                                R.string.action_delete
                                        } );

        DLG.setItemClickListener( (parent, view, position, id) -> {
            final Session SESSION = SessionCursorAdapter.selectedSession;
            final boolean isModify = ( position == 0 );

            DLG.hide();
            DLG.dismiss();

            if ( isModify ) {
                HistoryActivity.this.onEditSession( SESSION );
            } else {
                HistoryActivity.this.onDeleteSession( SESSION );
            }
        });

        DLG.show();
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

    @Override
    protected void update()
    {
        this.updateAllSessionsList();
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
        this.deleteSession( session );
        this.updateAllSessionsList();
    }

    private SessionCursorAdapter sessionsCursor;
}
