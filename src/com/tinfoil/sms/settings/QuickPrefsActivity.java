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

import com.tinfoil.sms.R;
import com.tinfoil.sms.utility.SMSUtility;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;

public class QuickPrefsActivity extends PreferenceActivity {
    
	public static final String ENABLE_SETTING_KEY = "enable";
	public static final String NATIVE_SAVE_SETTING_KEY = "native_save_settings";
	public static final String MESSAGE_LIMIT_SETTING_KEY = "message_limit";
	public static final String IMPORT_CONTACT_SETTING_KEY = "import_contacts";
	public static final String MANAGE_CONTACTS_SETTING_KEY = "manage_contacts";
	public static final String SHOW_ENCRYPT_SETTING_KEY = "show_encrypt";
	public static final String PUBLIC_KEY_SETTING_KEY = "public_key";
	public static final String NOTIFICATION_BAR_SETTING_KEY = "notification_bar";
	public static final String VIBRATE_SETTING_KEY = "vibrate";
	public static final String VIBRATE_LENGTH_SETTING_KEY = "vibrate_length_settings";
	public static final String RINGTONE_SETTING_KEY = "ringtone_settings";
	public static final String BUGSENSE_ENABLE_SETTING_KEY = "bugsense_enable";
	public static final String SOURCE_CODE_SETTING_KEY = "source_code";
	//public static final String messageLimitKey = 
	
	/**
	 * Things done when the preference menu is created  
	 * Left as default
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);
        
        /*
         * Add preferences from the options.xml file.
         */
        addPreferencesFromResource(R.xml.options);
        
        
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
        
        //TODO implement the OnPreferenceChangeListener for the other preferences that use numbers only
        EditTextPreference messageLimit = (EditTextPreference)findPreference(MESSAGE_LIMIT_SETTING_KEY);
        messageLimit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
				if(SMSUtility.isANumber(newValue.toString()) && Integer.valueOf(newValue.toString()) >0)
				{
					return true;
				}
				return false;
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
        return true;
    }
    
}