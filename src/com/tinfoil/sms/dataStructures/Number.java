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

package com.tinfoil.sms.dataStructures;

import java.util.ArrayList;

import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.SMSUtility;

/**
 * A class used to store information from the numbers table
 *
 */
public class Number {
	
	public static final int AUTO = 0;
	public static final int MANUAL = 1;
	public static final int IGNORE = 2;
	
	private long id;
	private String number;
	private int type;
	private int unreadMessageCount = 0;
	private ArrayList<Message> messages;

	private byte[] publicKey;
	private byte[] signature;
	private String s1;
	private String s2;
	private String bookPath;
	private String bookInversePath;
	
	private Integer enNonceCount;
	private Integer deNonceCount;
	
	private boolean initiator;

	private int keyExchangeFlag;
	
	/**
	 * A class used to store information from the numbers table
	 * 
	 * @param id The number's sql id.
	 * @param number A number for the contact
	 * @param type The type of number, whether it is a cell, home, etc.
	 * For more information of types of numbers please see the DBAccessor
	 * class for the 'TYPES' variable
	 * @param unreadMessageCount The number of messages that have
	 * not been read from this number
	 * @param publicKey The contact's public key.
	 * @param signature The contact's signature.
	 * @param enNonceCount The encryption nonce count
	 * @param deNonceCount The decryption nonce count
	 * @param initiator Set to false if equal to 0 otherwise true.
	 * @param keyExchangeFlag for how key exchange messages from the contact
	 * will be handled
	 */
	public Number (long id, String number, int type, int unreadMessageCount, 
			byte[] publicKey, byte[] signature, Integer enNonceCount,
			Integer deNonceCount, int initiator, int keyExchangeFlag)
	{
		this.id = id;
		this.setNumber(number);
		this.setType(type);
		this.setUnreadMessageCount(unreadMessageCount);
		this.messages = new ArrayList<Message>();
		this.publicKey = publicKey;
		this.signature = signature;
		this.enNonceCount = enNonceCount;
		this.deNonceCount = deNonceCount;
		
		if(initiator == 0)
		{
			this.initiator = false;
		}
		else
		{
			this.initiator = true;
		}
		this.keyExchangeFlag = keyExchangeFlag;
	}
	
	/**
	 * A class used to store information from the numbers table
	 * @param number The Number for the contact.
	 * @param publicKey The contact's public key.
	 */
	public Number (String number, byte[] publicKey)
	{
		this.setNumber(number);
		this.publicKey = publicKey;
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
		this.keyExchangeFlag = AUTO;
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
		this.keyExchangeFlag = AUTO;
	}
	
	/**
	 * Get the number
	 * @return The number
	 */
	public String getNumber() {
		return SMSUtility.format(number);
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
	 * Set both the shared secrets.
	 * @param sharedSecrets Contains both shared secrets. At index 0 is the
	 * first and at index 1 is the second secret.
	 */
	public void setSharedInfo(String[] sharedSecrets)
	{
		this.s1 = sharedSecrets[0];
		this.s2 = sharedSecrets[1];
	}
	
	/**
	 * Shared Information between the user and the contact. This information
	 * will be used for the key exchange encrypted messages sent. The default
	 * value will be either 'Initiator' or 'Receiver'
	 * @param s1 The first piece of shared information
	 */
	public void setSharedInfo1(String s1)
	{
		this.s1 = s1;
	}
	
	/**
	 * Shared Information between the user and the contact. This information
	 * will be used for the key exchange encrypted messages sent. The default
	 * value will be either 'Initiator' or 'Receiver'
	 * @return The first piece of shared information
	 */
	public String getSharedInfo1()
	{
		return s1;
	}
	
	/**
	 * Shared Information between the user and the contact. This information
	 * will be used for the key exchange encrypted messages sent. The default
	 * value will be either 'Initiator' or 'Receiver'
	 * @param s2 The second piece of shared information
	 */
	public void setSharedInfo2(String s2)
	{
		this.s2 = s2;
	}
	
	/**
	 * Shared Information between the user and the contact. This information
	 * will be used for the key exchange encrypted messages sent. The default
	 * value will be either 'Initiator' or 'Receiver'
	 * @return The second piece of shared information
	 */
	public String getSharedInfo2()
	{
		return s2;
	}

	/**
	 * Get the signature of the TrustedContact
	 * @return The signature of the trustedContact's key
	 */
	public byte[] getSignature() {
		return signature;
	}

	/**
	 * Set the signature of the TrustedContact
	 * @param signature The new signature
	 */
	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
	
	/**
	 * Set the book paths
	 * @param bookPaths Contains both book paths. The bookPath is at index 0 and
	 * the bookInversePath is at index 1. 
	 */
	public void setBookPaths(String[] bookPaths)
	{
		this.bookPath = bookPaths[0];
		this.bookInversePath = bookPaths[1];
	}

	/**
	 * Book path is used for the Steganography the bookpath is the folder path
	 * to the entropy source used to map encrypted text to obfuscated words.
	 * The user will have very little interaction with this.
	 * @param bookPath The path on the android phone to the entropy source
	 */
	public void setBookPath(String bookPath)
	{
		this.bookPath = bookPath;
	}
	
	/**
	 * Book path is used for the Steganography the bookpath is the folder path
	 * to the entropy source used to map encrypted text to obfuscated words.
	 * The user will have very little interaction with this.
	 * @return The path on the android phone to the entropy source
	 */
	public String getBookPath()
	{
		return bookPath;
	}
	
	/**
	 * Book path is used for the Steganography the bookInversePath is the
	 * folder path to the entropy source used to map the obfuscated words to
	 * encrypted text. The user will have very little interaction with this.
	 * @param The path on the android phone to the entropy source
	 */
	public void setBookInversePath(String bookInversePath)
	{
		this.bookInversePath = bookInversePath;
	}
	
	/**
	 * Book path is used for the Steganography the bookInversePath is the
	 * folder path to the entropy source used to map the obfuscated words to
	 * encrypted text. The user will have very little interaction with this.
	 * @return The path on the android phone to the entropy source
	 */
	public String getBookInversePath()
	{
		return bookInversePath;
	}

	/**
	 * Access the publicKey
	 * @return The contact's public publicKey used for encrypting messages
	 */
	public byte[] getPublicKey()
	{
		if (isPublicKeyNull())
		{
			return null;
		}
		return publicKey;
	}
	
	/**
	 * Set the contact's public publicKey
	 * @param publicKey The public key for the contact.
	 */
	public void setPublicKey(byte[] publicKey)
	{
		this.publicKey =  publicKey;
	}
	
	/**
	 * Erases the public key
	 */
	public void clearPublicKey()
	{
		this.publicKey = null;
		this.enNonceCount = 0;
		this.deNonceCount = 0;
	}

	/**
	 * Checks if the publickey is null
	 * @return True if the public key is null false otherwise.
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
	 * @return The unique id for the row in the database
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Get the index of the given number from a list of Numbers
	 * @param numbers The list of Numbers
	 * @param number The Number to look for.
	 * @return The index of the Number in the list of numbers, if the number is
	 * not found then the index returned will be -1.
	 */
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
	 * Get the Nonce for the encrypt.
	 * @return The encryption Nonce.
	 */
	public Integer getNonceEncrypt() {
		return enNonceCount;
	}

	/**
	 * Set the Nonce for the encrypt.
	 * @param The new encryption Nonce.
	 */
	public void setNonceEncrypt(Integer nonceEncrypt) {
		enNonceCount = nonceEncrypt;
	}

	/**
	 * Get the Nonce for the decrypt.
	 * @return The decryption nonce.
	 */
	public Integer getNonceDecrypt() {
		return deNonceCount;
	}

	/**
	 * Set the Nonce for the decrypt.
	 * @param The new decryption Nonce.
	 */
	public void setNonceDecrypt(Integer nonceDecrypt) {
		deNonceCount = nonceDecrypt;
	}

	/**
	 * Whether the contact is the initiator or not.
	 * @return The initiator.
	 */
	public boolean isInitiator() {
		return initiator;
	}

	/**
	 * Set the initiator flag. Set to true if the user initiated the key
	 * exchange. NOT the number the key exchange is taking place with
	 * @param Initiator the initiator to set.
	 */
	public void setInitiator(boolean initiator) {
		this.initiator = initiator;
	}
	
	/**
	 * Get the initiator flag that identifies whether the user is the initiator
	 * of the key exchange or not. This is the value that will be stored in the
	 * database.
	 * @return The value to be stored in the database.
	 */
	public int getInitiatorInt()
	{
		if(initiator)
		{
			return 1;
		}
		return 0;
	}

	/**
	 * Get the key exchange flag, how the user is going to handle key exchanges
	 * upon receiving a message.
	 * @return the keyExchangeFlag
	 */
	public int getKeyExchangeFlag() {
		return keyExchangeFlag;
	}

	/**
	 * Set the key exchange flag to either Number.AUTO, Number.MANUAL,
	 * Number.IGNORE
	 * @param keyExchangeFlag the keyExchangeFlag to set
	 */
	public void setKeyExchangeFlag(int keyExchangeFlag) {
		this.keyExchangeFlag = keyExchangeFlag;
	}
}
