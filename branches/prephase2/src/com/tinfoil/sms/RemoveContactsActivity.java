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
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class RemoveContactsActivity extends Activity {
	private ListView listView;
	private boolean [] contact;
	private ArrayList<TrustedContact> tc;
	Button delete;
    /** Called when the activity is first created. */
	//private ListView mContactList;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remove_contacts);
        
        
        delete = (Button)findViewById(R.id.delete_cont);
        //Linking the ListView object to the appropriate listview from the xml file.
        listView = (ListView)findViewById(R.id.listView1);
        
        update();
        
        contact = new boolean 
        		[tc.size()];
        for (int i = 0; i < tc.size(); i++)
        {
        	contact[i] = false;
        }
        
        //Create what happens when you click on a button
        listView.setOnItemClickListener(new OnItemClickListener()
		{
        	public void onItemClick(AdapterView<?> parent, View view,
        			int position, long id) {
        		
        		if (!contact[position])
        		{
        			contact[position] = true;
        		}
        		else
        		{
        			contact[position] = false;
        		}
        		

        	}});
        
        delete.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				for (int i = 0; i < tc.size(); i++)
				{
					Toast.makeText(getBaseContext(), ""+ tc.get(i).getNumber(), Toast.LENGTH_LONG);
					if (contact[i])
					{
						Prephase2Activity.dba.removeRow(tc.get(i).getNumber());
					}
				}
				update();
				
			}});
	}
	
	/**
	 * Updates the list of contacts
	 */
	private void update()
	{
		String[] names;
		tc  = Prephase2Activity.dba.getAllRows();
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
	}
	
}

