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

import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

import com.orwell.crypto.ECGKeyUtil;
import com.orwell.crypto.ECKey;
import com.orwell.crypto.ECKeyParam;


/**
 * A class which greatly simplifies the process of generating a secure,
 * unique public and private keypair using Elliptic Curve Cryptography.
 * This class should only be used when generating the initial
 * public/private keypair the very first time the application is 
 * executed or when a user wishes to generate a new keypair.
 */
public class KeyGenerator
{
    private final ECKeyParam param;
    private final ECKey key;
    
    
    /**
     * Initialize the key generator, which creates the initial, unique
     * point on the elliptic curve, which is used to generate the public
     * and private keys.
     */
    public KeyGenerator()
    {
        /* Create an instance of the ECKeyParam object with default curve */
        param = new ECKeyParam();
        
        /* Create an instance of the elliptic curve key */
        key = new ECKey(param.getECDomainParam());
        key.init();
    }
    
    
    /**
     * Generates a unique and secure public key using Elliptic Curve 
     * Cryptography and returns the public key encoded as BASE64, for
     * proper storage and transmission in textual form.
     * 
     * @return The public key encoded as BASE64 for storage/transmission
     */
    public byte[] generatePubKey()
    {
        /* Generate the public key and return it encoded as BASE64 */
        ECPublicKeyParameters pubKey = (ECPublicKeyParameters) key.getPublic();
        
        return ECGKeyUtil.encodeBase64PubKey(param, pubKey);
    }
    
    
    /**
     * Generates a unique and secure private key using Elliptic Curve 
     * Cryptography and returns the public key encoded as BASE64, for
     * proper storage and transmission in textual form.
     * 
     * @return The private key encoded as BASE64 for storage/transmission
     */
    public byte[] generatePriKey()
    {
        /* Generate the private key and return it encoded as BASE64 */
        ECPrivateKeyParameters priKey = (ECPrivateKeyParameters) key.getPrivate();
        
        return ECGKeyUtil.encodeBase64PriKey(param, priKey);
    }
}
