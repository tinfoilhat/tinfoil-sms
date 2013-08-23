/** 
 * Copyright (C) 2013 Jonathan Gillett, Joseph Heron
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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

import com.tinfoil.sms.R;
import com.tinfoil.sms.TinfoilSMS;
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
 * This activity shows all of the conversations the user has with contacts. The
 * list Will be updated every time a message is received. Upon clicking any of
 * the conversations MessageView activity will be started with selectedNumber =
 * the contacts number From the menu a user can select 'compose' to start
 * SendMessageActivity to start or continue a conversation with a contact. The
 * user can also select 'settings' which will take them to the main settings
 * page.
 */
public class ConversationView extends Activity {

	//public static DBAccessor dba;
    public static final String INBOX = "content://sms/inbox";
    public static final String SENT = "content://sms/sent";
    public static SharedPreferences sharedPrefs;
    public static String selectedNumber;
    public static final String selectedNumberIntent = "com.tinfoil.sms.Selected";
    private static ConversationAdapter conversations;
    public static List<String[]> msgList;
    private static ListView list;
    private static DefaultListAdapter ap;
    private final MessageReceiver boot = new MessageReceiver();
    private final SignalListener pSL = new SignalListener();
    public static boolean messageViewActive = false;
    
    public static MessageSender messageSender = new MessageSender();
      
    private ProgressDialog dialog;
    private static boolean update = false;
    public static final int LOAD = 0;
    public static final int UPDATE = 1;
    private static ListView emptyList; 
    
    private static ConversationLoader runThread;
    
    public static final int ADD_CONTACT = 0;
    public static final int IMPORT_CONTACT = 1;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((TelephonyManager) this.getSystemService(TELEPHONY_SERVICE)).listen(this.pSL, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        MessageService.mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        messageSender.startThread(getApplicationContext());
        
        MessageService.dba = new DBAccessor(this);
        
        SMSUtility.user = MessageService.dba.getUserRow();
        
        if(SMSUtility.user == null)
        {
        	//Toast.makeText(context, "New key pair is generating...", Toast.LENGTH_SHORT).show();
        	Log.v("First Launch", "keys are generating...");
        	//Create the keyGenerator
	        KeyGenerator keyGen = new KeyGenerator();
	        
	        SMSUtility.user = new User(keyGen.generatePubKey(), keyGen.generatePriKey());
	        
	        //Set the user's 
	        MessageService.dba.setUser(SMSUtility.user);
        }
        Log.v("public key", new String(SMSUtility.user.getPublicKey()));
        Log.v("private key", new String(SMSUtility.user.getPrivateKey()));
        
        // Set the user's build version
        String versionNumber =  Build.VERSION.RELEASE;
        String[] numbers = versionNumber.split("\\.");
        
        try {
        	if(Integer.valueOf(numbers[0]) >= 4 && Integer.valueOf(numbers[1]) >= 1)
        	{
        		TinfoilSMS.threadable = true;
        	}
        }
        // If the version cannot be parsed assume the version is requires the 2.3 version
        catch (NumberFormatException e){}
        
        Log.v("Build Version", Build.VERSION.RELEASE + " " + TinfoilSMS.threadable);
        Log.v("Build Thread Safe", ""+TinfoilSMS.threadable);

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
        
        /*this.dialog = ProgressDialog.show(this, "Loading Messages",
                "Please wait...", true, true, new OnCancelListener() {

        	public void onCancel(DialogInterface dialog) {
									
			}
        });*/
        
        update = false;
        runThread = new ConversationLoader(this, update, handler);
       
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
        if (MessageReceiver.myActivityStarted)
        {
        	if(conversations != null)
            {
            	runThread.setUpdate(true);
            }
            
            runThread.setStart(false);
            
            if (messageViewUpdate)
            {
                MessageView.updateList();
            }
        }
    }

    @Override
    protected void onResume()
    {
    	MessageService.mNotificationManager.cancel(MessageService.MULTI);
    	if(conversations != null || ap != null)
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
        
        runThread.setRunner(false);
        messageSender.setRunner(false);
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
		
	/**
	 * The handler class for cleaning up after the loading thread as well as the
	 * update thread.
	 */
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(final android.os.Message msg)
        {
        	if(msgList.isEmpty())
        	{
        		/*
        		 * The user has no contacts show the default list items to allow
        		 * for quick import or addition of contacts.
        		 */
        		List<String> emptyItems = new ArrayList<String>();
        		
        		emptyItems.add(ConversationView.this.getResources().getString(R.string.add_contacts_list));
        		emptyItems.add(ConversationView.this.getResources().getString(R.string.import_contacts_list));
        		
        		ap = new DefaultListAdapter(ConversationView.this, R.layout.empty_list_item, emptyItems);
                emptyList.setAdapter(ap);
        		emptyList.setVisibility(ListView.VISIBLE);
        		list.setVisibility(ListView.INVISIBLE);
        		ConversationView.this.dialog.dismiss();
        	}
        	else
        	{
        		//The user has contacts step-up the list view for the conversations
        		emptyList.setVisibility(ListView.INVISIBLE);
        		list.setVisibility(ListView.VISIBLE);
        		
	        	switch (msg.what){
	        	case LOAD:
	        		//First time load, the adapter must be constructed
	        		conversations = new ConversationAdapter(ConversationView.this, R.layout.listview_item_row, msgList);
	        		list.setAdapter(conversations);
	        		/*if(ConversationView.this.dialog.isShowing())
	        		{
	        			ConversationView.this.dialog.dismiss();
	        		}*/
		        	break;
	        	case UPDATE:
	        		//The list's data has changed and needs to be updated
	        		conversations.clear();
	                conversations.addData(msgList);
	        		break;
	        	}
        	}
        }
    };
}
