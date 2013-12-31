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

package com.tinfoil.sms.dataStructures;

import java.io.Serializable;
import java.util.Calendar;

/**
 * A class used to store information from the message table
 */
public class Message implements Serializable {
	
	/**
	 * Determines if a de-serialized file is compatible with this class.
     *
     * Maintainers must change this value if and only if the new version
     * of this class is not compatible with old versions. See Sun docs
     * for <a href=http://java.sun.com/products/jdk/1.1/docs/guide
     * /serialization/spec/version.doc.html> details. </a>
	 */
	private static final long serialVersionUID = -5145840563250998474L;
		
	private static final boolean clockStyle = true;
	private String message;
	private long date;
	private int sent;
	
	/**
	 * Possible states:
	 * 0. Sent (no encryption)
	 * 1. Sent (encrypted)
	 * 2. Sent (encrypted and obfuscated)
	 * 3. Received (not encrypted, not expecting encryption)
	 * 4. Received (encrypted (implies that they were expecting it to be
	 * encrypted and it was successfully able to decrypt it))
	 * 5. Received (HashMac failed, expecting encrypted but failed to decrypt)
	 * 6. Received (encrypted and obfuscated (implying again that they were
	 * expecting it such and succeeded in transforming it back to plain text))
	 * 7. Received (de-obfuscation failed (not sure if this is possible))
	 * 8. Received (de-obfuscated and failed to decrypt, Hash-Mac failed after
	 * de-obfuscation (not sure about this one either))
	 * 9. Sent a key exchange to the contact and was the initiator
	 * 10. Sent a key exchange to the contact and was not the initiator
	 * 11. Received a key exchange from the contact and was the initiator
	 * 12. Received a key exchange from the contact and was not the initiator
	 */
	public static final int SENT_DEFAULT = 0; 
	public static final int SENT_ENCRYPTED = 1;
	public static final int SENT_ENC_OBF = 2;
	public static final int RECEIVED_DEFAULT = 3;
	public static final int RECEIVED_ENCRYPTED = 4;
	public static final int RECEIVED_ENCRYPT_FAIL = 5;
	public static final int RECEIVED_ENC_OBF = 6; 
	public static final int RECEIVED_OBF_FAIL = 7;
	public static final int RECEIVED_ENC_OBF_FAIL = 8;
	public static final int SENT_KEY_EXCHANGE_INIT = 9;
	public static final int SENT_KEY_EXCHANGE_RESP = 10;
	public static final int RECEIVED_KEY_EXCHANGE_INIT = 11;
	public static final int RECEIVED_KEY_EXCHANGE_RESP = 12;
	
	
	/**
	 * A class for storing messages retrieved or to be stored in the database. 
	 * 
	 * @param message The body of the message
	 * @param date The date the message was sent or received 
	 * @param type Whether the message is send or received, 1 means received,
	 * 2 means sent. 
	 * *Please Note These values are based of the native application's send and
	 * received flags.
	 */
	public Message (String message, long date, int type)
	{
		this.setMessage(message);
		this.setDate(date);
		
		// Set the messages to default sent or received
		if (type == 2)
		{
			this.setSent(SENT_DEFAULT);
		}
		else if (type == 1)
		{
			this.setSent(RECEIVED_DEFAULT);
		}
	}
	
	/**
	 * A class for storing messages retrieved or to be stored in the database. 
	 * 
	 * @param message The body of the message
	 * @param currentTime Whether the date is set to the current time or not. 
	 * If true date is set to current time, otherwise date is set to 0
	 * @param sent Whether the message was sent or received. If sent = true then
	 * the message was sent otherwise the message was received
	 */
	public Message (String message, boolean currentTime, int sent)
	{
		this.setMessage(message);
		if (currentTime)
		{
			this.setDate();
		}
		else
		{
			this.setDate(0);
		}
		this.setSent(sent);
	}

	/**
	 * Get the message's body
	 * 
	 * @return The message's body
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the message's body
	 * 
	 * @param message The new message body
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 
	 * Get the date the message was sent or received
	 * @return The date
	 */
	public long getDate() {
		return date;
	}

	/**
	 * Set the date the message was sent or received.
	 * @param date The new date the message was sent
	 */
	private void setDate(long date) {
		this.date = date;
	}
	
	/**
	 * Set the date the message was sent or received to the current time.
	 */
	private void setDate() {
		
		Calendar calendar = Calendar.getInstance();
		this.date = calendar.getTimeInMillis();
	}
	
	/**
	 * TODO update this to use locale
	 * Convert the current time in milliseconds to the current time formated as:
	 * YYYY/MM/DD HH:MM AM/PM
	 * @param currentTime The current time in milliseconds
	 * @return The current time formated.
	 */
	public static String millisToDate(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        
        calendar.setTimeInMillis(currentTime);
        String date = calendar.get(Calendar.YEAR) + "/" +  (calendar.getTime().getMonth()+1) + "/" + 
        		calendar.getTime().getDate();
        
        if (clockStyle && calendar.getTime().getHours() > 12)
        {
        	date += " " + (calendar.getTime().getHours()-12);
        }
        else
        {
        	date += " " + calendar.getTime().getHours() ;
        }
        
        String minutes = ""+calendar.getTime().getMinutes();
        if (minutes.length() < 2)
        {
        	minutes = "0" + minutes;
        }
        date += ":" + minutes;

        if (calendar.get(Calendar.AM_PM) == 1)
        {
        	date += " PM";
        }
        else {
        	date += " AM";
        }
        return date;
    }
	
	/**
	 * Check whether the message was sent or received
	 * @return If the message was sent it will return true, otherwise false.
	 */
	public boolean isSent() {
		if (sent == SENT_DEFAULT || sent == SENT_ENCRYPTED || sent == SENT_ENC_OBF)  
		{
			return true;
		}
		return false;
	}
	
	/** 
	 * If the 'sent' flag that is already in use was used then all flags that relate to sending messages are mapped to the native sent flag
	 * 1, 2, 3 -> send = 2
	 * Otherwise map to a message being received
	 * 4, 5, 6, 7, 8, 9 -> sent = 1
	 */
	
	/**
	 * Get the send flag in terms of the native android messaging application
	 * this is more used to save messages back to the native application
	 * @return The flag of whether the message was sent or received. If the
	 * message was sent the return will be 2, otherwise the return will be 1.
	 */
	public int getSent() {
		return sent;
	}
	
	public int getNativeSent()
	{
		if (sent == SENT_DEFAULT || sent == SENT_ENCRYPTED || sent == SENT_ENC_OBF)
		{
			return 2;
		}
		return 1;
	}

	/**
	 * Set whether the message is sent or received
	 * @param sent Whether the message is now sent or received.
	 */
	private void setSent(int sent) {
		this.sent = sent;
	}
}
