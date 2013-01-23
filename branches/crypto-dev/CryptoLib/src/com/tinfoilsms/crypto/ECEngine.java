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

import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.agreement.ECDHCBasicAgreement;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.IESEngine;
import org.spongycastle.crypto.generators.KDF2BytesGenerator;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.modes.OFBBlockCipher;
import org.spongycastle.crypto.params.IESParameters;
import org.spongycastle.crypto.params.IESWithCipherParameters;


/**
 * A wrapper class that simplifies the creation of the IESEngine and all of
 * the parameters needed for using the IESEngine such as the block cipher mode,
 * block cipher engine, ECDH key exchange protocol, key derivative function (KDF),
 * and HMAC.
 */
public class ECEngine
{
	/* The block size and IES cipher key size, these values should NOT be
	 * changed unless you know what you are doing. For example, AES does NOT
	 * support a block size larger that 128-bit and will fail if you change
	 * block size to 256-bit.
	 */
	private static final int BLOCKSIZE = 128;
	private static final int CIPHERKEYSIZE = 256;

	private IESEngine engine;
	private BufferedBlockCipher cipher;
	private IESParameters param;

	
	/**
	 * The default constructor, creates an instance of the ECEngine using the
	 * following default parameters, it is recommended that you use this
	 * constructor unless you have a pressing need to deviate from the default
	 * parameters.
	 * 
	 * ECEngine is created using the following default parameters:
	 *  - OFBBlockCipher mode using AESEngine with 128-bit block size
	 *  - ECDHCBasicAgreement key agreement
	 *  - KDF2BytesGenerator Key derivative function
	 *  - SHA256 Digest/HMAC
	 * 
	 * @param sharedInfo the shared information exchanged by users a priori
	 * 
	 * TODO: For OFB and CTR, reusing an IV completely destroys security, look into
	 * the security vulnerabilities that exist in using OFB, verify if possible how
	 * the IV is handled by IESEngine, may have to use CBC or find a way to specify
	 * unique IV!
	 */
	public ECEngine(APrioriInfo sharedInfo)
	{
		/*
		 * AES With OFB, which does not require padding
		 * 
		 * WARNING: For OFB and CTR, reusing an IV completely destroys security.
		 * http://en.wikipedia.org/wiki/Block_cipher_modes_of_operation#Counter_.28CTR.29
		 * 
		 * I must do research to ensure that the IV is never reused, although
		 * I believe the library handles it already.
		 * 
		 * AESEngine ONLY SUPPORTS 128-bit block size, but supports 256-bit key
		 * a block size of 128-bit is being used with a 256-bit shared secret key
		 * 
		 */
		this.cipher = new BufferedBlockCipher(new OFBBlockCipher(new AESEngine(), BLOCKSIZE));
		
		/* Instantiate the IESEngine using the default parameters */
		this.engine = new IESEngine(new ECDHCBasicAgreement(),
									new KDF2BytesGenerator(new SHA256Digest()),
									new HMac(new SHA256Digest()),
									this.cipher);
		
		this.param = new IESWithCipherParameters(sharedInfo.getS1(), 
												 sharedInfo.getS2(), 
												 CIPHERKEYSIZE, 
												 CIPHERKEYSIZE);
	}
	
	
	/**
	 * Constructor, specify the block cipher to use such as CBC or BlowFish.
	 * 
	 * @param cipher The buffered block cipher to use such as CBC or BlowFish
	 * @param sharedInfo the shared information exchanged by users a priori
	 * 
	 * TODO: Not sure whether to have it to just take a cipher that implements
	 * BlockCipher interface and instantiate the BufferedBlockCipher or to allow
	 * it to take the BufferedBlockCipher and simply use that... BufferedBlockCipher
	 * has multiple subclasses which could be used such as CTSBlockCipher
	 */
	public ECEngine(BufferedBlockCipher cipher, APrioriInfo sharedInfo)
	{
		this.cipher = cipher;
		
		/* Instantiate the IESEngine using the default parameters */
		this.engine = new IESEngine(new ECDHCBasicAgreement(),
									new KDF2BytesGenerator(new SHA256Digest()),
									new HMac(new SHA256Digest()),
									this.cipher);
		
		this.param = new IESWithCipherParameters(sharedInfo.getS1(), 
												 sharedInfo.getS2(), 
												 CIPHERKEYSIZE, 
												 CIPHERKEYSIZE);
	}

	
	/**
	 * Constructor, specify the digest to use such as such as SHA256/MD5
	 * @param digest The digest to use for KDF/HMAC such as SHA256/MD5
	 * @param sharedInfo the shared information exchanged by users a priori
	 */
	public ECEngine(Digest digest, APrioriInfo sharedInfo)
	{
		/*
		 * AES With OFB, which does not require padding
		 * 
		 * WARNING: For OFB and CTR, reusing an IV completely destroys security.
		 * http://en.wikipedia.org/wiki/Block_cipher_modes_of_operation#Counter_.28CTR.29
		 * 
		 * I must do research to ensure that the IV is never reused, although
		 * I believe the library handles it already.
		 * 
		 * AESEngine ONLY SUPPORTS 128-bit block size, but supports 256-bit key
		 * a block size of 128-bit is being used with a 256-bit shared secret key
		 * 
		 */
		this.cipher = new BufferedBlockCipher(new OFBBlockCipher(new AESEngine(), BLOCKSIZE));
		
		/* Instantiate the IESEngine using the digest provided */
		this.engine = new IESEngine(new ECDHCBasicAgreement(),
									new KDF2BytesGenerator(digest),
									new HMac(digest),
									this.cipher);
		
		this.param = new IESWithCipherParameters(sharedInfo.getS1(), 
												 sharedInfo.getS2(), 
												 CIPHERKEYSIZE, 
												 CIPHERKEYSIZE);
	}
	
	
	/**
	 * Constructor, specify the block cipher and digest to use
	 * 
	 * @param cipher The buffered block cipher to use such as CBC or BlowFish
	 * @param digest The digest to use for KDF/HMAC such as SHA256/MD5
	 * @param sharedInfo the shared information exchanged by users a priori
	 */
	public ECEngine(BufferedBlockCipher cipher, Digest digest, APrioriInfo sharedInfo)
	{
		this.cipher = cipher;
		
		/* Instantiate the IESEngine using the digest provided */
		this.engine = new IESEngine(new ECDHCBasicAgreement(),
									new KDF2BytesGenerator(digest),
									new HMac(digest),
									this.cipher);
		
		this.param = new IESWithCipherParameters(sharedInfo.getS1(), 
												 sharedInfo.getS2(), 
												 CIPHERKEYSIZE, 
												 CIPHERKEYSIZE);		
	}
	
	
	/**
	 * A wrapper for IESEngine, initializes the elliptic curve encryption engine
	 * in either encryption or decryption mode.
	 * @param forEncrypt Engine mode, true for encryption, false for decryption
	 * @param priKey Your private key parameters
	 * @param pubKey The recipient's public key parameters
	 */
	public void init(boolean forEncrypt, CipherParameters priKey, CipherParameters pubKey)
	{
		engine.init(forEncrypt, priKey, pubKey, param);
	}
	
	
	/**
	 * A wrapper for IESEngine processBlock() method, receives user input and
	 * encrypts or decrypts the input received.
	 * @param in The input to encrypt or decrypt
	 * @return A byte array containing the contents of the encrypted/decrypted data
	 * 
	 * @throws InvalidCipherTextException if the encryption/decryption fails or if 
	 * the HMAC is invalid (possible data corruption or tampering) 
	 */
	public byte[] processBlock(byte[] in)
			throws InvalidCipherTextException
	{
		return engine.processBlock(in, 0, in.length);
	}
	
	
	/**
	 * A wrapper for IESEngine processBlock() method, receives user input and
	 * encrypts or decrypts the input received.
	 * @param in The input to encrypt or decrypt
	 * @param inOff The offset in the input to encrypt/decrypt
	 * @param inLen The length of the input to encyrpt/decrypt
	 * @return A byte array containing the contents of the encrypted/decrypted data
	 * 
	 * @throws InvalidCipherTextException if the encryption/decryption fails or if 
	 * the HMAC is invalid (possible data corruption or tampering) 
	 */
	public byte[] processBlock(byte[] in, int inOff, int inLen) 
			throws InvalidCipherTextException
	{
		return engine.processBlock(in, inOff, in.length);
	}
}