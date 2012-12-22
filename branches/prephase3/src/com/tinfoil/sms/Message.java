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

import java.util.Calendar;

/**
 * A class used to store information from the message table
 *
 */
public class Message {
	
	private static final boolean clockStyle = true;
	private String message;
	private long date;
	private boolean sent;
	
	/**
	 * A class for storing messages retrieved or to be stored in the database. 
	 * 
	 * @param message The body of the message
	 * @param date The date the message was sent or received 
	 * @param type Whether the message is send or received, 
	 * 1 means received, 2 means sent. 
	 * *Please Note These values are based of the native application's send and
	 * received flags.
	 */
	public Message (String message, long date, int type)
	{
		this.setMessage(message);
		this.setDate(date);
		if (type == 2)
		{
			this.setSent(true);
		}
		else if (type == 1)
		{
			this.setSent(false);
		}
	}
	
	/**
	 * A class for storing messages retrieved or to be stored in the database. 
	 * 
	 * @param message The body of the message
	 * @param currentTime Whether the date is set to the current time or not. 
	 * If true date is set to current time, otherwise date is set to 0
	 * @param sent Whether the message was sent or received
	 * If sent = true then the message was sent
	 * otherwise the message was received
	 */
	public Message (String message, boolean currentTime, boolean sent)
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
	 * @return If the message was sent it will return true,
	 * otherwise false.
	 */
	public boolean isSent() {
		return sent;
	}
	
	/**
	 * Get the send flag in terms of the native android messaging
	 * application this is more used to save messages back to the
	 * native application
	 * @return The flag of whether the message was sent or
	 * received. If the message was sent the return will be 2,
	 * otherwise the return will be 1.
	 */
	public int getSent() {
		if (sent)
		{
			return 2;
		}
		else
		{
			return 1;
		}
	}

	/**
	 * Set whether the message is sent or received
	 * @param sent Whether the message is now sent or received.
	 */
	private void setSent(boolean sent) {
		this.sent = sent;
	}
}
