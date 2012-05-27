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


package com.tinfoil.sms;

import java.util.ArrayList;

/**
 * A class for storing information retrieved or to be stored in the database. 
 * 
 * @param name The contact's name 
 * @param number The contact's number
 * @param key The contact's public key used to encrypt message sent to this contact
 * @param verified A identifier used to help maintain state during the key exchange.
 * 
 */
public class TrustedContact {
	
	private String name;
	private String key;		//Need to remove, is more or less to represent shared key 
	private ArrayList<String> numbers;
	//Need to add:
	//public key
	//signature
	//s1
	//s2
			
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 * @param key The contact's public key used to encrypt message sent to this contact
	 */
	public TrustedContact (String name, String key)
	{
		this.name = name;
		this.key = key;
		this.numbers = new ArrayList<String>();
	}
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 * @param key The contact's public key used to encrypt message sent to this contact
	 * @param numbers A list of numbers that are associated to the contact.
	 */
	public TrustedContact (String name, String key, ArrayList<String> numbers)
	{
		this.name = name;
		this.key = key;
		for (int i = 0; i<numbers.size(); i++)
		{
			this.numbers.add(numbers.get(i));
		}
	}
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 * @param numbers A list of numbers that are associated to the contact.
	 */
	public TrustedContact (String name, ArrayList<String> numbers)
	{
		this.name = name;
		this.key = null;
		this.numbers = numbers;
	}
	
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 */
	public TrustedContact (String name)
	{
		this.name = name;
		this.key = null;
		this.numbers = new ArrayList<String>();
	}
	
	/**
	 * Access the contact's name
	 * @return : String
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Set the contact's name
	 * @param name : String
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	public String getANumber()
	{
		for (int i = 0; i < numbers.size(); i++)
		{
			if (numbers.get(i) != null)
			{
				return numbers.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Set a number in the contact's numbers list
	 * @param index : int the index of the number
	 * @param number : String the new number
	 */
	public void setNumber(int index, String number)
	{
		this.numbers.set(index, number);
	}
	
	/**
	 * Add a number to the contact's numbers list
	 * @param number : String
	 */
	public void addNumber(String number)
	{
		this.numbers.add(number);
	}
	
	/**
	 * Access a contact's number from their contact list
	 * @return : String
	 */
	public String getNumber(int index)
	{
		return numbers.get(index);
	}
	
	/**
	 * Access a contact's number from their contact list
	 * @return : ArrayList<String>
	 */
	public ArrayList<String> getNumber()
	{
		return numbers;
	}
	
	/**
	 * Get the number of numbers a contact has.
	 * @return : int the number of numbers
	 */
	public int getNumberSize()
	{
		return numbers.size();
	}
	
	/**
	 * Whether the contact has numbers or not
	 * @return : boolean, true if the contact has no numbers
	 */
	public boolean isNumbersEmpty()
	{
		if (numbers == null || numbers.size() < 1)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Access the key
	 * @return : String the contact's public key 
	 * used for encrypting messages
	 */
	public String getKey()
	{
		return key;
		//return "test123";
	}
	
	/**
	 * Set the contact's public key
	 * @param key : String this should only be 
	 * changed when an exchange is underway
	 */
	public void setKey()
	{
		this.key = "test123";
	}
	
	public void clearKey()
	{
		this.key = null;
	}
	
	public boolean isKeyNull()
	{
		if (key == null)
		{
			return true;
		}
		return false;
	}
}
