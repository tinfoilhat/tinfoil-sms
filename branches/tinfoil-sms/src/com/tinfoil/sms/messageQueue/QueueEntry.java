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

package com.tinfoil.sms.messageQueue;

/**
 * The data structure for storing entries retrieved or sent to the database's
 * messaging queue.
 */
public class QueueEntry {
	private String number;
	private String message;
	private long id;
	
	public QueueEntry (String number, String message, long id)
	{
		this.setNumber(number);
		this.setMessage(message);
		this.setId(id);
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
	
}
