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
import java.util.List;
import java.util.regex.Pattern;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

/**
 * An abstract class used to retrieve contacts information from
 * the native database and format the data going into tinfoil-sms's
 * database.
 */
public abstract class ContactRetriever {
	private static final Pattern p = Pattern.compile("^[+]1.{10}");
	private static final Pattern numOnly = Pattern.compile("\\W");
	
	/**
	 * Create an array of Strings to display for the auto-complete
	 * @param tc : List<TrustedContact> all the TrustedContacts
	 * @return : List<String> a list of all the contacts and their numbers
	 * for the auto complete list.
	 */
	public static List<String> contactDisplayMaker(List<TrustedContact> tc)
	{
		List<String> contacts = new ArrayList<String>();
		for (int i = 0; i < tc.size(); i++)
		{
			for (int j = 0; j < tc.get(i).getNumber().size(); j++)
			{
				contacts.add(tc.get(i).getName() + ", " +tc.get(i).getNumber(j));
			}
		}
		
		return contacts;
	}

	/**
	 * Removes the preceding '1' or '+1' for the given number
	 * @param number : String, the number of the contact 
	 * @return : String, the number without the preceding '1' or '+1'
	 */
	public static String format(String number)
	{
		if (number.matches("^1.{10}"))
		{
			number = number.substring(1);
		}
		else if (number.matches(p.pattern())) 
		{
			number = number.substring(2);
		}
		//number = number.replaceAll("[\(\)\-\s]", "");
		number = number.replaceAll(numOnly.pattern(), "");
		
		return number;
	}
	
	/**
     * Sends the given message to the phone with the given number
     * @param number : String, the number of the phone that the message is sent to
     * @param message : String, the message, encrypted that will be sent to the contact
     */
    public static void sendSMS (Context c, String number, String message)
    {
    	PendingIntent pi = PendingIntent.getActivity(c, 0, new Intent(c, Object.class), 0);
        SmsManager sms = SmsManager.getDefault();
        
        //this is the function that does all the magic
        sms.sendTextMessage(number, null, message, pi, null);
    	
    }   
}
