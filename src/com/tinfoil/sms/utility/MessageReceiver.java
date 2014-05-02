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

package com.tinfoil.sms.utility;

import org.strippedcastle.crypto.InvalidCipherTextException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.tinfoil.sms.R;
import com.tinfoil.sms.crypto.Encryption;
import com.tinfoil.sms.crypto.KeyExchange;
import com.tinfoil.sms.crypto.KeyExchangeHandler;
import com.tinfoil.sms.dataStructures.Entry;
import com.tinfoil.sms.dataStructures.Message;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.settings.QuickPrefsActivity;
import com.tinfoil.sms.sms.ConversationView;
import com.tinfoil.sms.sms.KeyExchangeManager;

public class MessageReceiver extends BroadcastReceiver {
	public static boolean myActivityStarted = false;
	public static boolean keyExchangeManual = false;
	public static boolean keyExchange = false;
	public static boolean invalidKeyExchange = false;
	public static final String VIBRATOR_LENTH = "500";
	private static OnKeyExchangeResolvedListener listener;
	
	private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	
	private DBAccessor dba;

	public void setOnKeyExchangeResolvedListener(
			OnKeyExchangeResolvedListener newListener) {
		listener = newListener;
	}

	public void removeOnKeyExchangeResolvedListener() {
		listener = null;
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		dba = new DBAccessor(context);
		
		Bundle bundle = intent.getExtras();
		
		if (intent.getAction().equals(ACTION_SMS_RECEIVED)
				&& bundle != null) {

			/*
			 *  This will put every new message into a array of
			 *  SmsMessages. The message is received as a pdu,
			 *  and needs to be converted to a SmsMessage, if you want to
			 *  get information about the message.
			 */
			Object[] pdus = (Object[]) bundle.getSerializable("pdus");

			if (pdus != null) {
				keyExchangeManual = false;
				SmsMessage[] messages = new SmsMessage[pdus.length];
				String fullMessage = "";
				
				//TODO handle mms data
				for (int i = 0; i < pdus.length; i++) {
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					fullMessage += messages[i].getMessageBody();
				}

				Log.v("Message", fullMessage.toString());

				if (messages.length > -1) {


					/*
					 * Checks if the database interface has been initialized and
					 * if tinfoil-sms's preference interface has been dealt with
					 */
					if (dba == null || ConversationView.sharedPrefs == null) {
						dba = new DBAccessor(context);
						ConversationView.sharedPrefs = PreferenceManager
								.getDefaultSharedPreferences(context);
					}

					final String address = messages[0].getOriginatingAddress();
					String secretMessage = null;
						
					/*
					 * Checks if the contact is in the database
					 */
					//TODO re-think this for 4.4 (if Tinfoil-SMS is default then it should prob. catch all messages).
					if (address != null && dba.inDatabase(address)) {

						handleNotifSound(context);

						invalidKeyExchange = false;
						
						secretMessage = handleEncryptedMessage(context, address, fullMessage);
						
						if(secretMessage == null)
						{
							Log.v("message", fullMessage);

							/*
							 * Since the user is not trusted, the message could
							 * be a key exchange. Assume it is check for key
							 * exchange message Only once it fails is the
							 * message considered plain text.
							 */
							if(!handleKeyExchange(context, address, fullMessage))
							{
								handlePlainMessage(context, address, fullMessage);
							}
						}

						/*
						 * Update the list of messages to show the new messages
						 */
						ConversationView.updateList(context,
								ConversationView.messageViewActive);

						handleNotification(context, address, fullMessage, secretMessage);

						// Prevent other applications from seeing the message
						// received
						this.abortBroadcast();
					}
				}
			}
		}
	}
	
	private boolean handleKeyExchange(Context context, final String address, final String fullMessage)
	{
		Number number = dba.getNumber(SMSUtility.format(address));
		if (number.getKeyExchangeFlag() != Number.IGNORE
				&& KeyExchange.isKeyExchange(fullMessage)) {

			if ((number.getKeyExchangeFlag() == Number.AUTO ||
					(number.getKeyExchangeFlag() == Number.MANUAL && number.isInitiator()))
					&& SMSUtility.checksharedSecret(number.getSharedInfo1())
					&& SMSUtility.checksharedSecret(number.getSharedInfo2())) {
				// Handle the key exchange received
				new KeyExchangeHandler(context, number, fullMessage, false) {

					@Override
					public void accept() {
						keyExchange = true;

						Toast.makeText(this.getContext(), R.string.key_exchange_received,
								Toast.LENGTH_SHORT).show();
						Log.v("Key Exchange", "Exchange Key Message Received");

						Log.v("S1", this.getNumber().getSharedInfo1());
						Log.v("S2", this.getNumber().getSharedInfo2());

						this.getNumber().setPublicKey(KeyExchange.encodedPubKey(this
								.getSignedPubKey()));
						this.getNumber().setSignature(KeyExchange.encodedSignature(this
								.getSignedPubKey()));

						dba.updateNumberRow(this.getNumber(), this
								.getNumber().getNumber(), 0);

						if (!this.getNumber().isInitiator()) {
							Log.v("Key Exchange", "Not Initiator");
							
							String keyMessage = KeyExchange.sign(this.getNumber(),
									dba, SMSUtility.user);
							dba.addMessageToQueue(getNumber().getNumber(),
									keyMessage, true);
							
							//Store the key exchange in message list (received key exchange, not initiator)
							Message receivedMessage = new Message(fullMessage,
									true, Message.RECEIVED_KEY_EXCHANGE_RESP);
							dba.addNewMessage(receivedMessage, address, true);
							Message newMessage = new Message(keyMessage,
									true, Message.SENT_KEY_EXCHANGE_RESP);
							dba.addNewMessage(newMessage, address, true);
						}
						else
						{
							//Store the key exchange in message list (received key exchange, initiator)
							Message newMessage = new Message(fullMessage,
									true, Message.RECEIVED_KEY_EXCHANGE_INIT);
							dba.addNewMessage(newMessage, address, true);
						}

						if(listener != null)
						{
							Log.v("onKeyExchangeResolved", "TRUE, RECEIVED");
							listener.onKeyExchangeResolved();
						}
						super.accept();
					}

					@Override
					public void invalid() {
						invalidKeyExchange = true;
						super.invalid();
					}

					public void store() {
						cancel();
					}

					public void cancel() {
						//Handle a key exchange that was initiated by contact however user has already initiated one.
						keyExchangeManual = true;
						Toast.makeText(this.getContext(), R.string.key_exchange_received, Toast.LENGTH_SHORT).show();

						Log.v("Key Exchange", "Manual");
						String result = dba.addKeyExchangeMessage(new Entry(address, this.getSignedPubKey()));
						
						//Store the key exchange in message list
						Message newMessage = new Message(fullMessage,
								true, Message.RECEIVED_KEY_EXCHANGE_INIT_RESP);
						dba.addNewMessage(newMessage, address, true);

						if (result != null) {
							Toast.makeText(this.getContext(), result, Toast.LENGTH_LONG).show();
						}

						KeyExchangeManager.updateList();

						MessageService.contentTitle = null;
						MessageService.contentText = null;

						Intent serviceIntent = new Intent(this.getContext(), MessageService.class);
						this.getContext().startService(serviceIntent);
						super.cancel();
					}

					@Override
					public void finishWith() {
					}
				};

				// Might be good to condense this into a
				// method.
				/*
				 * if(KeyExchange.verify(number, message)) {
				 * 
				 * } else { invalidKeyExchange = true; }
				 */
			} else {
				keyExchangeManual = true;
				Toast.makeText(context,	R.string.key_exchange_received, Toast.LENGTH_SHORT).show();

				Log.v("Key Exchange", "Manual");
				String result = dba.addKeyExchangeMessage(new Entry(address, fullMessage));
				
				//TODO determine which type of key exchange it is
				Message newMessage = new Message(fullMessage,
						true, Message.RECEIVED_KEY_EXCHANGE_INIT);
				dba.addNewMessage(newMessage, address, true);

				if (result != null) {
					Toast.makeText(context, result, Toast.LENGTH_LONG).show();
				}

				KeyExchangeManager.updateList();
			}
			
			return true;
		}
		return false;
	}
	
	private void handlePlainMessage(Context context, String address, String fullMessage)
	{
		/*
		 * Send and store a plain text message to the
		 * contact
		 */
		SMSUtility.sendToSelf(context, address,
				fullMessage, ConversationView.INBOX);

		Message newMessage = new Message(fullMessage,
				true, Message.RECEIVED_DEFAULT);
		dba.addNewMessage(newMessage, address, true);
	}
	
	private String handleEncryptedMessage(Context context, String address, String fullMessage)
	{
		String secretMessage = null;
		/*
		 * Checks if the user is a trusted contact and if
		 * tinfoil-sms encryption is enabled.
		 */
		if (dba.isTrustedContact((address))
				&& ConversationView.sharedPrefs.getBoolean(
						QuickPrefsActivity.ENABLE_SETTING_KEY,
						true)) {

			
			Message encryMessage = null;
			/*
			 * Since contact is trusted assume it is NOT a key
			 * exchange and that the message IS encrypted. If
			 * the message fails to decrypt. A warning of
			 * possible Man-In-The-Middle attack is given.
			 */
			try {

				/*
				 * Now send the decrypted message to ourself,
				 * set the source address of the message to the
				 * original sender of the message
				 */
				SMSUtility.sendToSelf(context,
						address, fullMessage, ConversationView.INBOX);

				// Updates the last message received
				Message newMessage = null;

				Log.v("Before Decryption", fullMessage);

				// Initialize the cryptographic engine if null
				if (SMSUtility.cryptoEngine == null)
				{
				    SMSUtility.cryptoEngine = new Encryption(SMSUtility.getUser(dba, null));
				}

				Number contactNumber = dba.getNumber(SMSUtility.format(address));

				secretMessage = SMSUtility.cryptoEngine.decrypt(contactNumber, fullMessage);

				Log.v("After Decryption", secretMessage);

				dba.updateDecryptNonce(contactNumber);

				/*
				 * Checks if the user has set encrypted messages
				 * to be shown in messageView
				 */
				if (ConversationView.sharedPrefs.getBoolean(QuickPrefsActivity
						.SHOW_ENCRYPT_SETTING_KEY, false)) {
					encryMessage = new Message(fullMessage, true, Message.RECEIVED_ENCRYPTED);
					dba.addNewMessage(encryMessage, address, true);
				}

				SMSUtility.sendToSelf(context, address,	secretMessage, ConversationView.INBOX);

				/*
				 * Store the message in the database
				 */
				newMessage = new Message(secretMessage, true, Message.RECEIVED_ENCRYPTED);
				dba.addNewMessage(newMessage, address, true);
				
			} catch (InvalidCipherTextException e) {
				
				encryMessage = new Message(fullMessage, true, Message.RECEIVED_ENCRYPT_FAIL);
				dba.addNewMessage(encryMessage, address, true);

				Toast.makeText(context, R.string.key_exchange_failed_to_decrypt,
						Toast.LENGTH_LONG).show();
				Toast.makeText(context,	R.string.possible_man_in_the_middle_attack_warning,
						Toast.LENGTH_LONG).show();
				
				e.printStackTrace();
				BugSenseHandler.sendExceptionMessage("Type",
						"Decrypt Message Error or Man In The Middle Attack", e);
			} catch (Exception e) {
				e.printStackTrace();
				BugSenseHandler.sendExceptionMessage("Type",
						"Message Receiver Error", e);
			}
		}
		
		return secretMessage;
	}
	
	private void handleNotifSound(Context context)
	{
		if(SMSUtility.checkDefault(context))
		{
			/*
			 * Checks if the user has enabled the vibration option
			 */
			if (ConversationView.sharedPrefs.getBoolean(
					QuickPrefsActivity.VIBRATE_SETTING_KEY, true)) {
				Vibrator vibrator;
				vibrator = (Vibrator) context
						.getSystemService(Context.VIBRATOR_SERVICE);
				String value = ConversationView.sharedPrefs
						.getString(
								QuickPrefsActivity.VIBRATE_LENGTH_SETTING_KEY,
								VIBRATOR_LENTH);
				vibrator.vibrate(Long.valueOf(value));
			}
	
			if (ConversationView.sharedPrefs.getBoolean(
					QuickPrefsActivity.RINGTONE_SETTING_KEY, false)) {
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				
				if(notification == null){ 
					notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);               
		         }
				
				Ringtone ringtone = RingtoneManager.getRingtone(context, notification);
				if(ringtone != null)
				{
					ringtone.play();
				}
			}
		}
	}
	
	private void handleNotification(Context context, String address, String message, String secretMessage)
	{
		if(SMSUtility.checkDefault(context))
		{
			// Check if the message was an invalid key exchange
			if (!invalidKeyExchange) {
				// Check if there should be a key exchange
				// notification
				if (!keyExchange) {
					if (!keyExchangeManual) {
						/*
						 * Set the values needed for the
						 * notification
						 */
						MessageService.contentTitle = SMSUtility
								.format(address);
						if (secretMessage != null) {
							MessageService.contentText = secretMessage;
						} else {
							MessageService.contentText = message;
						}
					} else {
						MessageService.contentTitle = null;
						MessageService.contentText = null;
					}
					Intent serviceIntent = new Intent(context,
							MessageService.class);

					context.startService(serviceIntent);
				}
			}
		}
	}
}