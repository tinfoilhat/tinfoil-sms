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

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.tinfoil.sms.R;
import com.tinfoil.sms.utility.SMSUtility;
import com.tinfoil.sms.utility.Walkthrough;

public class QuickPrefsActivity extends PreferenceActivity {
    
	public static final String ENABLE_SETTING_KEY = "enable";
	public static final String ENABLE_WALKTHROUGH_SETTING_KEY = "enable_walkthrough";
	public static final String NATIVE_SAVE_SETTING_KEY = "native_save";
	public static final String MESSAGE_LIMIT_SETTING_KEY = "message_limit";
	public static final String IMPORT_CONTACT_SETTING_KEY = "import_contacts";
	public static final String MANAGE_CONTACTS_SETTING_KEY = "manage_contacts";
	public static final String SHOW_ENCRYPT_SETTING_KEY = "show_encrypt";
	public static final String PUBLIC_KEY_SETTING_KEY = "public_key";
	public static final String NOTIFICATION_BAR_SETTING_KEY = "notification_bar";
	public static final String VIBRATE_SETTING_KEY = "vibrate";
	public static final String VIBRATE_LENGTH_SETTING_KEY = "vibrate_length_settings";
	public static final String RINGTONE_SETTING_KEY = "ringtone_settings";
	public static final String SOURCE_CODE_SETTING_KEY = "source_code";
	public static final String REVERSE_MESSAGE_ORDERING_KEY = "list_order";
	//public static final String messageLimitKey = 
	
	/**
	 * Things done when the preference menu is created  
	 * Left as default
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);
        
        
        setupActionBar();
        /*
         * Add preferences from the options.xml file.
         */
        addPreferencesFromResource(R.xml.options);
        
        
        setKitKatPref();              
        
        PreferenceScreen sourceCode = (PreferenceScreen)findPreference(SOURCE_CODE_SETTING_KEY);
        sourceCode.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				   Intent i = new Intent(Intent.ACTION_VIEW);
	    		   i.setData(Uri.parse(QuickPrefsActivity.this.getString(R.string.tinfoil_sms_github)));
	    		   QuickPrefsActivity.this.startActivity(i);
				
	    		   return true;
			}
        	
        });
        
        EditTextPreference vibrateLength = (EditTextPreference)findPreference(VIBRATE_LENGTH_SETTING_KEY);
        vibrateLength.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				boolean ret = false;
								
				try{
					if(SMSUtility.isASmallNumber(newValue.toString())
							&& Integer.valueOf(newValue.toString()) > 0)
					{
						ret = true;
					}
				}
				catch(NumberFormatException e)
				{
					e.printStackTrace();
				}
				return ret;
			}
        	
        });
        
        //TODO implement the OnPreferenceChangeListener for the other preferences that use numbers only
        EditTextPreference messageLimit = (EditTextPreference)findPreference(MESSAGE_LIMIT_SETTING_KEY);
        messageLimit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				boolean ret = false;	
				
				try{
					if(SMSUtility.isASmallNumber(newValue.toString())
							&& Integer.valueOf(newValue.toString()) > 0)
					{
						ret = true;
					}
				}
				catch(NumberFormatException e)
				{
					e.printStackTrace();
				}
				return ret;
			}        
        });
        
        findPreference("enable_walkthrough").setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                // If walkthrough enabled, reset all the steps so they are displayed again
                if (Boolean.valueOf(newValue.toString()))
                {
                    Walkthrough.enableWalkthrough(QuickPrefsActivity.this);
                }
                else
                {
                    Walkthrough.disableWalkthrough(QuickPrefsActivity.this);
                }
                return true;
            }
        });
        
        /* Set an onclick listener for contact developers */
        findPreference("contact").setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                /**
                 * Create the Intent
                 */
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                /**
                 * Fill it with Data
                 */
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, QuickPrefsActivity.this
                        .getResources().getStringArray(R.array.dev_emails));
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_subject));
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                
                /**
                 * Send it off to the Activity-Chooser
                 */
                QuickPrefsActivity.this.startActivity(Intent.createChooser(emailIntent,
                        QuickPrefsActivity.this.getResources().getString(R.string.email_chooser)));
                return true;
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "Show current settings");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
		default:
            return super.onOptionsItemSelected(item);
    	}
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
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void setKitKatPref()
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
    	{
        	CheckBoxPreference p = (CheckBoxPreference)findPreference(NATIVE_SAVE_SETTING_KEY);
        	p.setChecked(false);
        	p.setEnabled(false);
    	}  
	}
}