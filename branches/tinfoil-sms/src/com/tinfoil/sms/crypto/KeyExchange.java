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

import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

import android.util.Base64;

import com.tinfoil.sms.utility.SMSUtility;
import com.tinfoilsms.crypto.APrioriInfo;
import com.tinfoilsms.crypto.ECGKeyExchange;
import com.tinfoilsms.crypto.ECGKeyUtil;
import com.tinfoilsms.crypto.ECKeyParam;

import com.tinfoil.sms.dataStructures.Number;


/**
 * A class which operates as a facade to greatly simplify the underlying complexity
 * required to sign and verify keys needed to execute the public key exchange operations.
 * 
 * TODO Add support to get the signature for the keys so it can be added to the DB
 * TODO CLEAN THIS CLASS UP!
 */
public abstract class KeyExchange
{
    /**
     * Attempts to identify if the message received is a key exchange by checking
     * if the message is encoded as BASE64, which is the encoding used for 
     * key exchanges.
     * 
     * TODO This is a TEMPORARY solution to the key exchange problem, and should
     * be replaced with a better method of identifying key exchanges. For example
     * adding a small, but very fast, 8 byte checksum of the entire message and
     * simplifying the process by checking if the message matches a certain length.
     * 
     *      -------------------------------------
     *      | public key | signature | checksum |
     *      -------------------------------------
     */
    public static boolean isKeyExchange(String message)
    {
        try
        {
            Base64.decode(message, Base64.DEFAULT);
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
        
        return true;
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
    public static String encodedPubKey(String signedPubKey)
    {
        ECKeyParam param = new ECKeyParam();
        ECPublicKeyParameters pubKey = ECGKeyUtil.decodeBase64SignedPubKey(
                                                                param, 
                                                                new SHA256Digest(), 
                                                                signedPubKey.getBytes());
        
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
    public static String encodedSignature(String signedPubKey)
    {   
        byte[] decodedSignedPubKey = Base64.decode(signedPubKey, Base64.DEFAULT);
        SHA256Digest digest = new SHA256Digest();
        byte[] signature = new byte[digest.getDigestSize()];
        
        System.arraycopy(
                decodedSignedPubKey, 
                decodedSignedPubKey.length - digest.getDigestSize(), 
                signature, 
                0, 
                decodedSignedPubKey.length);
        
        return Base64.encodeToString(signature, Base64.DEFAULT);
    }
    
    
    /**
     * Signs the current user's public key using the apriori information shared
     * between the current user and the number provided. 
     * 
     * @param number The number that the key will be exchanged with
     * 
     * @return The current user's signed public key based on shared information
     * the user has with the number given.
     */
    public static String sign(Number number)
    {
        /* Get the current user's public key and shared information */
        ECKeyParam param = new ECKeyParam();
        ECPublicKeyParameters pubKey = ECGKeyUtil.decodeBase64PubKey(param, SMSUtility.user.getPublicKey());
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
        
        /* Return the signed public key in a BASE64 encoded, transmissible form */
        return Base64.encodeToString(encodedSignedPubKey, Base64.DEFAULT);
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
        return ECGKeyExchange.verifyPubKey(
                                    new SHA256Digest(), 
                                    Base64.decode(signedPubKey, Base64.DEFAULT), 
                                    sharedInfo, 
                                    number.isInitiator());
    }
}
