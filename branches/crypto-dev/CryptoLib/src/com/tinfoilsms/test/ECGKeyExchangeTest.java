/**
 * 
 */
package com.tinfoilsms.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
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
	 * A test which verifies that the recipient receives the signed public
	 * from the initiator correctly and that the received public key received
	 * matches the original key sent.
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
																true);
		
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
	
	
	@Test
	public void test2()
	{
		fail("Not yet implemented");
	}

}
