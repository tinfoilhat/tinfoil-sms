/**
 * 
 */
package com.tinfoilsms.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.contrib.assumes.*;
import org.junit.runner.RunWith;
import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

import com.tinfoilsms.crypto.APrioriInfo;
import com.tinfoilsms.crypto.ECGKeyExchange;
import com.tinfoilsms.crypto.ECGKeyUtil;
import com.tinfoilsms.crypto.ECKey;
import com.tinfoilsms.crypto.ECKeyParam;

/**
 * TODO: There could be a lot more unit tests added for different
 * cases/scenarios
 *
 */
@RunWith(Corollaries.class)
public class ECGKeyExchangeTest
{
	private APrioriInfo priorInfo;
	private ECKeyParam param;
	
	private ECKey aliceKey;
	private ECKey bobKey;
	
	private ECPublicKeyParameters alicePubKey;
	private ECPublicKeyParameters bobPubKey;
	private ECPrivateKeyParameters alicePriKey;
	private ECPrivateKeyParameters bobPriKey;
	
	/**
	 * Setup the key exchange test:
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
	}

	/**
	 * A test which verifies that the recipient receives the signed public key
	 * from the initiator correctly and that the public key received matches 
	 * the original key sent.
	 */
	@Test
	public void KeyExchangeInitiator()
	{
		/* Alice (initiator) encodes, signs, and sends her public key to bob */
		byte[] AlicetoBobEncodedPubKey = ECGKeyUtil.encodePubKey(param, alicePubKey);
		byte[] AlicetoBobEncodedSignPubKey = ECGKeyExchange.signPubKey(
																new SHA256Digest(), 
																AlicetoBobEncodedPubKey, 
																priorInfo, 
																true);		// Initiator
		
		/* Bob (recipient) receives the key and verifies signature */
		assertTrue(ECGKeyExchange.verifyPubKey(
						new SHA256Digest(), 
						AlicetoBobEncodedSignPubKey, 
						priorInfo, 
						false));
		
		/* Verify that alice's decoded public key is the same as original */
		ECPublicKeyParameters AlicetoBobPubKey = ECGKeyUtil.decodeSignedPubKey(
														param, 
														new SHA256Digest(), 
														AlicetoBobEncodedSignPubKey);
		
		assertTrue(alicePubKey.getQ().getX().toBigInteger().equals(AlicetoBobPubKey.getQ().getX().toBigInteger()));
		assertTrue(alicePubKey.getQ().getY().toBigInteger().equals(AlicetoBobPubKey.getQ().getY().toBigInteger()));
		
		System.out.print("\nAlicetoBobPubKey: \tX = " + AlicetoBobPubKey.getQ().getX().toBigInteger());
		System.out.println(", Y = " + AlicetoBobPubKey.getQ().getY().toBigInteger());
		System.out.print("alicePubKey: \t\tX = " + alicePubKey.getQ().getX().toBigInteger());
		System.out.println(", Y = " + alicePubKey.getQ().getY().toBigInteger());
	}
	
	
	/**
	 * A test which verifies that the initiator receives the signed public key
	 * from the recipient correctly and that the public key received matches the
	 * original key sent.
	 * 
	 * Depends on the KeyExchangeInitiator executing successfully
	 */
	@Test
	@Assumes({"KeyExchangeInitiator"})
	public void keyExchangeRecipient()
	{
		/* Bob (recipient) encodes, signs, and sends public key to Alice (initiator) */
		byte[] BobtoAliceEncodedPubKey = ECGKeyUtil.encodePubKey(param, bobPubKey);
		byte[] BobtoAliceEncodedSignPubKey = ECGKeyExchange.signPubKey(
																new SHA256Digest(), 
																BobtoAliceEncodedPubKey, 
																priorInfo, 
																false);		// Recipient
		
		/* Alice (initiator) receives Bob's key and verifies signature */
		assertTrue(ECGKeyExchange.verifyPubKey(
						new SHA256Digest(), 
						BobtoAliceEncodedSignPubKey, 
						priorInfo, 
						true));		// Alice was the key exchange initiator
		
		
		/* Verify that Bob's decoded public key, which Alice received is the same as original */
		ECPublicKeyParameters BobtoAlicePubKey = ECGKeyUtil.decodeSignedPubKey(
														param, 
														new SHA256Digest(), 
														BobtoAliceEncodedSignPubKey);
		
		assertTrue(bobPubKey.getQ().getX().toBigInteger().equals(BobtoAlicePubKey.getQ().getX().toBigInteger()));
		assertTrue(bobPubKey.getQ().getY().toBigInteger().equals(BobtoAlicePubKey.getQ().getY().toBigInteger()));
		
		System.out.print("\nBobtoAlicePubKey: \tX = " + BobtoAlicePubKey.getQ().getX().toBigInteger());
		System.out.println(", Y = " + BobtoAlicePubKey.getQ().getY().toBigInteger());
		System.out.print("bobPubKey: \t\tX = " + bobPubKey.getQ().getX().toBigInteger());
		System.out.println(", Y = " + bobPubKey.getQ().getY().toBigInteger());
	
	}


	/**
	 * Test which verifies that an exception is thrown when the a priori shared information
	 * S1 and S2 is not specified (empty)
	 * 
	 * Depends on APrioriInfoTest.noSharedInfo,  APrioriInfoTest.oneSharedInfo
	 */
	@Test(expected=DataLengthException.class)
	@Assumes({"APrioriInfoTest.noSharedInfo", "APrioriInfoTest.oneSharedInfo"})
	public void signNoSharedInfo()
	{
		priorInfo = new APrioriInfo("", "");
		
		/* Bob (recipient) encodes, signs, and sends git public key to Alice (initiator) */
		byte[] tempEncodedPubKey = ECGKeyUtil.encodePubKey(param, alicePubKey);
		byte[] tempEncodedSignPubKey = ECGKeyExchange.signPubKey(
																new SHA256Digest(), 
																tempEncodedPubKey, 
																priorInfo, 
																true);		// Recipient	
	}
	
	
	/**
	 * "Man in the middle attack" test version 1, where an attacker intercepts Alice's key
	 * and tries to sign it using his own shared information S1/S2.
	 * 
	 * This test assumes that alice and bob are smart enough to use unique shared information
	 * (S1/S2) that they exchange a priori to the key exchange, the attacker mallory will
	 * try and simply sign alice's key using the default S1/S2 (initiator, recipient) hoping
	 * that bob will just be using defaults.
	 */
	@Test
	@Assumes({"KeyExchangeInitiator", "KeyExchangeRecipient"})
	public void manInMiddleAttackVer1()
	{
		APrioriInfo secureSharedInfo = new APrioriInfo("fuzzy", "wuzzy"); // maybe not the "most" secure
		
		/* Alice (initiator) encodes, signs, and sends her public key to bob */
		byte[] AlicetoBobEncodedPubKey = ECGKeyUtil.encodePubKey(param, alicePubKey);
		byte[] AlicetoBobEncodedSignPubKey = ECGKeyExchange.signPubKey(
																new SHA256Digest(), 
																AlicetoBobEncodedPubKey, 
																secureSharedInfo, 
																true);		// Initiator
		
		/* Mallory intercepts alice's signed key and tries to perform a man-in-the-middle
		 * attack by signing it with the default S1/S2 hoping bob is using defaults!
		 */
		ECPublicKeyParameters badPubKey = ECGKeyUtil.decodeSignedPubKey(
														param, 
														new SHA256Digest(), 
														AlicetoBobEncodedSignPubKey);
		
		byte[] badEncodedPubKey = ECGKeyUtil.encodePubKey(param, badPubKey);
		
		/* Mallory now sign's alice key with default shared info (initiator, recipient) */
		byte[] badEncodedSignPubKey = ECGKeyExchange.signPubKey(
														new SHA256Digest(), 
														AlicetoBobEncodedPubKey, 
														priorInfo, 	// The default shared info 
														true);		// Initiator
		
		/* Mallory now sends the "bad" man-in-the-middle attack key to bob
		 * pretending that he is Alice
		 */
		
		/* Bob (recipient) receives the "bad" key from Mallory and verifies the signature
		 * using the secure shared information (fuzzy, wuzzy) that he and Alice discussed
		 * prior to initiating the key exchange. The signature will fail, alerting bob to
		 * a potential man-in-the-middle attack!
		 */
		assertFalse(ECGKeyExchange.verifyPubKey(
						new SHA256Digest(), 
						badEncodedSignPubKey, 
						secureSharedInfo,    // The secure shared info alice and bob used
						false));	         // Recipient
	}
	
	
	/**
	 * "Man in the middle attack" test version 2, where an attacker intercepts Alice's key
	 * and tries to sign it using his own shared information S1/S2.
	 * 
	 * This test assumes that alice and bob unfortunately use the default shared information
	 * (S1/S2) and the attacker mallory will try and simply sign alice's key using the default
	 * S1/S2 (initiator, recipient) because he knowns that alice and bob will likely just be
	 * using the defaults.
	 * 
	 * This test highlights man-in-the-middle attack vulnerability if using the default shared
	 * information!
	 */
	@Test
	@Assumes({"KeyExchangeInitiator", "KeyExchangeRecipient"})
	public void manInMiddleAttackVer2()
	{		
		/* Alice (initiator) encodes, signs, and sends her public key to bob */
		byte[] AlicetoBobEncodedPubKey = ECGKeyUtil.encodePubKey(param, alicePubKey);
		byte[] AlicetoBobEncodedSignPubKey = ECGKeyExchange.signPubKey(
																new SHA256Digest(), 
																AlicetoBobEncodedPubKey, 
																priorInfo,	// INSECURE default shared info 
																true);		// Initiator
		
		/* Mallory intercepts alice's signed key and tries to perform a man-in-the-middle
		 * attack by signing it with the default S1/S2 hoping bob is using defaults!
		 */
		ECPublicKeyParameters badPubKey = ECGKeyUtil.decodeSignedPubKey(
														param, 
														new SHA256Digest(), 
														AlicetoBobEncodedSignPubKey);
		
		byte[] badEncodedPubKey = ECGKeyUtil.encodePubKey(param, badPubKey);
		
		/* Mallory now sign's alice key with default shared info (initiator, recipient) */
		byte[] badEncodedSignPubKey = ECGKeyExchange.signPubKey(
														new SHA256Digest(), 
														AlicetoBobEncodedPubKey, 
														priorInfo, 	// The default shared info 
														true);		// Initiator
		
		/* Mallory now sends the "bad" man-in-the-middle attack key to bob
		 * pretending that he is Alice
		 */
		
		/* Bob (recipient) receives the "bad" key from Mallory and verifies the signature
		 * using the INSECURE default shared information (initiator, recipient).
		 * The signature will be verified successfully, and will NOT alert bob to the
		 * man-in-the-middle attack!
		 */
		assertTrue(ECGKeyExchange.verifyPubKey(
						new SHA256Digest(), 
						badEncodedSignPubKey, 
						priorInfo,	// INSECURE default shared info 
						false));	// Recipient
	}
}