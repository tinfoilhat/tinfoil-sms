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

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ImportContacts extends Activity {
	Button confirm;
	private ListView importList;
	private ArrayList<TrustedContact> tc;
	 
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.importcontacts);
        
        confirm = (Button) findViewById(R.id.confirm);
        importList = (ListView)findViewById(R.id.import_contact_list);
        ContentResolver cr = getContentResolver();
        
        //**NOTE we should limit the columns retrieved by this query
        //Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        tc = new ArrayList<TrustedContact>();
                
        //String number=null;
        ArrayList<String> number = new ArrayList<String>();
        String name;
        //ArrayList<String> value = new ArrayList<String>();
        
        String columnsN[] = new String[] { Phone.NUMBER};
        String columnsC[] = new String[] { Contacts._ID, Contacts.DISPLAY_NAME, Contacts.HAS_PHONE_NUMBER};
        Uri mContacts = ContactsContract.Contacts.CONTENT_URI;
        Cursor cur = managedQuery(mContacts, columnsC, null, null, Contacts.DISPLAY_NAME);
        
        Hashtable<String,ArrayList<String>> ActualSender = new Hashtable<String,ArrayList<String>>();
        int i = 0;
        if (cur.moveToFirst()) {
                do {
                		name = cur.getString(cur.getColumnIndex(Contacts.DISPLAY_NAME));
                		String id  = cur.getString(cur.getColumnIndex(Contacts._ID));
                		if (cur.getString(cur.getColumnIndex(Contacts.HAS_PHONE_NUMBER)).equalsIgnoreCase("1"))
                		{
                			Cursor pCur = cr.query(Phone.CONTENT_URI, 
                					columnsN, Phone.CONTACT_ID +" = ?", 
                	 	 		    new String[]{id}, null);
                			if (pCur.moveToFirst())
                			{
                				//number = pCur.getString(pCur.getColumnIndex(Phone.NUMBER));
                				do
                				{
                					number.add(pCur.getString(pCur.getColumnIndex(Phone.NUMBER)));
                				} while (pCur.moveToNext());
                			}
                		}
                		
                        if(number!=null)
                        {
                        	
                        	if (!Prephase2Activity.dba.inDatabase(number))
                        	{
                        		ActualSender.put(name, number);
                        		//tc.add(new TrustedContact(name, number, null,0));
                        	}
                        }
                        i++;
                } while (cur.moveToNext());
        }

        //String id ="";
		//final ArrayList <String> names = new ArrayList<String>();
        
		/*if (cur.getCount() > 0) {
		    while (cur.moveToNext()) {
		        id = cur.getString(
	                        cur.getColumnIndex(ContactsContract.Contacts._ID));
		        //names.add(cur.getString(
	            //            cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
		    	tc.add(new TrustedContact(cur.getString(cur.getColumnIndex(
		    			ContactsContract.Contacts.DISPLAY_NAME))));
		       if (Integer.parseInt(cur.getString(
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
	 	        }
 			}
	 	}
	 	
	 	listView.setAdapter(new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_multiple_choice, getNames()));*/
        importList.setAdapter(new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_multiple_choice, getNames()));
		
        //importList.setItemsCanFocus(false);
        
        importList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        /*confirm.setOnClickListener(new View.OnClickListener() {
		
        	public void onClick(View v) {
			
			//Add Contacts to the tinfoil-sms database from android's database
	
        	}
        });   */
        
        
        importList.setOnItemClickListener(new OnItemClickListener(){
        	public void onItemClick(AdapterView<?> parent, View view,
        			int position, long id) {
        		Toast.makeText(getApplicationContext(), "Number "+ tc.get(position).getNumber(), Toast.LENGTH_SHORT).show();
        		//Keep track of the contacts selected.
        			
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
						importList.setItemChecked(i, true);
						//change(i, true);
					}
				}
		        return true;
	        		    
	        default:
	        return super.onOptionsItemSelected(item);
    	}
     
    }
	
}
