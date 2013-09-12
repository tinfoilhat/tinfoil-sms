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
			.setMessage(R.string.donate_dialog_message)
		    .setCancelable(true)
		    .setOnCancelListener(new OnCancelListener(){

				@Override
				public void onCancel(DialogInterface dialog) {
					DonationsActivity.this.finish();
				}		    	
		    })
		    .setNeutralButton(R.string.flattr_option, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//String url = "http://flattr.com/thing/1807230/Tinfoil-SMS-Texting-for-the-Paranoid";
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(DonationsActivity.this.getString(R.string.flattr_url)));
					DonationsActivity.this.startActivity(i);
					
					DonationsActivity.this.finish();
				}
			})
		    .setPositiveButton(R.string.paypal_option, new DialogInterface.OnClickListener() {
	    	   @Override
	    	   public void onClick(DialogInterface dialog, int id) {
	    		   //String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=QZP8ZHVFATTTY";
	    		   Intent i = new Intent(Intent.ACTION_VIEW);
	    		   i.setData(Uri.parse(DonationsActivity.this.getString(R.string.paypal_url)));
	    		   DonationsActivity.this.startActivity(i);
	    		   
	    		   DonationsActivity.this.finish();
	    	   }})
		    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
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
