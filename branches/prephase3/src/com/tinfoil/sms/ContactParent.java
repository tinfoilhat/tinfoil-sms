package com.tinfoil.sms;

import java.util.ArrayList;

public class ContactParent {

	//private boolean trusted;
	private String name;
	private ArrayList<ContactChild> numbers;
	//private boolean trusted;
	
	public ContactParent(String name, ArrayList<ContactChild> numbers)
	{
		this.setName(name);
		//this.setTrusted(trusted);
		this.numbers = numbers;
	}

	public boolean isTrusted() {
		for(int i = 0; i < numbers.size();i++)
		{
			if(numbers.get(i).isTrusted())
			{
				return true;
			}
		}
		return false;
	}

	/*public void setTrusted(boolean trusted) {
		this.trusted = trusted;
	}*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<ContactChild> getNumbers() {
		return numbers;
	}
	
	public ContactChild getNumber(int index)
	{
		return numbers.get(index);
	}

	public void setNumbers(ArrayList<ContactChild> numbers) {
		this.numbers = numbers;
	}
}
