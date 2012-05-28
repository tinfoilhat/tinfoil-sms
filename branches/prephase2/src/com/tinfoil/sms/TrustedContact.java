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
 * @param publicKey The contact's public key used to encrypt message sent to this contact
 * @param numbers an array list of numbers for the contact.
 * 
 */
public class TrustedContact {
	
	private String name;
	private String publicKey;		//Need to remove, is more or less to represent shared key 
	private ArrayList<String> numbers;
	private byte[] signature;
	//Need to add:
	//public key
	//signature
	//s1
	//s2
			
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 * @param publicKey The contact's public key used to encrypt message sent to this contact
	 */
	//public TrustedContact (String name, String publicKey)
	public TrustedContact (String name, String publicKey, byte[] signature)
	{
		this.name = name;
		this.publicKey = publicKey;
		this.numbers = new ArrayList<String>();
		this.signature = null;
	}
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 * @param publicKey The contact's public key used to encrypt message sent to this contact
	 * @param numbers A list of numbers that are associated to the contact.
	 */
	public TrustedContact (String name, String publicKey, ArrayList<String> numbers)
	{
		this.name = name;
		this.publicKey = publicKey;
		for (int i = 0; i<numbers.size(); i++)
		{
			this.numbers.add(numbers.get(i));
		}
		this.signature = null;
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
		this.publicKey = null;
		this.numbers = numbers;
		this.signature = null;
	}
	
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 */
	public TrustedContact (String name)
	{
		this.name = name;
		this.publicKey = null;
		this.numbers = new ArrayList<String>();
		this.signature = null;
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
	
	public byte[] getSignature()
	{
		return signature;
	}
	
	/*public void setSignature(byte[] signature)
	{
		this.signature = signature;
	}*/

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
	 * Access the publicKey
	 * @return : String the contact's public publicKey 
	 * used for encrypting messages
	 */
	public String getPublicKey()
	{
		return publicKey;
	}
	
	/**
	 * Set the contact's public publicKey
	 */
	public void setPublicKey()
	{
		this.publicKey = "test123";
	}
	
	public void clearPublicKey()
	{
		this.publicKey = null;
	}

	public boolean isPublicKeyNull()
	{
		if (publicKey == null)
		{
			return true;
		}
		return false;
	}
}
