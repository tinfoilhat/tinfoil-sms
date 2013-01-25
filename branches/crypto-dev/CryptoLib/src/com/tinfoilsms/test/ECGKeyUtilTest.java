package com.tinfoilsms.test;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;

import org.junit.Before;
import org.junit.Test;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

import com.tinfoilsms.crypto.ECKey;
import com.tinfoilsms.crypto.ECKeyParam;
import com.tinfoilsms.crypto.ECGKeyUtil;

/**
 * TODO: May need to improve how these tests are performed by having an expected
 * byte array to verify that encode/decode really works. It is difficult as the
 * public/private keys are unique/random.
 */
public class ECGKeyUtilTest
{
	private ECKeyParam param;
	private ECKey expKey;
	private ECPublicKeyParameters expPubKey;
	private ECPrivateKeyParameters expPriKey;
	private byte[] expEncodedPubKey;
	private byte[] expEncodedPriKey;
	
	@Before
	public void setUp() throws Exception
	{
		/* Create an instance of the ECKeyParam object with default curve */
		param = new ECKeyParam();
		
		/* Create an instance of the expected key and initialize it */
		expKey = new ECKey(param.getECDomainParam());
		expKey.init();
		
		/* Set the expected public/private keys */
		expPubKey = (ECPublicKeyParameters) expKey.getPublic();
		expPriKey = (ECPrivateKeyParameters) expKey.getPrivate();
		
	}
	

	/**
	 * Test that encoding a public key as ASN.1 for transmission and then decoding
	 * it back into an EC public key object results in the same public key (the point
	 * Q has the same X and Y values)
	 */
	@Test
	public void encodeDecodePubKey()
	{
		byte[] encodedPubKey = ECGKeyUtil.encodePubKey(param, expPubKey);
		ECPublicKeyParameters pubKey = ECGKeyUtil.decodePubKey(param, encodedPubKey);
		
		/* Verify that the decoded pub key is the same as the expected pub key */
		assertTrue(expPubKey.getQ().getX().toBigInteger().equals(pubKey.getQ().getX().toBigInteger()));
		assertTrue(expPubKey.getQ().getY().toBigInteger().equals(pubKey.getQ().getY().toBigInteger()));
		
		System.out.print("ExpPubKey: \tX = " + expPubKey.getQ().getX().toBigInteger());
		System.out.println(", Y = " + expPubKey.getQ().getY().toBigInteger());
		System.out.print("PubKey: \tX = " + pubKey.getQ().getX().toBigInteger());
		System.out.println(", Y = " + pubKey.getQ().getY().toBigInteger());
		
	}

	
	/**
	 * Test that encoding a public key as ASN.1 which is then further encoded as base64 
	 * for transmission and then decoding it back into an EC public key object results in
	 * the same public key (the point Q has the same X and Y values)
	 * 
	 * TODO: Cannot test any Base64 methods as they rely on android, will have to either include the
	 * specific android library or create a new android test project
	 * 
	 */
	public void encodeDecodeBase64PubKey()
	{
		byte[] base64EncodedPubKey = ECGKeyUtil.encodeBase64PubKey(param, expPubKey);
		ECPublicKeyParameters pubKey = ECGKeyUtil.decodeBase64PubKey(param, base64EncodedPubKey);
		
		/* Verify that the decoded public key is the same as the expected public key */
		assertTrue(expPubKey.getQ().getX().toBigInteger().equals(pubKey.getQ().getX().toBigInteger()));
		assertTrue(expPubKey.getQ().getY().toBigInteger().equals(pubKey.getQ().getY().toBigInteger()));
		
		System.out.print("ExpPubKey: \tX = " + expPubKey.getQ().getX().toBigInteger());
		System.out.println(", Y = " + expPubKey.getQ().getY().toBigInteger());
		System.out.print("PubKey: \tX = " + pubKey.getQ().getX().toBigInteger());
		System.out.println(", Y = " + pubKey.getQ().getY().toBigInteger());
	}
	
	
	/**
	 * Test that encoding a private key as ASN.1 and then decoding it back into 
	 * an EC public key object results in the same private key (D)
	 */
	@Test
	public void encodeDecodePriKey()
	{
		byte[] encodedPriKey = ECGKeyUtil.encodePriKey(param, expPriKey);
		ECPrivateKeyParameters priKey = ECGKeyUtil.decodePriKey(param, encodedPriKey);
		
		/* Verify that the decoded private key is same as expected private key */
		assertTrue(expPriKey.getD().equals(priKey.getD()));
		
		System.out.println("\nExpPriKey: \tD = " + expPriKey.getD());
		System.out.println("PriKey: \tD = " + priKey.getD());
	}
	
	/**
	 * Test that verifies that an exception is thrown if an encode function is 
	 * executed on the wrong key (ie. encodePubKey used for private key)
	 */
	@Test(expected=InvalidParameterException.class)
	public void encodePriKeyAsPubKey()
	{
		byte[] temp = ECGKeyUtil.encodePubKey(param, expPriKey);
	}
	
	
	/**
	 * Test that verifies that an exception is thrown if an encode function is 
	 * executed on the wrong key (ie. encodePubKey used for private key)
	 */
	@Test(expected=InvalidParameterException.class)
	public void encodePubKeyAsPriKey()
	{
		byte[] temp = ECGKeyUtil.encodePriKey(param, expPubKey);
	}
}