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
import java.nio.ByteOrder;
import java.util.HashMap;

import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.ISAACEngine;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.params.Nonce;

import android.util.Base64;
import android.util.Log;

import com.tinfoilsms.crypto.APrioriInfo;
import com.tinfoilsms.crypto.ECEngine;
import com.tinfoilsms.crypto.ECGKeyUtil;
import com.tinfoilsms.crypto.ECKeyParam;
import com.tinfoilsms.csprng.ISAACRandomGenerator;
import com.tinfoilsms.csprng.SDFGenerator;
import com.tinfoilsms.csprng.SDFParameters;

import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.utility.SMSUtility;

/**
 * A class which operates as a facade to greatly simplify the underlying
 * complexity required to encrypt/decrypt messages using Elliptic Curve 
 * Cryptography.
 */
public class Encryption
{
    private HashMap<Long, ECEngine> encryptMap;
    private HashMap<Long, ECEngine> decryptMap;
    
    /* Size of the nonce in bytes, usually 2 bytes, based on Nonce.MAX_CYCLES */
    private static final int NONCE_SIZE = 2;
    
    /**
     * The basic constructor, initializes the encrypt/decrypt hash maps.
     */
    public Encryption()
    {
        this.encryptMap = new HashMap<Long, ECEngine>();
        this.decryptMap = new HashMap<Long, ECEngine>();
    }
    
    
    /**
     * Encrypts the message provided using the public key belonging to the number
     * and adds a 32 byte verification signature (HMAC) to the message.
     * 
     * @param number The number that the message is to be encrypted for
     * @param message The plaintext message to encrypt
     * 
     * @return The ciphertext of the encrypted message
     * 
     * @throws InvalidCipherTextException If an error occurs attempting to encrypt the message.
     */
    public String encrypt(Number number, String message) throws InvalidCipherTextException
    {
        byte[] encMessage;
        
        Log.v("Original Message", message);
        
        /* Initialize the encryption engine if the number is not in hash map */
        if (! encryptMap.containsKey(number.getId()))
        {
            initECEngine(number, true);
        }
        
        /* Encrypt the message, increment and save the nonce cycle */
        encMessage = encryptMap.get(number.getId()).processBlock(message.getBytes());
        number.setNonceEncrypt(number.getNonceEncrypt() + 1);
        
        Log.v("Encrypted Message", Base64.encodeToString(encMessage, Base64.DEFAULT));
        
        return Base64.encodeToString(encMessage, Base64.DEFAULT);
    }
    
    
    /**
     * Decrypts the message provided using the public key belonging to the number
     * and adds a 32 byte verification signature (HMAC) to the message.
     * 
     * @param number The number that the message is to be decrypted for
     * @param message The ciphertext message to decrypt
     * 
     * @return The plaintext of the decrypted message
     * 
     * @throws InvalidCipherTextException If an error occurs attempting to encrypt the message.
     */
    public String decrypt(Number number, String message) throws InvalidCipherTextException
    {
        byte[] decMessage;
        
        Log.v("Encypted Message Received", message);
        
        /* Initialize the encryption engine if the number is not in hash map */
        if (! decryptMap.containsKey(number.getId()))
        {
            initECEngine(number, false);
        }
        
        /* decrypt the message, increment and save the nonce cycle */
        decMessage = decryptMap.get(number.getId()).processBlock(Base64.decode(message, Base64.DEFAULT));
        number.setNonceDecrypt(number.getNonceDecrypt() + 1);
        
        Log.v("Decrypted Message", new String(decMessage));
        
        return new String(decMessage);
    }
    
    
    /**
     * Initializes the encryption engine given the number, which contains 
     * the public key, shared apriori info, and nonces, unique to that
     * number, which is needed to initialize the encryption engine.
     * 
     * @param number The number, which contains the cryptographic info
     * @param mode The mode to initialize the engine as, true for encryption
     * and false for decryption mode.
     */
    private void initECEngine(Number number, boolean mode)
    {
        /* Initialize the seed derivative function, using SHA256,  which is used 
         * to generate the unique seed used by the deterministic CSPRNG
         */
        SDFGenerator generator = new SDFGenerator(new SHA256Digest());
        
        /* Initialize the seed generator based on the state of who initialized
         * the original key exchange, the initiator uses shared information S1 + S2 
         * for encrypt, S2 + S1 for decrypt, the recipient uses the inverse.
         */
        String sharedInfo1, sharedInfo2;
        APrioriInfo sharedInfo;
        if (number.isInitiator())
        {  
            sharedInfo1 = number.getSharedInfo1();
            sharedInfo2 = number.getSharedInfo2();
        }
        /* Number was key exchange recipient, use the inverse */
        else
        {
            sharedInfo1 = number.getSharedInfo2();
            sharedInfo2 = number.getSharedInfo1();
        }
        
        Log.v("SharedInfo 1", sharedInfo1);
        Log.v("SharedInfo 2", sharedInfo2);
        
        /* Setup apriori info, generator, and nonce used by the block cipher to generate IVs */
        CipherParameters nonce;
        ISAACRandomGenerator CSPRNG = new ISAACRandomGenerator(new ISAACEngine());
        if (mode)
        {
            /* encryption mode */
            sharedInfo = new APrioriInfo(sharedInfo1, sharedInfo2);
            generator.init(new SDFParameters(sharedInfo1, sharedInfo2));
            nonce = new Nonce(CSPRNG, number.getNonceEncrypt());
            Log.v("Nonce encrypt", String.valueOf(number.getNonceEncrypt()));
        }
        else
        {
            /* decryption mode */
            sharedInfo = new APrioriInfo(sharedInfo2, sharedInfo1);
            generator.init(new SDFParameters(sharedInfo2, sharedInfo1));
            nonce = new Nonce(CSPRNG, number.getNonceDecrypt());
            Log.v("Nonce decrypt", String.valueOf(number.getNonceDecrypt()));
        }
        
        /* Generate the seed, initialize the nonce */
        byte[] seed = new byte[generator.getDigest().getDigestSize()];
        generator.generateBytes(seed, 0, 0);
        ((Nonce)nonce).init(seed, seed.length);
        
        
        /* Initialize the keypair using the current user's private key and the number's public key */
        ECKeyParam param = new ECKeyParam();
        ECPrivateKeyParameters priKey = ECGKeyUtil.decodeBase64PriKey(param, SMSUtility.user.getPrivateKey());
        ECPublicKeyParameters pubKey = ECGKeyUtil.decodeBase64PubKey(param, number.getPublicKey());
        
        Log.v("My private key", new String(SMSUtility.user.getPrivateKey()));
        Log.v("Number's public key", new String(number.getPublicKey()));
        
        /* Finally initialize the encryption engine */
        ECEngine engine = new ECEngine(nonce, sharedInfo);
        engine.init(mode,priKey,pubKey);
                
        /* Add the engine to the hash map */
        if (mode)
        {
            encryptMap.put(number.getId(), engine);
        }
        else
        {
            decryptMap.put(number.getId(), engine);
        }
    }
    
    
    /**
     * Prefixes the nonce to the beginning of the message that is to be encrypted
     * 
     * @param message The message to prefix the nonce to
     * @return The message with the nonce value prefixed
     */
    private byte[] addNonce(byte[] message, Integer nonce)
    {
        byte[] output = new byte[message.length + NONCE_SIZE];
        
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(nonce);
        
        /* Truncate the integer to NONCE_SIZE and prefix it to output */
        System.arraycopy(buffer.array(), 2, output, 0, NONCE_SIZE);
        System.arraycopy(message, 0, output, NONCE_SIZE, message.length);
        
        return output;
    }
    
    
    /**
     * Gets the prefixed nonce from the message that has been decrypted
     * @param message The decrypted message to get the prefixed nonce from
     * @return The nonce value
     */
    private Integer getNonce(byte[] message)
    {
        byte[] nonce = new byte[NONCE_SIZE];
        
        System.arraycopy(message, 0, nonce, 0, NONCE_SIZE);
        
        /* Convert the nonce back to int */
        ByteBuffer buffer = ByteBuffer.wrap(nonce);
        buffer.order(ByteOrder.BIG_ENDIAN);
        
        /* Convert the nonce value to an unsigned short */
        return buffer.getShort() & 0xFFFF;
    }
}
