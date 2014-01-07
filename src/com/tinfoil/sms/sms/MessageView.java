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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tinfoil.sms.R;
import com.tinfoil.sms.adapter.MessageAdapter;
import com.tinfoil.sms.adapter.MessageBoxWatcher;
import com.tinfoil.sms.crypto.ExchangeKey;
import com.tinfoil.sms.dataStructures.Message;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.settings.AddContact;
import com.tinfoil.sms.settings.QuickPrefsActivity;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;

/**
 * MessageView activity allows the user to view through all the messages from or
 * to the defined contact. selectedNumber will equal the contact that the
 * messages belong to. If a message is sent or received the list of messages
 * will be updated and Prephase3Activity's messages will be updated as well.
 */
public class MessageView extends Activity {
    private EditText messageBox;
    private static ListView messageList;
    private static MessageAdapter messages;
    private static MessageBoxWatcher messageEvent;
    
    public static String selectedNumber;
    private static String contact_name;
    private ArrayList<TrustedContact> tc;
    private static AutoCompleteTextView phoneBox;
    private AlertDialog popup_alert;
    public static SharedPreferences sharedPrefs;    
    //private ProgressDialog dialog;
    private static ExchangeKey keyThread = new ExchangeKey();
    
    public static MessageLoader runThread;

    public static final int LOAD = 0;
    public static final int UPDATE = 1;
    public static final int FINISH = -1;
    
    public static final String CONTACT_NAME = "contact_name";
    public static final String MESSAGE_LIST = "message_list";
    public static final String UNREAD_COUNT = "unread_count";
    public static final String IS_TRUSTED = "is_trusted";
    
    public static final String MESSAGE_LABEL = "Message";
    
    private DBAccessor dba;
    
    //TODO merge MessageView and SendMessageActivity
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.new_message);
        
        setupActionBar();
        
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        dba = new DBAccessor(this);
        
        setupMessageInterface();        
        handleNumberIntent();        
        setupMessageViewUI();        
        handleNotifications();
        
    }
    
	public void setupMessageInterface() 
	{
		AutoCompleteTextView phone_box = (AutoCompleteTextView)findViewById(R.id.new_message_number);
		phone_box.setVisibility(AutoCompleteTextView.INVISIBLE);
	}

    private void handleNumberIntent()
    {
        //Finds the number of the recently sent message attached to the notification
        if (this.getIntent().hasExtra(MessageService.notificationIntent))
        {
            selectedNumber = this.getIntent().getStringExtra
            		(MessageService.notificationIntent);
        }
        else if (this.getIntent().hasExtra(ConversationView.selectedNumberIntent))
        {
            selectedNumber = this.getIntent().getStringExtra
            		(ConversationView.selectedNumberIntent);
        }
        else 
        {
            finish();
        }
        
        // No number is provided
        if(selectedNumber == null)
        {
        	finish();
        }
    }
    
    private void handleNotifications()
    {
    	/*	
         * Reset the number of unread messages for the contact to 0
         */
        if (dba.getUnreadMessageCount(selectedNumber) > 0)
        {
            //All messages are now read since the user has entered the conversation.
            dba.updateMessageCount(selectedNumber, 0);
            if (MessageService.mNotificationManager != null)
            {
                MessageService.mNotificationManager.cancel(MessageService.SINGLE);
            }
        }
    }
    
    private void setupMessageViewUI()
    {
    	//TODO fix this
    	ConversationView.messageViewActive = true;
    
	    /*
	     * Create a list of messages sent between the user and the contact
	     */
	    messageList = (ListView) this.findViewById(R.id.message_list);
	    
	    messageList.setVisibility(ListView.VISIBLE);
	
	    //This allows for the loading to be cancelled
	    /*this.dialog = ProgressDialog.show(this, "Loading Messages",
	            "Please wait...", true, true, new OnCancelListener() {
	
					public void onCancel(DialogInterface dialog) {
						MessageView.this.dialog.dismiss();
						MessageView.this.onBackPressed();						
					}        	
	    });*/
	    
	    runThread = new MessageLoader(selectedNumber, this, false, handler);
	
	    //Set an action for when a user clicks on a message        
	    messageList.setOnItemLongClickListener(new OnItemLongClickListener() {
	    	public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
	            final int item_num = position;
	
	            final AlertDialog.Builder popup_builder = new AlertDialog.Builder(MessageView.this);
	            popup_builder.setTitle(contact_name)
	                    .setItems(MessageView.this.getResources()
	                		.getStringArray(R.array.sms_options),
	                			new DialogInterface.OnClickListener() {
	
	                        public void onClick(final DialogInterface dialog, final int which) {
	
	                            final String[] messageValue = (String[]) messageList.getItemAtPosition(item_num);
	
	                            if (which == 0)
	                            {
	                                //option = Delete
	                                dba.deleteMessage(Long.valueOf(messageValue[3]));
	                                updateList();
	                            }
	                            else if (which == 1)
	                            {
	                            	//TODO test
	                            	copyText(messageValue[1]);
	                            }
	                            else if (which == 2)
	                            {
	                                //option = Forward message
	                                phoneBox = new AutoCompleteTextView(MessageView.this.getBaseContext());
	
	                                List<String> contact = null;
	                                if (MessageView.this.tc == null)
	                                {
	                                	//TODO Do in thread.
	                                    MessageView.this.tc = dba.getAllRows(DBAccessor.ALL);
	                                }
	
	                                if (MessageView.this.tc != null)
	                                {
	                                    if (contact == null)
	                                    {
	                                        contact = SMSUtility.contactDisplayMaker(MessageView.this.tc);
	                                    }
	                                }
	                                else
	                                {
	                                    contact = null;
	                                }
	                                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
	                                		MessageView.this.getBaseContext(), 
	                                		R.layout.auto_complete_list_item, contact);
	
	                                phoneBox.setAdapter(adapter);
	
	                                final AlertDialog.Builder contact_builder = new AlertDialog.Builder(MessageView.this);
	
	                                contact_builder.setTitle(R.string.forward_title)
	                                        .setCancelable(true)
	                                        .setView(phoneBox)
	                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	
	                                            public void onClick(final DialogInterface dialog, final int which) {
	                                            	
	                                            	forward(messageValue[1]);
	                                            }
	
	                                        });
	                                final AlertDialog contact_alert = contact_builder.create();
	
	                                MessageView.this.popup_alert.cancel();
	                                contact_alert.show();
	                            }
	                        }
	                    }).setCancelable(true);
	            MessageView.this.popup_alert = popup_builder.create();
	            MessageView.this.popup_alert.show();
	        			
				return false;
			}
	    });
    }
    
    /**
     * Forward the given message.
     * @param message The message that is going to be forwarded.
     */
    private void forward(String message)
    {
    	final String[] info = SMSUtility.parseAutoComplete(phoneBox.getText().toString());
    	String num = null;
        boolean invalid = false;

        if (info != null)
        {
        	if (info.length == 2 && info[1] != null)
            {
        		num = info[1];
                if (!SMSUtility.isANumber(info[1]))
                {              
                	invalid = true;
                }
            }
            else
            {
                num  = phoneBox.getText().toString();
                if (!SMSUtility.isANumber(num))
                {
                	 invalid = true;
                }
            }
        }

        if (invalid)
        {
            Toast.makeText(MessageView.this.getBaseContext(), R.string.invalid_number_message, Toast.LENGTH_SHORT).show();
        }
        else
        {
        	if(!dba.inDatabase(num))
        	{
        		dba.addRow(new TrustedContact(new Number(num)));
        	}
        	
        	if(dba.isTrustedContact(num))
        	{
        		dba.addNewMessage(new Message(message, true, Message.SENT_ENCRYPTED), num, true);
        	}
        	else
        	{
        		dba.addNewMessage(new Message(message, true, Message.SENT_DEFAULT), num, true);
        	}

            //Add the message to the queue to send it
            dba.addMessageToQueue(num, message, false);      
        }
    }

    /**
     * The onClick action for when the user clicks on the send message button
     * @param view The relavent view
     */
    public void sendMessage(View view)
    {
    	String text = MessageView.this.messageBox.getText().toString();
    	
    	if(text != null && text.length() > 0)
        {
            sendMessage(selectedNumber, text);
        }
    }
    
    /**
     * Take the message information and put the message in the queue.
     * @param number The number the message will be sent to
     * @param text The message content for the message
     */
    public void sendMessage(final String number, final String text)
    {
        if (number.length() > 0 && text.length() > 0)
        {
            //Sets so that a new message sent from the user will not show up as bold
            messages.setCount(0);
            this.messageBox.setText("");
            messageEvent.resetCount();
            dba.addMessageToQueue(number, text, false);

            if(dba.isTrustedContact(number))
            {
            	dba.addNewMessage(new Message(text, true, 
                		Message.SENT_ENCRYPTED), number, false);
            }
            else
            {
            	dba.addNewMessage(new Message(text, true, 
                		Message.SENT_DEFAULT), number, false);
            }
            
            //Encrypt the text message before sending it	
            //SMSUtility.sendMessage(number, text, this.getBaseContext());
            
            //Start update thread
            runThread.setUpdate(true);
            runThread.setStart(false);
        }
    }

    @Override
    protected void onResume()
    {
    	if(MessageService.mNotificationManager != null)
    	{
    		MessageService.mNotificationManager.cancel(MessageService.SINGLE);
    	}
        super.onResume();
    }

    /**
     * Update the list of messages shown when a new message is received or sent.
     */
    public static void updateList()
    {
        if (selectedNumber != null)
        {        	
        	runThread.setUpdate(true);
        	runThread.setStart(false);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(dba.isTrustedContact(selectedNumber))
        {
        	menu.findItem(R.id.exchange)
        		.setTitle(R.string.untrust_contact_menu_full)
        		.setTitleCondensed(this.getString(R.string.untrust_contact_menu_short));
        }
        else
        {
        	if(dba.getKeyExchangeMessage(selectedNumber) != null)
        	{
        		menu.findItem(R.id.exchange)
        			.setTitle(R.string.resolve_key_exchange_full)
        			.setTitleCondensed(this.getString(R.string.resolve_key_exchange_short));
        	}
        	else
        	{
        		menu.findItem(R.id.exchange)
        			.setTitle(R.string.exchange_key_full)
        			.setTitleCondensed(this.getString(R.string.exchange_key_short));
        	}
        }
        return true;
    }
    
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void copyText(String message) 
    {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		
    		android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); 
    		android.content.ClipData clip = android.content.ClipData.newPlainText(MESSAGE_LABEL, message);
    	    clipboard.setPrimaryClip(clip);
		}
    	else
    	{
    		
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    	    clipboard.setText(message);
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.message_view_menu, menu);
        
        return true;
	}

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        	case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
            case R.id.exchange:

            	//TODO update list of messages once key exchange is sent 
            	SMSUtility.handleKeyExchange(keyThread, dba, this, selectedNumber);

                return true;
            case R.id.delete:
            	
                if(dba.deleteMessage(selectedNumber))
                {
                	finish();
                }
                return true;

            case R.id.edit:
            	
            	AddContact.addContact = false;
                AddContact.editTc = dba.getRow(selectedNumber);

                Intent intent = new Intent(MessageView.this, AddContact.class);
                
                MessageView.this.startActivityForResult(intent, UPDATE);
            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if(resultCode == AddContact.UPDATED_NUMBER)
    	{	
	    	updateList();
    	}
    	/* Handle case where contact's number is deleted */
    	else if (resultCode == AddContact.DELETED_NUMBER)
    	{
    		finish();
    	}
    }

    @Override
    protected void onDestroy()
    {
    	ConversationView.messageViewActive = false;
	    runThread.setRunner(false);
	    super.onDestroy();
	}

	/**
	 * The handler class for cleaning up after the loading thread and the update
	 * thread.
	 */
	private final Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
		@Override
        public void handleMessage(final android.os.Message msg)
        {
        	Bundle b = msg.getData();
        	
        	switch (msg.what){
        	case LOAD:
		        contact_name = b.getString(MessageView.CONTACT_NAME);
		        messageEvent = new MessageBoxWatcher(MessageView.this, R.id.send_word_count);
		        messageBox = (EditText) MessageView.this.findViewById(R.id.new_message_message);
	        	messageBox.addTextChangedListener(messageEvent);
	        	messages = new MessageAdapter(MessageView.this, R.layout.listview_full_item_row, 
	        			(List<String[]>) b.get(MessageView.MESSAGE_LIST), b.getInt(MessageView.UNREAD_COUNT, 0));
	        	messageList.setAdapter(messages);
	            messageList.setItemsCanFocus(false);

	            /*
	             * Set the list to list from the bottom up and auto scroll to
	             * the bottom of the list
	             */
	            if(!sharedPrefs.getBoolean(QuickPrefsActivity.REVERSE_MESSAGE_ORDERING_KEY, false))
	            {
	            	messageList.setStackFromBottom(true);
	            	messageList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	            }

	        	break;
        	case UPDATE:
        		messages.clear();
        		messages.addData((List<String[]>) b.get(MessageView.MESSAGE_LIST));
        		messages.notifyDataSetChanged();
        		break;
        		
        	case FINISH:
        		messages.clear();
        		finish();
        		break;
        	}
        }
    };
 
    /**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
}