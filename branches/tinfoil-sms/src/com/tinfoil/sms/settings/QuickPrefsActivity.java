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

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class QuickPrefsActivity extends PreferenceActivity {
    
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
        
        //TODO implement the OnPreferenceChangeListener for the other preferences that use numbers only
        EditTextPreference messageLimit = (EditTextPreference)findPreference("message_limit");
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