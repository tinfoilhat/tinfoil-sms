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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ManageNumbersActivity extends Activity {
	private ListView listView;
	public static TrustedContact contact;
	private TrustedContact tc;
	
    /** Called when the activity is first created. */
	//private ListView mContactList;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_number_manager);
        
        tc = contact;
        contact = null;
        //Linking the ListView object to the appropriate listview from the xml file.
        listView = (ListView)findViewById(R.id.contact_number_list);
        
        update();
               
        //Create what happens when you click on a button
        listView.setOnItemClickListener(new OnItemClickListener()
		{
        	public void onItemClick(AdapterView<?> parent, View view,
        			int position, long id) {
        		//Note: the key does not actually affect the encryption currently
        		//In order to send a encrypted message a contact must have a key
        		
        		if (!tc.isNumbersEmpty())
        		{
        			//tc.setPrimaryNumber(tc.getNumber(position));
        			//setPrimary();
        			//Toast.makeText(getBaseContext(), tc.getPrimaryNumber(), Toast.LENGTH_SHORT).show();
        		}
        		else
        		{
        			//Go to add contact
        			//startActivity(new Intent(getBaseContext(), AddContact.class));
        			//finish();
        		}
        	}});
        
	}
	
	
	/*private void setPrimary()
	{
		listView.setItemChecked(tc.findPrimaryNumber(), true);
	}*/
	
	/**
	 * Updates the list of contacts
	 */
	private void update()
	{
		if (!tc.isNumbersEmpty())
        {
	        //populates listview with the declared strings, an option is also given for it to be multiple choice (check boxes), or single list (radio buttons) 
	        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, tc.getNumber()));

	        //Not setting focus on a particular list item, (focus is then left to default at the top of the page)
	        listView.setItemsCanFocus(false);

	        //Set the mode to single or multiple choice, (should match top choice)
	        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	        //setPrimary();
	    }
        else 
        {
        	//populates listview with the declared strings, an option is also given for it to be multiple choice (check boxes), or single list (radio buttons) 
	        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] {"Add a new number"}));

	        //Not setting focus on a particular list item, (focus is then left to default at the top of the page)
	        listView.setItemsCanFocus(false);
        }
	}

	/*
	 * Added the onResume to update the list of contacts
	 */
	protected void onResume()
	{
		update();
		super.onResume();
	}
	

}

