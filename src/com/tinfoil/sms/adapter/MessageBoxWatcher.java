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

package com.tinfoil.sms.adapter;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageBoxWatcher implements TextWatcher {
    private ImageView sendSMS;
	private TextView wordCount;
	private int wordCounter;
	
	// The limit for it to still be one message is 120 - 32 =~ 88
	private static final int LIMIT = 100;
	private static final int START_NUMBER = 64;
	
	public MessageBoxWatcher(Activity app, int buttonId, int countId)
	{
	    sendSMS = (ImageView) app.findViewById(buttonId);
		wordCount = (TextView) app.findViewById(countId);
	}
	
	public void afterTextChanged(Editable s)
	{
	    if (s.length() > 0)
	    {
	        sendSMS.setEnabled(true);
	        sendSMS.setClickable(true);
	    }
        else
        {
            sendSMS.setEnabled(false);
            sendSMS.setClickable(false);
        }
	    
		if (s.length() < wordCounter)
		{
			wordCounter--;
		}
		
		if (s.length() <= START_NUMBER)
		{
		    wordCount.setText("");
	        wordCount.setVisibility(View.GONE);  
		}
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) 
	{
		wordCounter = s.length();

		if(wordCounter > START_NUMBER)
		{
		    wordCount.setVisibility(View.VISIBLE);
			wordCount.setText(String.valueOf(LIMIT - wordCounter));
		}
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {}
	
	public void resetCount()
	{
		wordCounter = 0;
		wordCount.setText("");
	}
}
