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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
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
import com.tinfoil.sms.adapter.DefaultListAdapter;
import com.tinfoil.sms.crypto.KeyGenerator;
import com.tinfoil.sms.dataStructures.User;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.messageQueue.MessageSender;
import com.tinfoil.sms.messageQueue.SignalListener;
import com.tinfoil.sms.settings.AddContact;
import com.tinfoil.sms.settings.ImportContacts;
import com.tinfoil.sms.settings.QuickPrefsActivity;
import com.tinfoil.sms.utility.MessageReceiver;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;

/**
 * <ul>
 * <li>TODO add the proper version number and name to the manifest</li>
 * <li>TODO attempt to move all DB queries to queues</li>
 * <li>TODO change wrap_content to '0dp'</li>
 * <li>TODO list is updated check if the list is now empty and should display the alt list</li>
 * </ul>
 * This activity shows all of the conversations the user has with contacts. The
 * list Will be updated every time a message is received. Upon clicking any of
 * the conversations MessageView activity will be started with selectedNumber =
 * the contacts number From the menu a user can select 'compose' to start
 * SendMessageActivity to start or continue a conversation with a contact. The
 * user can also select 'settings' which will take them to the main settings
 * page.
 */
public class ConversationView extends Activity implements Runnable {

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
    
    public static MessageSender messageSender = new MessageSender();
    
    private ProgressDialog dialog;
    private static boolean update = false;
    public static final int LOAD = 0;
    public static final int UPDATE = 1;
    private static Thread thread;
    private static ListView emptyList; 
    
    public static final int ADD_CONTACT = 0;
    public static final int IMPORT_CONTACT = 1;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO move setup to a thread to focus on a faster UI interaction
        ((TelephonyManager) this.getSystemService(TELEPHONY_SERVICE)).listen(this.pSL, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        MessageService.mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        //Cancel all notifications from tinfoil-sms upon starting the main activity
        MessageService.mNotificationManager.cancelAll();

        MessageService.dba = new DBAccessor(this);

        SMSUtility.user = MessageService.dba.getUserRow();
        
        // Commented out for now since there is a library reference problem.
         
        if(SMSUtility.user == null)
        {
        	Toast.makeText(this, "New key pair is generating...", Toast.LENGTH_SHORT).show();
        	Log.v("First Launch", "keys are generating...");
        	//Create the keyGenerator
	        KeyGenerator keyGen = new KeyGenerator();
	        
	        SMSUtility.user = new User(keyGen.generatePubKey(), keyGen.generatePriKey());
	        //Set the user's 
	        MessageService.dba.setUser(SMSUtility.user);
        }
        
        Log.v("public key", new String(SMSUtility.user.getPublicKey()));
        Toast.makeText(this, "Public Key " + new String(SMSUtility.user.getPublicKey()), Toast.LENGTH_LONG);
        
        Log.v("private key", new String(SMSUtility.user.getPrivateKey()));
        Toast.makeText(this, "Private Key " + new String(SMSUtility.user.getPrivateKey()), Toast.LENGTH_LONG);
        
        messageSender.startThread(getApplicationContext());

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
        
        ConversationView.messageViewActive = false;
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
        emptyList = (ListView) this.findViewById(R.id.empty);
        
        this.dialog = ProgressDialog.show(this, "Loading Messages",
                "Please wait...", true, true, new OnCancelListener() {

					public void onCancel(DialogInterface dialog) {
										
					}
        });
        
        update = false;
        thread = new Thread(this);
        thread.start();
        
        //msgList = MessageService.dba.getConversations();
        

        //View header = (View)getLayoutInflater().inflate(R.layout.contact_message, null);
        //list.addHeaderView(header);
        
        /*
         * Load the selected conversation thread when clicked
         */
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {

                final Intent intent = new Intent(ConversationView.this.getBaseContext(), MessageView.class);
                intent.putExtra(ConversationView.selectedNumberIntent, msgList.get(position)[0]);
                ConversationView.this.startActivity(intent);
            }
        });
        
        emptyList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				switch (position)
				{
					case ADD_CONTACT:
						//Launch add contacts
						AddContact.addContact = true;
		                AddContact.editTc = null;
						ConversationView.this.startActivity(new Intent(
		                		ConversationView.this.getBaseContext(), 
		                		AddContact.class));
					break;
					case IMPORT_CONTACT:
						//Launch import contacts
						ConversationView.this.startActivity(new Intent(
		                		ConversationView.this.getBaseContext(), 
		                		ImportContacts.class));
					break;
					//Upon finishing the list view should be updated to show the contacts.
				}
			}
		});

    }

    /**
     * Updates the list of the messages in the main inbox and in the secondary
     * inbox that the user last viewed, or is viewing
     * 
     * @param list The ListView for this activity to update the message list
     */
    public static void updateList(final Context context, final boolean messageViewUpdate)
    {
    	//TODO remove
        //Toast.makeText(context, String.valueOf(messageViewUpdate), Toast.LENGTH_SHORT).show();
        if (MessageReceiver.myActivityStarted)
        {
        	//update = true;
        	//thread.start();
        	//
        	//thread.start();
        	
        	//TODO do this in a thread
            msgList = MessageService.dba.getConversations();
            if(conversations == null)
            {
            	update = true;
            	
            	//thread = new Thread(this);
            	if(!thread.isAlive())
            	{
            		thread.start();
            	}
            }
            else
            {
            	conversations.clear();
            	conversations.addData(msgList);
            }
            
            if (messageViewUpdate)
            {
                MessageView.updateList();
            }
        }
    }

    @Override
    protected void onResume()
    {
    	if(conversations != null)
    	{
    		updateList(this, false);
    	}
        super.onResume();
        
    }

    @Override
    protected void onPause()
    {
    	 MessageService.dba.close();
    	 super.onPause();
    }
    
    @Override
    protected void onDestroy()
    {	       
        this.stopService(new Intent(this, MessageService.class));
        
        conversations = null;
        //this.unbindService(null);
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
            case R.id.exchange:
            	this.startActivity(new Intent(this, KeyExchangeManager.class));
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

	public void run() {
		
		DBAccessor loader = new DBAccessor(this);
		msgList = loader.getConversations();
		if(!update) {
			this.handler.sendEmptyMessage(LOAD);
		}
		else
		{
			this.handler.sendEmptyMessage(UPDATE);
		}
	}
	
	/**
	 * The handler class for cleaning up after the loading thread as well as the
	 * update thread.
	 */
	private final Handler handler = new Handler() {
        @Override
        public void handleMessage(final android.os.Message msg)
        {
        	if(msgList.isEmpty())
        	{
        		List<String> emptyItems = new ArrayList<String>();
        		emptyItems.add("Add Contact");
        		emptyItems.add("Import Contact");
        		
        		DefaultListAdapter ap = new DefaultListAdapter(ConversationView.this, R.layout.empty_list_item, emptyItems);
                emptyList.setAdapter(ap);
        		emptyList.setVisibility(ListView.VISIBLE);
        		list.setVisibility(ListView.INVISIBLE);
        		ConversationView.this.dialog.dismiss();
        	}
        	else
        	{
        		emptyList.setVisibility(ListView.INVISIBLE);
        		list.setVisibility(ListView.VISIBLE);
	        	switch (msg.what){
	        	case LOAD:
	        		conversations = new ConversationAdapter(ConversationView.this, R.layout.listview_item_row, msgList);
	        		list.setAdapter(conversations);
	        		ConversationView.this.dialog.dismiss();
		        	break;
	        	case UPDATE:
	        		conversations.clear();
	                conversations.addData(msgList);
	        		break;
	        	}
        	}
        }
    };
}
