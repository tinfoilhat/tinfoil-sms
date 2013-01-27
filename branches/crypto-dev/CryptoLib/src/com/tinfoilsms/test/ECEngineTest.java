package com.tinfoilsms.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.contrib.assumes.Assumes;
import org.junit.contrib.assumes.Corollaries;
import org.junit.runner.RunWith;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

import com.tinfoilsms.crypto.APrioriInfo;
import com.tinfoilsms.crypto.ECEngine;
import com.tinfoilsms.crypto.ECKey;
import com.tinfoilsms.crypto.ECKeyParam;

@RunWith(Corollaries.class)
public class ECEngineTest
{
	private APrioriInfo priorInfo;
	private ECKeyParam param;
	
	private ECKey aliceKey;
	private ECKey bobKey;
	
	private ECPublicKeyParameters alicePubKey;
	private ECPublicKeyParameters bobPubKey;
	private ECPrivateKeyParameters alicePriKey;
	private ECPrivateKeyParameters bobPriKey;
	
	/* Encryption/decryption engines used for testing */
	private ECEngine aliceEngine;
	private ECEngine bobEngine;
	

	/* Sample text input for encryption/decryption tests */
	private static final String expASCIICharset = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
	private static final String exp128ByteMsg = "This message is exactly 128 bytes in length, please let me know if you receive this message correctly or say if it is corrupted.";
	private static final String exp33ByteMsg = "This msg does not fit in 1 block.";
	
	/**
	 * Setup the encryption/decryption test, assume that alice and
	 * bob have already exchanged their keys.
	 *  - set default a priori shared info, S1/S2
	 * 	- create alice's public/private keypair
	 * 	- create bob's public/private keypair
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		priorInfo = new APrioriInfo("initiator", "recipient");
		
		/* Create an instance of the ECKeyParam object with default curve */
		param = new ECKeyParam();
		
		/* Create an instance of alice and bob's elliptic curve key */
		aliceKey = new ECKey(param.getECDomainParam());
		aliceKey.init();
		bobKey = new ECKey(param.getECDomainParam());
		bobKey.init();
		
		/* Set the alice and bob's public/private keys */
		alicePubKey = (ECPublicKeyParameters) aliceKey.getPublic();
		bobPubKey = (ECPublicKeyParameters) bobKey.getPublic();
		
		alicePriKey = (ECPrivateKeyParameters) aliceKey.getPrivate();
		bobPriKey = (ECPrivateKeyParameters) bobKey.getPrivate();
		
		/* Setup alice's encryption engine to encrypt, bob's to decrypt msg from alice */
		aliceEngine = new ECEngine(priorInfo);
		aliceEngine.init(
				true,			// Encryption mode 
				alicePriKey, 	// Alice private key
				bobPubKey);		// Bob public key
		
		bobEngine = new ECEngine(priorInfo);
		bobEngine.init(
				false, 			// Decryption mode
				bobPriKey, 		// bob private key
				alicePubKey);	// alice pub key
	}


	/**
	 * Encyrption/Decryption test that verifies that a message containing the ASCII 
	 * character set (keyboard characters only) can be encrypted and decrypted back
	 * into the original message.
	 */
	@Test
	public void asciiCharset()
	{	
		/* Alice encrypts the ascii charset and sends it to bob who decrypts it */
		try
		{
			/* Alice encrypts the ascii character set */
			byte[] encASCIICharset = aliceEngine.processBlock(expASCIICharset.getBytes());
	
			/* Verify that the encrypted msg bob receives from alice and then decrypts matches
			 * the original ascii charset message
			 */
			byte[] decASCIICharset = bobEngine.processBlock(encASCIICharset);
			assertTrue(expASCIICharset.equals(new String(decASCIICharset)));
			
			System.out.println("\nOriginal Message: " + expASCIICharset);
			System.out.println("Encrypted: " + new String(encASCIICharset));
			System.out.println("Decrypted: " + new String(decASCIICharset));
			
		}
		catch (InvalidCipherTextException e)
		{
			// TODO Auto-generated catch block
			Assert.fail("Error occurred encrypting/decrypting the message");
			e.printStackTrace();
		}
	}
	
	/**
	 * Encryption test for a message that fits exactly within a multiple of blocks
	 * (128 bytes) and a message that is 1 byte to large for a block (33 bytes)
	 */
	@Test
	public void blockSizes()
	{
		
		/* Alice encrypts the messages and sends it to bob who decrypts it */
		try
		{
			/* Alice encrypts the message */
			byte[] enc128ByteMsg = aliceEngine.processBlock(exp128ByteMsg.getBytes());
			byte[] enc33ByteMsg = aliceEngine.processBlock(exp33ByteMsg.getBytes());
			
			/* Verify that the encrypted msg bob receives from alice and then decrypts matches
			 * the original message
			 */
			byte[] dec128ByteMsg = bobEngine.processBlock(enc128ByteMsg);
			byte[] dec33ByteMsg = bobEngine.processBlock(enc33ByteMsg);
			
			assertTrue(exp128ByteMsg.equals(new String(dec128ByteMsg)));
			assertTrue(exp33ByteMsg.equals(new String(dec33ByteMsg)));
			
		}
		catch (InvalidCipherTextException e)
		{
			// TODO Auto-generated catch block
			Assert.fail("Error occurred encrypting/decrypting the message");
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Test which verifies that the block cipher used is creating uniquely encrypted 
	 * messages each time. This test will fail for block cipher modes such as CBC, but
	 * should generate a uniquely encrypted message each time due to the use of an IV
	 * if block cipher modes such as OFB or CTR are used.
	 * 
	 * This test encrypts the same message multiple times and ensures that each subsequent
	 * encrypted message is unique even though the message being encrypted never changes.
	 * 
	 * @note This test takes a long time to execute, ideally it should perform thousands
	 * of iterations
	 */
	@Test
	@Assumes({"asciiCharset", "blockSizes"})
	public void uniqueEncryptedMsg()
	{
		/* A hash table containing the encrypted message and the resulting decrypted message */
		Hashtable<String, String> encryptedMsgs = new Hashtable<String, String>(100);

		/* Re-init new encrypt/decrypt engines for the test */
		ECEngine encEngine = new ECEngine(priorInfo);
		encEngine.init(
				true,			// Encryption mode 
				alicePriKey, 	// Alice private key
				bobPubKey);		// Bob public key
		
		ECEngine decEngine = new ECEngine(priorInfo);
		decEngine.init(
				false, 			// Decryption mode
				bobPriKey, 		// bob private key
				alicePubKey);	// alice pub key

		
		/* Encrypt the same sample message thousands of times and verify that the encrypted 
		 * message is unique each time and that it is also properly decrypted
		 */
		for (int i = 0; i < 100; ++i)
		{
			try
			{
				/* Encrypt the same message... */
				byte[] encMsg = encEngine.processBlock(expASCIICharset.getBytes());
				String strEncMsg = new String(encMsg);
				
				/* Verify encrypted message is unique and store decrypted msg in hashtable */
				assertFalse("Message not being uniquely encrypted!", encryptedMsgs.containsKey(strEncMsg));
				String decMsg = new String(decEngine.processBlock(encMsg));
				encryptedMsgs.put(strEncMsg, decMsg);
			}
			catch (InvalidCipherTextException e)
			{
				// TODO Auto-generated catch block
				Assert.fail("Error occurred encrypting/decrypting the message");
				e.printStackTrace();
			}
		}
		
		/* Verify that each decrypted msg matches the original message */
		System.out.println("\nEncrypted and decrypted messages.");
		for (String key : encryptedMsgs.keySet())
		{
			assertTrue(expASCIICharset.equals(encryptedMsgs.get(key)));
			System.out.println(key + ": " + encryptedMsgs.get(key));
		}
	}		
}
