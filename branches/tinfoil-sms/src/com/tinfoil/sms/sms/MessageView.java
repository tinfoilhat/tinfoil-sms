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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
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
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;

/**
 * MessageView activity allows the user to view through all the messages from or
 * to the defined contact. selectedNumber will equal the contact that the
 * messages belong to. If a message is sent or received the list of messages
 * will be updated and Prephase3Activity's messages will be updated as well.
 */
public class MessageView extends Activity implements Runnable{
    private EditText messageBox;
    private static ListView list2;
    private static List<String[]> msgList2;
    private static MessageAdapter messages;
    private static MessageBoxWatcher messageEvent;
    private static final String[] options = new String[] { "Delete message", "Copy message", "Forward message" };
    private static String contact_name;
    private ArrayList<TrustedContact> tc;
    private static AutoCompleteTextView phoneBox;
    private AlertDialog popup_alert;
    private ProgressDialog dialog;
    private static ExchangeKey keyThread = new ExchangeKey();
    private DBAccessor loader;
    private boolean update = false;
    public static final int LOAD = 0;
    public static final int UPDATE = 1;
    

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
            this.finish();
        }

        this.setContentView(R.layout.messageviewer);
        MessageService.dba = new DBAccessor(this);
        ConversationView.messageViewActive = true;
        
        /*
         * Create a list of messages sent between the user and the contact
         */
        list2 = (ListView) this.findViewById(R.id.message_list);

        //This allows for the loading to be cancelled
        this.dialog = ProgressDialog.show(this, "Loading Messages",
                "Please wait...", true, true, new OnCancelListener() {

					public void onCancel(DialogInterface dialog) {
						MessageView.this.dialog.dismiss();
						MessageView.this.onBackPressed();						
					}        	
        });
        
        Thread loadThread = new Thread(this);
        loadThread.start();

        //Set an action for when a user clicks on a message        
        list2.setOnItemLongClickListener(new OnItemLongClickListener() {
        	public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
                final int item_num = position;

                final AlertDialog.Builder popup_builder = new AlertDialog.Builder(MessageView.this);
                popup_builder.setTitle(contact_name)
                        .setItems(options, new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int which) {

                                final String[] messageValue = (String[]) list2.getItemAtPosition(item_num);

                                //Toast.makeText(MessageView.this, messageValue[1], Toast.LENGTH_SHORT).show();
                                if (which == 0)
                                {
                                    //option = Delete
                                    MessageService.dba.deleteMessage(Long.valueOf(messageValue[3]));
                                    updateList();
                                }
                                else if (which == 1)
                                {
                                    //TODO implement
                                    //option = Copy message
                                    Toast.makeText(MessageView.this.getBaseContext(), "implement me", Toast.LENGTH_SHORT).show();
                                }
                                else if (which == 2)
                                {
                                	//TODO fix so that if the message is forwarded to a contact that is not in db the number is auto added
                                    //option = Forward message
                                    phoneBox = new AutoCompleteTextView(MessageView.this.getBaseContext());

                                    List<String> contact = null;
                                    if (MessageView.this.tc == null)
                                    {
                                    	//Do in thread.
                                        MessageView.this.tc = MessageService.dba.getAllRows(DBAccessor.ALL);
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
                                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MessageView.this.getBaseContext(), R.layout.auto_complete_list_item, contact);

                                    phoneBox.setAdapter(adapter);

                                    final AlertDialog.Builder contact_builder = new AlertDialog.Builder(MessageView.this);

                                    contact_builder.setTitle("Input contact number")
                                            .setCancelable(true)
                                            .setView(phoneBox)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {

                                                public void onClick(final DialogInterface dialog, final int which) {
                                                	
                                                	// TODO user SMSUtility.parseAutoComplete(...)
                                                    final String[] info = phoneBox.getText().toString().split(", ");

                                                    boolean invalid = false;
                                                    //TODO identify whether a forwarded message has a special format
                                                    if (info != null)
                                                    {

                                                        if (info.length == 2 && info[1] != null)
                                                        {
                                                            if (SMSUtility.isANumber(info[1]))
                                                            {                      
                                                            	//SMSUtility.sendMessage(getBaseContext(), info[1], messageValue[1]);
                                                            	MessageView.this.sendMessage(info[1],messageValue[1]);
                                                            }
                                                            else
                                                            {
                                                                invalid = true;
                                                            }
                                                        }
                                                        else
                                                        {
                                                            final String num = phoneBox.getText().toString();
                                                            if (SMSUtility.isANumber(num))
                                                            {
                                                            	MessageView.this.sendMessage(num,messageValue[1]);
                                                            	//SMSUtility.sendMessage(getBaseContext(), num, messageValue[1]);
                                                            }
                                                            else
                                                            {
                                                                invalid = true;
                                                            }
                                                        }
                                                    }

                                                    if (invalid)
                                                    {
                                                        Toast.makeText(MessageView.this.getBaseContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                            });
                                    final AlertDialog contact_alert = contact_builder.create();

                                    MessageView.this.popup_alert.cancel();
                                    contact_alert.show();
                                }
                            }
                        })
                        .setCancelable(true);
                MessageView.this.popup_alert = popup_builder.create();
                MessageView.this.popup_alert.show();
            			
				return false;
			}
        });

        
      
        /*
         * Reset the number of unread messages for the contact to 0
         */
        if (MessageService.dba.getUnreadMessageCount(ConversationView.selectedNumber) > 0)
        {
            //All messages are now read since the user has entered the conversation.
            MessageService.dba.updateMessageCount(ConversationView.selectedNumber, 0);
            if (MessageService.mNotificationManager != null)
            {
                MessageService.mNotificationManager.cancel(MessageService.SINGLE);
            }
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
    		
    		//Log.v("Message Time", ""+newMessage.getDate());
    		//Log.v("Message Time", ""+Message.millisToDate(newMessage.getDate()));
    		
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
            MessageService.dba.addMessageToQueue(number, text, false);

            if(MessageService.dba.isTrustedContact(number))
            {
            	MessageService.dba.addNewMessage(new Message(text, true, 
                		Message.SENT_ENCRYPTED), number, false);
            }
            else
            {
            	MessageService.dba.addNewMessage(new Message(text, true, 
                		Message.SENT_DEFAULT), number, false);
            }
            
            //Encrypt the text message before sending it	
            //SMSUtility.sendMessage(number, text, this.getBaseContext());
            
            //Start update thread
            update = true;
            Thread thread = new Thread(this);
            thread.start();            
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    /**
     * Update the list of messages shown when a new message is received or sent.
     */
    public static void updateList()
    {
        if (ConversationView.selectedNumber != null)
        {
        	//TODO do this in a thread.
            msgList2 = MessageService.dba.getSMSList(ConversationView.selectedNumber);
            messages.clear();
            messages.addData(msgList2);
            messages.notifyDataSetChanged();

            MessageService.mNotificationManager.cancel(MessageService.SINGLE);
            MessageService.dba.updateMessageCount(ConversationView.selectedNumber, 0);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        /* TODO add "manage received key exchange" as an option if the contact
         * has a pending key exchange. So that the user can handle key exchanges
         * from the list of messages. Also, if the user does not have their
         * shared secrets set and click to resolve the key exchange (and the
         * confirms they desire to complete the key exchange) then a pop-up will
         * show (just like when a user without set shared secrets attempts to
         * send a key exchange) prompting the user to set the contact's shared
         * secrets.
         */
        if(MessageService.dba.isTrustedContact(ConversationView.selectedNumber))
        {
        	menu.findItem(R.id.exchange).setTitle("Untrust Contact")
        		.setTitleCondensed("Untrust");
        }
        else
        {
        	menu.findItem(R.id.exchange).setTitle("Exchange Keys")
        		.setTitleCondensed("Exchange");
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

            	ExchangeKey.keyDialog = ProgressDialog.show(this, "Exchanging Keys",
                        "Exchanging. Please wait...", true, false);

                if (!MessageService.dba.isTrustedContact(SMSUtility.format
                        (ConversationView.selectedNumber)))
                {
                    keyThread.startThread(this, SMSUtility.format(ConversationView.selectedNumber), null);
                }
                else
                {
                    keyThread.startThread(this, null, SMSUtility.format(ConversationView.selectedNumber));
                }

                return true;
            case R.id.delete:
            	
                if(MessageService.dba.deleteMessage(ConversationView.selectedNumber))
                {
                	finish();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }


	public void run() {
		
		if(!update)
		{
			loader = new DBAccessor(this);
	        final boolean isTrusted = loader.isTrustedContact(ConversationView.selectedNumber);
	
	        messageEvent = new MessageBoxWatcher(this, R.id.word_count, isTrusted);
	        
			msgList2 = loader.getSMSList(ConversationView.selectedNumber);
			final int unreadCount = loader.getUnreadMessageCount(ConversationView.selectedNumber);
	
	        //Toast.makeText(this, String.valueOf(unreadCount), Toast.LENGTH_SHORT).show();
	        messages = new MessageAdapter(this, R.layout.listview_full_item_row, msgList2,
	                unreadCount);
	     
	        //Retrieve the name of the contact from the database
	        contact_name = loader.getRow(ConversationView.selectedNumber).getName();
	
	        //sendSMS = (Button) this.findViewById(R.id.send);
	        this.messageBox = (EditText) this.findViewById(R.id.message);
	
	        /*final InputFilter[] FilterArray = new InputFilter[1];
	
	        if (isTrusted)
	        {
	        	
	            FilterArray[0] = new InputFilter.LengthFilter(SMSUtility.ENCRYPTED_MESSAGE_LENGTH);
	        }
	        else
	        {
	            FilterArray[0] = new InputFilter.LengthFilter(SMSUtility.MESSAGE_LENGTH);
	        }
	
	        this.messageBox.setFilters(FilterArray);*/
	
	        this.messageBox.addTextChangedListener(messageEvent);
	        
	        this.handler.sendEmptyMessage(LOAD);
		}
		else
		{
			msgList2 = MessageService.dba.getSMSList(ConversationView.selectedNumber);
			MessageService.dba.updateMessageCount(ConversationView.selectedNumber, 0);
			update = false;
			this.handler.sendEmptyMessage(UPDATE);
		}
	}
	
	/**
	 * The handler class for cleaning up after the loading thread and the update
	 * thread.
	 */
	private final Handler handler = new Handler() {
        @Override
        public void handleMessage(final android.os.Message msg)
        {
        	switch (msg.what){
        	case LOAD:
	        	list2.setAdapter(messages);
	            list2.setItemsCanFocus(false);
	        	MessageView.this.dialog.dismiss();
	        	break;
        	case UPDATE:
        		messages.clear();
        		messages.addData(msgList2);
        		break;
        	}
        }
    };
}
