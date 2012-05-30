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
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public abstract class ContactRetriever {
	private static final String dateColumn = "date DESC";
	private static final Pattern p = Pattern.compile("^[+]1.{10}");
	private static final int LIMIT = 50;
	private static final String USER_NAME = "Me";
	
	/**
	 * Get the list of 1 messages per unique contact for the main window
	 * @param c : Context
	 * @return : List<String[]>, a list of String arrays that contain
	 * the number, name, and the message. 
	 */
	//public static List<String[]> getSMS(Context c, int amount) {
	public static List<String[]> getSMS(Context c) {
		List<String[]> sms = new ArrayList<String[]>();
		final String[] projection = new String[]{"address", "body"};
		Uri uri = Uri.parse("content://mms-sms/conversations/");
		Cursor cur = c.getContentResolver().query(uri, projection, null, null, dateColumn);

		//int i = 0;
		while (cur.moveToNext())
		{
			/*if (amount!=0 && i > amount)
			{
				break;
			}*/
			String address = cur.getString(cur.getColumnIndex("address"));
			String name = nameHelper(address, c);
			String body = cur.getString(cur.getColumnIndexOrThrow("body"));
			sms.add(new String[] {address, name, body});
			//i++;
		}
		cur.close();
		return sms;
	}
	
	/**
	 * Get a list of messages received from a given number
	 * @param c : Context
	 * @return : List<String[]>, a list of String arrays that contain 
	 * the number, name, and the message.
	 */
	public static List<String[]> getPersonSMS(Context c) {
		
		final String[] projection = new String[]{"address", "body", "type"};
		List<String[]> sms = new ArrayList<String[]>();
		Uri uriSMSURI = Uri.parse("content://sms/");
		Cursor cur = c.getContentResolver().query(uriSMSURI, projection, 
				"address = '" + format(Prephase2Activity.selectedNumber) + 
				"' or address = '+1" + format(Prephase2Activity.selectedNumber) +
				"' or address = '1" + format(Prephase2Activity.selectedNumber) + "'", 
				null, dateColumn);
		
		int i = 0;
		while (cur.moveToNext()) {
			if (i == LIMIT)
			{
				break;
			}
			String address = cur.getString(cur.getColumnIndex("address"));
			String type = cur.getString(cur.getColumnIndex("type"));
			String name ="";
			if (type.equalsIgnoreCase("1"))
			{
				name = nameHelper(address, c);
			}
			else if (type.equalsIgnoreCase("2"))
			{
				name = USER_NAME; 
			}
			
			String body = cur.getString(cur.getColumnIndexOrThrow("body"));
			sms.add(new String[] {address, name, body});
			i++;
		}
		cur.close();
		return sms;
	}
	
	public static List<String> contactDisplayMaker(List<TrustedContact> tc)
	{
		List<String> contacts = new ArrayList<String>();
		for (int i = 0; i < tc.size(); i++)
		{
			for (int j = 0; j < tc.get(i).getNumberSize(); j++)
			{
				contacts.add(tc.get(i).getName() + ", " +tc.get(i).getNumber(j));
			}
		}
		
		return contacts;
	}
	
	/**
	 * Takes the information stored in the String array and 
	 * formats the display that the user will see.
	 * @param sms : List<String[]>, a list with a String array that
	 * contains the number, name, and the message.
	 * @return : List<String>, a list of messages formated.
	 */
	public static List<String> messageMaker (List<String[]> sms)
	{
		List <String> messageList = new ArrayList<String>();
		for (int i = 0; i < sms.size();i++)
		{
			messageList.add(sms.get(i)[1] + ": " + sms.get(i)[2]);
		}
		return messageList;
	}
	
	public static List<String> messageLimiter(List<String> sms)
	{
		final int LENGTH = 46;
		for (int i = 0; i < sms.size();i++)
		{
			if (sms.get(i).length() > LENGTH)
			{
				sms.set(i, sms.get(i).substring(0, LENGTH));
			}
			
		}
		return sms;
	}
	
	/**
	 * Facilitates finding the name. If the name is not found 
	 * it will check again removing possible formatting.
	 * @param number : String, the number to be looked up to find the contact's name
	 * @param c : Context
	 * @return : String, the name of the contact that has the given number
	 */
	public static String nameHelper(String number, Context c) {
		String num = findNameByAddress(number,c);
		if (num.equalsIgnoreCase(number)) {
			if (!number.equalsIgnoreCase(format(number)))
			{
				return findNameByAddress(format(number), c);
			}
		}
		return num;
	}
	
	/**
	 * Finds the name of the contact that has the given number
	 * @param addr : String, the number to be looked up to find the contact's name
	 * @param c : Context
	 * @return : String, the name of the contact that has the given number
	 */
	public static String findNameByAddress(String addr, Context c) {
		Uri myPerson = Uri.withAppendedPath(
				ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
				Uri.encode(addr));

		String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };

		Cursor cursor = c.getContentResolver().query(myPerson, projection, null, null, null);

		if (cursor.moveToFirst()) {

			String name = cursor.getString(cursor
					.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			cursor.close();
			return name;
		}

		cursor.close();

		return addr;
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
		number = number.replaceAll("-", "");
		
		return number;
	}
}
