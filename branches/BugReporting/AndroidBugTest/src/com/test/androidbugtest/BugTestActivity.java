package com.test.androidbugtest;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class BugTestActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabtest);

        TabHost tabHost=getTabHost();
        // no need to call TabHost.Setup()        
        
        //First Tab
        TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setIndicator("Tab 1");
        Intent in1=new Intent(this, MainActivity_1.class);
        spec1.setContent(in1);
        
        TabSpec spec2=tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("Tab 2");
        Intent in2=new Intent(this, MainActivity_2.class);
        spec2.setContent(in2);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
    }
}