package com.tinfoil.sms;

import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.spec.ECParameterSpec;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;

import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.KeyGenerationParameters;
import org.spongycastle.crypto.agreement.ECDHBasicAgreement;
import org.spongycastle.crypto.agreement.ECDHCBasicAgreement;
import org.spongycastle.crypto.digests.SHA1Digest;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.IESEngine;
import org.spongycastle.crypto.engines.RijndaelEngine;
import org.spongycastle.crypto.engines.TwofishEngine;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.generators.KDF2BytesGenerator;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.modes.OFBBlockCipher;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.params.IESParameters;
import org.spongycastle.crypto.params.IESWithCipherParameters;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.util.encoders.Hex;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class ECCKeyExchangeTests extends ECCKeyExchangeActivity
{
	/*
	 * Key exchange test, simulates two individuals alice and bob encoding and signing their
	 * public keys and exchanging them. Alice is the initiator of the public key exchange and
	 * bob is the recipient of the key exchange.
	 * 
	 * The key exchange performed is my own invention, ECG
	 */
	public boolean keyexchange_test(AsymmetricCipherKeyPair alice, AsymmetricCipherKeyPair bob, Context context)
	{
		/*
		 *  Encode and sign alice public key
		 *  Alice is the initiator of the public key exchange, sign public key as initiator
		 */
        byte[] alice_encodedPubKey = encode_publickey(alice.getPublic());
        byte[] alice_signedPubKey = sign_publickey(new SHA256Digest(), alice_encodedPubKey, true);
        
        /*
         *  Now Bob receives the signed public key from alice
         *  Bob verifies the public key as being signed by the intiator
         */
        if (verify_publickey(new SHA256Digest(), alice_signedPubKey, true))
        {
        	/*
        	 * If alice signed public key is valid then bob encodes and signs his public key
        	 * Bob is the recipient of the public key exchange, sign public key as recipient 
        	 */
        	byte[] bob_encodedPubKey = encode_publickey(bob.getPublic());
        	byte[] bob_signedPubKey = sign_publickey(new SHA256Digest(), bob_encodedPubKey, false);
        	
        	/*
        	 * Now alice receives the signed public key from bob
        	 * alice verifies the public key as being signed by the recipient
        	 */
        	if (verify_publickey(new SHA256Digest(), bob_signedPubKey, false))
        	{
        		Toast.makeText(context, "Key exchange between alice and bob PASSED", Toast.LENGTH_LONG).show();
        		return true;
        	}
        	else
        	{
        		Toast.makeText(context, "Key exchange response from recipient bob failed", Toast.LENGTH_LONG).show();
        	}
        }
        else
        {
        	Toast.makeText(context, "Key exchange initiated by alice to recipient bob failed", Toast.LENGTH_LONG).show();
        }
        return false;	// Key exchange failed, see toast for details
	}
	
	
	/*
	 * Block cipher test an ECC (ECIES) block cipher test that replicate the steps of the key exchange
	 * test without the error handling as that test already does error handling.
	 * 
	 * After the public keys have been exchanged using the ECG public key exchange protocol
	 * test simulates a session encrypting and decrypting messages between alice and bob.
	 */
	public void blockcipher_test(AsymmetricCipherKeyPair alice, AsymmetricCipherKeyPair bob, Context context) throws Exception
	{
		try {	
			/*
			 * AES With OFB, which does not require padding
			 * 
			 * WARNING: For OFB and CTR, reusing an IV completely destroys security.
			 * http://en.wikipedia.org/wiki/Block_cipher_modes_of_operation#Counter_.28CTR.29
			 * 
			 * I must do research to ensure that the IV is never reused, although
			 * I believe the library handles it already.
			 * 
			 * AESEngine ONLY SUPPORTS 128-bit block size, but supports 256-bit key
			 * a block size of 128-bit is being used with a 256-bit shared secret key
			 * 
			 */
			BufferedBlockCipher c1 = new BufferedBlockCipher(
				            		new OFBBlockCipher(new AESEngine(), 128));
			BufferedBlockCipher c2 = new BufferedBlockCipher(
									new OFBBlockCipher(new AESEngine(), 128));
			
			
			/*
			 * Testing more secure IESEngine parameters, the minimum security for
			 * any component should be 256-bit
			 */
	        IESEngine      i1 = new IESEngine(
			                    new ECDHCBasicAgreement(),
			                    new KDF2BytesGenerator(new SHA256Digest()),
			                    new HMac(new SHA256Digest()),
			                    c1);
	        IESEngine      i2 = new IESEngine(
			                    new ECDHCBasicAgreement(),
			                    new KDF2BytesGenerator(new SHA256Digest()),
			                    new HMac(new SHA256Digest()),
			                    c2);
	        
	        IESParameters p = new IESWithCipherParameters(S1, S2, 256, 256);
	        
	        
			/*
			 *  Encode and sign alice public key
			 *  Alice is the initiator of the public key exchange, sign public key as initiator
			 */
	        byte[] alice_encodedPubKey = encode_publickey(alice.getPublic());
	        byte[] alice_signedPubKey = sign_publickey(new SHA256Digest(), alice_encodedPubKey, true);
	        
        	/*
        	 * Assume that alice signed public key is valid then bob encodes and signs his public key
        	 * Bob is the recipient of the public key exchange, sign public key as recipient
        	 */
        	byte[] bob_encodedPubKey = encode_publickey(bob.getPublic());
        	byte[] bob_signedPubKey = sign_publickey(new SHA256Digest(), bob_encodedPubKey, false);
	        
        	/*
        	 * Now, assume that both signed public keys are valid and have been exchanged between
        	 * alice and bob. Now derive the public key object for each individual from the
        	 * exchanged keys.
        	 */
        	ECPublicKeyParameters alice_pubKey = decode_signed_publickey(new SHA256Digest(), alice_signedPubKey);
        	ECPublicKeyParameters bob_pubKey   = decode_signed_publickey(new SHA256Digest(), bob_signedPubKey);
        	
	        /*
	         * Initialize scenario where Alice encrypts message to Bob, Bob decrypts the message
	         */
	        i1.init(true, alice.getPrivate(), bob_pubKey, p);		// alice initializes IESEngine to encrypt
	        i2.init(false, bob.getPrivate(), alice_pubKey, p);		// bob initializes IESEngine to decrypt
	        
	        
	        // 120 bytes/characters message
	        String text_message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur semper, tellus sed venenatis consectetur, ante metus.";
	        
	        String text_message2 = "This is a message from Bob in response to Alice message, is it decrypted?";
	        
	        byte[] message = text_message.getBytes();
	        byte[] message2 = text_message2.getBytes();
	        
	        /*
	         * Display the original message 
	         */
	        Toast.makeText(context, "Alice original Message: " + new String(message) , Toast.LENGTH_LONG).show();
	        
	        /*
	         * Display the encrypted message
	         */
	        byte[]   out1 = i1.processBlock(message, 0, message.length);
	        Toast.makeText(context, "Alice Encrypted Message: " + new String(out1), Toast.LENGTH_LONG).show();
	        
	       /*
	        * Display the decrypted message 
	        */
	        byte[]   out2 = i2.processBlock(out1, 0, out1.length);
	        Toast.makeText(context, "Bob Decrypted Alice's Message: " + new String(out2) , Toast.LENGTH_LONG).show();
	
	        
	        /*
	         * Initialize scenario where Bob encrypts message to Alice, Alice decrypts the message
	         */	        
	        i1.init(true, bob.getPrivate(), alice_pubKey, p);		// bob initializes IESEngine to encrypt
	        i2.init(false, alice.getPrivate(), bob_pubKey, p);		// alice initializes IESEngine to decrypt
	        
	        
	        /*
	         * Display the original message 
	         */
	        Toast.makeText(context, "Bob original Message: " + new String(message2) , Toast.LENGTH_LONG).show();
	        
	        /*
	         * Display the encrypted message
	         */
	        byte[]   out3 = i1.processBlock(message2, 0, message2.length);
	        Toast.makeText(context, "Bob Encrypted Message: " + new String(out3), Toast.LENGTH_LONG).show();
	        
	       /*
	        * Display the decrypted message 
	        */
	        byte[]   out4 = i2.processBlock(out3, 0, out3.length);
	        Toast.makeText(context, "Alice Decrypted Bob's Message: " + new String(out4) , Toast.LENGTH_LONG).show();
	        
	        
	        
	        if (! (are_same(out2, message) && are_same(out4, message2)))
	        {
	        	Toast.makeText(context, "Block cipher test FAILED", Toast.LENGTH_LONG).show();
	        }
	        else
	        {
	        	Toast.makeText(context, "Block cipher test PASSED", Toast.LENGTH_SHORT).show();
	        	
	        }
	    }
	    catch (Exception ex)
	    {
	    	Toast.makeText(context, "Block cipher test exception " + ex.toString(), Toast.LENGTH_LONG).show();
	    }
	}
	
}
