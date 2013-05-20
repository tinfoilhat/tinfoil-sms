/** 
 * Copyright (C) 2011 Tinfoilhat
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tinfoil.sms.R;
import com.tinfoil.sms.utility.SMSUtility;

public class UserKeySettings extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_key_settings);
		
		TextView keyView = (TextView)findViewById(R.id.public_key);
		
		keyView.setText(new String(SMSUtility.user.getPublicKey()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_key_settings, menu);
		return true;
	}
	
	public void exportKey(View view)
	{
		if(SMSUtility.isMediaWritable())
		{
			
			//getExternalFilesDir(null);
			
			File root = Environment.getExternalStorageDirectory();
			
			File keys = new File(root.getAbsolutePath() + "/keys");
			keys.mkdirs();
			
			File pubKey = new File(keys, "public_key.txt");
			
			try {
				FileOutputStream f  = new FileOutputStream(pubKey);
				PrintWriter pw = new PrintWriter(f);
				
				/* 
				 * TODO create dialog to get the desired user's number.
				 * Could always get the user to select which contact they are
				 * giving their key to and then actually generate a key exchange
				 */
				pw.println(SMSUtility.user.getPublicKey());
				pw.flush();
				pw.close();
				f.close();
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			Toast.makeText(this, "Written to your sd card under /keys/public_key.txt", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	
	
	
}
