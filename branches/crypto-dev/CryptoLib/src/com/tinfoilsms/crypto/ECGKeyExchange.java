/** 
 * Copyright (C) 2012 Tinfoilhat
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

import java.util.Arrays;

import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.Digest;

/**
 * The Elliptic Curve Gillett (ECG) Exchange, provides support for signing
 * and verifying the keys exchanged as part of an ECC/IES encryption scheme
 * by using shared information S1 and S2 that users have agreed upon before 
 * initiating the exchange.
 * 
 * In the event that the users initiate a key exchange without any shared
 * information the default shared information S1 = "initiator" and 
 * S2 = "recipient" are used.
 */
public abstract class ECGKeyExchange
{
    /*
     * NOTE: THIS SHOULD THROW AN EXCEPTION IF S1 "OR" S2 ARE NULL, BUT S1 "AND" S2 NULL IS OK
     * 
     * signPubKey A function which takes an ASN.1 encoded public key Q and 
     * signs the public key using the shared information S1 if initiating the key
     * exchange or S2 if responding to a key exchange.
     * 
     * The function returns the signed public key which is a byte array containing the hash
     * of the public key concatenated with the public key.
     * 
     * @param digest The digest function to use for signing the key such as SHA256
     * @param encodedPubkey A byte array of the ASN.1 encoded public key Q
     * @param sharedInfo The shared information, S1 and S2
     * @param isInitiator True if initiating a key exchange, false if responding to a key exchange
     * 
     * @return A byte array containing public key concatenated with the hash of the public key
     * 
     * @throws DataLengthException if shared information is empty
     */
    static public byte[] signPubKey(Digest digest, 
    								byte[] encodedPubKey, 
									APrioriInfo sharedInfo, 
									boolean isInitiator)
	throws DataLengthException
    {
    	if (sharedInfo.getS1().length == 0 || sharedInfo.getS2().length == 0)
    	{
    		throw new DataLengthException("The shared information S1 and S2 cannot be null/empty!");
    	}

    	/*
    	 * The shared information to use for signing the public key
    	 * The digest input which is the public key concatenated with S1 or S2
    	 * Signed public key byte array to hold the public key and hash 
    	 */
    	byte[] S;
    	byte[] digestInput;
    	byte[] signedPubKey = new byte[encodedPubKey.length + digest.getDigestSize()];
    	System.arraycopy(encodedPubKey, 0, signedPubKey, 0, encodedPubKey.length);
    	
    	/*
    	 * Set the shared information as S1 if initiating key exchange, S2 if
    	 * responding to key exchange
    	 */
    	if (isInitiator) {
    		S = sharedInfo.getS1();
		} else { 
    		S = sharedInfo.getS2();		// Use S2 for the recipient	
		}
    	
    	/*
    	 * Calculate the signature of the encoded public key concatenated with
    	 * shared information, S 
    	 */
		digestInput = new byte[encodedPubKey.length + S.length];
		System.arraycopy(encodedPubKey, 0, digestInput, 0, encodedPubKey.length);
		System.arraycopy(S, 0, digestInput, encodedPubKey.length, S.length);
		
		/*
		 * Sign the public key, concatenate the signature to public key
		 */
		digest.update(digestInput, 0, digestInput.length);
		digest.doFinal(signedPubKey, encodedPubKey.length);
		digest.reset();
		
		return signedPubKey;
    }
    
    
    /*
     * NOTE: THIS SHOULD THROW AN EXCEPTION IF S1 OR S2 ARE NULL, BUT S1 AND S2 NULL IS OK
     * 
     * verify_publickey A function which takes a signed public key and verifies
     * the public key using the shared information S1 if initiating the key exchange
     * or S2 if responding to a key exchange.
     * 
     * The function returns true if the signature of the signed public key received
     * matches the calculated signature of the public key concatenated with S1 or S2.
     * 
     * @param digest the digest function to use for signing the key such as SHA256
     * @param signedPubKey byte array containing public key concatenated with the hash of the public key 
     * @param sharedInfo The shared information, S1 and S2
     * @param isInitiator True if initiating a key exchange, false if responding to a key exchange
     * 
     * @return true if the public key is verified to be valid
     * 
     * @throws DataLengthException if shared information is empty
     */
    static public boolean verifyPubKey(Digest digest, 
    								   byte[] signedPubKey, 
    								   APrioriInfo sharedInfo,
    								   boolean isInitiator)
	throws DataLengthException
    {
    	if (sharedInfo.getS1().length == 0 || sharedInfo.getS2().length == 0)
    	{
    		throw new DataLengthException("The shared information S1 and S2 cannot be null/empty!");
    	}
    	
    	/*
    	 * The shared information to use for verifying the public key
    	 * The digest input which is the public key concatenated with S1 or S2
    	 * The originator of the public key's signature and the calculated signature
    	 */
    	byte[] S;
    	byte[] digestInput;
    	byte[] origSignature = new byte[digest.getDigestSize()];
    	byte[] calcSignature = new byte[digest.getDigestSize()];
    	System.arraycopy(signedPubKey, signedPubKey.length - digest.getDigestSize(), 
    			origSignature, 0, digest.getDigestSize());
    	
    	/*
    	 * Set the shared information as S1 if initiating key exchange, S2 if
    	 * responding to key exchange
    	 */
    	if (isInitiator) {
    		S = sharedInfo.getS1();
		} else { 
    		S = sharedInfo.getS2();		// Use S2 for the recipient	
		}
    	
    	/*
    	 * Calculate the signature of the encoded public key concatenated with
    	 * shared information, S 
    	 */
		digestInput = new byte[signedPubKey.length - digest.getDigestSize() + S.length];
		System.arraycopy(signedPubKey, 0, digestInput, 0, signedPubKey.length - digest.getDigestSize());
		System.arraycopy(S, 0, digestInput, signedPubKey.length - digest.getDigestSize(), S.length);
		
		/*
		 * Calculated the signature of the public key
		 */
		digest.update(digestInput, 0, digestInput.length);
		digest.doFinal(calcSignature, 0);
		digest.reset();
		
		/*
		 * Verify that the calculated signature matches the signature of the
		 * signed public key received, returns true if the signatures match
		 */
		return signatureEquals(calcSignature, origSignature);
    }

    
	/**
	 * Verifies if two signatures are exactly equal using a byte-level
	 * comparison of the values. Usually used to verify the signature 
	 * of a key that has been exchanged.
	 * 
	 * @param sig1 The first signature to compare
	 * @param sig2 The second signature to compare
	 * 
	 * @return boolean, true if the signatures are identical
	 */
    private static boolean signatureEquals(byte[] sig1, byte[] sig2)
    {
        if (sig1.length != sig2.length)
        {
            return false;
        }

        for (int i = 0; i != sig1.length; ++i)
        {
            if (sig1[i] != sig2[i])
            {
                return false;
            }
        }

        return true;
    }
}
