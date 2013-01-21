/** 
 * Copyright (C) 2013 Tinfoilhat
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
package com.tinfoilsms.crypto;

import java.security.SecureRandom;

import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.KeyGenerationParameters;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;

/**
 * A wrapper class that simplifies the creation of a private/public Elliptic
 * Curve keypair. This class should be used to generate the initial
 * private/public keypair for the user.
 */
public class ECKey
{
	private ECKeyPairGenerator ECKeyPairGen;
	private KeyGenerationParameters keyGenParam;
	private AsymmetricCipherKeyPair ECKeyPair;
	
	/**
	 * ECKey object constructor
	 * @param param ECDomainParameters for creating the elliptic curve keypair
	 */
	public ECKey(ECDomainParameters param)
	{
		/* Instantiate the elliptic curve key parameters */
		this.ECKeyPairGen = new ECKeyPairGenerator();
		this.keyGenParam = new ECKeyGenerationParameters(param, new SecureRandom());
	}


	/**
	 * Wrapper for AsymmetricCipherKeyPairGenerator init() and generateKeyPair()
	 * function which initializes the elliptic curve and generates a keypair
	 */
	public void init()
	{
		ECKeyPairGen.init(keyGenParam);
		ECKeyPair = ECKeyPairGen.generateKeyPair();
	}
	
	 
	/**
	 * Wrapper for AsymmetricCipherKeyPair getPrivate()
	 * @return The private key parameters
	 */
	public CipherParameters getPrivate()
	{
		return ECKeyPair.getPrivate();
	}
	
	
	/**
	 * Wrapper for AsymmetricCipherKeyPair getPublic()
	 * @return The public key parameters
	 */
	public CipherParameters getPublic()
	{
		return ECKeyPair.getPublic();
	}
	
	
	/**
	 * Wrapper for ECKeyGenerationParameters getDomainParameters
	 * @return The object's ECDomainParameters
	 */
	public ECDomainParameters getDomainParameters()
	{
		return ((ECKeyGenerationParameters)keyGenParam).getDomainParameters();
	}
}