package com.tinfoil.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.gsm.SmsManager;
import android.widget.Toast;

public class PrePhase0Activity extends Activity {

  /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
     // SMS RECEIVER
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
                                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                                
                                if (messages.length > -1)
                                {
                                    // Shows a Toast with the phone number of the sender,
                                    // and the message.
                                    String smsToast = "New SMS received from "
                                                    + messages[0].getOriginatingAddress() + "\n'Test"
                                                    + messages[0].getMessageBody() + "'";
                                    Toast.makeText(context, smsToast, Toast.LENGTH_LONG).show();
                                        
                                    /* 	Now send the decrypted message to ourself, set the source address
                                   		of the message to the original sender of the message
                                   	*/
                                    sendToSelf(messages[0].getOriginatingAddress(), Cipher.rot13(messages[0].getMessageBody()));
                                }
                        }
                        
                        // Prevent other applications from seeing the message received
                        this.abortBroadcast();
                        
            }
        };
        // The BroadcastReceiver needs to be registered before use.
        IntentFilter SMSfilter = new IntentFilter(SMS_RECEIVED);
        this.registerReceiver(SMSbr, SMSfilter);
    }
    
    private void sendToSelf(String srcNumber,String decMessage)
    {
    	ContentValues values = new ContentValues();
        values.put("address", srcNumber);
        values.put("body", decMessage);
        getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
    }
    
    /*private boolean checkSMS() {
        // Sets the sms inbox's URI
        Uri uriSMS = Uri.parse("content://sms");
        Cursor c = getBaseContext().getContentResolver().query(uriSMS, null,
                        "read = 0", null, null);
        // Checks the number of unread messages in the inbox
        if (c.getCount() == 0) {
                return false;
        } else
                return true;
    }*/
    
 
    
}