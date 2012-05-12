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
import java.util.Hashtable;
import java.util.List;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ContactRetriever {
	
	public static List<String[]> getSMS(Context c) {
		List<String[]> sms = new ArrayList<String[]>();
		Uri uriSMSURI = Uri.parse("content://sms/inbox");
		Cursor cur = c.getContentResolver().query(uriSMSURI, null, null, null,
				null);
		
		// Used to remove duplication
		Hashtable<String, Boolean> numbers = new Hashtable<String, Boolean>();

		while (cur.moveToNext()) {
			String address = cur.getString(cur.getColumnIndex("address"));
			String id = cur.getString(cur.getColumnIndex("_id"));
			if (numbers.isEmpty() || numbers.get(address) == null) {
				numbers.put(address, true);
				String name = nameHelper(address, c);
				String body = cur.getString(cur.getColumnIndexOrThrow("body"));
				sms.add(new String[] {address, name, body});
			}
		}
		cur.close();
		return sms;
	}
	
	public static List<String[]> getPersonSMS(Context c) {
		List<String[]> sms = new ArrayList<String[]>();
		Uri uriSMSURI = Uri.parse("content://sms/inbox");
		Cursor cur = c.getContentResolver().query(uriSMSURI, null, "address = ?",
				new String[] {Prephase2Activity.selectedNumber}, null);
		// ContentResolver cr = getContentResolver();
		// Cursor nCur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
		// null, null, null);

		// Used to remove duplication
		//Hashtable<String, Boolean> numbers = new Hashtable<String, Boolean>();

		while (cur.moveToNext()) {
			String address = cur.getString(cur.getColumnIndex("address"));
			String id = cur.getString(cur.getColumnIndex("_id"));
			//if (numbers.isEmpty() || numbers.get(address) == null) {
				//numbers.put(address, true);
				String name = nameHelper(address, c);
				String body = cur.getString(cur.getColumnIndexOrThrow("body"));
				//msg.add("Number: " + address + " < Name " + name
					//	+ "> Message: " + body);
				//msg2.add(address);
				//sms.add(msg2);
				sms.add(new String[] {address, name, body});
			//}
		}
		cur.close();
		return sms;
	}
	
	public static List<String> messageMaker (List<String[]> sms)
	{
		List <String> messageList = new ArrayList<String>();
		for (int i = 0; i < sms.size();i++)
		{
			messageList.add(sms.get(i)[1] + ": " + sms.get(i)[2]);
		}
		return messageList;
		
	}
	
	public static String nameHelper(String number, Context c) {
		String num = findNameByAddress(number,c);
		if (num.equalsIgnoreCase(number)) {
			return findNameByAddress(format(number), c);
		}
		return num;
	}
	
	public static String format(String number) {
		if (!number.substring(0, 2).equalsIgnoreCase("+1")) {
			return number;
		}
		return number.substring(2);
	}

	public static String findNameByAddress(String addr, Context c) {
		Uri myPerson = Uri.withAppendedPath(
				ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
				Uri.encode(addr));

		String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };

		Cursor cursor = c.getContentResolver().query(myPerson, projection, null,
				null, null);

		if (cursor.moveToFirst()) {

			String name = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			cursor.close();
			return name;
		}

		cursor.close();

		return addr;
	}

}
