package com.tinfoil.sms;

/**
 * A class for holding information involving the user.
 * 
 */
public class Contact {
	
	private String name;
	private String number;
	private byte[] publicKey;
	
	
	public Contact (String name, byte[] publicKey, String number)
	{
		this.setName(name);
		this.setNumber(number);
		this.publicKey = publicKey;
	}
	
	public Contact (String name, String number)
	{
		this.setName(name);
		this.setNumber(number);
		this.publicKey = null;
	}
	
	public byte[] getPublicKey()
	{
		return publicKey;
	}
	
	/**
	 * Set the contact's public publicKey
	 */
	public void setPublicKey()
	{
		this.publicKey = Encryption.generateKey();
	}
	
	/**
	 * Erases the public key
	 */
	public void clearPublicKey()
	{
		this.publicKey = null;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

}
