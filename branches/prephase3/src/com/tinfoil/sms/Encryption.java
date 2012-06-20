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

import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;


/*
 * A simple abstract class that implements various ciphers using a number
 */
abstract class Encryption
{
	private static final byte[] KEY = ("test123").getBytes();
	private static final int ROT_SHIFT = 13;
	private static final String CIPHER_ALGORITHM = "AES";
	/* Key size in BYTES */
	private static final int KEY_SIZE = 16;
	
	/*
	 * Function which returns ROT encrypted ciphertext
	 * @param plaintext The plaintext to be encrypted
	 */
	public static String rot13(String plaintext)
	{
		String ciphertext = "";
		
		// Shift each character
        for (int i = 0; i < plaintext.length(); ++i)
        {
            char c = plaintext.charAt(i);
            if       (c >= 'a' && c <= 'm') c += ROT_SHIFT;
            else if  (c >= 'n' && c <= 'z') c -= ROT_SHIFT;
            else if  (c >= 'A' && c <= 'M') c += ROT_SHIFT;
            else if  (c >= 'A' && c <= 'Z') c -= ROT_SHIFT;
            ciphertext += c;
        }
        return ciphertext;
	}
	
	
	/*
	 * Function which returns the AES encrypted ciphertext
	 * @param password the secret key used to encrypt the plaintext
	 * @param plaintext The plaintext to be encrypted
	 */
	public static String aes_encrypt( String password, String plaintext ) throws Exception 
	{
		byte[] secret_key = generateKey( password.getBytes() );
		//byte[] secretKey = secret_key.getBytes();
	    byte[] clear = plaintext.getBytes();
		
	    SecretKeySpec secretKeySpec = new SecretKeySpec( secret_key, CIPHER_ALGORITHM );
	    Cipher cipher = Cipher.getInstance( CIPHER_ALGORITHM );
	    cipher.init( Cipher.ENCRYPT_MODE, secretKeySpec );
	    
	    byte[] encrypted = cipher.doFinal( clear );
	    String encryptedString = Base64.encodeToString( encrypted, Base64.DEFAULT );
	    
	    return encryptedString;
		//return encrypted.toString();
	}
	
	
	/*
	 * Function which returns the decrypted AES plaintext
	 * @param secret_key the secret key used to encrypt the plaintext
	 * @param ciphertext The ciphertext to be decrypted
	 */
	public static String aes_decrypt( String password, String ciphertext ) throws Exception 
	{
		byte[] secret_key = generateKey( password.getBytes() );
		
		SecretKeySpec secretKeySpec = new SecretKeySpec( secret_key, CIPHER_ALGORITHM );
		Cipher cipher = Cipher.getInstance( CIPHER_ALGORITHM );
	    cipher.init( Cipher.DECRYPT_MODE, secretKeySpec );
	    
	    byte[] encrypted = Base64.decode( ciphertext, Base64.DEFAULT );
	    //byte[] encrypted = ciphertext.getBytes();
	    byte[] decrypted = cipher.doFinal( encrypted );
	    
		return new String( decrypted );
	}
	
	
	/*
	 * Function which returns a securely generated key based on the
	 * AES password specified by the user
	 * @param seed The AES secret_key specified by the user
	 */
	public static byte[] generateKey( byte[] password ) throws Exception
	{
		byte[] key = new byte[KEY_SIZE];
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		byte[] hash = sha.digest(password);

		/* The key is the first 128 bits of SHA-1 hash */
		System.arraycopy(hash, 0, key, 0, 15);	
		
		return key;
	}
	
	public static byte[] generateKey()
	{
		return KEY;
	}
}

