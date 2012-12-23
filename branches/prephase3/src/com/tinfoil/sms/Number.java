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

import java.util.ArrayList;
import java.util.Calendar;

/**
 * A class used to store information from the numbers table
 *
 */
public class Number {
	
	private String number;
	private int type;
	private int unreadMessageCount = 0;
	private ArrayList<Message> messages;
	
	/**
	 * A class used to store information from the numbers table
	 * 
	 * @param number A number for the contact
	 * @param type The type of number, whether it is a cell, home, etc.
	 * For more information of types of numbers please see the DBAccessor
	 * class for the 'TYPES' variable
	 * @param unreadMessageCount The number of messages that have
	 * not been read from this number
	 */
	public Number (String number, int type, int unreadMessageCount)
	{
		this.setNumber(number);
		this.setType(type);
		this.setUnreadMessageCount(unreadMessageCount);
		this.messages = new ArrayList<Message>();
	}
	
	/**
	 * A class used to store information from the numbers table
	 * 
	 * @param number A number for the contact
	 * @param type The type of number, whether it is a cell, home...
	 * For more information of types please see the DBAccessor class
	 * for the 'TYPES' variable
	 */
	public Number (String number, int type)
	{
		this.setNumber(number);
		this.setType(type);
		this.messages = new ArrayList<Message>();
	}
	
	/**
	 * A class used to store information from the numbers table
	 * 
	 * @param number A number for the contact
	 */
	public Number (String number)
	{
		this.setNumber(number);
		this.setType(DBAccessor.OTHER_INDEX);
		this.messages = new ArrayList<Message>();
	}
	
	/**
	 * Get the number
	 * @return The number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Set the number
	 * @param number The new number
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * Get the type of number
	 * @return The type of number (the index for it)
	 */
	public int getType() {
		if (type > DBAccessor.LENGTH)
		{
			return DBAccessor.OTHER_INDEX;
		}
		return type;
	}

	/**
	 * Set the type of number
	 * @param type The new type
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Add a message to the list of messages for the number
	 * @param newMessage The new message to add the list of messages
	 */
	public void addMessage(Message newMessage)
	{
		this.messages.add(newMessage);
	}
	
	/**
	 * Get the list of messages for the number
	 * @return The list of messages
	 */
	public ArrayList<Message> getMessages()
	{
		return this.messages;
	}
	
	/**
	 * Get the message given the index
	 * @param index The index of the message desired
	 * @return The message at the given index
	 */
	public Message getMessage(int index)
	{
		return this.messages.get(index);
	}

	/**
	 * Get the number of messages currently unread
	 * @return The current count of unread messages
	 */
	public int getUnreadMessageCount() {
		return unreadMessageCount;
	}

	/**
	 * Set the number of unread messages
	 * @param unreadMessageCount The new number of messages unread
	 */
	public void setUnreadMessageCount(int unreadMessageCount) {
		this.unreadMessageCount = unreadMessageCount;
	}
	
	/**
	 * Reset the number of messages unread to 0
	 */
	public void resetUnreadMessageCount() {
		this.unreadMessageCount = 0;
	}
	
	/**
	 * Increment the number of messages unread
	 */
	public void addUnreadMessageCount() {
		this.unreadMessageCount++;
	}

}
