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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import com.tinfoil.sms.dataStructures.Entry;
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
    private static ListView list2;
    private static MessageAdapter messages;
    private static MessageBoxWatcher messageEvent;
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
    
    private DBAccessor dba;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Finds the number of the recently sent message attached to the notification
        if (this.getIntent().hasExtra(MessageService.notificationIntent))
        {
            ConversationView.selectedNumber = this.getIntent().getStringExtra
            		(MessageService.notificationIntent);
        }
        else if (this.getIntent().hasExtra(ConversationView.selectedNumberIntent))
        {
            ConversationView.selectedNumber = this.getIntent().getStringExtra
            		(ConversationView.selectedNumberIntent);
        }
        else 
        {
            finish();
        }
        
        // No number is provided
        if(ConversationView.selectedNumber != null)
        {
        	finish();
        }

        this.setContentView(R.layout.messageviewer);
        
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        dba = new DBAccessor(this);
        ConversationView.messageViewActive = true;
        
        /*
         * Create a list of messages sent between the user and the contact
         */
        list2 = (ListView) this.findViewById(R.id.message_list);

        //This allows for the loading to be cancelled
        /*this.dialog = ProgressDialog.show(this, "Loading Messages",
                "Please wait...", true, true, new OnCancelListener() {

					public void onCancel(DialogInterface dialog) {
						MessageView.this.dialog.dismiss();
						MessageView.this.onBackPressed();						
					}        	
        });*/
        
        runThread = new MessageLoader(this, false, handler);

        //Set an action for when a user clicks on a message        
        list2.setOnItemLongClickListener(new OnItemLongClickListener() {
        	public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
                final int item_num = position;

                final AlertDialog.Builder popup_builder = new AlertDialog.Builder(MessageView.this);
                popup_builder.setTitle(contact_name)
                        .setItems(MessageView.this.getResources()
                    		.getStringArray(R.array.sms_options),
                    			new DialogInterface.OnClickListener() {

                        	@SuppressLint({ "NewApi"})
                        	@TargetApi(11)
                            public void onClick(final DialogInterface dialog, final int which) {

                                final String[] messageValue = (String[]) list2.getItemAtPosition(item_num);

                                //Toast.makeText(MessageView.this, messageValue[1], Toast.LENGTH_SHORT).show();
                                //Log.v("message", messageValue[1]);
                                if (which == 0)
                                {
                                    //option = Delete
                                    dba.deleteMessage(Long.valueOf(messageValue[3]));
                                    updateList();
                                }
                                else if (which == 1)
                                {
                                	//option = Copy message
                                	//TODO look into not using the deprecated version of copy
                                	
                                	/*if(TinfoilSMS.threadable)
                                	{
                                		android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(MessageView.this.CLIPBOARD_SERVICE); 
                                		android.content.ClipData clip = android.content.ClipData.newPlainText("text label","text to clip");
                                	    clipboard.setPrimaryClip(clip);
                                	}
                                	else
                                	{*/
                                		android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                	    clipboard.setText(messageValue[1]);
                                	//}
                                    
                                    //Toast.makeText(MessageView.this.getBaseContext(), "implement me", Toast.LENGTH_SHORT).show();
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
                                            .setPositiveButton(R.string.forward_positive_button, new DialogInterface.OnClickListener() {

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

        /*
         * Reset the number of unread messages for the contact to 0
         */
        if (dba.getUnreadMessageCount(ConversationView.selectedNumber) > 0)
        {
            //All messages are now read since the user has entered the conversation.
            dba.updateMessageCount(ConversationView.selectedNumber, 0);
            if (MessageService.mNotificationManager != null)
            {
                MessageService.mNotificationManager.cancel(MessageService.SINGLE);
            }
        }       
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
            sendMessage(ConversationView.selectedNumber, text);
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
        if (ConversationView.selectedNumber != null)
        {        	
        	runThread.setUpdate(true);
        	runThread.setStart(false);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(dba.isTrustedContact(ConversationView.selectedNumber))
        {
        	menu.findItem(R.id.exchange)
        		.setTitle(R.string.untrust_contact_menu_full)
        		.setTitleCondensed(this.getString(R.string.untrust_contact_menu_short));
        }
        else
        {
        	if(dba.getKeyExchangeMessage(ConversationView.selectedNumber) != null)
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.message_view_menu, menu);
        
        return true;
	}

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exchange:

            	/*ExchangeKey.keyDialog = ProgressDialog.show(this, "Exchanging Keys",
                        "Exchanging. Please wait...", true, false);*/

                if (!dba.isTrustedContact(SMSUtility.format
                        (ConversationView.selectedNumber)))
                {
                	final Entry entry = dba.getKeyExchangeMessage(ConversationView.selectedNumber);
                	
                	if (entry != null)
                	{
                		final TrustedContact tc = dba.getRow(ConversationView.selectedNumber);
                		final Number number = tc.getNumber(ConversationView.selectedNumber);
                		
                		AlertDialog.Builder builder = new AlertDialog.Builder(this);
                		
                		builder.setMessage(this.getString(R.string.key_exchange_dialog_message)
                			+ " " + tc.getName() + ", " + number.getNumber() + "?")
                			.setCancelable(true)
                			.setPositiveButton(R.string.key_exchange_dialog_pos_button,
	         		    		new DialogInterface.OnClickListener() {
         		    	   @Override
         		    	   public void onClick(DialogInterface dialog, int id) {
         		            //Save the shared secrets
         		    		if(SMSUtility.checksharedSecret(number.getSharedInfo1()) &&
          							SMSUtility.checksharedSecret(number.getSharedInfo2()))
          					{
                      			KeyExchangeManager.respondMessage(MessageView.this, number, entry);
          					}
                      		else
                      		{
                      			KeyExchangeManager.setAndSend(MessageView.this, number, tc.getName(), entry);
                      		}
	         		    }})
	         		    .setNegativeButton(R.string.key_exchange_dialog_neg_button,
	         		    		new DialogInterface.OnClickListener() {
         		    	   @Override
         		    	   public void onClick(DialogInterface arg0, int arg1) {
         		    		   // Cancel the key exchange
         		    		   Toast.makeText(MessageView.this, MessageView.this
         		    				   .getString(R.string.key_exchange_cancelled), Toast.LENGTH_LONG).show();
         		    		   
         		    		   // Delete key exchange
         		    		   dba.deleteKeyExchangeMessage(number.getNumber());
         		    		   
         		    		  if(dba.getKeyExchangeMessageCount() == 0)
         		    		  {
      							  MessageService.mNotificationManager.cancel(MessageService.KEY);
         		    		  }
         		    	   }
	         		    });
                		
		         		AlertDialog alert = builder.create();
		         		//ExchangeKey.keyDialog.dismiss();
		         		alert.show();
                	}
                	else 
                	{
                		keyThread.startThread(this, SMSUtility.format(ConversationView.selectedNumber), null);
                	}
                }
                else
                {
                	keyThread.startThread(this, null, SMSUtility.format(ConversationView.selectedNumber));
                }

                return true;
            case R.id.delete:
            	
                if(dba.deleteMessage(ConversationView.selectedNumber))
                {
                	finish();
                }
                return true;

            case R.id.edit:
            	
            	AddContact.addContact = false;
                AddContact.editTc = dba.getRow(ConversationView.selectedNumber);

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
		        messageEvent = new MessageBoxWatcher(MessageView.this, R.id.word_count, b.getBoolean(MessageView.IS_TRUSTED));
		        messageBox = (EditText) MessageView.this.findViewById(R.id.message);
	        	messageBox.addTextChangedListener(messageEvent);
	        	messages = new MessageAdapter(MessageView.this, R.layout.listview_full_item_row, 
	        			(List<String[]>) b.get(MessageView.MESSAGE_LIST), b.getInt(MessageView.UNREAD_COUNT, 0));
	        	list2.setAdapter(messages);
	            list2.setItemsCanFocus(false);

	            /*
	             * Set the list to list from the bottom up and auto scroll to
	             * the bottom of the list
	             */
	            if(!sharedPrefs.getBoolean(QuickPrefsActivity.REVERSE_MESSAGE_ORDERING_KEY, false))
	            {
	            	list2.setStackFromBottom(true);
	            	list2.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
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
}
