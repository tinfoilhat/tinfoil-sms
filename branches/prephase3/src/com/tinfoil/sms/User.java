package com.tinfoil.sms;

/**
 * A class for holding information involving the user.
 * 
 */
public class User {
	
	private byte[] publicKey;
	private byte[] privateKey;
	private byte[] signature;	
	
	public User (byte[] publicKey, byte[] privateKey, byte[]signature)
	{
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.signature = signature;
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
	
	public byte[] getSignature()
	{
		return signature;
	}
	
	public void setSignature(byte[] signature)
	{
		this.signature = signature;
	}

}
