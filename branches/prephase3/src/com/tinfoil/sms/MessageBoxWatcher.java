package com.tinfoil.sms;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class MessageBoxWatcher implements TextWatcher {
	private TextView wordCount;
	private boolean trusted;
	
	public MessageBoxWatcher(Activity app, int id, boolean trusted)
	{
		wordCount = (TextView) app.findViewById(id);
		this.trusted = trusted;
	}
	
	public void afterTextChanged(Editable s) {}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		int wordCounter = s.length();
		
		//TODO adjust so that once it gets larger than the message size limit show D/N where D = number of characters and N = number of messages 
		
		if(wordCounter > 5)
		{
			wordCount.setText(String.valueOf(wordCounter));
		}
		else
		{
			wordCount.setText("");
		}
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {}

}
