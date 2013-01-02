package com.example.bugtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.j2bugzilla.base.Bug;
import com.j2bugzilla.base.BugFactory;
import com.j2bugzilla.base.BugzillaConnector;
import com.j2bugzilla.base.BugzillaException;
import com.j2bugzilla.base.ConnectionException;
import com.j2bugzilla.rpc.LogIn;
import com.j2bugzilla.rpc.ReportBug;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Create new connection
        BugzillaConnector conn = new BugzillaConnector();
        
        BugFactory factory = new BugFactory();
        
        Map<String, Object> bugMap = new HashMap<String, Object>();

        bugMap.put("bug_id", 1);
        bugMap.put("product", "Tinfoil-SMS");
        bugMap.put("component", "User Interface");
        bugMap.put("summary", "Chop-A-Stick");
        bugMap.put("description", "There be-ith starvation");
        bugMap.put("version", "unspecified");
        bugMap.put("cf_android_ver", "2.2 - Froyo");
        bugMap.put("op_sys", "Android");
        bugMap.put("platform", "PC");
        bugMap.put("priority", "P1");
        bugMap.put("severity", "Normal");
        bugMap.put("status", "NEW");
        
        Bug bug = factory.createBug(bugMap);
        /*
        Bug bug = factory.newBug()
            .setOperatingSystem("Android")
            .setPlatform("2.2")
            .setPriority("P1")
            .setProduct("Tinfoil-SMS")
            .setComponent("Messaging")
            .setSummary("Broken.")
            .setVersion("1.0-alpha")
            .setDescription("It doesn't work.")
            .createBug();*/
        if(bug == null)
        {
        	Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
        }
        
        
        int id =0;
        //Toast.makeText(this, "Success " + bug.getID() , Toast.LENGTH_LONG).show();
        
        ReportBug report = new ReportBug(bug);
        //Try to connect to tinfoil-sms bug site
        try {
			conn.connectTo("http://code4peace.dyndns.org/bugzilla/xmlrpc.cgi", "heron.joseph@gmail.com", "hatewalid123");
			
			//Try to send the repot
			try {
				conn.executeMethod(report);
			} catch (BugzillaException e) {
				e.printStackTrace();
			}
			
			//id = report.getID();
			
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
       
        Toast.makeText(this, "Success " + id, Toast.LENGTH_LONG).show();
        
        
        
        
        
        /*TextView tv = (TextView)findViewById(R.id.textView1);
        
        try 
		{
			Process process = Runtime.getRuntime().exec("logcat -d");
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			
			StringBuilder log = new StringBuilder();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				log.append(line);
				tv.setText("hello");
			}
			
			tv.setText(log.toString());
		} 
        catch (IOException e) {
        }*/
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
