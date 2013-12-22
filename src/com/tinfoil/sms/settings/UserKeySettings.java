/** 
 * Copyright (C) 2013 Jonathan Gillett, Joseph Heron
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

package com.tinfoil.sms.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.tinfoil.sms.R;
import com.tinfoil.sms.crypto.KeyExchange;
import com.tinfoil.sms.crypto.KeyGenerator;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.dataStructures.User;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;

public class UserKeySettings extends Activity {

	private ArrayList<TrustedContact> tc;
	private AlertDialog popup_alert;
	private AutoCompleteTextView phoneBook;
	
	public static final String path = "/keys";
	public static final String file = "exchange.txt";
	
	private DBAccessor dba;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_key_settings);
		
		dba = new DBAccessor(this);
		TextView keyView = (TextView)findViewById(R.id.public_key);
		
		// Check if the user is null
		if(SMSUtility.user == null)
		{
			SMSUtility.user = dba.getUserRow();
		}
		
		//Check if the user is still null (never set in db)	
		if(SMSUtility.user == null)
		{
			// Generate the user's public key
			KeyGenerator keyGen = new KeyGenerator();
	        
	        SMSUtility.user = new User(keyGen.generatePubKey(), keyGen.generatePriKey());
	        
	        //Set the user's 
	        dba.setUser(SMSUtility.user);
		}
		
		keyView.setText(new String(SMSUtility.user.getPublicKey()));
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_key_settings, menu);
		return true;
	}*/
	
	public void exportKey(View view)
	{
		if(SMSUtility.isMediaWritable())
		{
			phoneBook = new AutoCompleteTextView(this);
			List<String> contact = null;
            if (tc == null)
            {
            	//Do in thread.
                tc = dba.getAllRows(DBAccessor.ALL);
            }

            if (tc != null)
            {
                if (contact == null)
                {
                    contact = SMSUtility.contactDisplayMaker(tc);
                }
            }
            else
            {
                contact = null;
            }
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getBaseContext(), R.layout.auto_complete_list_item, contact);

            phoneBook.setAdapter(adapter);
			
			final AlertDialog.Builder popup_builder = new AlertDialog.Builder(this);
			popup_builder.setTitle(R.string.import_contacts_title)
				.setCancelable(true)
                .setView(phoneBook)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

                	public void onClick(final DialogInterface dialog, final int which) { 
                		
                		String[] contactInfo = SMSUtility.parseAutoComplete(phoneBook.getText().toString());
                		//String number = null;
                		
                		
                		boolean invalid = false;
                		if(contactInfo != null)
                		{
                			if(contactInfo[0] == null)
                			{
                				contactInfo[0] = contactInfo[1];
                			}
                			
                			final Number number = dba.getNumber(contactInfo[1]);
                			
                			if(number != null)
                			{                			
	                			AlertDialog.Builder builder = new AlertDialog.Builder(UserKeySettings.this);
	                			LinearLayout linearLayout = new LinearLayout(UserKeySettings.this);
	                			linearLayout.setOrientation(LinearLayout.VERTICAL);
	
	                			final EditText sharedSecret1 = new EditText(UserKeySettings.this);
	                			sharedSecret1.setHint(UserKeySettings.this.getString(R.string.shared_secret_hint_1));
	                			sharedSecret1.setMaxLines(EditNumber.SHARED_INFO_MAX);
	                			sharedSecret1.setInputType(InputType.TYPE_CLASS_TEXT);
	                			linearLayout.addView(sharedSecret1);
	
	                			final EditText sharedSecret2 = new EditText(UserKeySettings.this);
	                			sharedSecret2.setHint(UserKeySettings.this.getString(R.string.shared_secret_hint_2));
	                			sharedSecret2.setMaxLines(EditNumber.SHARED_INFO_MAX);
	                			sharedSecret2.setInputType(InputType.TYPE_CLASS_TEXT);
	                			linearLayout.addView(sharedSecret2);
	                			
	                			builder.setMessage(UserKeySettings.this.getString(R.string.set_shared_secrets)
	                					+ " " + contactInfo[0] + ", " + number.getNumber())
	                			   .setTitle(R.string.set_shared_secrets_title)
	                		       .setCancelable(true)
	                		       .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
	                		    	   @Override
	                		    	   public void onClick(DialogInterface dialog, int id) {
	                		               //Save the shared secrets
	                		    		   
	                		    		   String s1 = sharedSecret1.getText().toString();
	                		    		   String s2 = sharedSecret2.getText().toString();
	                		    		   if(SMSUtility.checksharedSecret(s1) &&
	                								SMSUtility.checksharedSecret(s2))
	                		    		   {
	                		    			   number.setSharedInfo1(s1);
	                		    			   number.setSharedInfo2(s2);
	                		    			   dba.updateNumberRow(number, number.getNumber(), number.getId());
	
	                		    			   number.setInitiator(true);
	                		    			   dba.updateInitiator(number);
		       		                			
	                		    			   //TODO add check for shared secrets
	                		    			   String keyExchangeMessage = KeyExchange.sign(number,
	                		    					   dba, SMSUtility.user);
		       		
	                		    			   writeToFile(number.getNumber(), keyExchangeMessage);
		       			            			
	                		    			   Toast.makeText(UserKeySettings.this, UserKeySettings.this.getString(R.string.written_path)
	                		    					   + " " + path + "/" + number.getNumber() + "_" + file, Toast.LENGTH_SHORT).show();
	       			            			
	                		    		   }
	                		    		   else
	                		    		   {
	                		    			   Toast.makeText(UserKeySettings.this, R.string.invalid_secrets, Toast.LENGTH_LONG).show();
	                		    		   }
	                		    }})
			    		 	       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			    		 	    	   @Override
			    		 	    	   public void onClick(DialogInterface arg0, int arg1) {
			    		 	    		   	//Cancel the key exchange
			    		 	    		   Toast.makeText(UserKeySettings.this, R.string.key_exchange_cancelled, Toast.LENGTH_LONG).show();
			    		 	    	   }});
			    		 		AlertDialog alert = builder.create();
			    		 		
			    		 		alert.setView(linearLayout);
			    		 		alert.show();
                			}
                			else
                			{
                				invalid = true;
                			}
                		}
                		else
                		{
                			invalid = true;
                		}
                		
                		if(invalid)
                		{
                			Toast.makeText(UserKeySettings.this, R.string.invalid_number_message, Toast.LENGTH_LONG).show();
                		}
                	}
                });
			
			popup_alert = popup_builder.create();
			popup_alert.show();
			
			//getExternalFilesDir(null);
		}
	}
	
	public static void writeToFile(String name, String text)
	{
		File root = Environment.getExternalStorageDirectory();
		
		File keys = new File(root.getAbsolutePath() + path);
		keys.mkdirs();
		
		File pubKey = new File(keys, name + "_" + file);
		
		try {
			FileOutputStream f  = new FileOutputStream(pubKey);
			PrintWriter pw = new PrintWriter(f);
			pw.println(text);
			pw.flush();
			pw.close();
			f.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			BugSenseHandler.sendExceptionMessage("Type", "Export Public Key Not Found Error", e);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			BugSenseHandler.sendExceptionMessage("Type", "Export Public Key Error", e);
		}
	}
}
