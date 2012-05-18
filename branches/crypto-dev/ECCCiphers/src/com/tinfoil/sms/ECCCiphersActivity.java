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

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;

import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.BufferedBlockCipher;
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

public class ECCCiphersActivity extends Activity {
	private static final String TAG = "ECIES";
	static {
	    Security.addProvider(new BouncyCastleProvider());
	}
	
	/*
	 * are_same A function which checks if two array of bytes are identical
	 * 
	 * @param a first array of bytes
	 * @param b first array of bytes
	 * 
	 * @return boolean, true if identical
	 */
	private boolean are_same(
	        byte[]  a,
	        byte[]  b)
	    {
	        if (a.length != b.length)
	        {
	            return false;
	        }

	        for (int i = 0; i != a.length; i++)
	        {
	            if (a[i] != b[i])
	            {
	                return false;
	            }
	        }

	        return true;
	    }
	
	/*
	 * stream_test performs an ECC (ECIES) stream cipher test
	 * 
	 */
	public void stream_test(AsymmetricCipherKeyPair alice, AsymmetricCipherKeyPair bob) throws Exception
	{
		try {
	        //
	        // stream test
	        //
			/*
			 * Testing more secure IESEngine parameters, the minimum security for
			 * any component should be 256-bit
			 */
	        IESEngine      i1 = new IESEngine(
			                    new ECDHCBasicAgreement(),
			                    new KDF2BytesGenerator(new SHA256Digest()),
			                    new HMac(new SHA256Digest()));
	        IESEngine      i2 = new IESEngine(
			                    new ECDHCBasicAgreement(),
			                    new KDF2BytesGenerator(new SHA256Digest()),
			                    new HMac(new SHA256Digest()));

	        byte[]         d = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
	        byte[]         e = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
	        IESParameters  p = new IESParameters(d, e, 256);
	        
	        i1.init(true, alice.getPrivate(), bob.getPublic(), p);
	        i2.init(false, bob.getPrivate(), alice.getPublic(), p);
	        
	        /*
	         * Test: If the IESParamaeters (the shared information) are different MAC should fail!
	        
	        IESParameters  q = new IESParameters(new byte[] {}, new byte[] {}, 64);
	        i2.init(false, bob.getPrivate(), alice.getPublic(), q);
	        */
	       
	        /*
	         * Different sample size text messages
	         */
	        // 160 bytes/characters
	        //String text_message = 	"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer rutrum " +
	        //		"porttitor molestie. Donec mauris arcu, auctor vel suscipit vitae, lacinia ut elit metus.";
	        
	        // 120 bytes/characters
	        String text_message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur semper, tellus sed venenatis consectetur, ante metus.";
	        
	        byte[] message = text_message.getBytes();
	        
	        /*
	         * Display the original message 
	         */
	        Toast.makeText(getApplicationContext(), "Original Message: " + new String(message) , Toast.LENGTH_LONG).show();
	        
	        /*
	         * Display the encrypted message
	         */
	        byte[]   out1 = i1.processBlock(message, 0, message.length);
	        Toast.makeText(getApplicationContext(), "Encrypted Message: " + new String(out1), Toast.LENGTH_LONG).show();
	        
	       /*
	        * Display the decrypted message 
	        */
	        byte[]   out2 = i2.processBlock(out1, 0, out1.length);
	        Toast.makeText(getApplicationContext(), "Decrypted Message: " + new String(out2) , Toast.LENGTH_LONG).show();
	
	        if (!are_same(out2, message))
	        {
	        	Toast.makeText(getApplicationContext(), "Stream cipher test FAILED", Toast.LENGTH_LONG).show();
	        }
	        else
	        {
	        	Toast.makeText(getApplicationContext(), "Stream cipher test PASSED", Toast.LENGTH_SHORT).show();
	        	
	        }
	    }
	    catch (Exception ex)
	    {
	    	Toast.makeText(getApplicationContext(), "stream cipher test exception " + ex.toString(), Toast.LENGTH_LONG).show();
	    }
    }

	/*
	 * block_test performs an ECC (ECIES) block cipher test
	 */
	public void block_test(AsymmetricCipherKeyPair alice, AsymmetricCipherKeyPair bob) throws Exception
	{
		try {
	        /*
	         * twofish with CBC
	         *
	        BufferedBlockCipher c1 = new PaddedBufferedBlockCipher(
	                                    new CBCBlockCipher(new TwofishEngine()));
	        BufferedBlockCipher c2 = new PaddedBufferedBlockCipher(
	                                    new CBCBlockCipher(new TwofishEngine()));
	        */
			/*
			 * AES with CTR Mode (more secure) Instead of CBC -- FAILS, appears IESEngine
			 * Doesn't handle the IV parameter
	        BufferedBlockCipher c1 = new BufferedBlockCipher(
			                    		new SICBlockCipher(new AESEngine()));
	        BufferedBlockCipher c2 = new BufferedBlockCipher(
            							new SICBlockCipher(new AESEngine()));
            */
			
			/*
			 * AES With plain old CBC, it is using the PKCS7 standard which according to
			 * Wikipedia is more secure than PKCS5, but because it requires padding is 
			 * not as secure as CTR
			 * http://en.wikipedia.org/wiki/Block_cipher_modes_of_operation#Counter_.28CTR.29
			 * http://en.wikipedia.org/wiki/Padding_%28cryptography%29
			
			BufferedBlockCipher c1 = new PaddedBufferedBlockCipher(
				            		new CBCBlockCipher (new AESEngine()));
			BufferedBlockCipher c2 = new PaddedBufferedBlockCipher(
									new CBCBlockCipher (new AESEngine()));
			
			*/
			
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

	        byte[]         d = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
	        byte[]         e = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
	        
	        IESParameters p = new IESWithCipherParameters(d, e, 256, 256);
	        
	        /*
	         * Initialize scenario where Alice encrypts message to Bob, Bob decrypts the message
	         */
	        i1.init(true, alice.getPrivate(), bob.getPublic(), p);
	        i2.init(false, bob.getPrivate(), alice.getPublic(), p);
	        
	        
	        // 120 bytes/characters message
	        String text_message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur semper, tellus sed venenatis consectetur, ante metus.";
	        
	        String text_message2 = "This is a message from Bob in response to Alice message, is it decrypted?";
	        
	        byte[] message = text_message.getBytes();
	        byte[] message2 = text_message2.getBytes();
	        
	        /*
	         * Display the original message 
	         */
	        Toast.makeText(getApplicationContext(), "Alice original Message: " + new String(message) , Toast.LENGTH_LONG).show();
	        
	        /*
	         * Display the encrypted message
	         */
	        byte[]   out1 = i1.processBlock(message, 0, message.length);
	        Toast.makeText(getApplicationContext(), "Alice Encrypted Message: " + new String(out1), Toast.LENGTH_LONG).show();
	        
	       /*
	        * Display the decrypted message 
	        */
	        byte[]   out2 = i2.processBlock(out1, 0, out1.length);
	        Toast.makeText(getApplicationContext(), "Bob Decrypted Alice's Message: " + new String(out2) , Toast.LENGTH_LONG).show();
	
	        
	        
	        /*
	         * Initialize scenario where Bob encrypts message to Alice, Alice decrypts the message
	         */	        
	        i1.init(true, bob.getPrivate(), alice.getPublic(), p);
	        i2.init(false, alice.getPrivate(), bob.getPublic(), p);
	        
	        /*
	         * Display the original message 
	         */
	        Toast.makeText(getApplicationContext(), "Bob original Message: " + new String(message2) , Toast.LENGTH_LONG).show();
	        
	        /*
	         * Display the encrypted message
	         */
	        byte[]   out3 = i1.processBlock(message2, 0, message2.length);
	        Toast.makeText(getApplicationContext(), "Bob Encrypted Message: " + new String(out3), Toast.LENGTH_LONG).show();
	        
	       /*
	        * Display the decrypted message 
	        */
	        byte[]   out4 = i2.processBlock(out3, 0, out3.length);
	        Toast.makeText(getApplicationContext(), "Alice Decrypted Bob's Message: " + new String(out4) , Toast.LENGTH_LONG).show();
	        
	        
	        
	        
	        if (! (are_same(out2, message) && are_same(out4, message2)))
	        {
	        	Toast.makeText(getApplicationContext(), "Block cipher test FAILED", Toast.LENGTH_LONG).show();
	        }
	        else
	        {
	        	Toast.makeText(getApplicationContext(), "Block cipher test PASSED", Toast.LENGTH_SHORT).show();
	        	
	        }
	    }
	    catch (Exception ex)
	    {
	    	Toast.makeText(getApplicationContext(), "Block cipher test exception " + ex.toString(), Toast.LENGTH_LONG).show();
	    }
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
/*        ECCurve.Fp curve = new ECCurve.Fp(
                new BigInteger("6277101735386680763835789423207666416083908700390324961279"), // q
                new BigInteger("fffffffffffffffffffffffffffffffefffffffffffffffc", 16), // a
                new BigInteger("64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1", 16)); // b

	            ECDomainParameters params = new ECDomainParameters(
                curve,
                curve.decodePoint(Hex.decode("03188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012")), // G
                new BigInteger("6277101735386680763835789423176059013767194773182842284081")); // n
*/		
        
        /* 
         * Get the elliptic specifications for NIST P-256
         */
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
        
        /*
         * Define the elliptic curve parameters based on NIST P-256 specs
         */
        ECDomainParameters params = new ECDomainParameters(
        									ecSpec.getCurve(),	// Curve
									        ecSpec.getG(),		// G
									        ecSpec.getN());		// N
        
        ECKeyPairGenerator eGen = new ECKeyPairGenerator();
        KeyGenerationParameters gParam = new ECKeyGenerationParameters(params, new SecureRandom());

        eGen.init(gParam);

        AsymmetricCipherKeyPair p1 = eGen.generateKeyPair();
        AsymmetricCipherKeyPair p2 = eGen.generateKeyPair();
       
        ECPublicKeyParameters test = (ECPublicKeyParameters)p2.getPublic();
        
        ECPoint Q = test.getQ();
        
        TextView tv = new TextView(this);
        tv.setText("The folowing are the public and private keys of each person" + "\n\nPerson1:" + p1.getPrivate().toString() + p1.getPublic().toString() +
        			"\n\nPerson2:" + p2.getPrivate().toString() + p2.getPublic().toString() + "Person2 public key parameters:" + Q.getX().toBigInteger().toString());
        setContentView(tv);
        
        /*
         * Execute the ECIES cipher tests
         */
        try
		{
			//stream_test(p1, p2);
			block_test(p1, p2);
		}
        catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}