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

/**
 * A class for holding information involving the user.
 * TODO comment
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
