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
import java.security.Security;
import java.util.HashMap;

import org.strippedcastle.crypto.InvalidCipherTextException;
import org.strippedcastle.crypto.digests.SHA256Digest;
import org.strippedcastle.crypto.engines.ISAACEngine;
import org.strippedcastle.crypto.params.ECPrivateKeyParameters;
import org.strippedcastle.crypto.params.ECPublicKeyParameters;
import org.strippedcastle.crypto.prng.RandomGenerator;
import org.strippedcastle.jce.provider.BouncyCastleProvider;

import android.util.Base64;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.orwell.crypto.APrioriInfo;
import com.orwell.crypto.ECEngine;
import com.orwell.crypto.ECGKeyUtil;
import com.orwell.csprng.ISAACRandomGenerator;
import com.orwell.csprng.SDFGenerator;
import com.orwell.params.ECKeyParam;
import com.orwell.params.Nonce;
import com.orwell.params.SDFParameters;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.User;

/**
 * A class which operates as a facade to greatly simplify the underlying
 * complexity required to encrypt/decrypt messages using Elliptic Curve 
 * Cryptography.
 */
public class Encryption
{
    private HashMap<Long, ECEngine> encryptMap;
    private HashMap<Long, ECEngine> decryptMap;
    
    private User user;
    
    /* Size of the message counter in bytes, usually 2 bytes, based on Nonce.MAX_CYCLES */
    private static final int COUNT_SIZE = 2;
    
    /* Register spongycastle as the most preferred security provider */
    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }
    
    /**
     * The basic constructor, initializes the encrypt/decrypt hash maps.
     */
    public Encryption(User user)
    {
        this.encryptMap = new HashMap<Long, ECEngine>();
        this.decryptMap = new HashMap<Long, ECEngine>();
        this.user = user;
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
        byte[] finalMessage;
        
        Log.v("Original Message", message);
        
        /* Initialize the encryption engine if the number is not in hash map */
        if (! encryptMap.containsKey(number.getId()))
        {
            initECEngine(number, true);
        }
        
        /* Encrypt the message, add the counter to the message, which is used by the nonce */
        encMessage = encryptMap.get(number.getId()).processBlock(message.getBytes());
        finalMessage = addCounter(encMessage, number.getNonceEncrypt());
        
        Log.v("Nonce counter:", Integer.toString(number.getNonceEncrypt()));
        
        /* Increment and save the counter used by the nonce for encryption */
        number.setNonceEncrypt(number.getNonceEncrypt() + 1);
       
        Log.v("Encrypted Message", Base64.encodeToString(finalMessage, Base64.DEFAULT));
        
        return Base64.encodeToString(finalMessage, Base64.DEFAULT);
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
        byte[] decodedMessage = Base64.decode(message, Base64.DEFAULT);
        byte[] encMessage = new byte[decodedMessage.length - COUNT_SIZE];
        byte[] decMessage;
        
        Log.v("Encypted Message Received", message);
        
        /* Get the nonce counter from the message */
        Integer counter = getCounter(decodedMessage);
        
        Log.v("Nonce counter received:", Integer.toString(counter));
        
        /* Re-initialize the encryption engine if the nonce counter is incorrect
         * due to message not being received in the correct order (benefit of the doubt)
         * 
         * NOTE: In the event of a malicious attack (trying to exhaust the nonce counter)
         * the engine will not be re-initialized, it will only be reinitialized if the counter 
         * is greater than the current counter (to avoid an attack of re-using IVs) and
         * if it is greater by at most 10.
         */
        if ((counter > number.getNonceDecrypt()) && (counter - number.getNonceDecrypt()) <= 10)
        {
            Log.v("Re-initializing nonce counter to:", Integer.toString(counter));
            number.setNonceDecrypt(counter);
            initECEngine(number, false);
        }
        /* Otherwise, initialize the encryption engine if the number is not in hash map */
        else if (! decryptMap.containsKey(number.getId()))
        {
            initECEngine(number, false);
        }
        
        /* Remove the nonce counter from the message received */
        System.arraycopy(decodedMessage, COUNT_SIZE, encMessage, 0, encMessage.length);
        
		/* Log the message data, before it's decrypted, in the event of a crash */
        BugSenseHandler.addCrashExtraData("Original", message);
        BugSenseHandler.addCrashExtraData("Decoded", new String(decodedMessage));
        BugSenseHandler.addCrashExtraData("Encrypted", new String(encMessage));

        /* decrypt the message, increment and save the nonce counter */
        decMessage = decryptMap.get(number.getId()).processBlock(encMessage);
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
        RandomGenerator nonce;
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
        ECPrivateKeyParameters priKey = ECGKeyUtil.decodeBase64PriKey(param, user.getPrivateKey());
        ECPublicKeyParameters pubKey = ECGKeyUtil.decodeBase64PubKey(param, number.getPublicKey());
        
        Log.v("My private key", new String(user.getPrivateKey()));
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
     * Prefixes the message count to the beginning of the message that is to be encrypted
     * 
     * @param message The message to prefix the counter to
     * @return The message with the counter value prefixed
     */
    private byte[] addCounter(byte[] message, Integer counter)
    {
        byte[] output = new byte[message.length + COUNT_SIZE];
        
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(counter);
        
        /* Truncate the integer to COUNTER_SIZE and prefix it to output */
        System.arraycopy(buffer.array(), 2, output, 0, COUNT_SIZE);
        System.arraycopy(message, 0, output, COUNT_SIZE, message.length);
        
        return output;
    }
    
    
    /**
     * Gets the prefixed message count from the message received
     * @param message The message received to get the prefixed counter from
     * @return The counter value
     */
    private Integer getCounter(byte[] message)
    {
        byte[] counter = new byte[COUNT_SIZE];
        
        System.arraycopy(message, 0, counter, 0, COUNT_SIZE);
        
        /* Convert the nonce back to int */
        ByteBuffer buffer = ByteBuffer.wrap(counter);
        buffer.order(ByteOrder.BIG_ENDIAN);
        
        /* Convert the nonce value to an unsigned short */
        return buffer.getShort() & 0xFFFF;
    }
}
