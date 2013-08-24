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

/**
 * The data structure for storing entries retrieved or sent to the database's
 * messaging queue.
 */
public class Entry {
	private String number;
	private String message;
	private long id;
	
	 /*
	  * Messages placed in the queue use the exchange flag to identify that the
	  * message is a key exchange. Therefore it will not be encrypted. This is
	  * not so much an issue for the initiator of the key exchange (since they
	  * will not have the other contact's key. However, upon receiving a key
	  * exchange message a user must save the their now TrustedContact's key
	  * (the actual having of a key makes any contact trusted). This however
	  * will make all queued messages get encrypted (to that contact). This will
	  * thus make the response key exchange message encrypted and thus
	  * unreadable. This flag therefore allows the response to the key exchange
	  * message to be sent not encrypted even though the contact is trusted.
	  */
	private boolean exchange;
	
	public static final int TRUE = 1;
	public static final int FALSE = 0;
	
	/**
	 * Create a entry in the queue.
	 * @param number The number the message will be sent to
	 * @param message The message that will be sent
	 * @param id The queue's unique ID
	 * @param exchange Whether the message is a key exchange or not
	 */
	public Entry (String number, String message, long id, int exchange)
	{
		this.number = number;
		this.message = message;
		this.id = id;
		
		if(exchange == FALSE)
		{
			this.exchange = false;
		}
		else
		{
			this.exchange = true;
		}
		
	}
	
	/**
	 * Create a entry in the queue.
	 * @param number The number the message will be sent to
	 * @param message The message that will be sent
	 */
	public Entry(String number, String message)
	{
		this.number = number;
		this.message = message;
	}

	/**
	 * Get the number.
	 * @return The number.
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Set the number.
	 * @param number The new number
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * The message from the queue.
	 * @return The message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the message to a new message.
	 * @param message The new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * The queue entry's id.
	 * @return The id of the queue entry.
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Set the queue entry's id.
	 * @param id The new id.
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Whether the message is a key exchange or not
	 * @return the exchange
	 */
	public boolean isExchange() {
		return exchange;
	}
	
	/**
	 * Get the integer value of exchange. This is to store the value in the
	 * database.
	 * @return The integer value of exchange
	 */
	public int getExchange()
	{
		if(exchange)
		{
			return TRUE;
		}
		return FALSE;
	}

	/**
	 * Set whether the text is a key exchange message
	 * @param exchange the exchange to set
	 */
	public void setExchange(boolean exchange) {
		this.exchange = exchange;
	}
	
}
