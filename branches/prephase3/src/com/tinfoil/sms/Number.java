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
	private String lastMessage;
	private int type;
	private int unreadMessageCount = 0;
	private long date;
	private ArrayList<Message> messages;
	/**
	 * 
	 * @param number
	 * @param type
	 * @param lastMessage
	 * @param date
	 */
	public Number (String number, int type, int unreadMessageCount)
	{
		this.setNumber(number);
		//this.setLastMessage(lastMessage);
		this.setType(type);
		//this.setDate(date);
		//messages.add(new Message());
		this.setUnreadMessageCount(unreadMessageCount);
		this.messages = new ArrayList<Message>();
	}
	
	/**
	 * Date not set
	 * @param number
	 * @param type
	 */
	public Number (String number, int type)
	{
		this.setNumber(number);
		this.setType(type);
		this.messages = new ArrayList<Message>();
	}
	
	/**
	 * Date not set
	 * @param number
	 */
	public Number (String number)
	{
		this.setNumber(number);
		this.setType(DBAccessor.OTHER_INDEX);
		this.messages = new ArrayList<Message>();
	}
	
	/**
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * @param number the number to set
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		if (type > DBAccessor.LENGTH)
		{
			return DBAccessor.OTHER_INDEX;
		}
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	public void addMessage(Message newMessage)
	{
		this.messages.add(newMessage);
	}
	
	public ArrayList<Message> getMessages()
	{
		return this.messages;
	}
	
	public Message getMessage(int index)
	{
		return this.messages.get(index);
	}
	
	public static String millisToDate(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        return calendar.getTime().toString();
    }

	public int getUnreadMessageCount() {
		return unreadMessageCount;
	}

	public void setUnreadMessageCount(int unreadMessageCount) {
		this.unreadMessageCount = unreadMessageCount;
	}
	
	public void resetUnreadMessageCount() {
		this.unreadMessageCount = 0;
	}
	
	public void addUnreadMessageCount() {
		this.unreadMessageCount++;
	}

}
