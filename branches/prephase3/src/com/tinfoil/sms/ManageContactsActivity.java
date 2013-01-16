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
import java.util.HashMap;

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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ListView;

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
public class ManageContactsActivity extends Activity implements Runnable {
	//private ListView listView;
	private ExpandableListView listView;
	private Button exchangeKeys;
	private ArrayList<TrustedContact> tc;
	private ProgressDialog loadingDialog;
	private String[] names = null;
	private ArrayAdapter<String> arrayAp;
	//private TrustedAdapter contactAdapter;
	private boolean[] trusted;
	private AlertDialog popup_alert;
	
	private ArrayList<ContactParent> contacts;
	private ArrayList<ContactChild> contactNumbers;
	private static ManageContactAdaptor adapter;
	
	private boolean notChecked = false;
	
	private static boolean[] selected;
	
	private static HashMap<String, Boolean> subSelected;

	private static ArrayList<Number> numbers;
	private static ExchangeKey keyThread = new ExchangeKey();

    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //TODO Override dialog to make so if BACK is pressed it exits the activity if it hasn't finished loading
        loadingDialog = ProgressDialog.show(this, "Loading Contacts", 
                "Loading. Please wait...", true, false);

        setContentView(R.layout.contact);
        listView = (ExpandableListView)findViewById(R.id.contacts_list);
        exchangeKeys = (Button)findViewById(R.id.exchange_keys);
     
        //update();
        
        listView.setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> parent, View view,
        			int position, long id) {
				
				AddContact.addContact = false;
				AddContact.editTc = tc.get(position);
				ManageContactsActivity.this.startActivity(new Intent
						(ManageContactsActivity.this, AddContact.class));

				//This stops other on click effects from happening after this one.
				return true; 
			}
        	
        });
        
        listView.setOnChildClickListener(new OnChildClickListener(){

			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				if (tc != null)
           		{
					CheckedTextView checked_text = (CheckedTextView)v.findViewById(R.id.trust_name);
					
					adapter.getContacts().get(groupPosition).getNumber(childPosition).toggle();
					
					checked_text.setChecked(adapter.getContacts().get(groupPosition).getNumber(childPosition).isSelected());
           		}
				else
				{
					//Go to add contact
        			AddContact.addContact = true;
    				AddContact.editTc = null;
        			startActivity(new Intent(getBaseContext(), AddContact.class));
				}
				
				return true;
			}});
        
        /*listView.setOnItemClickListener(new OnItemClickListener()
		{
        	public void onItemClick(AdapterView<?> parent, View view,
        			int position, long id) {
        		
        		if (tc != null)
           		{
        			//TODO implement using expandable lists
        			//Decided to implement the list using expandable lists
        			
        			numbers = tc.get(position).getNumber();
        			if(numbers != null && numbers.size() > 0)
        			{        				
        				if(numbers.size() == 1)
        				{
        					//Contact only has a single number, check if that number is trusted
        					if(listView.isItemChecked(position))
		        			{
			        			selected[position] = true;
			        		}
			        		else
			        		{
			        			selected[position] = false;
			        		}
        				}
        				else
        				{
        					final int contactIndex = position;
        					
        					 //Update the check box after the items in the sublist have been chosen
        					if(listView.isItemChecked(contactIndex))
        					{
        						notChecked = true;
        					}
        					else
        					{
        						notChecked = false;
        					}
       						listView.setItemChecked(contactIndex, !notChecked);

        					AlertDialog.Builder popup_builder = new AlertDialog.Builder(ManageContactsActivity.this);
        					
        					boolean[] trustNum = new boolean[numbers.size()]; 
        					for(int i = 0; i < numbers.size(); i++)
        					{
        						trustNum[i] = subSelected.get(numbers.get(i).getNumber());
        					}
        					        					
        					//TODO implement ListAdapter 
        					popup_builder.setTitle("Numbers")
        							//.setAdapter(adapter, listener)
        						   .setMultiChoiceItems(tc.get(contactIndex).getNumbers().toArray(new String[0]),
        						   trustNum, new DialogInterface.OnMultiChoiceClickListener(){

									public void onClick(DialogInterface dialog,
											int which, boolean isChecked) {
																				
										if(isChecked)
										{
						        			subSelected.put(tc.get(contactIndex).getNumber(which), true);
										}
										else
										{
						        			subSelected.put(tc.get(contactIndex).getNumber(which), false);
										}
									}})
									.setPositiveButton("Okay", new DialogInterface.OnClickListener(){

										public void onClick(DialogInterface dialog, int which) {
											//Add the sublist to the full list of numbers to exchange keys with
											
											boolean checked = false;
											
											for(int i = 0; i < tc.get(contactIndex).getNumber().size(); i++)
											{
												if(subSelected.get(tc.get(contactIndex).getNumber(i)))
												{
													checked = true;
													break;
												}
											}
											
											listView.setItemChecked(contactIndex, checked);
											selected[contactIndex] = checked;
										}
									})
									.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

										public void onClick(DialogInterface dialog, int which) {
											
											//TODO undo the changes made
											listView.setItemChecked(contactIndex, !notChecked);
											dialog.cancel();
										}
									}).setCancelable(true);
        					popup_alert = popup_builder.create();
        					popup_alert.show();
        				}
        				
        			}
	        			
        		}
        		else
        		{
        			//Go to add contact
        			AddContact.addContact = true;
    				AddContact.editTc = null;
        			startActivity(new Intent(getBaseContext(), AddContact.class));
        		}
        	}});*/
        
        exchangeKeys.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				/*
				 * Launch Exchange Keys thread.
				 */
				
				//TODO Override to adjust cancel
				ExchangeKey.keyDialog = ProgressDialog.show(ManageContactsActivity.this, "Exchanging Keys", 
		                "Exchanging. Please wait...", true, false);
				
				keyThread.startThread(ManageContactsActivity.this, tc, subSelected, selected);	
			}});
	}

	/**
	 * Reinitializes the list to ensure contacts that are
	 * trusted are selected.
	 */
	/*private void initList()
	{
		if(trustedNumbers != null || untrustedNumbers != null)
		{
			for (int i = 0; i < trustedNumbers.size();i++)
			{
				//TODO implement
				
				// i does not equal the list's index
				//listView.setItemChecked(i, true);
				
				
			}
			for(int i = 0; i < untrustedNumbers.size(); i++)
			{
				//TODO implement
			}
		}
	}*/

	/**
	 * Updates the list of contacts
	 */
	private void update()
	{
		if(tc != null)
		{
			listView.setAdapter(adapter);
		}
		else
		{
			listView.setAdapter(arrayAp);
		}
		//listView.setItemsCanFocus(false);
		
		if (tc != null)
        {
	        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        //initList();
        }

	}

	/*
	 * Added the onResume to update the list of contacts
	 */
	protected void onResume()
	{
		//TODO This should be called in on Create and also after the exchange keys thread is finished
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
			//names = new String[tc.size()];
			//selected = new boolean[tc.size()];
			//subSelected = new HashMap<String, Boolean>();
			contacts = new ArrayList<ContactParent>();
			

			int size = 0;
			
			
			
	        for (int i = 0; i < tc.size(); i++)
	        {
	        	size = tc.get(i).getNumber().size();
	       
	        	contactNumbers = new ArrayList<ContactChild>();
	        	
	        	trusted = MessageService.dba.isTrustedContact(tc.get(i).getNumber());
	        	
	        	for(int j = 0; j < size; j++)
	        	{	        			
		       		//TODO change to use primary key from trusted contact table
	        		contactNumbers.add(new ContactChild(tc.get(i).getNumber(j), 
	        				trusted[j],false));
	        	}
	        	contacts.add(new ContactParent(tc.get(i).getName(), contactNumbers));
	        }
	        
	        //TODO create contact list
	        
	        adapter = new ManageContactAdaptor(this, contacts);
	        //listView.setAdapter(adapter);
	        
	        //arrayAp = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, names);
	        
	        
	        
	        //contactAdapter = new TrustedAdapter(this, android.R.layout.simple_list_item_multiple_choice, names, trusted);
	        //contactAdapter = new TrustedAdapter(this, R.layout.trusted_contact_manage, names, trusted);
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
        	loadingDialog.dismiss();
        }
	};

}