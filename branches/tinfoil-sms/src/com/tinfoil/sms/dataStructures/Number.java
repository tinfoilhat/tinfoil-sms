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

package com.tinfoil.sms.dataStructures;

import java.util.ArrayList;

import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.encryption.Encryption;

/**
 * A class used to store information from the numbers table
 *
 */
public class Number {
	
	private long id;
	private String number;
	private int type;
	private int unreadMessageCount = 0;
	private ArrayList<Message> messages;

	private byte[] publicKey;
	private byte[] symmetricKey;
	private byte[] signature;
	private String s1;
	private String s2;
	private String bookPath;
	private String bookInversePath;
	
	/**
	 * A class used to store information from the numbers table
	 * 
	 * @param number A number for the contact
	 * @param type The type of number, whether it is a cell, home, etc.
	 * For more information of types of numbers please see the DBAccessor
	 * class for the 'TYPES' variable
	 * @param unreadMessageCount The number of messages that have
	 * not been read from this number
	 */
	public Number (long id, String number, int type, int unreadMessageCount, 
			byte[] publicKey, byte[] symmetricKey, byte[] signature)
	{
		this.id = id;
		this.setNumber(number);
		this.setType(type);
		this.setUnreadMessageCount(unreadMessageCount);
		this.messages = new ArrayList<Message>();
		this.publicKey = publicKey;
		this.symmetricKey = symmetricKey;
		this.signature = signature;
	}
	
	/**
	 * A class used to store information from the numbers table
	 * 
	 * @param number A number for the contact
	 * @param type The type of number, whether it is a cell, home...
	 * For more information of types please see the DBAccessor class
	 * for the 'TYPES' variable
	 */
	public Number (String number, int type)
	{
		this.setNumber(number);
		this.setType(type);
		this.messages = new ArrayList<Message>();
	}
	
	/**
	 * A class used to store information from the numbers table
	 * 
	 * @param number A number for the contact
	 */
	public Number (String number)
	{
		this.setNumber(number);
		this.setType(DBAccessor.OTHER_INDEX);
		this.messages = new ArrayList<Message>();
	}
	
	/**
	 * Get the number
	 * @return The number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Set the number
	 * @param number The new number
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * Get the type of number
	 * @return The type of number (the index for it)
	 */
	public int getType() {
		if (type > DBAccessor.LENGTH)
		{
			return DBAccessor.OTHER_INDEX;
		}
		return type;
	}

	/**
	 * Set the type of number
	 * @param type The new type
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Add a message to the list of messages for the number
	 * @param newMessage The new message to add the list of messages
	 */
	public void addMessage(Message newMessage)
	{
		this.messages.add(newMessage);
	}
	
	/**
	 * Get the list of messages for the number
	 * @return The list of messages
	 */
	public ArrayList<Message> getMessages()
	{
		return this.messages;
	}
	
	/**
	 * Get the message given the index
	 * @param index The index of the message desired
	 * @return The message at the given index
	 */
	public Message getMessage(int index)
	{
		return this.messages.get(index);
	}

	/**
	 * Get the number of messages currently unread
	 * @return The current count of unread messages
	 */
	public int getUnreadMessageCount() {
		return unreadMessageCount;
	}

	/**
	 * Set the number of unread messages
	 * @param unreadMessageCount The new number of messages unread
	 */
	public void setUnreadMessageCount(int unreadMessageCount) {
		this.unreadMessageCount = unreadMessageCount;
	}
	
	/**
	 * Reset the number of messages unread to 0
	 */
	public void resetUnreadMessageCount() {
		this.unreadMessageCount = 0;
	}
	
	/**
	 * Increment the number of messages unread
	 */
	public void addUnreadMessageCount() {
		this.unreadMessageCount++;
	}

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
	 * Get the signature of the TrustedContact
	 * @return : byte[] the signature of the trustedContact's key
	 */
	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
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
		this.publicKey =  Encryption.generateKey();
		//TODO update signature as well
	}
	
	/**
	 * Erases the public key
	 */
	public void clearPublicKey()
	{
		this.publicKey = null;
		//TODO update signature as well
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

	/**
	 * Get the unique database Id for the number
	 * @return id the long unique id for the row in the database
	 */
	public long getId() {
		return id;
	}

	/**
	 * Get the unique database Id for the number
	 * @param id the new long unique id for the row in the database
	 */
	/*public void setId(long id) {
		this.id = id;
	}*/
	public static int hasNumber(ArrayList<Number> numbers, Number number)
	{
		for(int i = 0; i < numbers.size(); i++)
		{
			if(numbers.get(i).getNumber().equalsIgnoreCase(number.getNumber()))
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the symmetric key
	 * @return The symmetric key
	 */
	public byte[] getSymmetricKey() {
		return symmetricKey;
	}

	/**
	 * Set the symmetric key 
	 * @param symmetricKey
	 */
	public void setSymmetricKey(byte[] symmetricKey) {
		this.symmetricKey = symmetricKey;
	}
}
