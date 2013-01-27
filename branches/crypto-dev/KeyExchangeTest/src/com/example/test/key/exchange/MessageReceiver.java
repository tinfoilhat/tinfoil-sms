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

package com.example.test.key.exchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

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
			Object[] pdus = (Object[]) bundle.get("pdus");
			final SmsMessage[] messages = new SmsMessage[pdus.length];
			for (int i = 0; i < pdus.length; i++) {
				messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			}

			/*
			 * Might have to update this so that it accounts for multiple messages received
			 */
			if (messages.length > -1) {
				

				String address = messages[0].getOriginatingAddress();
				String message = messages[0].getMessageBody();
				
				//1. Check if number is in db
				//	1.1. Check if contact is trusted
				//		1.1.1. Decrypt Message
				//			1.1.1.1. Possible Man in the Middle Attack
				//		... (and all the rest)
				//	1.2. Check if message is key exchange
				//		1.2.1. Continue key exchange
				//		1.2.2. Add to trusted and so on..
				//	1.3. Regular message received

				/*
				 * For this test the following assumptions are made:
				 * 1. Every message received come from a contact that is within db (there is no check or db for that matter)
				 * 2. No contact is trusted (even upon key exchange they not actually set as trusted)
				 * 3. Messages are either plain text or key exchanges
				 * 4. Key exchange is automatically accepted 
				 */
				
				/*
				 * TODO determine the exact specifications of the key exchange message
				 * 	aka what format it is as well as how to validate that it is a key exchange messages
				 * TODO implement validation for whether a message is a key exchange message
				 */
		    	
				Toast.makeText(context, "Message Received", Toast.LENGTH_SHORT).show();
					
				// Prevent other applications from seeing the message received
				this.abortBroadcast();
				
					
			}
		}
    }
}