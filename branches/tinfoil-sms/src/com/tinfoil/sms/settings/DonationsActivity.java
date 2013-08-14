package com.tinfoil.sms.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;

import com.tinfoil.sms.R;

public class DonationsActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_donations);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		
		builder.setTitle(this.getString(R.string.donate))
			.setMessage("Would you like to donate to Tinfoil-SMS?")
		    .setCancelable(true)
		    .setOnCancelListener(new OnCancelListener(){

				@Override
				public void onCancel(DialogInterface dialog) {
					DonationsActivity.this.finish();
				}		    	
		    })
		    .setPositiveButton(this.getString(R.string.donate), new DialogInterface.OnClickListener() {
	    	   @Override
	    	   public void onClick(DialogInterface dialog, int id) {
	    		   String url = "https://github.com/tinfoilhat/tinfoil-sms";
	    		   Intent i = new Intent(Intent.ACTION_VIEW);
	    		   i.setData(Uri.parse(url));
	    		   DonationsActivity.this.startActivity(i);

	    	   }})
		    .setNegativeButton("No", new DialogInterface.OnClickListener() {
	    	   @Override
	    	   public void onClick(DialogInterface arg0, int arg1) {
	    		   // Cancel the key exchange
	    		   DonationsActivity.this.finish();
	    	   }
		    });
		
 		AlertDialog alert = builder.create();
 		alert.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.donations, menu);
		return true;
	}

}
