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


package com.tinfoil.sms;

import java.util.List;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Prephase1Activity extends Activity {
	Button sendSMS;
	EditText phoneBox;
    EditText messageBox;
    TextView tv;
    static DBAccessor dba;
    static SharedPreferences sharedPrefs;
    
    //Change the password here or give a user possibility to change it
    //private static final byte[] PASSWORD = new byte[]{ 0x20, 0x32, 0x34, 0x47, (byte) 0x84, 0x33, 0x58 };
    private static final String PASSWORD = "test123";

    /** Called when the activity is first created. */
    //@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        dba = new DBAccessor(this);
        
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        
        //String actualNumber = "5555215556";
        //dba.addRow("billy", actualNumber, "12345", 2);
        
        sendSMS = (Button) findViewById(R.id.send);
        phoneBox = (EditText) findViewById(R.id.phoneNum);
        messageBox = (EditText) findViewById(R.id.message);
        sendSMS.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v) 
			{
		        String number = phoneBox.getText().toString();
				String text = messageBox.getText().toString();
				
				if (number.length() > 0 && text.length() > 0)
				{
					//Toast.makeText(getBaseContext(), "Message sending", Toast.LENGTH_SHORT).show();

					//Encrypt the text message before sending it	
					try
					{
                    	//Only expects encrypted messages from trusted contacts in the secure state
						if (dba.isTrustedContact(number) && sharedPrefs.getBoolean("enable", true))
						{
							sendSMS(number, Encryption.aes_encrypt(PASSWORD, text));
							Toast.makeText(getBaseContext(), "Encrypted Message sent", Toast.LENGTH_SHORT).show();
						}
						else
						{
							sendSMS(number, text);
							Toast.makeText(getBaseContext(), "Message sent", Toast.LENGTH_SHORT).show();
						}
						
						messageBox.setText("");
						phoneBox.setText("");
				    }
			        catch ( Exception e ) 
			        { 
			        	Toast.makeText(getBaseContext(), "FAILED TO SEND", Toast.LENGTH_LONG).show();
			        	e.printStackTrace(); 
			    	}
				}
				else
				{
					Toast.makeText(getBaseContext(), 
							"You have failed to provide sufficient information", Toast.LENGTH_SHORT).show();
				}
				
			}
        });
      
        final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
        BroadcastReceiver SMSbr = new BroadcastReceiver() {
         
                @Override
                public void onReceive(Context context, Intent intent) {
                        // Called every time a new sms is received
                        Bundle bundle = intent.getExtras();
                        if (bundle != null) {
                                // This will put every new message into a array of
                                // SmsMessages. The message is received as a pdu,
                                // and needs to be converted to a SmsMessage, if you want to
                                // get information about the message.
                                Object[] pdus = (Object[]) bundle.get("pdus");
                                final SmsMessage[] messages = new SmsMessage[pdus.length];
                                for (int i = 0; i < pdus.length; i++)
                                {
                                	messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                                }
                                
                                if (messages.length > -1)
                                {
                                    // Shows a Toast with the phone number of the sender,
                                    // and the message.
                                    /*String smsToast = "New SMS received from "
                                                    + messages[0].getOriginatingAddress() + "\n'Test"
                                                    + messages[0].getMessageBody() + "'";*/
                                	
                                	//TrustedContact tc = dba.getRow(messages[0].getOriginatingAddress());
                					//Toast.makeText(getBaseContext(),tc.getName() + "\n" + tc.getNumber() + "\n" + tc.getKey() +"\n"+ tc.getVerified(), Toast.LENGTH_SHORT).show();
                                	//Toast.makeText(getBaseContext(),messages[0].getOriginatingAddress(), Toast.LENGTH_SHORT).show();
                                	//Only expects encrypted messages from trusted contacts in the secure state
                                	String mess = messages[0].getOriginatingAddress();
                                	mess = mess.substring(1,mess.length());
                                	//Toast.makeText(getBaseContext(),mess, Toast.LENGTH_SHORT).show();
                                	if (dba.isTrustedContact(mess))
            						{
	                                	Toast.makeText(context, "Encrypted Message Received", Toast.LENGTH_LONG).show();
	                                    Toast.makeText(context, messages[0].getMessageBody(), Toast.LENGTH_LONG).show();
	                                    
	                                    
	                                    /* 	Now send the decrypted message to ourself, set the source address
	                                   		of the message to the original sender of the message
	                                   	*/
	                                    try
	                                    {
	                                    	sendToSelf(	messages[0].getOriginatingAddress(), 
	                                    				Encryption.aes_decrypt(PASSWORD, messages[0].getMessageBody()));
	                                    	Toast.makeText(context, "Message Decrypted", Toast.LENGTH_LONG).show();
	                                    	//openInbox();
	                                    }
		            			        catch ( Exception e ) 
		            			        { 
		            			        	Toast.makeText(context, "FAILED TO DECRYPT", Toast.LENGTH_LONG).show();
		            			        	e.printStackTrace(); 
		            			    	}
            						}
                                	else
                                	{
                                		Toast.makeText(context, "Message Received", Toast.LENGTH_LONG).show();
	                                    Toast.makeText(context, messages[0].getMessageBody(), Toast.LENGTH_LONG).show();
                                	}
                                }
                        }
                        
                        // Prevent other applications from seeing the message received
                        //this.abortBroadcast();
                        
            }
        };
        // The BroadcastReceiver needs to be registered before use.
        IntentFilter SMSfilter = new IntentFilter(SMS_RECEIVED);
        this.registerReceiver(SMSbr, SMSfilter);

    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	 
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.texting_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	        case R.id.add:
	        startActivity(new Intent(this, AddContact.class));
	        return true;
	        case R.id.settings:
	        startActivity(new Intent(this, QuickPrefsActivity.class));
	        return true;
	        case R.id.message:
		    startActivity(new Intent(this, MessageView.class));
		    return true;
	        default:
	        return super.onOptionsItemSelected(item);
    	}
     
    }
      
    /**
     * Sends the given message to the phone with the given number
     * @param number : String, the number of the phone that the message is sent to
     * @param message : String, the message, encrypted that will be sent to the contact
     */
    public void sendSMS (String number, String message)
    {
    	PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, Object.class), 0);
        SmsManager sms = SmsManager.getDefault();
        
        //this is the function that does all the magic
        sms.sendTextMessage(number, null, message, pi, null);
    	
    }
    
    /**
     * Drops the message into the in-box of the default SMS program.
     * Tricks the in-box to think the message was send by the original sender
     * @param srcNumber : String, the number of the contact that sent the message
     * @param decMessage : String, the message sent from the contact
     */
    private void sendToSelf(String srcNumber,String decMessage)
    {
    	ContentValues values = new ContentValues();
        values.put("address", srcNumber);
        values.put("body", decMessage);
        getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
    }
    
    /**
     * Opens up the default text messaging in-box, to display the message.
     */
    public void openInbox() {
        String application_name = "com.android.mms";
        try {
	        Intent intent = new Intent("android.intent.action.MAIN");
	        intent.addCategory("android.intent.category.LAUNCHER");
	
	        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	        List<ResolveInfo> resolveinfo_list = this.getPackageManager()
	        .queryIntentActivities(intent, 0);

	        for (ResolveInfo info : resolveinfo_list) {
	        	if (info.activityInfo.packageName.equalsIgnoreCase(application_name)) {
	        		launchComponent(info.activityInfo.packageName,
	        				info.activityInfo.name);
	        		break;
	        	}
	        }
        } 
    	catch (ActivityNotFoundException e) {
    		Toast.makeText(this.getApplicationContext(), 
    		"There was a problem loading the application: "
    				+ application_name, Toast.LENGTH_SHORT).show();
    	}
    }

    private void launchComponent(String packageName, String name) {
	    Intent launch_intent = new Intent("android.intent.action.MAIN");
	    launch_intent.addCategory("android.intent.category.LAUNCHER");
	    launch_intent.setComponent(new ComponentName(packageName, name));
	    launch_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    this.startActivity(launch_intent);
    }
}
