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
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
* RemoveContactActivity is an activity that allows for contacts to be
* deleted from tinfoil-sms's database.
* ***Please note that contacts will not be deleted from the native database. 
*/
public class RemoveContactsActivity extends Activity  implements Runnable {
	private ListView listView;
	private boolean [] contact;
	private ArrayList<TrustedContact> tc;
	//private ArrayList<Contact> cont;
	private Button delete;
	private ProgressDialog dialog;
	private boolean clicked = false;
	private ArrayAdapter<String> appAdapt;
	
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remove_contacts);
        clicked = false;
        
        delete = (Button)findViewById(R.id.delete_cont);

        listView = (ListView)findViewById(R.id.removeable_contact_list);
        
        dialog = ProgressDialog.show(this, "Loading Contacts", 
                "Loading. Please wait...", true, false);
        
        //update();
        Thread thread = new Thread(this);
        thread.start();
        
        //Create what happens when you click on a button
        listView.setOnItemClickListener(new OnItemClickListener()
		{
        	public void onItemClick(AdapterView<?> parent, View view,
        			int position, long id) {
        		
        		toggle(position);
        		
        	}});

        delete.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				if (tc != null)
				//if (cont != null)
				{
					clicked = true;
					dialog = ProgressDialog.show(RemoveContactsActivity.this, "Deleting Contacts", 
				                "Deleting. Please wait...", true, false);
					Thread thread2 = new Thread(RemoveContactsActivity.this);
			        thread2.start();
					
					//update();
				}
				
			}});
	}
	
	/**
	 * Toggle the contact's status from to be deleted to not be deleted
	 * or from not be deleted to be deleted
	 * @param i : the contact that is selected
	 */
	public void toggle(int i)
	{
		if (!contact[i])
		{
			contact[i] = true;
		}
		else
		{
			contact[i] = false;
		}
	}
	
	/**
	 * Updates the list of contacts
	 */
	private void update()
	{
		String[] names;
		tc  = MessageService.dba.getAllRows();
		//cont = MessageService.dba.getAllRowsLimited();
		
		if (tc != null)
		//if (cont != null)
		{
			//The string that is displayed for each item on the list 
	        names = new String[tc.size()];
			//names = new String[cont.size()];
	        for (int i = 0; i < tc.size(); i++)
			//for (int i = 0; i < cont.size(); i++)
	        {
	        	names[i] = tc.get(i).getName();
				//names[i] = cont.get(i).getName();
	        }
			appAdapt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, names);
		}
		else 
		{
			names = new String[] {"No Contacts"};
			appAdapt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
		}
		
	}
		
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.remove_contacts_menu, menu);
		return true;		
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.all:
			if (tc!=null)
			//if (cont!=null)
			{
				for (int i = 0; i < tc.size();i++)
				//for (int i = 0; i < cont.size();i++)
				{
					listView.setItemChecked(i, true);
					contact[i] = true;
				}
			}
			return true;
		case R.id.remove:
			if (tc!=null)
			//if (cont!=null)
			{
				for (int i = 0; i < tc.size();i++)
				//for (int i = 0; i < cont.size();i++)
				{
					listView.setItemChecked(i, false);
					contact[i] = false;
				}
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	public void run() {
		if (clicked)
		{
			//for (int i = 0; i < cont.size(); i++)
			for (int i = 0; i < tc.size(); i++)
			{
				if (contact[i])
				{
					MessageService.dba.removeRow(tc.get(i).getANumber());
					//MessageService.dba.removeRow(cont.get(i).getNumber());
				}
			}
		}
			
		update();
		//if (cont != null)
		if (tc != null)
		{
			//contact = new boolean [cont.size()];
			contact = new boolean [tc.size()];
	        //for (int i = 0; i < cont.size(); i++)
			for (int i = 0; i < tc.size(); i++)
	        {
	        	contact[i] = false;
	        }
		}
        handler.sendEmptyMessage(0);
	}
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
        	listView.setAdapter(appAdapt);
        	//if (cont!= null)
        	if (tc!= null)
        	{
        		
		        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        	}
        	listView.setItemsCanFocus(false);
        	
        	dialog.dismiss();
        }
	};
	
}

