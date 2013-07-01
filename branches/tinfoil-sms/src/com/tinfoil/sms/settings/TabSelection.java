package com.tinfoil.sms.settings;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.tinfoil.sms.R;

public class TabSelection extends TabActivity {

	public static final String EXCHANGE = "com.tinfoil.sms.settings.TabSelection.exchange";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tab_selection);
		
		TabHost tabHost=getTabHost();
        // no need to call TabHost.Setup()  
        
        //First Tab
        TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setIndicator("Contacts");
        Intent in1=new Intent(this, ManageContactsActivity.class);
        in1.putExtra(EXCHANGE, true);
        spec1.setContent(in1);
        
        TabSpec spec2=tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("Trusted Contacts");
        
        Intent in2=new Intent(this, ManageContactsActivity.class);
        in2.putExtra(EXCHANGE, false);
        spec2.setContent(in2);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tab_selection, menu);
		return true;
	}

}
