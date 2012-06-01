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
	private byte[] publicKey;		
	private ArrayList<String> numbers;
	private byte[] signature;
	private String s1;
	private String s2;
	private String bookPath;
	private String bookInversePath;
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 * @param publicKey The contact's public key used to encrypt message sent to this contact
	 */
	public TrustedContact (String name, byte[] publicKey, byte[] signature)
	{
		this.name = name;
		this.publicKey = publicKey;
		this.numbers = new ArrayList<String>();
		this.signature = null;
		this.s1 = null;
		this.s2 = null;
		this.bookPath = null;
		this.bookInversePath = null;
	}
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 * @param publicKey The contact's public key used to encrypt message sent to this contact
	 * @param numbers A list of numbers that are associated to the contact.
	 */
	public TrustedContact (String name, byte[] publicKey, ArrayList<String> numbers)
	{
		this.name = name;
		this.publicKey = publicKey;
		for (int i = 0; i<numbers.size(); i++)
		{
			this.numbers.add(numbers.get(i));
		}
		this.signature = null;
		this.s1 = null;
		this.s2 = null;
		this.bookPath = null;
		this.bookInversePath = null;
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
		this.s1 = null;
		this.s2 = null;
		this.bookPath = null;
		this.bookInversePath = null;
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
		this.s1 = null;
		this.s2 = null;
		this.bookPath = null;
		this.bookInversePath = null;
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
	
	public void setSharedInfo1(String s1)
	{
		this.s1 = s1;
	}
	
	public String getSharedInfo1()
	{
		return s1;
	}
	
	public void setSharedInfo2(String s2)
	{
		this.s2 = s2;
	}
	
	public String getSharedInfo2()
	{
		return s2;
	}
	
	public void setBookPath(String bookPath)
	{
		this.bookPath = bookPath;
	}
	
	public String getBookPath()
	{
		return bookPath;
	}
	
	public void setBookInversePath(String bookInversePath)
	{
		this.bookInversePath = bookInversePath;
	}
	
	public String getBookInversePath()
	{
		return bookInversePath;
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
	 * Access the publicKey
	 * @return : String the contact's public publicKey 
	 * used for encrypting messages
	 */
	public String getPublicKey()
	{
		if (isPublicKeyNull())
		{
			return null;
		}
		return new String(publicKey);
	}
	
	/**
	 * Set the contact's public publicKey
	 */
	public void setPublicKey()
	{
		this.publicKey = ("test123").getBytes();
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
