package com.tinfoil.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class MessageReceiver extends BroadcastReceiver {
	public static boolean myActivityStarted = false;
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	//Toast.makeText(context, "MY BOOT", Toast.LENGTH_LONG).show();
    	//Intent i = new Intent(context, Prephase3Activity.class);
    	//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	//context.startActivity(i);
    	
    	// Called every time a new sms is received
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				
				// This will put every new message into a array of
				// SmsMessages. The message is received as a pdu,
				// and needs to be converted to a SmsMessage, if you want to
				// get information about the message.
				Object[] pdus = (Object[]) bundle.get("pdus");
				final SmsMessage[] messages = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; i++) {
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				}

				if (messages.length > -1) {
					
					/* Shows a Toast with the phone number of the sender, and the message.
					 * String smsToast = "New SMS received from " +
					 * messages[0].getOriginatingAddress() + "\n'Test" +
					 * messages[0].getMessageBody() + "'";
					 */

					String address = messages[0].getOriginatingAddress();
											
					// Only expects encrypted messages from trusted contacts in the secure state
					if (Prephase3Activity.dba.isTrustedContact((address))) {
						Toast.makeText(context,	"Encrypted Message Received", Toast.LENGTH_SHORT).show();
						Toast.makeText(context,	messages[0].getMessageBody(), Toast.LENGTH_LONG).show();
						
						/*
						 * Now send the decrypted message to ourself, set
						 * the source address of the message to the original
						 * sender of the message
						 */
						try {
							Prephase3Activity.sendToSelf(context, messages[0].getOriginatingAddress(), 
									messages[0].getMessageBody(), Prephase3Activity.INBOX);
							Prephase3Activity.sendToSelf(context, messages[0].getOriginatingAddress(),	
									Encryption.aes_decrypt(Prephase3Activity.dba.getRow(ContactRetriever.format
									(address)).getPublicKey(), messages[0].getMessageBody()), Prephase3Activity.INBOX);
							Prephase3Activity.updateList(context);
							Toast.makeText(context, "Message Decrypted", Toast.LENGTH_SHORT).show();
						} 
						catch (Exception e) 
						{
							Toast.makeText(context, "FAILED TO DECRYPT", Toast.LENGTH_LONG).show();
							e.printStackTrace();
						}
					}
					else
					{
						Toast.makeText(context, "Message Received", Toast.LENGTH_LONG).show();
						Toast.makeText(context, messages[0].getMessageBody(), Toast.LENGTH_LONG).show();
						Prephase3Activity.sendToSelf(context, messages[0].getOriginatingAddress(),
								messages[0].getMessageBody(), Prephase3Activity.INBOX);
						//Prephase3Activity.updateList(context);
					}
				}
			}

			// Prevent other applications from seeing the message received			
			this.abortBroadcast();
			
    	}
}