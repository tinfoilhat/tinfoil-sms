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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.tinfoil.sms.R;
import com.tinfoil.sms.crypto.KeyExchange;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.SMSUtility;

/**
 * TODO add remove initiator flag.
 * An activity used to edit a single number from a given contact. 
 * 
 * This activity returns a AddContact.RESULT_CODE if changes have been made and
 * the list needs to be update, else returns default result code.
 * 
 * The Extras are included are:
 * EditNumber.UPDATE, whether the number has been updated or not
 * EditNumber.NUMBER, the number that has been edited
 * AddContact.POSITION, the position of the given number in the contact's
 * numbers array list, -1 if it is a new number
 * EditNumber.ADD, whether the number is to update or deleted
 */
public class EditNumber extends Activity{
    
	public static final String UPDATE = "update";
	public static final String NUMBER = "number";
	public static final String ADD = "add";
	public static final String DELETE = "delete";
	public static final String IS_DELETED = "is_deleted";
	
	private EditText phoneNumber;
	private EditText sharedInfo1;
	private EditText sharedInfo2;
	private EditText bookPath;
	private EditText bookInverse;
	private EditText pubKey;
	private Number number;
	private String originalNumber;
	private static int position;
	private String name;
	
	public static final int SHARED_INFO_MIN = 6;
	public static final int SHARED_INFO_MAX = 128;
	
	private ArrayList<RadioButton> keyExchangeSetting;
	private DBAccessor dba;
	
	@Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.edit_number);
        setupActionBar();
        
        dba = new DBAccessor(this);

        keyExchangeSetting = new ArrayList<RadioButton>();
        
        keyExchangeSetting.add((RadioButton)this.findViewById(R.id.auto_exchange));
        keyExchangeSetting.add((RadioButton)this.findViewById(R.id.manual_exchange));
        keyExchangeSetting.add((RadioButton)this.findViewById(R.id.ignore_exchange));
        
        phoneNumber = (EditText)findViewById(R.id.phone_number);
        sharedInfo1 = (EditText)findViewById(R.id.shared_secret_1);
        sharedInfo2 = (EditText)findViewById(R.id.shared_secret_2);
        bookPath = (EditText)findViewById(R.id.book_path);
        bookInverse = (EditText)findViewById(R.id.book_inverse);
        
        
        pubKey = (EditText)findViewById(R.id.contact_pub_key);
        boolean trusted = false;
        
        Intent intent = this.getIntent();
        
        /* Check if there is intent */
        if(intent != null)
        {
        	/*
        	 * Get the extra values in the intent.
        	 */
        	name = intent.getStringExtra(AddContact.NAME);
        	originalNumber = intent.getStringExtra(AddContact.EDIT_NUMBER);
        	position = intent.getIntExtra(AddContact.POSITION, AddContact.NEW_NUMBER_CODE); 
        	this.getIntent().removeExtra(AddContact.NAME);
            this.getIntent().removeExtra(AddContact.EDIT_NUMBER);
            this.getIntent().removeExtra(AddContact.POSITION);
        }
        else
        {
        	finish();
        }
        
        /*
         * Is the number a new number
         */
        if(position != AddContact.NEW_NUMBER_CODE)
        {
        	/*
        	 * Initialize the values to be adjusted
        	 */
        	number = dba.getNumber(SMSUtility.format(originalNumber));
        	
        	// Number was not found in the database
        	if(number == null) {
        		finish();
        	}
        
	        phoneNumber.setText(originalNumber);        
	        
	        sharedInfo1.setText(number.getSharedInfo1());
	        
	        sharedInfo2.setText(number.getSharedInfo2());
	        
	        bookPath.setText(number.getBookPath());
	        
	        bookInverse.setText(number.getBookInversePath());

	        keyExchangeSetting.get(number.getKeyExchangeFlag()).setChecked(true);
	        
	        if(dba.isTrustedContact(originalNumber))
	        {
	        	trusted = true;
	        }
        }
        else
        {
        	/*
        	 * Initialize the values to the default values
        	 */	        
	        bookPath.setText(DBAccessor.DEFAULT_BOOK_PATH);
	        
	        bookInverse.setText(DBAccessor.DEFAULT_BOOK_INVERSE_PATH);
        }
        
        if(trusted)
        {
        	TextView pubKeyView = (TextView)findViewById(R.id.view_pub_key);
        	
        	sharedInfo1.setEnabled(false);
        	sharedInfo2.setEnabled(false);
        	phoneNumber.setEnabled(false);
        	//pubKeyTitle.setVisibility(TextView.VISIBLE);
        	pubKey.setVisibility(EditText.INVISIBLE);
        	
        	pubKeyView.setVisibility(TextView.VISIBLE);
        	pubKeyView.setText(new String(number.getPublicKey()));
        	
        	// Find the radio button group view
        	View rg = (View)findViewById(R.id.radioGroup1);
        	
        	/*
        	 * Set it so now it is below book_inverse since the other two
        	 * elements are invisible.
        	 */
        	RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        	        ViewGroup.LayoutParams.WRAP_CONTENT);
        	p.addRule(RelativeLayout.BELOW,R.id.view_pub_key);
        	rg.setLayoutParams(p);
        }
	}
	
	/**
     * The onClick action for when the user clicks on save information
     * @param view The view that is involved
     */
	public void saveNumberInfo(View view)
	{
		String s1 = sharedInfo1.getText().toString();
		String s2 = sharedInfo2.getText().toString();
		if (((s1 == null || s1.length() == 0) && (s2 == null || s2.length() == 0)) ||
				(SMSUtility.checksharedSecret(s1) && SMSUtility.checksharedSecret(s2)))
		{
			/* Is there a valid number */
			if(phoneNumber.getText().toString() != null &&
					phoneNumber.getText().toString().length() > 0)
			{
				//Number tempNumber;
				TrustedContact tc = null;
				
				if(number == null)
				{
					/* Number is a new number */
					number = new Number(phoneNumber.getText().toString());
				}
				else
				{
					/*
					 * Editing the current number. Get the Number row
					 * for the previous number.
					 */
					
					number.setNumber(phoneNumber.getText().toString());
				}
				
				/*
				 * Set the updated information
				 */
				//String newSharedInfo1 = s1;
				//String newSharedInfo2 = s2;
				
				if(SMSUtility.isChanged(number.getSharedInfo1(), s1) ||
						SMSUtility.isChanged(number.getSharedInfo2(), s2))
				{
					number.setInitiator(false);
				}
				
				number.setSharedInfo1(s1);
				number.setSharedInfo2(s2);
				number.setBookPath(bookPath.getText().toString());
				number.setBookInversePath(bookInverse.getText().toString());
				
				if(pubKey.getVisibility() == EditText.VISIBLE)
				{
					String key = pubKey.getText().toString().trim();
					if(key != "" && key.length() > 0)
					{
						number.setPublicKey(key.getBytes());
					}
				}
				
				int index = 0;
				if(keyExchangeSetting.get(Number.AUTO).isChecked())
				{
					index = Number.AUTO;
				}
				else if(keyExchangeSetting.get(Number.MANUAL).isChecked())
				{
					index = Number.MANUAL;
				}
				else
				{
					index = Number.IGNORE;
				}
				number.setKeyExchangeFlag(index);
				
				
				/*
				 * Update/Add the number to the database
				 */
				if(originalNumber != null)
				{
					if(position == AddContact.NEW_NUMBER_CODE)
					{							
						if(checkUniqueNumber())
						{
							tc = dba.getRow(originalNumber);
							tc.addNumber(number);
							dba.updateRow(tc, originalNumber);
						}
						else
						{
							finish();
						}
					}
					else
					{
						if(number.getNumber().equals(originalNumber) || checkUniqueNumber())
						{
							dba.updateNumberRow(number, originalNumber, 0);
						}
						else
						{
							finish();
						}
					}
				}
				else
				{
					if(checkUniqueNumber())
					{
						tc = new TrustedContact();
						tc.addNumber(number);
						dba.addRow(tc);
					}
					else
					{
						finish();
					}
				}
				
				
				Intent data = new Intent();
				
				/*
				 * Return intent with given parameters 
				 */
				if(originalNumber != null && number.getNumber().equalsIgnoreCase(originalNumber))
				{					
					data.putExtra(EditNumber.UPDATE, false);					
				}
				else
				{
					data.putExtra(EditNumber.UPDATE, true);
				}
				
				data.putExtra(EditNumber.NUMBER, number.getNumber());
				data.putExtra(AddContact.POSITION, position);
				data.putExtra(EditNumber.ADD, true);
				data.putExtra(AddContact.NAME, name);
				
				/*
				 * Set result code to identify that whether the list in
				 * ManageContactsActivity.
				 */
				EditNumber.this.setResult(AddContact.REQUEST_CODE, data);
	
				EditNumber.this.finish();
			}
		}
		else
		{
			//TODO create a better notification for invalid shared secrets (since they key board blocks them)
			Toast.makeText(this, this.getString(R.string.shared_secret)
					+ SHARED_INFO_MIN, Toast.LENGTH_LONG).show();
		}
	}

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
    	final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.edit_text_menu, menu);
        return true;
    }
    
    private String[] fileNames;
    private AlertDialog popup_alert;
    
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
    	   	
        switch (item.getItemId()) {
	        case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
            case R.id.import_key:
           	
            	importKey();
            	return true;
            	
            case R.id.delete:
            	
            	deleteNumber();
            	return true;            
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public static void exportOrSend(Context context,final Number number)
    {
    	final DBAccessor dba = new DBAccessor(context);
    	final String name = dba.getRow(number.getNumber()).getName();
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setMessage(context.getString(R.string.key_exchange_respond)
    			+ " " + name + ", " + number.getNumber() + "?")
		    .setCancelable(true)
		    .setPositiveButton(R.string.sms_option, new DialogInterface.OnClickListener() {
	    	   @Override
	    	   public void onClick(DialogInterface dialog, int id) {
	    		   dba.addMessageToQueue(number.getNumber(),
							KeyExchange.sign(number, dba,
							SMSUtility.user), true);
		    }})
		    .setNegativeButton(R.string.export_option, new DialogInterface.OnClickListener() {
	    	   @Override
	    	   public void onClick(DialogInterface dialog, int id) {
	    		   UserKeySettings.writeToFile(number.getNumber(), KeyExchange.sign(number, 
	    				   dba, SMSUtility.user));
	    	   }
		    });
		
 		AlertDialog alert = builder.create();
 		alert.show();
    }
    
    private boolean checkUniqueNumber()
    {
    	if(dba.inDatabase(number.getNumber()))
    	{  	
	    	Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED, returnIntent);
			
			Toast.makeText(this, R.string.number_exists, Toast.LENGTH_SHORT).show();
			
			return false;
    	}
    	return true;
    }
    
    /**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	private void deleteNumber()
	{
		Intent data = new Intent();
    	
    	if (originalNumber == null || originalNumber == "" || dba.getRow(originalNumber) == null)
    	{
    		// User deleted the number so 
    		finish();
    	}            		
    	else {
        	if (dba.getRow(originalNumber).getNumbers().size() == 1)
        	{
        		dba.removeRow(originalNumber);
        		data.putExtra(EditNumber.IS_DELETED, true);
        	}
        	else
        	{
        		dba.deleteNumber(originalNumber);
        	}
    	}
    	
    	data.putExtra(EditNumber.UPDATE, true);
		data.putExtra(AddContact.POSITION, position);
		data.putExtra(EditNumber.ADD, true);
    	data.putExtra(EditNumber.DELETE, originalNumber);
		setResult(AddContact.REQUEST_CODE, data);
		
		finish();
	}

	private void importKey()
	{
		if (originalNumber == null || originalNumber == "" || dba.getRow(originalNumber) == null)
    	{
    		Toast.makeText(getApplicationContext(), R.string.number_must_exist, Toast.LENGTH_LONG).show();
    	}
    	else
    	{	            	
        	if(SMSUtility.isMediaAvailable() && number != null && SMSUtility.checksharedSecret(number.getSharedInfo1()) 
        			&& SMSUtility.checksharedSecret(number.getSharedInfo2()))
        	{
        		final File root = Environment.getExternalStorageDirectory();
        		
        		File dir = new File(root.getAbsolutePath() + UserKeySettings.path + "/"); 
        		File[] contents = dir.listFiles();
        		
        		if(contents.length > 0) {
	        		fileNames = new String[contents.length];
	        		for (int i = 0; i < contents.length; i++)
	        		{
	        			fileNames[i] = contents[i].getAbsolutePath();
	        		}
	        		
	    			
	    			final AlertDialog.Builder popup_builder = new AlertDialog.Builder(this);
	    			
	    			popup_builder.setTitle(R.string.export_number_title)
	    				.setCancelable(true)
	                    .setSingleChoiceItems(fileNames, 0, new DialogInterface.OnClickListener() {
	
	                    	public void onClick(final DialogInterface dialog, final int which) { 
	                    		
	                    		popup_alert.dismiss();
	                    		String selectedFile = (String) fileNames[which];
	                    		
	                    		if(selectedFile != null)
	                    		{                        			
	                    			StringBuilder sb = new StringBuilder();
	                    			
	                    			File pubKey = new File(selectedFile);
	                        		
	                        		try {
	            						FileInputStream f = new FileInputStream(pubKey);
	            						
	            						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(f));
	            					    String line;
	            					    while ((line = bufferedReader.readLine()) != null) {
	            					        sb.append(line);
	            					    }
	            					    bufferedReader.close();
	            					    f.close();
	            					} catch (FileNotFoundException e) {
	            						e.printStackTrace();
	            						BugSenseHandler.sendExceptionMessage("Type", "Import Public Key Not Found Error", e);
	            					} catch (IOException e) {
	            						e.printStackTrace();
	            						BugSenseHandler.sendExceptionMessage("Type", "Import Public Key Error", e);
	            					}
	                       		
	                        		String keyExchangeMessage = sb.toString();
	                        		
	                        		if(KeyExchange.isKeyExchange(keyExchangeMessage))
	                        		{
	                        			if(KeyExchange.verify(number, keyExchangeMessage))
	            						{
	                        				Log.v("Key Exchange", "Exchange Key Message Received");
	            			                
	            							number.setPublicKey(KeyExchange.encodedPubKey(keyExchangeMessage));
	            							number.setSignature(KeyExchange.encodedSignature(keyExchangeMessage));
	            							
	            							dba.updateNumberRow(number, number.getNumber(), 0);
	            							Toast.makeText(EditNumber.this, R.string.key_exchange_import, Toast.LENGTH_SHORT).show();
	            							
	            							if(!number.isInitiator())
											{
												Log.v("Key Exchange", "Not Initiator");
												//MessageService.dba.addMessageToQueue(number.getNumber(),
													//	KeyExchange.sign(number), true);
												exportOrSend(EditNumber.this, number);
											}
											
	            							EditNumber.this.setResult(AddContact.UPDATED_NUMBER);
	            						}
	                        		}
	    	            			
	                    		}
	                    	}
	                    });
	    			
	    			popup_alert = popup_builder.create();
	    			popup_alert.show();
	        	}
        		else {
        			Toast.makeText(this, R.string.empty_key_exchange_list, Toast.LENGTH_SHORT).show();
        		}
        	}
    	}
	}
	
}
