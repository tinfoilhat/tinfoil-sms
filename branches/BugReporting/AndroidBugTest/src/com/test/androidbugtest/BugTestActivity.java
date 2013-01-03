package com.test.androidbugtest;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;

public class BugTestActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug_test);
        
        /* Test Crash */
        String derpity = null;
        Toast.makeText(this, derpity.length(), Toast.LENGTH_LONG).show();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_bug_test, menu);
        return true;
    }
}