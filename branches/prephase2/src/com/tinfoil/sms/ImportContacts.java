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
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ImportContacts extends Activity {
	Button confirm;
	private ListView listView;
	private ArrayList<TrustedContact> tc;
	 
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.importcontacts);
        
        confirm = (Button) findViewById(R.id.confirm);
        listView = (ListView)findViewById(R.id.import_contact_list);
        ContentResolver cr = getContentResolver();
        
        //**NOTE we should limit the columns retrieved by this query
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        tc = new ArrayList<TrustedContact>();
        
        //String id ="";
		//final ArrayList <String> names = new ArrayList<String>();
        
		if (cur.getCount() > 0) {
		    while (cur.moveToNext()) {
		        /*id = cur.getString(
	                        cur.getColumnIndex(ContactsContract.Contacts._ID));*/
		        //names.add(cur.getString(
	            //            cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
		    	tc.add(new TrustedContact(cur.getString(cur.getColumnIndex(
		    			ContactsContract.Contacts.DISPLAY_NAME))));
		       /*if (Integer.parseInt(cur.getString(
	 				cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
 					Cursor pCur = cr.query(
	 	 		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
	 	 		    null, 
	 	 		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
	 	 		    new String[]{id}, null);
	 	 	        while (pCur.moveToNext()) {
	 	 		    // Do something with phones
	 	 	        } 
	 	 	        pCur.close();
	 	        }*/
 			}
	 	}
		
		listView.setAdapter(new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_multiple_choice, getNames()));
        
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        listView.setOnItemClickListener(new OnItemClickListener(){
        	public void onItemClick(AdapterView<?> parent, View view,
        			int position, long id) {
        		//Keep track of the contacts selected.
        	}
        	
        });
                
        confirm.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				//Add Contacts to the tinfoil-sms database from android's database
				
			}
		});       
        
	}
	
	/**
	 * Produces an ArrayList of contact names from the ArrayList of TrustedContacts
	 * @return : ArrayList, a list of the names of each person on the list.
	 */
	public ArrayList<String> getNames()
	{
		ArrayList <String> names = new ArrayList<String>();
		if (tc != null)
		{
			for (int i = 0; i < tc.size();i++)
			{
				names.add(tc.get(i).getName());
			}
			return names;
		}
		return null;
		
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
   	 
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.import_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	        case R.id.all:
	        	if (tc!=null)
				{
					for (int i = 0; i < tc.size();i++)
					{
						listView.setItemChecked(i, true);
						//change(i, true);
					}
				}
		        return true;
	        		    
	        default:
	        return super.onOptionsItemSelected(item);
    	}
     
    }
	
}
