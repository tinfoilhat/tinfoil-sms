package com.tinfoil.sms;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ImportContacts extends Activity {
	Button confirm;
	 
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.importcontacts);
        
        confirm = (Button) findViewById(R.id.confirm);
        ListView listView = (ListView)findViewById(R.id.listView1);					//The object which is populated with the items on the list
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        
        //String id ="";
		final ArrayList <String> names = new ArrayList<String>();
		if (cur.getCount() > 0) {
		    while (cur.moveToNext()) {
		        /*id = cur.getString(
	                        cur.getColumnIndex(ContactsContract.Contacts._ID));*/
		        names.add(cur.getString(
	                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
		       /*if (Integer.parseInt(cur.getString(
	 				cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
 					Cursor pCur = cr.query(
	 	 		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
	 	 		    null, 
	 	 		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
	 	 		    new String[]{id}, null);
	 	 	        while (pCur.moveToNext()) {
	 	 		    // Do something with phones
	 	 	        } 
	 	 	        pCur.close();
	 	        }*/
 			}
	 	}
		
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, names));
		
        listView.setItemsCanFocus(false);
        
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        listView.setOnItemClickListener(new OnItemClickListener(){
        	public void onItemClick(AdapterView<?> parent, View view,
        			int position, long id) {
        		//Keep track of the contacts selected.
        	}
        	
        });
                
        confirm.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				//Add Contacts to the tinfoil-sms database from android's database
				
			}
		});       
        
	}
	
}
