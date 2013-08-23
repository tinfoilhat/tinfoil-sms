package com.tinfoil.sms.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

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
		    .setNeutralButton("Flattr", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//String url = "http://flattr.com/thing/1807230/Tinfoil-SMS-Texting-for-the-Paranoid";
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(DonationsActivity.this.getString(R.string.flattr_url)));
					DonationsActivity.this.startActivity(i);
					
					DonationsActivity.this.finish();
				}
			})
		    .setPositiveButton("PayPal", new DialogInterface.OnClickListener() {
	    	   @Override
	    	   public void onClick(DialogInterface dialog, int id) {
	    		   //String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=QZP8ZHVFATTTY";
	    		   Intent i = new Intent(Intent.ACTION_VIEW);
	    		   i.setData(Uri.parse(DonationsActivity.this.getString(R.string.paypal_url)));
	    		   DonationsActivity.this.startActivity(i);
	    		   
	    		   DonationsActivity.this.finish();
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

}
