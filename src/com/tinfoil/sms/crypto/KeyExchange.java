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
package com.tinfoil.sms.crypto;

import java.nio.ByteBuffer;
import java.security.Security;
import java.util.Arrays;
import java.util.zip.CRC32;

import org.strippedcastle.crypto.digests.SHA256Digest;
import org.strippedcastle.crypto.params.ECPublicKeyParameters;
import org.strippedcastle.jce.provider.BouncyCastleProvider;
import org.strippedcastle.util.encoders.Hex;

import android.util.Base64;
import android.util.Log;

import com.orwell.crypto.APrioriInfo;
import com.orwell.crypto.ECGKeyExchange;
import com.orwell.crypto.ECGKeyUtil;
import com.orwell.params.ECKeyParam;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.User;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.database.InvalidDatabaseStateException;


/**
 * A class which operates as a facade to greatly simplify the underlying complexity
 * required to sign and verify keys needed to execute the public key exchange operations.
 * 
 * TODO Add support to get the signature for the keys so it can be added to the DB
 * TODO CLEAN THIS CLASS UP!
 */
public abstract class KeyExchange
{
    /* The size in bytes, of the checksum used, CRC32 is 4 bytes (32 bits) */
    private static final int CHECKSUM_SIZE = 4;
    
    public static final int VALID_KEY_EXCHANGE = 0;
    public static final int VALID_KEY_REVERSE = 1;
    public static final int INVALID_KEY_EXCHANGE = 2;    
    
    /* Register spongycastle as the most preferred security provider */
    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }
    
    /**
     * Attempts to identify if the message received is a key exchange by checking
     * if the message is encoded as Base64, matches the expected length of a
     * key exchange message, and has a valid CRC32 checksum.
     */
    public static boolean isKeyExchange(String message)
    {
        byte[] decodedMsg = null;
        
        try
        {
            /* Test if message is a key exchange, based on length and checksum */
            decodedMsg = Base64.decode(message, Base64.DEFAULT);
            
            Log.v("decoded message", new String(decodedMsg));
            Log.v("decoded message length", Integer.toString(decodedMsg.length));
            
            if (decodedMsg.length > 32 && validChecksum(decodedMsg))
            {
                Log.v("Message is key exchange", new String(Hex.encode(decodedMsg)));
                return true;
            }
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            return false;
        }
        
        return false;
    }
    
    
    /**
     * Gets the public key from the BASE64 encoded key exchange, which contains
     * the key and signature, and returns the public key encoded as BASE64, for 
     * proper storage and transmission in textual form.
     * 
     * @param signedPubKey The signed public key the user received from the number
     * 
     * @return The public key received, encoded as BASE64 for storage
     */
    public static byte[] encodedPubKey(String signedPubKey)
    {
        ECKeyParam param = new ECKeyParam();
        
        /* Decode the message in order to remove the checksum, then get the pubkey */
        byte[] decodedSignedPubKey = stripChecksum(Base64.decode(signedPubKey, Base64.DEFAULT));
        
        ECPublicKeyParameters pubKey = ECGKeyUtil.decodeBase64SignedPubKey(
                                                                param, 
                                                                new SHA256Digest(), 
                                                                Base64.encode(decodedSignedPubKey, Base64.DEFAULT));
        
       return ECGKeyUtil.encodeBase64PubKey(param, pubKey);
    }
    
    
    /**
     * Gets the signature from the BASE64 encoded key exchange, which contains
     * the key and signature, and returns the signature encoded as BASE64, for 
     * proper storage and transmission in textual form.
     * 
     * @param signedPubKey The signed public key the user received from the number
     * 
     * @return The public key received, encoded as BASE64 for storage
     */
    public static byte[] encodedSignature(String signedPubKey)
    {   
        byte[] decodedSignedPubKey = stripChecksum(Base64.decode(signedPubKey, Base64.DEFAULT));
        SHA256Digest digest = new SHA256Digest();
        byte[] signature = new byte[digest.getDigestSize()];
        
        System.arraycopy(
                decodedSignedPubKey, 
                decodedSignedPubKey.length - digest.getDigestSize(), 
                signature, 
                0, 
                digest.getDigestSize());
        
        return Base64.encode(signature, Base64.DEFAULT);
    }
    
    /**
     * Ensure that the user's information is in memory.
     * @param dba The database accessor to retrieve the user's key if necessary
     * @param user The user stored in memory. 
     * @return The user that is not null
     * @throws InvalidDatabaseStateException If the user's keys are not found then they have
     * been deleted thus making any key exchange prior invalid.
     */
    public static User getUser(DBAccessor dba, User user) throws InvalidDatabaseStateException
    {
    	if(user == null)
    	{
    		user = dba.getUserRow();
    		if (user == null)
    		{
    			throw new InvalidDatabaseStateException("User's keys not set in database");
    			//user = new User();
    			//dba.setUser(user);
    		}
    	}
    	return user;
    }
    
    /**
     * Wrapper method to sign a key exchange with the user's public key. This is used
     * to ensure that the user's information is in memory (and if not it is retrieved)
     * and catches and handles possible errors involving illegal database states.
     * 
     * Signs the current user's public key using the apriori information shared
     * between the current user and the number provided. After signing the
     * public key the CRC32 checksum is calculated and appended to the signed
     * public key.
     * 
     *      -------------------------------------
     *      | public key | signature | checksum |
     *      -------------------------------------
     * 
     * @param number The contact's Number.
     * @param dba The database interface.
     * @param user The user's information.
     * @return The current user's signed public key based on shared information
     * the user has with the number given.
     */
    public static String sign(Number number, DBAccessor dba, User user)
    {
    	try {
			user = getUser(dba, user);
			return signWithUser(number, user);
		} catch (InvalidDatabaseStateException e) {
			//TODO create error message to tell user key exchange failed because of db issue
			e.printStackTrace();
		}
    	return null;
    }
    
    /**
     * Signs the current user's public key using the apriori information shared
     * between the current user and the number provided. After signing the
     * public key the CRC32 checksum is calculated and appended to the signed
     * public key.
     * 
     *      -------------------------------------
     *      | public key | signature | checksum |
     *      -------------------------------------
     * 
     * @param number The number that the key will be exchanged with
     * @param user The user's information containing the user's public key.
     * 
     * @return The current user's signed public key based on shared information
     * the user has with the number given.
     */
    private static String signWithUser(Number number, User user)
    {
        /* Get the current user's public key and shared information */
        ECKeyParam param = new ECKeyParam();
        ECPublicKeyParameters pubKey = ECGKeyUtil.decodeBase64PubKey(param, user.getPublicKey());
        APrioriInfo sharedInfo = new APrioriInfo(number.getSharedInfo1(), number.getSharedInfo2());
        
        /* Sign the public key using the shared information based on whether the
         * current user is the initiator of the key exchange with the number
         */
        byte[] encodedPubKey = ECGKeyUtil.encodePubKey(param, pubKey);
        byte[] encodedSignedPubKey = ECGKeyExchange.signPubKey(
                                                        new SHA256Digest(), 
                                                        encodedPubKey, 
                                                        sharedInfo, 
                                                        number.isInitiator());
        
        /* Calculate the checksum of the signed public key */
        byte[] checksum = checksum(encodedSignedPubKey);
        byte[] signedPubKeySum = new byte[encodedSignedPubKey.length + checksum.length];
        
        Log.v("checksum", new String(checksum));
        Log.v("checksum length", Integer.toString(checksum.length));
        Log.v("publickey", Base64.encodeToString(encodedPubKey, Base64.DEFAULT));
        
        Log.v("Shared Info1:", number.getSharedInfo1());
        Log.v("Shared Info2:", number.getSharedInfo2());
        
        Log.v("decoded signedpubkey", new String(Hex.encode(encodedSignedPubKey)));
        
        
        System.arraycopy(encodedSignedPubKey, 0, signedPubKeySum, 0, encodedSignedPubKey.length);
        System.arraycopy(checksum, 0, signedPubKeySum, encodedSignedPubKey.length, checksum.length);
        
        /* Return the signed public key in a BASE64 encoded, transmissible form */
        Log.v("encoded signedpubkey", Base64.encodeToString(signedPubKeySum, Base64.DEFAULT));
        
        return Base64.encodeToString(signedPubKeySum, Base64.DEFAULT);
    }
    
    /**
     * Facilitate the verification of the key exchange and allow for
     * the user to decide if they wish to ignore the security vulnerability
     * caused by accepting a key exchange initiation from the other contact
     * after initiating a key exchange them self. (User A sends a key
     * exchange to User B and then receives a key exchange from User B
     * but the key exchange has User B as the initiator) This could lead to 
     * a man in the middle attack.
     * 
     * @param number The contact's Number
     * @param signedPubKey The signed public key received from the contact
     * @return If it is a valid key exchange the return will be VALID_KEY_EXCHANGE,
     * if the sender was set as the initiator (reversed) the return will be
     * VALID_KEY_REVERSE, otherwise it will return INVALID_KEY_EXCHANGE
     */
    public static int validateKeyExchange(Number number, String signedPubKey)
    {
    	if(verify(number, signedPubKey))
    	{
    		return VALID_KEY_EXCHANGE;
    	}
    	
    	if(number.isInitiator())
    	{
	    	// Check if the key exchange is initiated by the other person
	    	number.setInitiator(false);
	    	
	    	if(verify(number, signedPubKey))
	    	{
	    		return VALID_KEY_REVERSE;
	    	}
	    	
	    	// Set the initiator back to previous state
	    	number.setInitiator(true);
    	}
    	
		return INVALID_KEY_EXCHANGE;
    }
    
    /**
     * Verifies the public key received and verifies that the signature is valid
     * given the apriori information shared between the current user and the
     * number the key has been received from.
     * 
     * @param number The number that the key will be exchanged with
     * @param signedPubKey The signed public key the user received from the number
     * 
     * @return True if the signed public key received is valid
     * 
     * TODO possibly throw an exception instead
     */
    public static boolean verify(Number number, String signedPubKey)
    {
        APrioriInfo sharedInfo = new APrioriInfo(number.getSharedInfo1(), number.getSharedInfo2());
        
        Log.v("signedpubkey received", signedPubKey);
        Log.v("Shared Info1:", number.getSharedInfo1());
        Log.v("Shared Info2:", number.getSharedInfo2());
        
        /* Decode the key exchange, and strip the checksum */
        byte[] decodedSignedPubKey = stripChecksum(Base64.decode(signedPubKey, Base64.DEFAULT));

        Log.v("decoded signedpubkey received", new String(Hex.encode(decodedSignedPubKey)));
        
        boolean result = ECGKeyExchange.verifyPubKey(
                                    new SHA256Digest(), 
                                    decodedSignedPubKey, 
                                    sharedInfo, 
                                    number.isInitiator());
        
        Log.v("valid key?", Boolean.toString(result));
        return result;
    }
    
    
    /**
     * Calculates the CRC32 checksum of the input provided.
     * 
     * @param input The input to calculate the CRC32 checksum for
     * @return The CRC32 checksum
     */
    private static byte[] checksum(byte[] input)
    {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        CRC32 checksum = new CRC32();
        
        checksum.update(input);
        buffer.putLong(checksum.getValue());
        
        Log.v("checksum as hex",  Long.toHexString(checksum.getValue()));
        
        /* Since CRC32 is 4 bytes , truncate the value to only be 4 bytes */
        byte[] checksum32 = new byte[CHECKSUM_SIZE];
        System.arraycopy(buffer.array(), 4, checksum32, 0, CHECKSUM_SIZE);
        Log.v("checksum converted to 4 bytes",  new String(Hex.encode(checksum32)));
        
        return checksum32;
    }
    
    
    /**
     * Strips the checksum from the byte data for the signed public key
     * used in the key exchange, which includes the additional checksum.
     */
    private static byte[] stripChecksum(byte[] input)
    {
        byte[] data = new byte[input.length - CHECKSUM_SIZE];
        
        /* Remove the checksum from the input data */
        System.arraycopy(input, 0, data, 0, data.length);
        
        return data;
    }
    
    
    /**
     * Calculates if the checksum the input has matches the calculated
     * checksum, returns true if the checksum is valid.
     * 
     * @param input Which includes the checksum to validate
     * @return True if the input has a valid checksum
     */
    private static boolean validChecksum(byte[] input)
    {
        /* Get the data and the checksum of the data */
        byte[] data = new byte[input.length - CHECKSUM_SIZE];
        byte[] checksum = new byte[CHECKSUM_SIZE];
        
        System.arraycopy(input, 0, data, 0, data.length);
        System.arraycopy(input, data.length, checksum, 0, checksum.length);
        
        /* Calculate the checksum of the data */
        byte[] calcChecksum = checksum(data);
        
        Log.v("original checksum", new String(checksum));
        Log.v("calculated checksum", new String(calcChecksum));
        
        /* Check if the checksum of the data matches the calculated checksum */
        return Arrays.equals(checksum, calcChecksum);
    }
}
