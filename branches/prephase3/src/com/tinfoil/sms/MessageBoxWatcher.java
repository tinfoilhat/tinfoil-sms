package com.tinfoil.sms;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class MessageBoxWatcher implements TextWatcher {
	private TextView wordCount;
	private boolean trusted;
	private int wordCounter;
	private static final int START_NUMBER = 5;
	
	public MessageBoxWatcher(Activity app, int id, boolean trusted)
	{
		wordCount = (TextView) app.findViewById(id);
		this.trusted = trusted;
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
		
		if(trusted)
		{
			limit = SMSUtility.ENCRYPTED_MESSAGE_LENGTH;
		}
		else
		{
			limit = SMSUtility.MESSAGE_LENGTH;
		}
		
		//TODO adjust so that once it gets larger than the message size limit show D/N where D = number of characters and N = number of messages 
		
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
