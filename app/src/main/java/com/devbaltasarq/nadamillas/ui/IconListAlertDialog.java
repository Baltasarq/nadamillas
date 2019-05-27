package com.devbaltasarq.nadamillas.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


/** An AlertDialog with a list of choices, each one with an icon. */
public class IconListAlertDialog extends AlertDialog {
    /** Creates a basic IconListAlertDialog. */
    public IconListAlertDialog(Context cntxt, int[] choiceIds, int[] drawableIds)
    {
        this( cntxt,
                android.R.drawable.ic_dialog_alert, android.R.string.dialog_alert_title,
                choiceIds, drawableIds );
    }

    /** Creates a complete IconListAlertDialog. */
    public IconListAlertDialog(Context cntxt, int iconResId, int titleResId,
                               int[] drawableIds, int[] choiceIds)
    {
        super( cntxt, true, null );

        final int NUM_CHOICES = choiceIds.length;

        assert NUM_CHOICES == drawableIds.length:
                "IconListAlertDialog: different number of choices and icons.";

        // Initial touches
        this.setTitle( titleResId );
        this.setIcon( AppCompatResources.getDrawable( cntxt, iconResId ) );
        this.setButton( BUTTON_NEGATIVE, "Cancel", (OnClickListener) null );

        // Set view
        this.lvChoices = new ListView( cntxt );
        this.setView( lvChoices );

        // Setup
        this.entries = new IconListAlertDialogEntry[ NUM_CHOICES ];

        for(int i = 0; i < NUM_CHOICES; ++i) {
            int icon = drawableIds[ i ];
            int choice = choiceIds[ i ];

            if ( icon < 0
              && choice < 0 )
            {

                continue;
            }

            if ( icon < 0 ) {
                icon = android.R.drawable.btn_dialog;
            }

            if ( choice < 0 ) {
                choice = android.R.string.ok;
            }

            this.entries[ i ] = new IconListAlertDialogEntry(
                    AppCompatResources.getDrawable( cntxt, icon ),
                    cntxt.getString( choice ) );
        }

        // Setup list contents
        lvChoices.setAdapter( new IconListAlertDialogEntryArrayAdapter( cntxt, this.entries ) );
    }

    /** Sets the listener for selected items
     * @param selection The listener.
     * @see AdapterView<?>.OnItemClickListener
     */
    public void setItemClickListener(AdapterView.OnItemClickListener selection)
    {
        this.lvChoices.setOnItemClickListener( selection );
    }


    private IconListAlertDialogEntry[] entries;
    private ListView lvChoices;

    /** Represents a single entry in the group files list view. */
    private static class IconListAlertDialogEntry {
        public IconListAlertDialogEntry(Drawable icon, String choice)
        {
            this.icon = icon;
            this.choice = choice;
        }

        /** @return The icon of this entry. */
        public Drawable getIcon()
        {
            return this.icon;
        }

        /** @return The string of this choice. */
        public String getChoice()
        {
            return this.choice;
        }

        private Drawable icon;
        private String choice;
    }

    /** Represents an adapter of the special items for the ListView of media files. */
    private static class IconListAlertDialogEntryArrayAdapter extends ArrayAdapter<IconListAlertDialogEntry> {
        public IconListAlertDialogEntryArrayAdapter(Context cntxt, IconListAlertDialogEntry[] entries)
        {
            super( cntxt, 0, entries );
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            final Context cntxt = this.getContext();
            final IconListAlertDialogEntry entry = this.getItem( position );

            // Create view
            LinearLayout hrzLayout = new LinearLayout( cntxt );
            hrzLayout.setPadding( 5, 5, 5, 5 );
            hrzLayout.setOrientation( LinearLayout.HORIZONTAL );
            hrzLayout.setLayoutParams( new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT ) );

            final TextView lblChoice = new TextView( cntxt );
            final ImageView ivIcon = new ImageView( cntxt );

            lblChoice.setLayoutParams( new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.2f
            ));

            lblChoice.setPadding( 5, 5, 5, 5 );

            ivIcon.setLayoutParams( new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.8f
            ));
            ivIcon.setPadding( 5, 5, 5, 5 );

            hrzLayout.addView( ivIcon );
            hrzLayout.addView( lblChoice );
            convertView = hrzLayout;

            // Assign values
            if ( entry != null ) {
                lblChoice.setText( entry.getChoice() );
                ivIcon.setImageDrawable( entry.getIcon() );
            }

            return convertView;
        }
    }
}
