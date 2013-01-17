/** 
 * Copyright (C) 2011 Tinfoilhat
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tinfoil.sms.sms;

import java.util.List;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.tinfoil.sms.R;
import com.tinfoil.sms.adapter.ConversationAdapter;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.messageQueue.SignalListener;
import com.tinfoil.sms.settings.QuickPrefsActivity;
import com.tinfoil.sms.utility.MessageReceiver;
import com.tinfoil.sms.utility.MessageService;

/**
 * <ul>
 * <li>TODO add the proper version number and name to the manifest</li>
 * <li>TODO adjust the list view to default (if empty) to suggest importing
 * contact or composing a message or adding a contact, it can really be either.</li>
 * <li>TODO update the layout to indicate whether the conversation is trusted or
 * not Prephase3Activity is the activity that is launched for the start of the
 * program.</li>
 * </ul>
 * This activity shows all of the conversations the user has with contacts. The
 * list Will be updated every time a message is received. Upon clicking any of
 * the conversations MessageView activity will be started with selectedNumber =
 * the contacts number From the menu a user can select 'compose' to start
 * SendMessageActivity to start or continue a conversation with a contact. The
 * user can also select 'settings' which will take them to the main settings
 * page.
 */
public class Prephase3Activity extends Activity {

	//public static DBAccessor dba;
    public static final String INBOX = "content://sms/inbox";
    public static final String SENT = "content://sms/sent";
    public static SharedPreferences sharedPrefs;
    public static String selectedNumber;
    public static final String selectedNumberIntent = "com.tinfoil.sms.Selected";
    private static ConversationAdapter conversations;
    private static List<String[]> msgList;
    private static ListView list;
    private final MessageReceiver boot = new MessageReceiver();
    private final SignalListener pSL = new SignalListener();
    public static boolean messageViewActive = false;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((TelephonyManager) this.getSystemService(TELEPHONY_SERVICE)).listen(this.pSL, this.pSL.LISTEN_SIGNAL_STRENGTHS);
        MessageService.mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        //Cancel all notifications from tinfoil-sms upon starting the main activity
        MessageService.mNotificationManager.cancelAll();

        MessageService.dba = new DBAccessor(this);

        if (this.getIntent().hasExtra(MessageService.multipleNotificationIntent))
        {
            /*
             * Check if there is the activity has been entered from a notification.
             * This check specifically is to find out if there are multiple pending
             * received messages.
             */
            this.getIntent().removeExtra(MessageService.multipleNotificationIntent);
        }

        if (this.getIntent().hasExtra(MessageService.notificationIntent))
        {
            /*
             * Check if there is the activity has been entered from a notification.
             * This check is to find out if there is a single message received pending.
             * If so then the conversation with that contact will be loaded.
             */
            final Intent intent = new Intent(this, MessageView.class);
            intent.putExtra(selectedNumberIntent, this.getIntent().getStringExtra(MessageService.notificationIntent));
            this.getIntent().removeExtra(MessageService.notificationIntent);
            this.startActivity(intent);
        }
        Prephase3Activity.messageViewActive = false;
        this.setContentView(R.layout.main);

        /*
         * Load the shared preferences
         */
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        MessageReceiver.myActivityStarted = true;

        /*
         * Set the list of conversations
         */
        list = (ListView) this.findViewById(R.id.conversation_list);

        msgList = MessageService.dba.getConversations();
        conversations = new ConversationAdapter(this, R.layout.listview_item_row, msgList);

        //View header = (View)getLayoutInflater().inflate(R.layout.contact_message, null);
        //list.addHeaderView(header);

        list.setAdapter(conversations);

        /*
         * Load the selected conversation thread when clicked
         */
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {

                final Intent intent = new Intent(Prephase3Activity.this.getBaseContext(), MessageView.class);
                intent.putExtra(Prephase3Activity.selectedNumberIntent, msgList.get(position)[0]);
                Prephase3Activity.this.startActivity(intent);
            }
        });

    }

    /**
     * Updates the list of the messages in the main inbox and in the secondary
     * inbox that the user last viewed, or is viewing
     * 
     * @param list
     *            : ListView, the ListView for this activity to update the
     *            message list
     */
    public static void updateList(final Context context, final boolean messageViewUpdate)
    {
        Toast.makeText(context, String.valueOf(messageViewUpdate), Toast.LENGTH_SHORT).show();
        if (MessageReceiver.myActivityStarted)
        {
            msgList = MessageService.dba.getConversations();
            conversations.clear();
            conversations.addData(msgList);

            if (messageViewUpdate)
            {
                MessageView.updateList(context);
            }
        }
    }

    @Override
    protected void onResume()
    {
        updateList(this, false);
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        MessageService.dba.close();
        this.stopService(new Intent(this, MessageService.class));
        MessageReceiver.myActivityStarted = false;

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.texting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.compose:
                this.startActivity(new Intent(this, SendMessageActivity.class));
                return true;
            case R.id.settings:
                this.startActivity(new Intent(this, QuickPrefsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}