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
 * Through out tinfoil-sms TrustedContact is used to describe the class where
 * Trusted Contact is used to describe a contact that a key exchange has 
 * successfully taken place and secure messages are sent and expected.
 * A TrustedContact != Trusted Contact but a TrustedContact can be a
 * Trusted Contact, if key != null
 */
public class TrustedContact {
	
	private String name;
	private byte[] publicKey;		
	private ArrayList<Number> numbers;
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
	 * @param signature The contact's signature
	 */
	public TrustedContact (String name, byte[] publicKey, byte[] signature)
	{
		this.name = name;
		this.publicKey = publicKey;
		this.numbers = new ArrayList<Number>();
		//this.lastMessage = new ArrayList<String>();
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
		this.numbers = new ArrayList<Number>();
		//this.lastMessage = new ArrayList<String>();
		for (int i = 0; i<numbers.size(); i++)
		{
			this.numbers.add(new Number (numbers.get(i)));
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
	public TrustedContact (String name, ArrayList<Number> numbers)
	{
		this.name = name;
		this.publicKey = null;
		this.numbers = new ArrayList<Number>();
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
	 */
	public TrustedContact (String name)
	{
		this.name = name;
		this.publicKey = null;
		this.numbers = new ArrayList<Number>();
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
	
	/**
	 * Get the signature of the TrustedContact
	 * @return : byte[] the signature of the trustedContact's key
	 */
	public byte[] getSignature()
	{
		return signature;
	}
	
	/*public void setSignature(byte[] signature)
	{
		this.signature = signature;
	}*/
	
	/**
	 * Shared Information between the user and the contact.
	 * This information will be used for the key exchange 
	 * encrypted messages sent. The default value will be
	 * either 'Initiator' or 'Receiver'
	 * @param s1 : String, the first piece of shared information
	 */
	public void setSharedInfo1(String s1)
	{
		this.s1 = s1;
	}
	
	/**
	 * Shared Information between the user and the contact.
	 * This information will be used for the key exchange 
	 * encrypted messages sent. The default value will be
	 * either 'Initiator' or 'Receiver'
	 * @return : String, the first piece of shared information
	 */
	public String getSharedInfo1()
	{
		return s1;
	}
	
	/**
	 * Shared Information between the user and the contact.
	 * This information will be used for the key exchange 
	 * encrypted messages sent. The default value will be
	 * either 'Initiator' or 'Receiver'
	 * @param s2 : String, the second piece of shared information
	 */
	public void setSharedInfo2(String s2)
	{
		this.s2 = s2;
	}
	
	/**
	 * Shared Information between the user and the contact.
	 * This information will be used for the key exchange 
	 * encrypted messages sent. The default value will be
	 * either 'Initiator' or 'Receiver'
	 * @return : String, the second piece of shared information
	 */
	public String getSharedInfo2()
	{
		return s2;
	}
	
	/**
	 * Book path is used for the Steganography the bookpath
	 * is the folder path to the entropy source used to map
	 * encrypted text to obfuscated words. The user will have 
	 * very little interaction with this.
	 * @param bookPath : String, the path on the android phone to 
	 * the entropy source
	 */
	public void setBookPath(String bookPath)
	{
		this.bookPath = bookPath;
	}
	
	/**
	 * Book path is used for the Steganography the bookpath
	 * is the folder path to the entropy source used to map
	 * encrypted text to obfuscated words. The user will have 
	 * very little interaction with this.
	 * @param : String, the path on the android phone to 
	 * the entropy source
	 */
	public String getBookPath()
	{
		return bookPath;
	}
	
	/**
	 * Book path is used for the Steganography the bookInversePath
	 * is the folder path to the entropy source used to map
	 * the obfuscated words to encrypted text. The user will have 
	 * very little interaction with this.
	 * @param bookInversePath : String, the path on the android  
	 * phone to the entropy source
	 */
	public void setBookInversePath(String bookInversePath)
	{
		this.bookInversePath = bookInversePath;
	}
	
	/**
	 * Book path is used for the Steganography the bookInversePath
	 * is the folder path to the entropy source used to map
	 * the obfuscated words to encrypted text. The user will have 
	 * very little interaction with this.
	 * @param : String, the path on the android phone to the 
	 * entropy source
	 */
	public String getBookInversePath()
	{
		return bookInversePath;
	}

	/**
	 * Get any of the numbers for the contact.
	 * This is used for retrieving a single number
	 * to be used. 
	 * @return : String, the first non-null number found
	 */
	public String getANumber()
	{
		for (int i = 0; i < numbers.size(); i++)
		{
			if (numbers.get(i) != null)
			{
				return numbers.get(i).getNumber();
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
		this.numbers.get(index).setNumber(number);
	}
	
	/**
	 * Add a number to the contact's numbers list
	 * @param number : String
	 */
	public void addNumber(String number)
	{
		this.numbers.add(new Number(number));
	}
	
	/**
	 * Add a number to the contact's numbers list
	 * @param number : String
	 */
	public void addNumber(Number number)
	{
		this.numbers.add(number);
	}
	
	/**
	 * Access a contact's number from their contact list
	 * @return : String
	 */
	public String getNumber(int index)
	{
		return numbers.get(index).getNumber();
	}
	
	/**
	 * Access a contact's number from their contact list
	 * @return : ArrayList<String>
	 */
	public ArrayList<Number> getNumber()
	{
		return numbers;
	}
	
	/**
	 * Access a contact's number from their contact list
	 * @return : ArrayList<String>
	 */
	public ArrayList<String> getNumbers()
	{
		ArrayList<String> num = new ArrayList<String>();
		
		for (int i =0; i < numbers.size(); i++)
		{
			num.add(numbers.get(i).getNumber());
		}
		return num;
	}
	
	/**
	 * Set a lastMessage in the contact's last message list
	 * @param number : String the number the message came from
	 * @param number : String the new last message
	 */
	public void setLastMessage(String number, String lastMessage)
	{
		for (int i = 0; i < this.numbers.size(); i++)
		{
			if (this.getNumber(i).equalsIgnoreCase(number))
			{
				this.numbers.get(i).setLastMessage(lastMessage);
				break;
			}
		}
		
	}
	
	/**
	 * Set a lastMessage in the contact's last message list
	 * @param index : int the index of the number
	 * @param lastMessage : String the new last message
	 */
	public void setLastMessage(int index, String lastMessage)
	{
		this.numbers.get(index).setLastMessage(lastMessage);
	}
	
	/**
	 * Access a contact's number from their contact list
	 * @return : String
	 */
	public String getLastMessage(int index)
	{
		if (index < numbers.size())
		{
			return numbers.get(index).getLastMessage();
		}
		return null;
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
	
	/**
	 * Erases the public key
	 */
	public void clearPublicKey()
	{
		this.publicKey = null;
	}

	/**
	 * Checks if the publickey is null
	 * @return : boolean
	 * true if the public key is null,
	 * false if the public key is not null.
	 */
	public boolean isPublicKeyNull()
	{
		if (publicKey == null)
		{
			return true;
		}
		return false;
	}
	
	
}
