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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * ManageContactActivity is an activity that allows the user to exchange keys, 
 * edit and delete contacts. A list of contacts will be shown with an check box,
 * if check then the user is either exchanging or have exchanged keys with the
 * contact. To edit a contact's information hold down for a long press, which 
 * will start AddContact activity with addContact == false and editTc != null. A
 * contact can be added by click 'Add Contact' in the menu this will start the 
 * AddContact activity with addContact == true and editTc == null. Contacts can
 * be deleted from tinfoil-sms's database by clicking 'Delete Contact' in the
 * menu which will start RemoveContactActivity. 
 */
public class ManageContactsActivity extends Activity {
	private ListView listView;
	//private ArrayList<TrustedContact> tc;
	private ArrayList<Contact> contact;

    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact);
        
        listView = (ListView)findViewById(R.id.contact_list);
        
        update();
        
        listView.setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> parent, View view,
        			int position, long id) {

				AddContact.addContact = false;
				//AddContact.editTc = tc.get(position);
				AddContact.editTc = MessageService.dba.getRow(contact.get(position).getNumber());
				ManageContactsActivity.this.startActivity(new Intent
						(ManageContactsActivity.this, AddContact.class));

				return true; //This stops other on click effects from happening after this one.
			}
        	
        });
               
        //Create what happens when you click on a button
        listView.setOnItemClickListener(new OnItemClickListener()
		{
        	public void onItemClick(AdapterView<?> parent, View view,
        			int position, long id) {
        		        		
        		//if (tc != null)
        		if (contact != null)
        		{
        			//if (MessageService.dba.isTrustedContact(tc.get(position).getANumber()))
        			if (MessageService.dba.isTrustedContact(contact.get(position).getNumber()))
        			{
        				Toast.makeText(getApplicationContext(), "Contact removed from\nTrusted Contacts", Toast.LENGTH_SHORT).show();
        				change(position, false);
	        		}
	        		else
	        		{
	        			Toast.makeText(getApplicationContext(), "Contact added from\nTrusted Contacts", Toast.LENGTH_SHORT).show();
	        			change(position, true);
	        		}
        		}
        		else
        		{
        			//Go to add contact
        			startActivity(new Intent(getBaseContext(), AddContact.class));
        		}

        	}});
        
	}

	/**
	 * Used to toggle the contact from being in or out of the 
	 * trusted state.
	 * @param position : int, the position on the list of contacts.
	 * @param add : boolean, if true the contact will be added.
	 * If false the contact will be removed.
	 */
	public void change(int position, boolean add)
	{
		//Contact contact;
		if (add)
		{
			//contact = new Contact(tc.get(position).getName(), tc.get(position).getANumber());
			contact.get(position).setPublicKey();
			//tc.get(position).setPublicKey();
		}
		else
		{
			//contact = new Contact(tc.get(position).getName(), tc.get(position).getANumber());
			contact.get(position).clearPublicKey();
			//tc.get(position).clearPublicKey();
		}

		MessageService.dba.updateRow(contact.get(position));
		//MessageService.dba.updateRow(tc.get(position),tc.get(position).getANumber());
	}

	/**
	 * Reinitialises the list to ensure contacts that are
	 * trusted are selected.
	 */
	private void initList()
	{
		//for (int i = 0; i < tc.size();i++)
		for (int i = 0; i < contact.size();i++)
		{				
			//if (MessageService.dba.isTrustedContact(tc.get(i).getANumber()))
			if (MessageService.dba.isTrustedContact(contact.get(i).getNumber()))
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
		//tc  = MessageService.dba.getAllRows();
		contact = MessageService.dba.getAllRowsLimited();
		//if (tc != null)
		if (contact != null)
        {
	        //The string that is displayed for each item on the list 
	        /*names = new String[tc.size()];
	        for (int i = 0; i < tc.size(); i++)
	        {
	        	names[i] = tc.get(i).getName();
	        }*/
			names = new String[contact.size()];
	        for (int i = 0; i < contact.size(); i++)
	        {
	        	names[i] = contact.get(i).getName();
	        }

	        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, names));

	        listView.setItemsCanFocus(false);

	        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        initList();
        }
        else 
        {
        	names = new String[1];
        	names[0] = "Add a Contact";
        	        
	        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names));

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
			//if (tc != null)
			if (contact != null)
			{
				AddContact.addContact = true;
				startActivity(new Intent(this, AddContact.class));
			}
			return true;
		}
		case R.id.all:
			//if (tc!=null)
			if (contact != null)
			{
				//for (int i = 0; i < tc.size();i++)
				for (int i = 0; i < contact.size();i++)
				{
					listView.setItemChecked(i, true);
					change(i, true);
				}
			}
			return true;
		case R.id.remove:
			//if (tc!=null)
			if (contact !=null)
			{
				//for (int i = 0; i < tc.size();i++)
				for (int i = 0; i < contact.size();i++)
				{
					listView.setItemChecked(i, false);
					change(i, false);
				}
			}
			return true;
		case R.id.delete:
		{
			//if (tc!=null)
			if (contact!=null)
			{
				startActivity(new Intent(getApplicationContext(), RemoveContactsActivity.class));
			}
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}

	}

}