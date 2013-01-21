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

import android.util.Base64;

import java.math.BigInteger;
import java.security.InvalidParameterException;

import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

/**
 * A helpful key utility class which contains utility operations which are used
 * when performing an Elliptic Curve Gillett (ECG) Exchange.
 * 
 * It provide key exchange utility operations for encoding and decoding Elliptic
 * Curve keys.
 */
public abstract class ECGKeyUtil
{
    /**
     * encodePubKey A function which takes an ECC public key parameter object
     * and returns the ASN.1 encoded X and Y values for the public key Q.
     * 
     * @param keyParam The Elliptic Curve key parameter which contains the curve
     * specifications and domain parameters
     * @param pubKey an ECC public key parameter which implements CipherParameters
     * @return A byte array of the ASN.1 encoded public key Q
     */
    static public byte[] encodePubKey(ECKeyParam keyParam, CipherParameters pubKey)
    		throws InvalidParameterException
    {
    	if (pubKey instanceof ECPublicKeyParameters)
    	{
    		/*
    		 * This statement does the following:
    		 *     
    		 *     1. It takes the X and Y value of the public key Q
    		 *     2. Then creates a single encoded byte array for the public key Q
    		 *     3. Finally it creates a hex encoded byte array (ASN.1) of the encoded public key Q
    		 */
    		return keyParam.getCurve().createPoint(
    					((ECPublicKeyParameters)pubKey).getQ().getX().toBigInteger(), 	// X
    					((ECPublicKeyParameters)pubKey).getQ().getY().toBigInteger(), 	// Y
    					true) 															// Use Compression
					.getEncoded();	// Encoded public key Q
    	}
    	else
    	{
    		throw new InvalidParameterException("The public key provided is not an ECPublicKeyParameters");
    	}
    }
    
    
    /**
     * encodeBase64PubKey A wrapper function for encodePubKey() which takes an ECC 
     * public key parameter object and returns the ASN.1 encoded X and Y values
     * for the public key Q that is then encoded in base64 encoding for proper
     * storage and transmission in textual form.
     * 
     * This may be needed to transmit/store the ASN.1 encoded public key properly.
     * 
     * @param keyParam The Elliptic Curve key parameter which contains the curve
     * specifications and domain parameters
     * @param pubKey an ECC public key parameter which implements CipherParameters
     * @return A byte array of the ASN.1 encoded public key Q
     */
    static public byte[] encodeBase64PubKey(ECKeyParam keyParam, CipherParameters pubKey)
    		throws InvalidParameterException
    {
    	return Base64.encode(encodePubKey(keyParam, pubKey), Base64.DEFAULT);
    }
    
    
    /**
     * encodeBase64PriKey A function which takes an ECC private key parameter object
     * and returns the private key D BigInteger value that is encoded as base64 for
     * proper storage and transmission in textual form.
     * 
     * @param keyParam The Elliptic Curve key parameter which contains the curve
     * specifications and domain parameters
     * @param priKey an ECC private key parameter object which implements CipherParameters
     * @return A byte array of the base64 encoded private key D BigInteger value
     * @throws InvalidParameterException
     */
    static public byte[] encodeBase64PriKey(ECKeyParam keyParam, CipherParameters priKey)
    		throws InvalidParameterException
	{
    	if (priKey instanceof ECPrivateKeyParameters)
    	{
    		/* Return the private key D BigInteger value encoded as base64 */
    		return Base64.encode(
    				((ECPrivateKeyParameters) priKey).getD().toByteArray(), 
    				Base64.DEFAULT);
    	}
    	else
    	{
    		throw new InvalidParameterException("The private key provided is not an ECPrivateKeyParameters");
    	}
	}
    
    
    /**
     * decodePubKey A function which takes an ASN.1 encoded ECC public key Q
     * and returns an ECPublicKeyParameters object for the public key Q. 
     * 
     * @param keyParam The Elliptic Curve key parameter which contains the curve
     * specifications and domain parameters
     * @param encodedPubkey A byte array of the ASN.1 encoded public key Q
     * @return An ECC public key parameter for Q, ECPublicKeyParametersimplements
     */
    static public ECPublicKeyParameters decodePubKey(ECKeyParam keyParam, 
    												 byte[] encodedPubKey)
    {
		/*
		 * Takes the encoded public key Q and decodes an X and Y value for 
		 * the point Q, then returns an ECPublicKeyParameters object for
		 * the elliptic curve parameters specified 
		 */
    	return new ECPublicKeyParameters(
    			keyParam.getCurve().decodePoint(encodedPubKey), 	// Q
    			keyParam.getECDomainParam());
    }
    
    
    /**
     * decodeBase64PubKey A wrapper function for decodePubKey which takes an 
     * ASN.1 encoded ECC public key Q that was then encoded as base64
     * and returns an ECPublicKeyParameters object for the public key Q. 
     * 
     * @param keyParam The Elliptic Curve key parameter which contains the curve
     * specifications and domain parameters
     * @param encodedPubkey A byte array of the ASN.1 encoded public key Q
     * @return An ECC public key parameter for Q, ECPublicKeyParametersimplements
     */
    static public ECPublicKeyParameters decodeBase64PubKey(	ECKeyParam keyParam, 
    														byte[] encodedPubKey)
    {
    	return decodePubKey(keyParam, Base64.decode(encodedPubKey, Base64.DEFAULT));
    }
    
    
    /**
     * decodeSignedPubKey A function which takes an ASN.1 encoded ECC public key Q
     * that is signed using the Elliptic Curve Gillett (ECG) Exchange key exchange
     * and returns an ECPublicKeyParameters object for the public key Q.
     * 
     * @param keyParam The Elliptic Curve key parameter which contains the curve
     * specifications and domain parameters
     * @param digest The digest function used to originally sign the key such as SHA256
     * @param signedPubkey A byte array of the ASN.1 encoded public key Q that is signed
     * @return An ECC public key parameter for Q, ECPublicKeyParametersimplements
     */
    public ECPublicKeyParameters decodeSignedPubKey(ECKeyParam keyParam, 
													Digest digest, 
													byte[] signedPubKey)
    {
    	/*
    	 * Retrieve the ASN.1 encoded ECC public key Q from the contents of signed public key  
    	 */
    	byte[] encodedPubKey = new byte[signedPubKey.length - digest.getDigestSize()];
    	System.arraycopy(signedPubKey, 0, encodedPubKey, 0, signedPubKey.length - digest.getDigestSize());
    	
		/*
		 * Takes the encoded public key Q and decodes an X and Y value for 
		 * the point Q, then returns an ECPublicKeyParameters object for
		 * the elliptic curve parameters specified 
		 */
    	
    	return new ECPublicKeyParameters(
    			keyParam.getCurve().decodePoint(encodedPubKey), 	// Q
    			keyParam.getECDomainParam());
    }
    
    
    /**
     * decodeBase64SignedPubKey A wrapper function for decodeSignedPubKey which 
     * takes an ASN.1 encoded ECC public key Q that is signed using the Elliptic
     * Curve Gillett (ECG) Exchange key exchange and that was then encoded as base64
     * and returns an ECPublicKeyParameters object for the public key Q.
     * 
     * @param keyParam The Elliptic Curve key parameter which contains the curve
     * specifications and domain parameters
     * @param digest The digest function used to originally sign the key such as SHA256
     * @param signedPubkey A byte array of the ASN.1 encoded public key Q that is signed
     * @return An ECC public key parameter for Q, ECPublicKeyParametersimplements
     */
    public ECPublicKeyParameters decodeBase64SignedPubKey(ECKeyParam keyParam, 
    													  Digest digest, 
														  byte[] signedPubKey)
    {
    	return decodeSignedPubKey(keyParam, digest, Base64.decode(signedPubKey, Base64.DEFAULT));
    	
    }
    
    
    /**
     * decodePriKey A function which takes an ECC private key parameter object and 
     * returns an ECPrivateKeyParameters object for the private key D BigInteger 
     * value.
     * 
     * @param keyParam The Elliptic Curve key parameter which contains the curve
     * specifications and domain parameters
     * @param priKey a byte array of the private key D BigInteger value
     * @return an ECPrivateKeyParameters object for the private key D BigInteger value
     */
    public ECPrivateKeyParameters decodePriKey(ECKeyParam keyParam,
    												 byte[] encodedPriKey)
    {
    	return new ECPrivateKeyParameters(
    			new BigInteger(encodedPriKey),		// D
    			keyParam.getECDomainParam());
    }
    
    
    /**
     * decodeBase64PriKey A function wrapper function for decodePriKey which takes
     * a base64 encoded ECC private key parameter object and returns an 
     * ECPrivateKeyParameters object for the private key D BigInteger value.
     * 
     * @param keyParam The Elliptic Curve key parameter which contains the curve
     * specifications and domain parameters
     * @param priKey a base64 encoded byte array of the private key D BigInteger value
     * @return an ECPrivateKeyParameters object for the private key D BigInteger value
     */
    public ECPrivateKeyParameters decodeBase64PriKey(ECKeyParam keyParam,
    												 byte[] encodedPriKey)
    {
    	return decodePriKey(keyParam, Base64.decode(encodedPriKey, Base64.DEFAULT));
    }
}