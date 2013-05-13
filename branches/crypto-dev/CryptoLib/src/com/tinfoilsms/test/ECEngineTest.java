package com.tinfoilsms.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.contrib.assumes.Assumes;
import org.junit.contrib.assumes.Corollaries;
import org.junit.runner.RunWith;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.ISAACEngine;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.params.Nonce;
import org.spongycastle.util.encoders.Hex;

import com.tinfoilsms.crypto.APrioriInfo;
import com.tinfoilsms.crypto.ECEngine;
import com.tinfoilsms.crypto.ECKey;
import com.tinfoilsms.crypto.ECKeyParam;
import com.tinfoilsms.csprng.ISAACRandomGenerator;
import com.tinfoilsms.csprng.SDFGenerator;
import com.tinfoilsms.csprng.SDFParameters;

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
	
	/* CSPRNG used for the encryption/decryption nonce/IV */
    private SDFGenerator generator;
    private byte[] seed;
    private ISAACRandomGenerator encCSPRNG;
    private ISAACRandomGenerator decCSPRNG;

    /* Nonce used for block cipher IV */
    CipherParameters encNonce;
    CipherParameters decNonce;
    
	/* Sample text input for encryption/decryption tests */								
	private static final String expASCIICharset = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
	private static final String exp129ByteMsg = "This message is exactly 129 bytes in length, please let me know if you receive this message correctly or say if it is corrupted!!";
	private static final String exp128ByteMsg = "This message is exactly 128 bytes in length, please let me know if you receive this message correctly or say if it is corrupted.";
	private static final String exp65ByteMsg = "This message is exactly sixty five bytes in length see yourself!!";
	private static final String exp64ByteMsg = "This message is exactly sixty four bytes in length see yourself!";
	private static final String exp63ByteMsg = "This message is exactly sixty three bytes in length see urself!";
	private static final String exp33ByteMsg = "This msg does not fit in 1 block.";
	private static final String exp32ByteMsg = "This msg does fit in one block!!";
	private static final String exp12ByteMsg = "Twelve Bytes";
	private static final String exp1ByteMsg = "?";
	
	/* Array containing all of the sample text above used as input for tests */
	private ArrayList<String> expBlockSizes = new ArrayList<String>();
	
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
		
        /* Generate seeds using SHA256 digest */
        generator = new SDFGenerator(new SHA256Digest());
        
        /* Initialize the seed generator and generate seed */
        generator.init(new SDFParameters("initiator", "recipient"));
        seed = new byte[generator.getDigest().getDigestSize()];
        generator.generateBytes(seed, 0, 0);
        
        /* Instantiate the CSPRNG */
        encCSPRNG = new ISAACRandomGenerator(new ISAACEngine());
        decCSPRNG = new ISAACRandomGenerator(new ISAACEngine());
        
        /* Initialize the nonce used by the block cipher to generate IVs */
        encNonce = new Nonce(encCSPRNG);
        decNonce = new Nonce(decCSPRNG);
        ((Nonce)encNonce).init(seed, seed.length);
        ((Nonce)decNonce).init(seed, seed.length);
        
        
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
		aliceEngine = new ECEngine(encNonce, priorInfo);
		aliceEngine.init(
				true,			// Encryption mode 
				alicePriKey, 	// Alice private key
				bobPubKey);		// Bob public key
		
		bobEngine = new ECEngine(decNonce, priorInfo);
		bobEngine.init(
				false, 			// Decryption mode
				bobPriKey, 		// bob private key
				alicePubKey);	// alice pub key
		
		
		/* Add block sizes to block sizes test array */
		expBlockSizes.add(exp129ByteMsg);
		expBlockSizes.add(exp128ByteMsg);
		expBlockSizes.add(exp65ByteMsg);
		expBlockSizes.add(exp64ByteMsg);
		expBlockSizes.add(exp63ByteMsg);
		expBlockSizes.add(exp33ByteMsg);
		expBlockSizes.add(exp32ByteMsg);
		expBlockSizes.add(exp12ByteMsg);
		expBlockSizes.add(exp1ByteMsg);
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
		
		System.out.println("\n\nEncrypted & Decrypted Blocks: \n");
		/* Alice encrypts the messages and sends it to bob who decrypts it */
		try
		{
			for (String expBlock : expBlockSizes)
			{
				/* Alice encrypts the message */
				byte[] encBlock = aliceEngine.processBlock(expBlock.getBytes());	
		
				/* Verify that the encrypted msg bob receives from alice and then decrypts matches
				 * the original message
				 */
				byte[] decBlock = bobEngine.processBlock(encBlock);
				String encBlockStr = new String(Hex.encode(encBlock));
				String decBlockStr = new String(decBlock);
				assertTrue(expBlock.equals(decBlockStr));
				
				
				System.out.println(encBlockStr.length() / 2 + ": " + encBlockStr);	
				System.out.println(decBlockStr.length() + ": " + decBlockStr);				
			}
			
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
		/* A hash map containing the encrypted message and the resulting decrypted message */
		HashMap<String, String> encryptedMsgs = new HashMap<String, String>(100);
	    
		/* Re-init new encrypt/decrypt engines for the test */
		ECEngine encEngine = new ECEngine(encNonce, priorInfo);
		encEngine.init(
				true,			// Encryption mode 
				alicePriKey, 	// Alice private key
				bobPubKey);		// Bob public key
		
		ECEngine decEngine = new ECEngine(decNonce, priorInfo);
		decEngine.init(
				false, 			// Decryption mode
				bobPriKey, 		// bob private key
				alicePubKey);	// alice pub key

		
		/* Encrypt the same sample message thousands of times and verify that the encrypted 
		 * message is unique each time and that it is also properly decrypted
		 */
		for (int i = 0; i < 1000; ++i)
		{
			try
			{
				/* Encrypt the same message... */
				byte[] encMsg = encEngine.processBlock(expASCIICharset.getBytes());
				String strEncMsg = new String(Hex.encode(encMsg));
				
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
		for (String key : encryptedMsgs.keySet())
		{
			assertTrue(expASCIICharset.equals(encryptedMsgs.get(key)));
		}
		
		/* Display 10 of the encrypted and decrypted messages */
		System.out.println("\nEncrypted and decrypted messages.");
		int i = 0;
        for (String key : encryptedMsgs.keySet())
        {
            if (i >= 10)
            {
                break;
            }
            
            System.out.println(key + " <==> " + encryptedMsgs.get(key));
            ++i;
        }
	}		
}
