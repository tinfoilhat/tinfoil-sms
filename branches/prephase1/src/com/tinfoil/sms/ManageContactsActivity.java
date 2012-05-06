package com.tinfoil.sms;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ManageContactsActivity extends Activity {
    /** Called when the activity is first created. */
	//private ListView mContactList;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact);
               
        ListView listView;					//The object which is populated with the items on the list
        
        ArrayList<TrustedContact> tc = Prephase1Activity.dba.getAllRows();
        
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
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				
		}});
	}
	
	
}