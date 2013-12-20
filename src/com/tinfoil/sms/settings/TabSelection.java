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

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.tinfoil.sms.R;
import com.tinfoil.sms.sms.KeyExchangeManager;

/**
 * The parent class for ManageContactsActivity. The first tab has contacts that
 * are not trusted by the user. The second tab has contacts who have been
 * trusted.
 *
 */
public class TabSelection extends TabActivity {

	public static final String EXCHANGE = "com.tinfoil.sms.settings.TabSelection.exchange";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tab_selection);
		
		TabHost tabHost=getTabHost();
        
        //First Tab
        TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setIndicator(this.getString(R.string.contacts_tab));
        Intent in1=new Intent(this, ManageContactsActivity.class);
        in1.putExtra(EXCHANGE, true);
        spec1.setContent(in1);
        
        //Second Tab
        TabSpec spec2=tabHost.newTabSpec("Tab 2");
        spec2.setIndicator(this.getString(R.string.trusted_contact_tab));
        Intent in2=new Intent(this, ManageContactsActivity.class);
        in2.putExtra(EXCHANGE, false);
        spec2.setContent(in2);
        
        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        
        //Second Tab
        TabSpec spec3=tabHost.newTabSpec("Tab 3");
        spec3.setIndicator(this.getString(R.string.pending_key_exchange_tab));
        Intent in3=new Intent(this, KeyExchangeManager.class);
        //in3.putExtra(EXCHANGE, false);
        spec3.setContent(in3);
        
        tabHost.addTab(spec3);
	}
}
