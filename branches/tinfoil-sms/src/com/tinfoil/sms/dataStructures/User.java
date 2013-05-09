package com.tinfoil.sms.dataStructures;

/**
 * A class for holding information involving the user.
 * 
 */
public class User {
	
	private byte[] publicKey;
	private byte[] privateKey;
	
	public User (byte[] publicKey, byte[] privateKey)
	{
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	
	public byte[] getPublicKey()
	{
		return publicKey;
	}
	
	public void setPublicKey(byte[] publicKey)
	{
		this.publicKey = publicKey;
	}
	
	public byte[] getPrivateKey()
	{
		return privateKey;
	}
	
	public void setPrivateKey(byte[] privateKey)
	{
		this.privateKey = privateKey;
	}
}
