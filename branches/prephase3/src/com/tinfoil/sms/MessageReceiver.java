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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
				
				
		    	
				if (MessageService.dba == null)
				{
					//Sometimes the dba will not be initilized by the service this will catch it for those times
					MessageService.dba = new DBAccessor(context);
				}
				String address = messages[0].getOriginatingAddress();
				String secretMessage = null;
					    	
		    	//Check if contact is in db
				// Only expects encrypted messages from trusted contacts in the secure state
				if (MessageService.dba.inDatabase(address))
				{
					TrustedContact tcMess = MessageService.dba.getRow(ContactRetriever.format(address));
					if (MessageService.dba.isTrustedContact((address))) {
						//Toast.makeText(context,	"Encrypted Message Received", Toast.LENGTH_SHORT).show();
						//Toast.makeText(context,	messages[0].getMessageBody(), Toast.LENGTH_LONG).show();
						
						/*
						 * Now send the decrypted message to ourself, set
						 * the source address of the message to the original
						 * sender of the message
						 */
						try {
							Prephase3Activity.sendToSelf(context, messages[0].getOriginatingAddress(), 
									messages[0].getMessageBody(), Prephase3Activity.INBOX);
							
							/*Prephase3Activity.sendToSelf(context, messages[0].getOriginatingAddress(),	
									Encryption.aes_decrypt(Prephase3Activity.dba.getRow(ContactRetriever.format
									(address)).getPublicKey(), messages[0].getMessageBody()), Prephase3Activity.INBOX);
									*/
	
							secretMessage = Encryption.aes_decrypt(MessageService.dba.getRow(
									ContactRetriever.format(address)).getPublicKey(), 
									messages[0].getMessageBody());
							Prephase3Activity.sendToSelf(context, address,	
									secretMessage , Prephase3Activity.INBOX);
							
							//Updates the last message recieved
							
							/*Number newMessage = tcMess.getNumber(address);
							newMessage.setLastMessage(secretMessage);
							newMessage.setDate();
							newMessage.addUnreadMessageCount();
							MessageService.dba.updateLastMessage(newMessage);*/
							
							/*Message newMessage = new Message(secretMessage,true);
							MessageService.dba.updateLastMessage(newMessage, address);*/
							
							Message newMessage = new Message(messages[0].getMessageBody(),true);
							MessageService.dba.addNewMessage(newMessage, address, false);
							
							newMessage = new Message(secretMessage,true);
							MessageService.dba.addNewMessage(newMessage, address, true);
							
							Prephase3Activity.updateList(context, true);
							//Toast.makeText(context, "Message Decrypted", Toast.LENGTH_SHORT).show();
						} 
						catch (Exception e) 
						{
							Toast.makeText(context, "FAILED TO DECRYPT", Toast.LENGTH_LONG).show();
							e.printStackTrace();
						}
					}
					else
					{
						//Toast.makeText(context, "Message Received", Toast.LENGTH_LONG).show();
						//Toast.makeText(context, messages[0].getMessageBody(), Toast.LENGTH_LONG).show();
						Prephase3Activity.sendToSelf(context, address,
								messages[0].getMessageBody(), Prephase3Activity.INBOX);
						
						
						//Number newMessage = new Number (address, messages[0].getMessageBody());
						/*Number newMessage = tcMess.getNumber(address);
						newMessage.setLastMessage(messages[0].getMessageBody());
						newMessage.setDate();
						newMessage.addUnreadMessageCount();
						MessageService.dba.updateLastMessage(newMessage);*/
						
						/*Message newMessage = new Message(messages[0].getMessageBody(),true);
						MessageService.dba.updateLastMessage(newMessage, address);*/
						
						Message newMessage = new Message(messages[0].getMessageBody(),true);
						MessageService.dba.addNewMessage(newMessage, address);
						
						Prephase3Activity.updateList(context, true);
					}
					
					MessageService.contentTitle = ContactRetriever.format(address);
					if (secretMessage != null)
					{
						MessageService.contentText = secretMessage;
					}
					else
					{
						MessageService.contentText = messages[0].getMessageBody();
					}
					Intent serviceIntent = new Intent(context, MessageService.class);
					context.startService(serviceIntent);
					this.abortBroadcast();
				}
					
			}
		}

		// Prevent other applications from seeing the message received			
		//this.abortBroadcast();
	}
}