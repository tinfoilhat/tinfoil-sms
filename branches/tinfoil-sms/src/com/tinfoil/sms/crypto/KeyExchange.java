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
 */
public abstract class KeyExchange
{
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
        
        /* Sign the public key using the shared information and based on who is
         * the initiator of the key exchange
         */
        byte[] encodedPubKey = ECGKeyUtil.encodeBase64PubKey(param, pubKey);
        byte[] encodedSignPubKey = ECGKeyExchange.signPubKey(
                                                        new SHA256Digest(), 
                                                        encodedPubKey, 
                                                        sharedInfo, 
                                                        number.isInitiator());
        
        return new String(encodedSignPubKey);
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
                                    signedPubKey.getBytes(), 
                                    sharedInfo, 
                                    number.isInitiator());
    }
}
