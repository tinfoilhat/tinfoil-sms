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
import android.widget.TextView;

public class MessageBoxWatcher implements TextWatcher {
	private TextView wordCount;
	private int wordCounter;
	private static final int START_NUMBER = 5;
	
	public MessageBoxWatcher(Activity app, int id)
	{
		wordCount = (TextView) app.findViewById(id);
	}
	
	public void afterTextChanged(Editable s) {
		if (s.length() < wordCounter)
		{
			wordCounter--;
		}
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

		wordCounter = s.length();
		int limit = 0;
				
		if(wordCounter > START_NUMBER)
		{
			wordCount.setText(String.valueOf(limit - wordCounter));
		}
		else
		{
			wordCount.setText("");
		}
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {}

	
	public void resetCount()
	{
		wordCounter = 0;
		wordCount.setText("");
	}
}
