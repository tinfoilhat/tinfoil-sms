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
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ManageContactsActivity extends Activity {
	private final String KEY = "test123";
	private final int VERIFY = 2;
	private ListView listView;
	private ArrayList<TrustedContact> tc;
	
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact);
        
        //Linking the ListView object to the appropriate listview from the xml file.
        listView = (ListView)findViewById(R.id.contact_list);
        
        update();
               
        //Create what happens when you click on a button
        listView.setOnItemClickListener(new OnItemClickListener()
		{
        	public void onItemClick(AdapterView<?> parent, View view,
        			int position, long id) {
        		//Note: the key does not actually affect the encryption currently
        		//In order to send a encrypted message a contact must have a key
        		
        		if (tc != null)
        		{
        			//if (Prephase2Activity.dba.isTrustedContact(tc.get(position).getPrimaryNumber()))
        			if (Prephase2Activity.dba.isTrustedContact(tc.get(position).getANumber()))
        			{
        				change(position, false);
	        			//Toast.makeText(getApplicationContext(), "Contact removed from\nTrusted Contacts\n" + tc.get(position).getPrimaryNumber(), Toast.LENGTH_SHORT).show();
        				Toast.makeText(getApplicationContext(), "Contact removed from\nTrusted Contacts", Toast.LENGTH_SHORT).show();
	        		}
	        		else
	        		{
	        			change(position, true);
	        			//Toast.makeText(getApplicationContext(), "Contact added from\nTrusted Contacts\n" + tc.get(position).getPrimaryNumber(), Toast.LENGTH_SHORT).show();
	        			Toast.makeText(getApplicationContext(), "Contact added from\nTrusted Contacts", Toast.LENGTH_SHORT).show();
	        		}
        		}
        		else
        		{
        			//Go to add contact
        			startActivity(new Intent(getBaseContext(), AddContact.class));
        			//finish();
        		}

        	}});
        
	}
	
	
	/**
	 * Sets Contact to the not trusted state. Secure messages 
	 * will not be sent or expected from this contact. 
	 * @param position : int, the position on the list of
	 * contacts.
	 */
	private void remove(int position)
	{
		tc.get(position).setKey(null);
		tc.get(position).setVerified(0);
	}
	
	/**
	 * Sets Contact to the trusted state. Secure messages 
	 * will be sent and expected from this contact. 
	 * @param position : int, the position on the list of
	 * contacts.
	 */
	private void add(int position)
	{
		tc.get(position).setKey(KEY);
		tc.get(position).setVerified(VERIFY);
	}
	
	/**
	 * Used to toggle the contact from being in or out of
	 * the trusted state.
	 * @param position : int, the position on the list of
	 * contacts.
	 * @param add : boolean, if true the contact will be
	 * added. If false the contact will be removed.
	 */
	public void change(int position, boolean add)
	{
		if (add)
		{
			add(position);
		}
		else
		{
			remove(position);
		}
		//Prephase2Activity.dba.updateRow(tc.get(position),tc.get(position).getPrimaryNumber());
		Prephase2Activity.dba.updateRow(tc.get(position),tc.get(position).getANumber());
	}
	
	/**
	 * Reinitialises the list to ensure contacts that are
	 * trusted are selected.
	 */
	private void initList()
	{
		for (int i = 0; i < tc.size();i++)
		{				
			//if (Prephase2Activity.dba.isTrustedContact(tc.get(i).getPrimaryNumber()))
			if (Prephase2Activity.dba.isTrustedContact(tc.get(i).getANumber()))
			{
				listView.setItemChecked(i, true);
    		}
			else
			{
				listView.setItemChecked(i, false);
			}
		}
	}
	
	/**
	 * Updates the list of contacts
	 */
	private void update()
	{
		String[] names;
		tc  = Prephase2Activity.dba.getAllRows();
		if (tc != null)
        {
	        //The string that is displayed for each item on the list 
	        names = new String[tc.size()];
	        for (int i = 0; i < tc.size(); i++)
	        {
	        	names[i] = tc.get(i).getName();
	        }

	        //populates listview with the declared strings, an option is also given for it to be multiple choice (check boxes), or single list (radio buttons) 
	        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, names));

	        //Not setting focus on a particular list item, (focus is then left to default at the top of the page)
	        listView.setItemsCanFocus(false);

	        //listView.set
	        
	        //Set the mode to single or multiple choice, (should match top choice)
	        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        initList();
        }
        else 
        {
        	names = new String[1];
        	names[0] = "Add a Contact";
        	        
	        //populates listview with the declared strings, an option is also given for it to be multiple choice (check boxes), or single list (radio buttons) 
	        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names));

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
	
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.manage_contacts_menu, menu);
		return true;		
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add:
		{
			if (tc != null)
			{
				AddContact.editTc = tc.get(0);
				startActivity(new Intent(this, AddContact.class));
			}
			return true;
		}
		case R.id.all:
			if (tc!=null)
			{
				for (int i = 0; i < tc.size();i++)
				{
					listView.setItemChecked(i, true);
					change(i, true);
				}
			}
			return true;
		case R.id.remove:
			if (tc!=null)
			{
				for (int i = 0; i < tc.size();i++)
				{
					listView.setItemChecked(i, false);
					change(i, false);
				}
			}
			return true;
		case R.id.delete:
		{
			if (tc!=null)
			{
				startActivity(new Intent(getApplicationContext(), RemoveContactsActivity.class));
			}
			else
			{
				//**Note need an alert message here
				Toast.makeText(this, "You need to have contacts before you can delete them!", Toast.LENGTH_SHORT);
			}
			return true;
		}
		/*case R.id.edit_number:
		{
			if (tc!=null)
			{
				//Need to find a place to activate this activity.
				ManageNumbersActivity.contact = tc.get(0);
				startActivity(new Intent(getBaseContext(), ManageNumbersActivity.class));
				
			}
			return true;
		}*/
		default:
			return super.onOptionsItemSelected(item);
		}

	}

}

