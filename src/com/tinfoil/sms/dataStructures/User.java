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

import com.tinfoil.sms.crypto.KeyGenerator;

/**
 * A class for holding information involving the user.
 */
public class User {
	
	private byte[] publicKey;
	private byte[] privateKey;
	
	/**
	 * Default constructor to create a new user with newly generated keys.
	 */
	public User()
	{
		//Create the keyGenerator
		KeyGenerator keyGen = new KeyGenerator();
        
		this.publicKey = keyGen.generatePubKey();
		this.privateKey = keyGen.generatePriKey();
	}
	
	public User (byte[] publicKey, byte[] privateKey)
	{
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	
	/**
	 * Get the User's public key.
	 * @return The User's public key.
	 */
	public byte[] getPublicKey()
	{
		return publicKey;
	}
	
	/**
	 * Set the User's public key.
	 * @param publicKey The new public key for the User.
	 */
	public void setPublicKey(byte[] publicKey)
	{
		this.publicKey = publicKey;
	}
	
	/**
	 * Get the User's private key.
	 * @return The User's private key.
	 */
	public byte[] getPrivateKey()
	{
		return privateKey;
	}
	
	/**
	 * Set the User's private key.
	 * @param privateKey The new private key for the User.
	 */
	public void setPrivateKey(byte[] privateKey)
	{
		this.privateKey = privateKey;
	}
}
