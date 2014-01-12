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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
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
import com.tinfoil.sms.utility.Walkthrough;
import com.tinfoil.sms.utility.Walkthrough.Step;

/**
 * SendMessageActivity is an activity that allows a user to create a new or
 * continue an old conversation. If the message is sent to a Trusted Contact (a
 * contact that has exchanged their key with the user) then it will be
 * encrypted. If the message is sent to a new contact a pop-up dialog will ask
 * the user if they would like to add the contact to tinfoil-sms's database. If
 * they user accepts AddContact will be started with addContact == true and
 * editTc != null
 */
public class SendMessageActivity extends Activity {
	
	private static MessageAdapter messages;
    private static MessageBoxWatcher messageEvent;
    private AutoCompleteTextView phoneBox;
    private EditText messageBox;
    private ImageButton sendSMS;
	private static ListView messageList;
	private AlertDialog popup_alert;
	
    public static final int TRUSTED = 0;
    public static final int RESOLVE = 1;
    public static final int UNTRUSTED = 2;
    
    public static final int LOAD = 0;
    public static final int UPDATE = 1;
    public static final int FINISH = -1;
    
    public static final String MESSAGE_LABEL = "Message";
    
    public static final String CONTACT_NAME = "contact_name";
    public static final String MESSAGE_LIST = "message_list";
    public static final String UNREAD_COUNT = "unread_count";
    public static final String IS_TRUSTED = "is_trusted";
	
    private ArrayList<TrustedContact> tc;
    private TrustedContact newCont;
    public static String selectedNumber;
    public static MessageLoader runThread;
    private static String contact_name;
        
    private DBAccessor dba;
    private static ExchangeKey keyThread = new ExchangeKey();
    public static SharedPreferences sharedPrefs;
    
    private int currentActivity = -1;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.new_message);
        
        setupActionBar();

        //String a = null;
        //Toast.makeText(this, a.length(), Toast.LENGTH_LONG).show();
        dba = new DBAccessor(this);
        
        if(this.getIntent().hasExtra(ConversationView.MESSAGE_INTENT))
        {
        	int intentValue = this.getIntent().getIntExtra(ConversationView.MESSAGE_INTENT, ConversationView.COMPOSE);
        	if(intentValue == ConversationView.MESSAGE_VIEW)
        	{
        		setupMessageView(null, null);
        	}
        	else
        	{
        		this.newCont = new TrustedContact();
                
                setupPhoneBox();
                
        		if(intentValue == ConversationView.COMPOSE)
        		{
        			this.setTitle(R.string.send_message);
                    setupMessageBox();                    
                    currentActivity = ConversationView.COMPOSE;
        		}
        		else if (intentValue == ConversationView.NEW_KEY_EXCHANGE)
        		{
        			setupKeyExchangeInterface();
        			this.setTitle(R.string.new_key_exchange);
        			currentActivity = ConversationView.NEW_KEY_EXCHANGE;
        		}
        		else
        		{
        			finish();
        		}
        	}
        }
        else
        {
            handleSendIntent();
        }

        // Set the send button as disabled until they enter text
        sendSMS = (ImageButton) this.findViewById(R.id.new_message_send);
        sendSMS.setEnabled(false);
        sendSMS.setClickable(false);
    }
    
    private void setupComposeView(String number, String message)
    {
    	this.newCont = new TrustedContact();
        
        setupPhoneBox();
        
        if(number != null)
        {
	        this.phoneBox.setText(number);
	        newCont.setName(number);
        }
        
        setupMessageBox();
        
        if(message != null)
        {
        	messageBox.setText(message);
        }
        
        currentActivity = ConversationView.COMPOSE;
    }
    
    private void setupMessageView(String number, String message)
    {
		this.setTitle(R.string.message);
		currentActivity = ConversationView.MESSAGE_VIEW;
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		setupMessageInterface();
        
		if(number == null)
		{
			handleNumberIntent();
		}
		else
		{
			selectedNumber = number;
		}
        
        setupMessageViewUI();
        
        if(message != null)
        {
        	messageBox.setText(message);
        }
        
        handleNotifications();
    }
    
    private void handleSendIntent()
    {
        Intent intent = this.getIntent();
        
        //Toast.makeText(this, ""+(Intent.ACTION_SENDTO.equals(intent.getAction())&& intent.getType() != null), Toast.LENGTH_LONG).show();
        
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            
            if ("text/plain".equals(intent.getType())) {
                
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                
                if (sharedText != null) {
                    setupComposeView(null, sharedText);
                }
            }
        }
        else if(Intent.ACTION_SENDTO.equals(intent.getAction())){
            
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            Uri uri = this.getIntent().getData();
            
            if(uri.getSchemeSpecificPart() != null){

                String number = uri.getSchemeSpecificPart();
                
                if(dba.inDatabase(number))
                {
                	setupMessageView(SMSUtility.format(number), sharedText);
                }
                else
                {
                	setupComposeView(SMSUtility.format(number), sharedText);
                }
            }
        }
    }
    
    private void setupPhoneBox()
    {
    	//Do in thread.
        tc = dba.getAllRows(DBAccessor.ALL);
        
        phoneBox = (AutoCompleteTextView)findViewById(R.id.new_message_number);
        List<String> contact;
        if (tc != null)
        {
            contact = SMSUtility.contactDisplayMaker(tc);
        }
        else
        {
            contact = null;
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.auto_complete_list_item, contact);
        
        phoneBox.setAdapter(adapter);

        phoneBox.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(final Editable s) {

                final String[] info = s.toString().split(", ");

                if (!info[0].trim().equals(""))
                {
                    if (info.length > 1 && !info[0].trim().equalsIgnoreCase(s.toString()))
                    {
                        SendMessageActivity.this.newCont.setName(info[0].trim());
                        SendMessageActivity.this.newCont.setNumber(info[1].trim());
                    }
                    else
                    {
                        if (SMSUtility.isANumber(info[0].trim()))
                        {
                            if (SendMessageActivity.this.newCont.isNumbersEmpty())
                            {
                                SendMessageActivity.this.newCont.addNumber(info[0].trim());
                            }
                            else
                            {
                                SendMessageActivity.this.newCont.setNumber(info[0].trim());
                            }
                        }
                    }
                }
            }

            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            }
        });
    }
    
    public void setupMessageBox()
    {
    	 messageEvent = new MessageBoxWatcher(this, R.id.new_message_send, R.id.send_word_count);
         messageBox = (EditText) this.findViewById(R.id.new_message_message);
         messageBox.addTextChangedListener(messageEvent);
    }
    
    public void sendMessage (View view)
    {
    	if(currentActivity == ConversationView.MESSAGE_VIEW)
    	{
    		String text = messageBox.getText().toString();
    	
	    	if(text != null && text.length() > 0)
	        {
	            sendMessage(selectedNumber, text);
	        }
    	}
    	else if(currentActivity == ConversationView.COMPOSE)
    	{
	    	String box =  SendMessageActivity.this.messageBox.getText().toString();
	    	String[] temp = checkValidNumber(this, newCont, box, true, true);
	    	
	    	if(temp != null)
	    	{
	    		String number = temp[0];
	    		String text = temp[1];
	    		/*
	             * Numbers are now automatically added upon sending a message to
	             * them (if they are not already in the database). The user can
	             * then go and edit their information as they please.
	             */
	        	
	        	//Add contact to the database
	        	if(!dba.inDatabase(number))
	        	{
	        		dba.addRow(new TrustedContact(new Number(number)));
	        	}
	        	
	        	//Add the message to the database
	        	if(dba.isTrustedContact(number))
	        	{
	        		dba.addNewMessage(new Message(text, true, Message.SENT_ENCRYPTED), number, true);
	        	}
	        	else
	        	{
	        		dba.addNewMessage(new Message(text, true, Message.SENT_DEFAULT), number, true);
	        	}
	
	            //Add the message to the queue to send it
	            dba.addMessageToQueue(number, text, false);         
	            
	            SendMessageActivity.this.messageBox.setText("");
	            SendMessageActivity.this.phoneBox.setText("");
	    	}  	
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
    
	public void setupMessageInterface() 
	{
		AutoCompleteTextView phone_box = (AutoCompleteTextView)findViewById(R.id.new_message_number);
		phone_box.setVisibility(AutoCompleteTextView.INVISIBLE);
	}
    
	public void setupKeyExchangeInterface() 
	{
		LinearLayout et = (LinearLayout)findViewById(R.id.new_message_field);
		
		et.setVisibility(LinearLayout.INVISIBLE);
		
		LinearLayout layout = (LinearLayout)findViewById(R.id.key_exchange_field);
		layout.setVisibility(LinearLayout.VISIBLE);
		
		//Button exchange = (Button)findViewById(R.id.key_exchange);
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
	
	            final AlertDialog.Builder popup_builder = new AlertDialog.Builder(SendMessageActivity.this);
	            popup_builder.setTitle(contact_name)
	                    .setItems(SendMessageActivity.this.getResources()
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
	                            	copyText(messageValue[1]);
	                            }
	                            else if (which == 2)
	                            {
	                                //option = Forward message
	                                phoneBox = new AutoCompleteTextView(SendMessageActivity.this.getBaseContext());
	
	                                List<String> contact = null;
	                                if (tc == null)
	                                {
	                                	//TODO Do in thread.
	                                	tc = dba.getAllRows(DBAccessor.ALL);
	                                }
	
	                                if (tc != null)
	                                {
	                                    if (contact == null)
	                                    {
	                                        contact = SMSUtility.contactDisplayMaker(tc);
	                                    }
	                                }
	                                else
	                                {
	                                    contact = null;
	                                }
	                                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
	                                		SendMessageActivity.this.getBaseContext(), 
	                                		R.layout.auto_complete_list_item, contact);
	
	                                phoneBox.setAdapter(adapter);
	
	                                final AlertDialog.Builder contact_builder = new AlertDialog.Builder(SendMessageActivity.this);
	
	                                contact_builder.setTitle(R.string.forward_title)
	                                        .setCancelable(true)
	                                        .setView(phoneBox)
	                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	
	                                            public void onClick(final DialogInterface dialog, final int which) {
	                                            	
	                                            	forward(messageValue[1]);
	                                            }
	
	                                        });
	                                final AlertDialog contact_alert = contact_builder.create();
	
	                                SendMessageActivity.this.popup_alert.cancel();
	                                contact_alert.show();
	                            }
	                        }
	                    }).setCancelable(true);
	            SendMessageActivity.this.popup_alert = popup_builder.create();
	            SendMessageActivity.this.popup_alert.show();
	        			
				return false;
			}
	    });
    }
	
	public void sendKeyExchange(View view)
	{
		final String[] temp = SendMessageActivity.checkValidNumber(this, newCont, null, false, true);
		
		if(temp != null)
		{
            // Show the tutorial for setting shared secrets
            if (! Walkthrough.hasShown(Step.SET_SECRET, this))
            {
                Walkthrough.showWithListener(Step.SET_SECRET, SendMessageActivity.this, 
                        new OnShowcaseEventListener() {
                            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                // Load the dialog to get the shared secrets
                                SMSUtility.handleKeyExchange(keyThread, dba, SendMessageActivity.this, temp[0]);
                            }
                            
                            @Override
                            public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                            }
                            
                            @Override
                            public void onShowcaseViewShow(ShowcaseView showcaseView) {
                            }
                        });
            }
            else
            {
                SMSUtility.handleKeyExchange(keyThread, dba, this, temp[0]);
            }	

			//TODO Give user feedback.
			//Toast.makeText(this, R.string.key_exchange_sent, Toast.LENGTH_SHORT).show();
			
			//finish();
		}
		else
		{
			//TODO Handle bad number
		}
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

    public boolean onCreateOptionsMenu(Menu menu) {

    	MenuInflater inflater = getMenuInflater();
    	
    	if(currentActivity == ConversationView.COMPOSE)
    	{
    		inflater.inflate(R.menu.new_message_menu, menu);
    	}
    	else if(currentActivity == ConversationView.MESSAGE_VIEW)
    	{
    		inflater.inflate(R.menu.message_view_menu, menu);
    	}
        
        if(currentActivity == ConversationView.NEW_KEY_EXCHANGE)
        {
        	return false;
        }
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        if(currentActivity == ConversationView.COMPOSE)
        {
	        String text =  SendMessageActivity.this.messageBox.getText().toString();
	        String[] values = checkValidNumber(this, newCont, text, false, false);
	        if(values != null)
	        {
	        	int ret = validNumber(dba, values[0]);
	        	
	        	if(ret == TRUSTED)
	        	{
		        	menu.findItem(R.id.exchange)
		        		.setTitle(R.string.untrust_contact_menu_full)
		        		.setTitleCondensed(this.getString(R.string.untrust_contact_menu_short))
		        		.setEnabled(true);
		        }
		        else if(ret == UNTRUSTED)
		        {
	        		menu.findItem(R.id.exchange)
	        			.setTitle(R.string.resolve_key_exchange_full)
	        			.setTitleCondensed(this.getString(R.string.resolve_key_exchange_short))
	        			.setEnabled(true);
		        }
	        	else if(ret == RESOLVE)
	        	{
	        		menu.findItem(R.id.exchange)
	        			.setTitle(R.string.exchange_key_full)
	        			.setTitleCondensed(this.getString(R.string.exchange_key_short))
	        			.setEnabled(true);
		        }
	        }
	        else
	        {
	        	menu.findItem(R.id.exchange).setEnabled(false);
	        }
        }
        else if(currentActivity == ConversationView.MESSAGE_VIEW)
        {
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
        }
        return true;
    }
    
    public static String[] checkValidNumber(Context context, TrustedContact newCont,
    		String text, boolean checkText, boolean showError)
    {
    	if (!newCont.getNumber().isEmpty()) {
            final String number = newCont.getNumber(0);
            
            if (number.length() > 0 && (!checkText || text.length() > 0))
            {
            	return new String[]{number, text};

            }
            else if(showError)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.insufficent_information_provided)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }

        }
    	else if(showError)
    	{
    		Toast.makeText(context, R.string.invalid_number_message, Toast.LENGTH_SHORT).show();
    	}
    	return null;
    }

    public static int validNumber(DBAccessor dba, String value)
    {
        if(dba.isTrustedContact(value))
        {
        	return TRUSTED;
        }
        else
        {
        	if(dba.getKeyExchangeMessage(value) != null)
        	{
        		return RESOLVE;
        	}
        	else
        	{
        		return UNTRUSTED;
        	}
        }
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
    
    /**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
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
        if(currentActivity == ConversationView.MESSAGE_VIEW)
        {
            ConversationView.messageViewActive = false;
            runThread.setRunner(false);
        }
        super.onDestroy();
    }


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
            	
            	if(currentActivity == ConversationView.COMPOSE)
            	{
            		//This is a bit of a redundant check
                	String text =  SendMessageActivity.this.messageBox.getText().toString();
                	String[] value = checkValidNumber(this, newCont, text, false, false);
                	if(value!= null) {
                		
                		//Add contact to the database
                    	if(!dba.inDatabase(value[0]))
                    	{
                    		dba.addRow(new TrustedContact(new Number(value[0])));
                    	}
                    	                	
                    	SMSUtility.handleKeyExchange(keyThread, dba, this, value[0]);
                	}
            	}
            	else if(currentActivity == ConversationView.MESSAGE_VIEW)
            	{
            		//TODO update list of messages once key exchange is sent 
            		SMSUtility.handleKeyExchange(keyThread, dba, this, selectedNumber);
            	}

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
	
	            Intent intent = new Intent(this, AddContact.class);
	            
	            startActivityForResult(intent, UPDATE);
            	return true;
            default:
            return super.onOptionsItemSelected(item);
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
            Toast.makeText(SendMessageActivity.this.getBaseContext(), R.string.invalid_number_message, Toast.LENGTH_SHORT).show();
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
		        contact_name = b.getString(SendMessageActivity.CONTACT_NAME);
		        messageEvent = new MessageBoxWatcher(SendMessageActivity.this, R.id.new_message_send, R.id.send_word_count);
		        messageBox = (EditText) SendMessageActivity.this.findViewById(R.id.new_message_message);
	        	messageBox.addTextChangedListener(messageEvent);
	        	messages = new MessageAdapter(SendMessageActivity.this, R.layout.listview_full_item_row, 
	        			(List<String[]>) b.get(SendMessageActivity.MESSAGE_LIST), b.getInt(SendMessageActivity.UNREAD_COUNT, 0));
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
        		messages.addData((List<String[]>) b.get(SendMessageActivity.MESSAGE_LIST));
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
