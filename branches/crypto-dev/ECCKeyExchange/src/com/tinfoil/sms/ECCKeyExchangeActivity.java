/** 
 * Copyright (C) 2011 Tinfoilhat
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

package com.tinfoil.sms;

import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.spec.ECParameterSpec;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;

import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.KeyGenerationParameters;
import org.spongycastle.crypto.agreement.ECDHBasicAgreement;
import org.spongycastle.crypto.agreement.ECDHCBasicAgreement;
import org.spongycastle.crypto.digests.SHA1Digest;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.IESEngine;
import org.spongycastle.crypto.engines.RijndaelEngine;
import org.spongycastle.crypto.engines.TwofishEngine;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.generators.KDF2BytesGenerator;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.modes.OFBBlockCipher;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.params.IESParameters;
import org.spongycastle.crypto.params.IESWithCipherParameters;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.util.encoders.Hex;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;
import android.os.Bundle;

public class ECCKeyExchangeActivity extends Activity {
	private static final String TAG = "ECIES";
	static {
	    Security.addProvider(new BouncyCastleProvider());
	}
	
    /* 
     * Get the elliptic curve specifications for NIST P-256
     */
    private static final ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
    
    /*
     * Define the elliptic curve parameters based on NIST P-256 specs
     */
    private static final ECDomainParameters params = new ECDomainParameters(
    									ecSpec.getCurve(),	// Curve
								        ecSpec.getG(),		// G
								        ecSpec.getN());		// N
    
    
    /*
     * The pre-defined shared information that the users have agreed upon before
     * initiating the key exchange 
     */
    byte[] S1 = "This is 256bit secret passphrase".getBytes();
    byte[] S2 = "Another 256bit secret passphrase".getBytes();
    
    
	/*
	 * are_same A function which checks if two array of bytes are identical
	 * 
	 * @param a first array of bytes
	 * @param b first array of bytes
	 * 
	 * @return boolean, true if identical
	 */
	private boolean are_same(
	        byte[]  a,
	        byte[]  b)
	    {
	        if (a.length != b.length)
	        {
	            return false;
	        }

	        for (int i = 0; i != a.length; i++)
	        {
	            if (a[i] != b[i])
	            {
	                return false;
	            }
	        }

	        return true;
	    }
    
	
    /*
     * pubKey_to_string A function which takes an ECC public key parameter
     * object and returns the X and Y values for the public key Q as a string
     */
    public String publickey_to_string(CipherParameters pubKey) throws InvalidParameterException
    {
    	if (pubKey instanceof ECPublicKeyParameters)
    	{
    		// Cast the CipherParameters argument as ECPublicKeyParameters
    		return "X: \n" + ((ECPublicKeyParameters)pubKey).getQ().getX().toBigInteger().toString() 
        			+ "\nY: \n" + ((ECPublicKeyParameters)pubKey).getQ().getY().toBigInteger().toString();
    	}
    	else
    	{
    		throw (new InvalidParameterException("The public key provided is not an ECPublicKeyParameters"));
    	}
    }
    
    /*
     * encode_publickey A function which takes an ECC public key parameter
     * object and returns the ASN.1 encoded X and Y values for the public key
     * Q.
     * 
     * @param pubKey an ECC public key parameter which implements CipherParameters
     * @return A byte array of the hex encoded public key Q
     */
    public byte[] encode_publickey(CipherParameters pubKey) throws InvalidParameterException
    {
    	if (pubKey instanceof ECPublicKeyParameters)
    	{
    		/*
    		 * This statement does the following:
    		 *     
    		 *     1. It takes the X and Y value of the public key Q
    		 *     2. Then creates a single encoded byte array for the public key Q
    		 *     3. Finally it creates a hex encoded byte array of the encoded public key Q
    		 *     
    		 */
        	return Hex.encode(ecSpec.getCurve().createPoint(
        				((ECPublicKeyParameters)pubKey).getQ().getX().toBigInteger(), 	// X
        				((ECPublicKeyParameters)pubKey).getQ().getY().toBigInteger(), 	// Y
        				true).getEncoded());	// Encoded public key Q
    	}
    	else
    	{
    		throw (new InvalidParameterException("The public key provided is not an ECPublicKeyParameters"));
    	}
    }
    
    
    /*
     * decode_publickey A function which takes a hex encoded byte array of the 
     * ASN.1 encoded ECC public key Q and returns an ECPublicKeyParameters object
     * for the public key Q 
     * 
     * @param encodedPubkey A byte array of the hex encoded ASN.1 encoded public key Q
     * @return An ECC public key parameter for Q, ECPublicKeyParametersimplements
     */
    public ECPublicKeyParameters decode_publickey(byte[] encodedPubKey)
    {
    		/*
    		 * Takes the encoded public key Q and decodes an X and Y value for 
    		 * the point Q, then returns an ECPublicKeyParameters object for
    		 * the elliptic curve parameters specified 
    		 */
        	return new ECPublicKeyParameters(
        			ecSpec.getCurve().decodePoint(Hex.decode(encodedPubKey)), 	// Q
        		    params);
    }    
   
    
    /*
     * NOTE: THIS SHOULD THROW AN EXCEPTION IF S1 OR S2 ARE NULL, BUT S1 AND S2 NULL IS OK
     * 
     * sign_publickey A function which takes a hex encoded ASN.1 encoded public key Q
     * and signs the public key using the shared information S1 if initiating the key exchange
     * or S2 if responding to a key exchange.
     * 
     * The function returns the signed public key which is a byte array containing the hash
     * of the public key concatenated with the public key.
     * 
     * @param digest the digest function to use for signing the key such as SHA256
     * @param encodedPubkey A byte array of the hex encoded ASN.1 encoded public key Q
     * @param isResponse True if responding to a key exchange, false if initiating key exchange
     * @return byte array containing public key concatenated with the hash of the public key
     */
    public byte[] sign_publickey(Digest digest, byte[] encodedPubKey, boolean isResponse)
    {
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
    	if (isResponse) {
    		S = S2;
		} else { 
    		S = S1;
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
     * @param isResponse True if responding to a key exchange, false if initiating key exchange
     * @return true if the public key is verified to be valid
     */
    public boolean verify_publickey(Digest digest, byte[] signedPubKey, boolean isResponse)
    {
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
    	if (isResponse) {
    		S = S2;
		} else { 
    		S = S1;
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
		
		/*
		 * Verify that the calculated signature matches the signature of the
		 * signed public key received
		 */
		if (are_same(calcSignature, origSignature))
		{
			Toast.makeText(getApplicationContext(), "Valid signature", Toast.LENGTH_LONG).show();
			return true;
		}
		else
		{
			Toast.makeText(getApplicationContext(), "INVALID SIGNATURE!", Toast.LENGTH_LONG).show();
			return false;
		}
    }
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*
         * Generate alice and bob's keypairs. This simulates each user already
         * having their keypair generated on their own device
         */
        ECKeyPairGenerator eGen = new ECKeyPairGenerator();
        KeyGenerationParameters gParam = new ECKeyGenerationParameters(params, new SecureRandom());

        eGen.init(gParam);

        AsymmetricCipherKeyPair alice = eGen.generateKeyPair();
        AsymmetricCipherKeyPair bob = eGen.generateKeyPair();
        
        // Bob encoded public key Q
        byte[] encodedPubKey = encode_publickey(bob.getPublic());
        
        // Bob signed public key
        byte[] signedPubKey = sign_publickey(new SHA256Digest(), encodedPubKey, false);
        
        TextView tv = new TextView(this);
        tv.setText("Bob public key, Q: \n" + publickey_to_string(bob.getPublic())
        		+ "\n\nBob encoded public key, Q:\n" + new String(encodedPubKey)
        		+ "\n\nBob encoded public key Q, DECODED:\n" + publickey_to_string(decode_publickey(encodedPubKey))
        		+ "\n\nBob signed public key:\n" + new String(signedPubKey));
        setContentView(tv);
        
        // Verify Bob signed public key
        verify_publickey(new SHA256Digest(), signedPubKey, false);
    }
}