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
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class MessageView extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.messages);
		
		ListView list = (ListView) findViewById(R.id.listView1);
		List<String> msgList = getSMS();
		//String []msgList = {"bla", "sasdd"};
		
		//Toast.makeText(getApplicationContext(), "Here", Toast.LENGTH_SHORT).show();
		list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.test_list_item, msgList));
		list.setItemsCanFocus(false);
		
		list.setOnItemClickListener(new OnItemClickListener()
		{
        	public void onItemClick(AdapterView<?> parent, View view,
        			int position, long id) {
        		
				
        	}
        });
    }

    public List<String> getSMS()
    {
    	List<String> sms = new ArrayList<String>();
		Uri uriSMSURI = Uri.parse("content://sms/inbox");
		Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);
		//ContentResolver cr = getContentResolver();
		//Cursor nCur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		//Used to remove duplication
		Hashtable <String, Boolean> numbers = new Hashtable <String, Boolean>();
		
		while (cur.moveToNext()) 
		{
			String address = cur.getString(cur.getColumnIndex("address"));
			String id = cur.getString(cur.getColumnIndex("_id"));
			if (numbers.isEmpty() || numbers.get(address) == null)
			{
				numbers.put(address, true);
				
				//String name = findNameByAddress(address);
				String name = nameHelper(address);
				String body = cur.getString(cur.getColumnIndexOrThrow("body"));
				sms.add("Number: " + address + " < Name " + name + "> Message: " + body);
				//sms.add("Number: " + address + " Message: " + body);
			}
		}
		cur.close();
		return sms;
    }
    
    public String format (String number)
    {
    	if (!number.substring(0,2).equalsIgnoreCase("+1"))
    	{
    		return number;
    	}
    	return number.substring(2);
    }

    public String nameHelper(String number)
    {
    	String num = findNameByAddress(number);
    	if (num.equalsIgnoreCase(number))
    	{
    		return findNameByAddress(format(number));
    	}
    	return num;
    }

	public String findNameByAddress(String addr)
	{
		Uri myPerson = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
	                Uri.encode(addr));
	
		String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };
	
		Cursor cursor = getContentResolver().query(myPerson,
	                projection, null, null, null);
	
		if (cursor.moveToFirst()) {
	
			String name=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			cursor.close();
			return name;
		}
	
	    cursor.close();
	
	    return addr;
	}
    
}