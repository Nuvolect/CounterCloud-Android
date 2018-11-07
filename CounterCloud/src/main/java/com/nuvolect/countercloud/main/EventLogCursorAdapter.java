package com.nuvolect.countercloud.main;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.data.DbProvider.ELTab;
import com.nuvolect.countercloud.util.LogUtil;


public class EventLogCursorAdapter extends CursorAdapter {

    private final LayoutInflater m_inflater;
    private final int m_layout;
    private int m_contact_idIndex;
    private int m_descriptionIndex;
    private int m_eventIndex;
    private int m_timeIndex;
    private SimpleDateFormat sdf;
    private EventLogCaCallbacks m_listener;

    public interface EventLogCaCallbacks {

        public void itemClick( long contact_id);
    }

    public EventLogCursorAdapter(Context ctx, Cursor c, int flags, int layout,
            EventLogCaCallbacks listener) {
        super(ctx, c, flags);

        this.m_layout = layout;
        this.m_inflater=LayoutInflater.from( ctx);
        m_listener = listener;

        m_contact_idIndex = c.getColumnIndex(ELTab.contact_id.toString());
        m_timeIndex = c.getColumnIndex(ELTab.time.toString());
        m_eventIndex = c.getColumnIndex(ELTab.event.toString());
        m_descriptionIndex = c.getColumnIndex(ELTab.display_name.toString());

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        LogUtil.log("EventLogCursorAdapter");
    }

    @Override
    public void bindView(View view, Context ctx, Cursor c) {

        TextView timeTv = (TextView) view.findViewById(R.id.timeTv);
        String time = sdf.format( c.getLong( m_timeIndex));
        timeTv.setText( time );

        TextView eventTv = (TextView) view.findViewById(R.id.eventTv);
        eventTv.setText( c.getString( m_eventIndex ));

        TextView descriptionTv = (TextView) view.findViewById(R.id.descriptionTv);
        descriptionTv.setText(c.getString( m_descriptionIndex ));

//        LogUtil.log("bindView: "+c.getString(m_descriptionIndex));

        /*
         * Get the contact ID associated with this update and save it as a tag
         * If the user later clicks on it it will be used to look up the contact record.
         * Note that contact_id will be an invalid '0' for deletes.
         */
        long contact_id = c.getLong(m_contact_idIndex);
        LinearLayout logItemLL = (LinearLayout) view.findViewById(R.id.logItemLL);
        logItemLL.setTag(contact_id);

        logItemLL.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {

                LinearLayout logItemLL = (LinearLayout) v.findViewById(R.id.logItemLL);
                long contact_id = (Long) logItemLL.getTag();

                // Inform the fragment that an item has been clicked and pass the ID
                m_listener.itemClick(contact_id);

            }
            }
        );
    }

    @Override
    public View newView(Context ctx, Cursor arg1, ViewGroup arg2) {

        return m_inflater.inflate( m_layout, null);
    }

}
