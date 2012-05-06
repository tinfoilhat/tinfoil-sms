package com.tinfoil.sms;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ManageContactsActivity extends Activity {
    /** Called when the activity is first created. */
	//private ListView mContactList;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact);
               
        ListView listView;					//The object which is populated with the items on the list
        
        final ArrayList<TrustedContact> tc = Prephase1Activity.dba.getAllRows();
        
      //The string that is displayed for each item on the list 
        String[] names = new String[tc.size()];
        for (int i = 0; i < tc.size(); i++)
        {
        	names[i] = tc.get(i).getName();
        }
        
 
        //Linking the ListView object to the appropriate listview from the xml file.
        listView = (ListView)findViewById(R.id.listView1);
        
        //populates listview with the declared strings, an option is also given for it to be multiple choice (check boxes), or single list (radio buttons) 
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, names));
        
        //Not setting focus on a particular list item, (focus is then left to default at the top of the page)
        listView.setItemsCanFocus(false);
        
        //Set the mode to single or multiple choice, (should match top choice)
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                
        //Create what happens when you click on a button
        listView.setOnItemClickListener(new OnItemClickListener()
		{
        	public void onItemClick(AdapterView<?> parent, View view,
        			int position, long id) {
        		//Note: the key does not actually affect the encryption currently
        		//In order to send a encrypted message a contact must have a key
        		final String key = "12345";
        		final int verify = 2;
        		
        		
        		Toast.makeText(getApplicationContext(), tc.get(position).getKey(), Toast.LENGTH_SHORT).show();
        		if (Prephase1Activity.dba.isTrustedContact(tc.get(position).getNumber()))
        		{
        			//Prephase1Activity.dba.updateKey(tc.get(position).getNumber(), null);
        			tc.get(position).setKey(null);
        			tc.get(position).setVerified(0);
        			Prephase1Activity.dba.removeRow(tc.get(position).getNumber());
        			Prephase1Activity.dba.addRow(tc.get(position));
        			//Prephase1Activity.dba.updateVerified(tc.get(position).getNumber(), 0);
        			Toast.makeText(getApplicationContext(), "Contact removed from\nTrusted Contacts", Toast.LENGTH_SHORT).show();
        		}
        		else
        		{
        			tc.get(position).setKey(key);
        			tc.get(position).setVerified(verify);
        			Prephase1Activity.dba.removeRow(tc.get(position).getNumber());
        			Prephase1Activity.dba.addRow(tc.get(position));
        			Toast.makeText(getApplicationContext(), "Contact added from\nTrusted Contacts", Toast.LENGTH_SHORT).show();
        		}
				
        	}});
	}
	
	
}