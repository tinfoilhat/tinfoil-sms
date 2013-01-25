package com.tinfoilsms.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

import com.tinfoilsms.crypto.ECKey;
import com.tinfoilsms.crypto.ECKeyParam;

public class ECKeyTest
{
	private ECKeyParam param;
	private ECKey expKey;
	private ECPublicKeyParameters expPubKey;
	private ECPrivateKeyParameters expPriKey;
	
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
	 * Test that multiple subsequent elliptic curve keypairs created using the
	 * same domain parameters have unique private and public keys and are therefore
	 * random
	 * 
	 * TODO: This test is pretty poor at testing the actual cryptographic security...
	 */
	@Test
	public void uniqueECKeys()
	{
		ECKey key;
		ECPublicKeyParameters pubKey;
		ECPrivateKeyParameters priKey;
		
		/* Keep creating new keypairs and assure that there are no cycles and that the
		 * keys are uniformly random... this test needs to be improved and done properly...
		 */
		for (int i = 0; i < 100; ++i)
		{
			key = new ECKey(param.getECDomainParam());
			key.init();
			pubKey = (ECPublicKeyParameters) key.getPublic();
			priKey = (ECPrivateKeyParameters) key.getPrivate();
			
			/* Assert that the X and Y parameters of public keys are different, thus unique */
			assertFalse(expPubKey.getQ().getX().toBigInteger().equals(pubKey.getQ().getX().toBigInteger()));
			assertFalse(expPubKey.getQ().getY().toBigInteger().equals(pubKey.getQ().getY().toBigInteger()));
			
			/* Assert that the private keys are different, thus unique */
			assertFalse(expPriKey.getD().equals(priKey.getD()));
		}
	}

}
