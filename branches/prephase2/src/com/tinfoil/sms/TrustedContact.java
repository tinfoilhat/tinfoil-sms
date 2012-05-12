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
	private String number;
	private String key;
	private int verified;
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 * @param number The contact's number
	 * @param key The contact's public key used to encrypt message sent to this contact
	 * @param verified A identifier used to help maintain state during the key exchange.
	 */
	public TrustedContact (String name, String number, String key, int verified)
	{
		this.name = name;
		this.number = number;
		this.key = key;
		this.verified = verified;
	}
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 */
	public TrustedContact (String name)
	{
		this.name = name;
		this.number = null;
		this.key = null;
		this.verified = 0;
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
	 * Access the contact's number
	 * @return : String
	 */
	public String getNumber()
	{
		return number;
	}
	
	/**
	 * Set the contact's number
	 * @param number : String
	 */
	public void setNumber(String number)
	{
		this.number = number;
	}
	
	/**
	 * Access the key
	 * @return : String the contact's public key 
	 * used for encrypting messages
	 */
	public String getKey()
	{
		return key;
	}
	
	/**
	 * Set the contact's public key
	 * @param key : String this should only be 
	 * changed when an exchange is underway
	 */
	public void setKey(String key)
	{
		this.key = key;
	}
	
	public boolean isKeyNull()
	{
		if (key == null)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Access the verified value
	 * @return : int can be 0, 1 or 2 
	 * 0 indicates no key received, (sending plain text)
	 * 1 indicates sent a key, initiating the exchange, (sending plain text)
	 * 2 indicates received key and continued exchange
	 * OR received contact's key after sending (previously being a 1)
	 * when verified == 2 the key exchange is complete and encrypted texts are sent
	 * 
	 */
	public int getVerified()
	{
		return verified;
	}
	
	/**
	 * Set the verified value
	 * @param verified : int can be 0, 1 or 2 indicating
	 * not received a key, sent a key initiating exchange, 
	 * or send key upon receiving a key continuing exchange
	 */
	public void setVerified(int verified)
	{
		this.verified = verified;
	}

}
