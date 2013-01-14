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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * TODO add another indicator to show contact is trusted rather then selected
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
	private ProgressDialog loadingDialog;
	private String[] names = null;
	private ArrayAdapter<String> arrayAp;
	private TrustedAdapter contactAdapter;
	private boolean[] trusted;
	private AlertDialog popup_alert;
	
	private boolean notChecked = false;
	
	private static ArrayList<Number> trustedNumbers;
	private static ArrayList<Number> untrustedNumbers;
	
	//TODO add hash to map the selected items and their locations in the list
	//private HashMap<String, Integer> selected;
	
	private static boolean[] selected;
	
	//private static ArrayList<boolean[]> subSelected;
	private static HashMap<String, boolean[]> subSelected;
	//private HashMap<String, Integer> subSelected;
	
	private static ArrayList<Number> sublistTrust;
	private static ArrayList<Number> sublistUntrust;
	private static ArrayList<Number> numbers;
	private static ExchangeKey keyThread = new ExchangeKey();

    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //TODO Override dialog to make so if BACK is pressed it exits the activity if it hasn't finished loading
        loadingDialog = ProgressDialog.show(this, "Loading Contacts", 
                "Loading. Please wait...", true, false);

        setContentView(R.layout.contact);
        listView = (ListView)findViewById(R.id.contact_list);
        exchangeKeys = (Button)findViewById(R.id.exchange_keys);

        trustedNumbers = new ArrayList<Number>();
        untrustedNumbers = new ArrayList<Number>();
        sublistTrust = new ArrayList<Number>();
        sublistUntrust = new ArrayList<Number>();
        //selected = new HashMap<String, Integer>();
        //subSelected = new HashMap<String, Integer>();
        
        

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
               
       
        listView.setOnItemClickListener(new OnItemClickListener()
		{
        	public void onItemClick(AdapterView<?> parent, View view,
        			int position, long id) {
        		
        		if (tc != null)
           		{
        			/* TODO implement second indicator to show contacts are trusted
        			 *  1. Contacts will show up with a check mark next to their name if at least 1 of their numbers is trusted. 
        			 *  2. Numbers in popup window have an indicator showing they are trusted
        			 *  3. Initialize the check box list if the lists are not null
        			 *  	- For numbers as well
        			 */
        			numbers = tc.get(position).getNumber();
        			if(numbers != null && numbers.size() > 0)
        			{
        				int index = -1;
        				if(numbers.size() == 1)
        				{
        					//Toast.makeText(getBaseContext(), "still clicking", Toast.LENGTH_SHORT).show();
        					//Contact only has a single number, check if that number is trusted
		        			if (!MessageService.dba.isTrustedContact(numbers.get(0).getNumber()))
		        			{
		        				//Toast.makeText(getApplicationContext(), "Contact added from\nTrusted Contacts", Toast.LENGTH_SHORT).show();
		        				
		        				//numbers.get(0).setPublicKey();
		        				index = Number.hasNumber(untrustedNumbers, numbers.get(0));
			        			if(index >= 0)
			        			{
			        				//Toast.makeText(getBaseContext(), "un- " + untrustedNumbers.get(index).getNumber(), Toast.LENGTH_LONG).show();
			        				untrustedNumbers.remove(index);
			        				selected[position] = false;
			        				//selected.remove(numbers.get(0).getNumber());
			        			}
			        			else
			        			{
			        				trustedNumbers.add(numbers.get(0));
			        				selected[position] = true;
			        				//selected.put(numbers.get(0).getNumber(), position);
			        				//Toast.makeText(getBaseContext(), "trust+ " + trustedNumbers.get(trustedNumbers.size()-1).getNumber(), Toast.LENGTH_LONG).show();
			        			}
			        		}
			        		else
			        		{
			        			//Toast.makeText(getApplicationContext(), "Contact remove from\nTrusted Contacts", Toast.LENGTH_SHORT).show();
			        			
			        			//numbers.get(0).clearPublicKey();
			        			index = Number.hasNumber(trustedNumbers, numbers.get(0));
			        			if(index >= 0)
			        			{
									//Toast.makeText(getBaseContext(), "trust- " + trustedNumbers.get(index).getNumber(), Toast.LENGTH_LONG).show();
			        				trustedNumbers.remove(index);
			        				selected[position] = false;
			        				//selected.remove(numbers.get(0).getNumber());
			        			}
			        			else  
			        			{
			        				untrustedNumbers.add(numbers.get(0));
			        				selected[position] = true;
			        				//selected.put(numbers.get(0).getNumber(), position);
			        				//Toast.makeText(getBaseContext(), "un+ " + untrustedNumbers.get(untrustedNumbers.size()-1).getNumber(), Toast.LENGTH_LONG).show();
			        			}
			        		}
        				}
        				else
        				{
        					final int contactIndex = position;
        					
        					/* Update the check box after the items in the sublist have been choosen */
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
        					
        					//TODO implement ListAdapter 
        					popup_builder.setTitle("Numbers")
        							//.setAdapter(adapter, listener)
        						   .setMultiChoiceItems(tc.get(contactIndex).getNumbers().toArray(new String[0]),
        						   //subSelected.get(contactIndex),
        					       subSelected.get(tc.get(contactIndex).getNumber(0)),
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
						        				//Toast.makeText(getBaseContext(), "un- " + sublistUntrust.get(index).getNumber(), Toast.LENGTH_LONG).show();
						        				sublistUntrust.remove(index);
						        				//subSelected.get(contactIndex)[index] = false;
						        				subSelected.get(tc.get(contactIndex).getNumber(0))[which] = true;
						        				//subSelected.remove(numbers.get(which).getNumber());
						        			}
						        			else
						        			{
						        				sublistTrust.add(numbers.get(which));
						        				//subSelected.get(contactIndex)[which] = true;
						        				subSelected.get(tc.get(contactIndex).getNumber(0))[which] = true;
						        				//subSelected.put(numbers.get(which).getNumber(), which);
						        				//Toast.makeText(getBaseContext(), "trust+ " + sublistTrust.get(sublistTrust.size()-1).getNumber(), Toast.LENGTH_LONG).show();
						        			}
										}
										else
										{
											//Remove from the sublist of numbers to exchange keys with
											index = Number.hasNumber(sublistTrust, numbers.get(0));
						        			if(index >= 0)
						        			{
						        				//Toast.makeText(getBaseContext(), "trust- " + sublistTrust.get(index).getNumber(), Toast.LENGTH_LONG).show();
						        				sublistTrust.remove(index);
						        				subSelected.get(tc.get(contactIndex).getNumber(0))[index] = false;
						        				//subSelected.remove(numbers.get(which).getNumber());
						        			}
						        			else
						        			{
						        				sublistUntrust.add(numbers.get(which));
						        				//subSelected.put(numbers.get(which).getNumber(), which);
						        				subSelected.get(tc.get(contactIndex).getNumber(0))[which] = false;
						        				//Toast.makeText(getBaseContext(), "un+ " + sublistUntrust.get(sublistUntrust.size()-1).getNumber(), Toast.LENGTH_LONG).show();
						        			}
										}
									}})
									.setPositiveButton("Okay", new DialogInterface.OnClickListener(){

										public void onClick(DialogInterface dialog, int which) {
											//Add the sublist to the full list of numbers to exchange keys with
											
											boolean checked = false;
											trustedNumbers.addAll(sublistTrust);
											untrustedNumbers.addAll(sublistUntrust);

											for(int i = 0; i < tc.get(contactIndex).getNumber().size(); i++)
											{
												if(subSelected.get(tc.get(contactIndex).getNumber(0))[i])
												{
													checked = true;
													break;
												}
											}
											
											listView.setItemChecked(contactIndex, checked);
											
											
											/*
											 * TODO identify whether at least one of the numbers is trusted
											 * IF it is then it should be added to the selected hash
											 * IF not then if a number has now been selected then it should be added
											 * IF no number is trusted and no number is selected then it should not be added 
											 */
												
											sublistTrust.clear();
											sublistUntrust.clear();
										}
									})
									.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

										public void onClick(DialogInterface dialog, int which) {
											//Sublist is not added to the full list
											sublistTrust.clear();
											sublistUntrust.clear();
											listView.setItemChecked(contactIndex, !notChecked);
											dialog.cancel();
										}
									})
        						   .setCancelable(true);
        					popup_alert = popup_builder.create();
        					popup_alert.show();
        				}
        				
        				//MessageService.dba.updateKey(tc.get(position),tc.get(position).getANumber());
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
        
        exchangeKeys.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				/*
				 * Launch Exchange Keys thread.
				 */
				
				//TODO Override to adjust cancel
				ExchangeKey.keyDialog = ProgressDialog.show(ManageContactsActivity.this, "Exchanging Keys", 
		                "Exchanging. Please wait...", true, false);
				
				keyThread.startThread(ManageContactsActivity.this, untrustedNumbers, trustedNumbers);
				
			}
        	
        });
	}

	/**
	 * Reinitializes the list to ensure contacts that are
	 * trusted are selected.
	 */
	private void initList()
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
	}

	/**
	 * Updates the list of contacts
	 */
	private void update()
	{
		if(tc != null)
		{
			listView.setAdapter(contactAdapter);
		}
		else
		{
			listView.setAdapter(arrayAp);
		}
		//listView.setItemsCanFocus(false);
		
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
			selected = new boolean[tc.size()];
			subSelected = new HashMap<String, boolean[]>();
			boolean[] tempArray; 
			int size = 0;
			//int count = 0;
	        for (int i = 0; i < tc.size(); i++)
	        {
	        	names[i] = tc.get(i).getName();
	        	selected[i] = false;
	        	size = tc.get(i).getNumber().size();
	        	if(size > 1)
	        	{
	        		//subSelected.add(new boolean[size]);
	        		tempArray = new boolean[size];
	        		for(int j = 0; j < size; j++)
	        		{
	        			//subSelected.get(count)[j] = false;
	        			tempArray[j] = false;
	        			//TODO change to use primary key from trusted contact table
	        			subSelected.put(tc.get(i).getNumber(0), tempArray);
	        		}
	        		//count++;
	        	}
	        }
	        //arrayAp = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, names);
	        
	        trusted = MessageService.dba.isTrustedContact(tc);
	        
	        //contactAdapter = new TrustedAdapter(this, android.R.layout.simple_list_item_multiple_choice, names, trusted);
	        contactAdapter = new TrustedAdapter(this, R.layout.trusted_contact_manage, names, trusted);

	        
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