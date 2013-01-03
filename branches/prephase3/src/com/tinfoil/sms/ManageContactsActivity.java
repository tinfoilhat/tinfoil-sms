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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * TODO add another indicator to show contact is trusted rather then selected
 * TODO add a button to initiate key exchange
 * TODO remove ability to exchange keys (or adjust so a popup comes up for users with multiple numbers)
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
public class ManageContactsActivity extends Activity implements Runnable {
	private ListView listView;
	private Button exchangeKeys;
	private ArrayList<TrustedContact> tc;
	private ProgressDialog dialog;
	private String[] names = null;
	private ArrayAdapter<String> arrayAp;
	private boolean[] trusted;
	private AlertDialog popup_alert;
	private static ArrayList<Number> trustedNumbers;
	private static ArrayList<Number> untrustedNumbers;
	private static ArrayList<Number> sublistTrust;
	private static ArrayList<Number> sublistUntrust;
	private static ArrayList<Number> numbers;

    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //***Need to override dialog to make so if BACK is pressed it exits the activity if it hasnt finished loading
        dialog = ProgressDialog.show(this, "Loading Contacts", 
                "Loading. Please wait...", true, false);

        setContentView(R.layout.contact);
        listView = (ListView)findViewById(R.id.contact_list);
        exchangeKeys = (Button)findViewById(R.id.exchange_keys);

        trustedNumbers = new ArrayList<Number>();
        untrustedNumbers = new ArrayList<Number>();
        sublistTrust = new ArrayList<Number>();
        sublistUntrust = new ArrayList<Number>();

        //update();
        
        listView.setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> parent, View view,
        			int position, long id) {

				AddContact.addContact = false;
				AddContact.editTc = tc.get(position);
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
        		        		
        		if (tc != null)
           		{
        			/* TODO modify to popup with a menu if the contact has multiple numbers
        			 * The layout of this will change to:
        			 * 1. If the contact has only 1 number then it will act a it did previously 
        			 * Else if they have more than 1 numbers then:
        			 * 	1. User clicks on the contact's name
        			 *  2. Pop menu shows the different numbers associated with the contact
        			 *  3. user can click on the different numbers they wish to exchange keys with
        			 *  4. user clicks positive button (there will also be negative button)
        			 *  5. Key exchange takes place with those contacts. **Note might need to make a thread to run that process and make a progress wheel for the user
        			 *  6. Contacts will show up with a check mark next to their name if at least 1 of their numbers is trusted. 
        			 */
        			numbers = tc.get(position).getNumber();
        			if(numbers != null && numbers.size() > 0)
        			{
        				int index = -1;
        				if(numbers.size() == 1)
        				{
        					//Contact only has a single number, check if that number is trusted
		        			if (MessageService.dba.isTrustedContact(numbers.get(0).getNumber()))
		        			{
		        				Toast.makeText(getApplicationContext(), "Contact removed from\nTrusted Contacts", Toast.LENGTH_SHORT).show();
		        				
		        				//numbers.get(0).setPublicKey();
		        				index = Number.hasNumber(untrustedNumbers, numbers.get(0));
			        			if(index >= 0)
			        			{
			        				untrustedNumbers.remove(index);
			        			}
			        			else
			        			{
			        				trustedNumbers.add(numbers.get(0));
			        			}
			        		}
			        		else
			        		{
			        			Toast.makeText(getApplicationContext(), "Contact added from\nTrusted Contacts", Toast.LENGTH_SHORT).show();
			        			
			        			//numbers.get(0).clearPublicKey();
			        			index = Number.hasNumber(trustedNumbers, numbers.get(0));
			        			if(index >= 0)
			        			{
			        				trustedNumbers.remove(index);
			        			}
			        			else
			        			{
			        				untrustedNumbers.add(numbers.get(0));
			        			}
			        		}
        				}
        				else
        				{
        					AlertDialog.Builder popup_builder = new AlertDialog.Builder(ManageContactsActivity.this);
        					
        					//TODO implement ListAdapter 
        					popup_builder.setTitle("Numbers")
        							//.setAdapter(adapter, listener)
        						   .setMultiChoiceItems(tc.get(position).getNumbers().toArray(new String[0]),
        						   MessageService.dba.isNumberTrusted(numbers),
        						   new DialogInterface.OnMultiChoiceClickListener(){

									public void onClick(DialogInterface dialog,
											int which, boolean isChecked) {
										int index = 0;
										if(isChecked)
										{
											//Add to the sublist of numbers to exchange keys with
											index = Number.hasNumber(sublistUntrust, numbers.get(0));
						        			if(index >= 0)
						        			{
						        				sublistUntrust.remove(index);
						        			}
						        			else
						        			{
						        				sublistTrust.add(numbers.get(which));
						        			}
										}
										else
										{
											//Remove from the sublist of numbers to exchange keys with
											index = Number.hasNumber(sublistTrust, numbers.get(0));
						        			if(index >= 0)
						        			{
						        				sublistTrust.remove(index);
						        			}
						        			else
						        			{
						        				sublistUntrust.remove(numbers.get(which));
						        			}
										}
									}})
									.setPositiveButton("Okay", new DialogInterface.OnClickListener(){

										public void onClick(DialogInterface dialog, int which) {
											//Add the sublist to the full list of numbers to exchange keys with
											trustedNumbers.addAll(sublistTrust);
											untrustedNumbers.addAll(sublistUntrust);
											sublistTrust.clear();
											sublistUntrust.clear();
										}
									})
									.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

										public void onClick(DialogInterface dialog, int which) {
											//Sublist is not added to the full list
											dialog.cancel();
										}
									})
        						   .setCancelable(true);
        					popup_alert = popup_builder.create();
        					popup_alert.show();
        				}
        				
        				MessageService.dba.updateKey(tc.get(position),tc.get(position).getANumber());
        			}
	        			
        		}
        		else
        		{
        			//Go to add contact
        			AddContact.addContact = true;
    				AddContact.editTc = null;
        			startActivity(new Intent(getBaseContext(), AddContact.class));
        		}
        	}});
	}

	/**
	 * TODO remove
	 * Used to toggle the contact from being in or out of the 
	 * trusted state.
	 * @param position : int, the position on the list of contacts.
	 * @param add : boolean, if true the contact will be added.
	 * If false the contact will be removed.
	 */
	public void change(int position, boolean add)
	{
		
		ArrayList<Number> numbers = tc.get(position).getNumber();
		if (add)
		{			
			if(numbers != null && numbers.size() > 0)
			{
				if(numbers.size() == 1)
				{
					numbers.get(0).setPublicKey();
				}
				else
				{
					//Contact has multiple numbers
				}
			}
		}
		else
		{
			if(numbers != null && numbers.size() > 0)
			{
				if(numbers.size() == 1)
				{
					numbers.get(0).clearPublicKey();
				}
				else
				{
					//Contact has multiple numbers
				}
			}
		}
		
		//TODO fix so it will update all of the changed keys
		MessageService.dba.updateKey(tc.get(position),tc.get(position).getANumber());
	}

	/**
	 * Reinitializes the list to ensure contacts that are
	 * trusted are selected.
	 */
	private void initList()
	{
		for (int i = 0; i < tc.size();i++)
		{				
			//TODO change so check box is not the method of indicating that a contact is trusted
			if (trusted[i])
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
		listView.setAdapter(arrayAp);
		listView.setItemsCanFocus(false);
		
		if (tc != null)
        {
	        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        initList();
        }

	}

	/*
	 * Added the onResume to update the list of contacts
	 */
	protected void onResume()
	{
		Thread thread = new Thread(this);
	    thread.start();
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
			AddContact.addContact = true;
			AddContact.editTc = null;
			startActivity(new Intent(this, AddContact.class));
			
			return true;
		}
		case R.id.all:
			if (tc!=null)
			{
				//TODO change, this really isnt ideal anymore
				/*for (int i = 0; i < tc.size();i++)
				{
					listView.setItemChecked(i, true);
					change(i, true);
				}*/
			}
			return true;
		case R.id.remove:
			if (tc!=null)
			{
				//TODO change, this really isnt ideal anymore
				/*for (int i = 0; i < tc.size();i++)
				{
					listView.setItemChecked(i, false);
					change(i, false);
				}*/
			}
			return true;
		case R.id.delete:
		{
			if (tc!=null)
			{
				startActivity(new Intent(getApplicationContext(), RemoveContactsActivity.class));
			}
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	public void run() {
		tc = MessageService.dba.getAllRows();
		
		if (tc != null)
		{			
			names = new String[tc.size()];
	        for (int i = 0; i < tc.size(); i++)
	        {
	        	names[i] = tc.get(i).getName();
	        }
	        arrayAp = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, names);
	        

	        trusted = MessageService.dba.isTrustedContact(tc);
		}
		else
		{
			names = new String[1];
        	names[0] = "Add a Contact";
        	
        	arrayAp = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
		}		
		
		handler.sendEmptyMessage(0);
	}
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
        	update();
        	dialog.dismiss();
        }
	};

}