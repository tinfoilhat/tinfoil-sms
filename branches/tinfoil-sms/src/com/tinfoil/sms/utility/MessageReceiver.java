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

package com.tinfoil.sms.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.tinfoil.sms.crypto.KeyExchange;
import com.tinfoil.sms.dataStructures.Entry;
import com.tinfoil.sms.dataStructures.Message;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.crypto.Encryption;
import com.tinfoil.sms.sms.ConversationView;

public class MessageReceiver extends BroadcastReceiver {
	public static boolean myActivityStarted = false;
	public static final String VIBRATOR_LENTH = "500";
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	
    	Bundle bundle = intent.getExtras();
		if (bundle != null) {
			
			// This will put every new message into a array of
			// SmsMessages. The message is received as a pdu,
			// and needs to be converted to a SmsMessage, if you want to
			// get information about the message.
			
			//TODO extend this so that messages will be looped through.
			
			Object[] pdus = (Object[]) bundle.get("pdus");
			
			SmsMessage[] messages = new SmsMessage[pdus.length];
			StringBuilder mes = new StringBuilder();
			for (int i = 0; i < pdus.length; i++) {
				messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				mes.append(messages[i].getMessageBody());
			}
			Log.v("Message",mes.toString());
			
			//Log.v("message", messages[0].getMessageBody() + messages[1].getMessageBody());
			/*
			 * TODO account for multiple messages
			 * Might have to update this so that it accounts for multiple messages received
			 */
			if (messages.length > -1) {
				
				/* Shows a Toast with the phone number of the sender, and the message.
				 * String smsToast = "New SMS received from " +
				 * messages[0].getOriginatingAddress() + "\n'Test" +
				 * messages[0].getMessageBody() + "'";
				 */
				
				/*
				 * Checks if the database interface has been initialized and if tinfoil-sms's 
				 * preference interface has been dealt with
				 */
				if (MessageService.dba == null || ConversationView.sharedPrefs == null)
				{
					MessageService.dba = new DBAccessor(context);
					ConversationView.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
				}
				
				String address = messages[0].getOriginatingAddress();
				String secretMessage = null;
					    	
		    	/*
		    	 * Checks if the contact is in the database
		    	 */
				if (MessageService.dba.inDatabase(address))
				{
					
					/*
					 * Checks if the user has enabled the vibration option
					 */
					if (ConversationView.sharedPrefs.getBoolean("vibrate", true))
							//&& Prephase3Activity.sharedPrefs.getBoolean("notification_bar", true))
					{
						Vibrator vibrator;
						vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
						String value = ConversationView.sharedPrefs.getString("vibrate_length", VIBRATOR_LENTH);
						vibrator.vibrate(Long.valueOf(value));
					}

					//TrustedContact tcMess = MessageService.dba.getRow(ContactRetriever.format(address));
					
					/*
					 * Checks if the user is a trusted contact and if tinfoil-sms encryption is
					 * enabled.
					 */
					if (MessageService.dba.isTrustedContact((address)) && 
							ConversationView.sharedPrefs.getBoolean("enable", true)) {
						
						/*
						 * Since contact is trusted assume it is NOT a key exchange and that the message IS encrpyted.
						 * If the message fails to decrypt. A warning of possible Man-In-The-Middle attack is given. 
						 */
						try {
							
							/*
							 * Now send the decrypted message to ourself, set
							 * the source address of the message to the original
							 * sender of the message
							 */
							SMSUtility.sendToSelf(context, messages[0].getOriginatingAddress(), 
									messages[0].getMessageBody(), ConversationView.INBOX);
	
							//Updates the last message received
							Message newMessage = null;
							
							Log.v("Before Decryption", messages[0].getMessageBody());
							/*
							 * Checks if the user has set encrypted messages to be shown in
							 * messageView
							 */
							if (ConversationView.sharedPrefs.getBoolean("showEncrypt", true))
							{
								newMessage = new Message(messages[0].getMessageBody(), true, false);
								MessageService.dba.addNewMessage(newMessage, address, true);
							}
							
							Encryption CryptoEngine = new Encryption();
							
							Number contactNumber = MessageService.dba.getNumber(SMSUtility.format(address));
							
							secretMessage = CryptoEngine.decrypt(contactNumber, messages[0].getMessageBody());
							
							Log.v("After Decryption", secretMessage);
							
							MessageService.dba.updateDecryptNonce(contactNumber);
							
							/*secretMessage = Encryption.aes_decrypt(new String (MessageService.dba.getNumber
									(SMSUtility.format(address)).getPublicKey()), messages[0].getMessageBody());
							 */
							
							SMSUtility.sendToSelf(context, address,	
									secretMessage , ConversationView.INBOX);
							
							/*
							 * Store the message in the database
							 */
							newMessage = new Message(secretMessage, true, false);
							MessageService.dba.addNewMessage(newMessage, address, true);
						} 
						catch (Exception e) 
						{
							Toast.makeText(context, "FAILED TO DECRYPT", Toast.LENGTH_LONG).show();
							Toast.makeText(context, "Possible Man In The Middle Attack", Toast.LENGTH_LONG).show();
							e.printStackTrace();
						}
						
					}
					else
					{
						String message = messages[0].getMessageBody();
						Log.v("message", message);
												
						/*
						 * Since the user is not trusted, the message could be a key exchange
						 * Assume it is check for key exchange message
						 * Only once it fails is the message considered plain text.
						 * 
						 * TODO add an option to never expect a key exchange from a contact's number to improve performance
						 */
						//TODO implement actual key exchange check
						//TODO change the notification for a key exchange
						Number number = MessageService.dba.getNumber(SMSUtility.format(address));
						
						// TODO fix error, isKeyExchange returns true for plain text inputs.
						if(number.getKeyExchangeFlag() != Number.IGNORE &&
								KeyExchange.isKeyExchange(message))
						{
							///Number number = MessageService.dba.getNumber(SMSUtility.format(address));
							//if(ConversationView.sharedPrefs.getBoolean("auto_key_exchange", true))
							if(number.getKeyExchangeFlag() == Number.AUTO)
							{
								//Might be good to condense this into a method.
								
								//TODO fix error crashes given plain text information. (ArrayIndexOutOfBounds
								if(KeyExchange.verify(number, message))
								{
									Toast.makeText(context, "Exchange Key Message Received", Toast.LENGTH_SHORT).show();
									Log.v("Key Exchange", "Exchange Key Message Received");
									
									number.setPublicKey(KeyExchange.encodedPubKey(message));
									number.setSignature(KeyExchange.encodedSignature(message));
									
									MessageService.dba.updateNumberRow(number, number.getNumber(), 0);
									
									if(!number.isInitiator())
									{
										Log.v("Key Exchange", "Not Initiator");
										MessageService.dba.addMessageToQueue(number.getNumber(),
												KeyExchange.sign(number), true);
									}
								}
							}
							else
							{
								//TODO Add handling for multiple key exchange messages from a single contact
								MessageService.dba.addKeyExchangeMessage(
										new Entry(address, message));
							}
						}
						else
						{
							/*
							 * Send and store a plain text message to the contact
							 */
							SMSUtility.sendToSelf(context, address,
									message, ConversationView.INBOX);
							
							
							Message newMessage = new Message(message, true, false);
							MessageService.dba.addNewMessage(newMessage, address, true);
						}
						
					}
					
					/*
					 * Update the list of messages to show the new messages
					 */
					ConversationView.updateList(context, ConversationView.messageViewActive);
					
					/*
					 * Set the values needed for the notification
					 */
					MessageService.contentTitle = SMSUtility.format(address);
					if (secretMessage != null)
					{
						MessageService.contentText = secretMessage;
					}
					else
					{
						MessageService.contentText = messages[0].getMessageBody();
					}
					Intent serviceIntent = new Intent(context, MessageService.class);
					//ServiceConnection conn = new ServiceConnection() {};
					context.startService(serviceIntent);
					
					// Prevent other applications from seeing the message received
					this.abortBroadcast();
				}
					
			}
		}
    }
}