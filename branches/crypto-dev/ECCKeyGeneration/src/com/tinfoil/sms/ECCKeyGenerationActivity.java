package com.tinfoil.sms;

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.ECPrivateKeySpec;
import org.spongycastle.jce.spec.ECPublicKeySpec;
import org.spongycastle.util.encoders.Hex;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.IEKeySpec;
import org.spongycastle.jce.spec.IESParameterSpec;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ECCKeyGenerationActivity extends Activity {
	private static final String TAG = "ECIES";
	static {
	    Security.addProvider(new BouncyCastleProvider());
	}
	
	/*
	 * same_as A function which checks if two array of bytes are identical
	 * 
	 * @param a first array of bytes
	 * @param b first array of bytes
	 * 
	 * @return boolean, true if identical
	 */
	private boolean same_as(
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
	 * encryption_test performs a series of ECC (ECIES) tests
	 * 
	 * Execute a series of tests, generate a public and private keypair
	 * and a simple steam cipher example that encrypts a predefined test string
	 */
	public void encryption_test(KeyPairGenerator generator)
	{
		try 
	    {
	        //
	        // a side
	        //
	        KeyPair     aKeyPair = generator.generateKeyPair();
	        PublicKey   aPub = aKeyPair.getPublic();
	        PrivateKey  aPriv = aKeyPair.getPrivate();
	        Toast.makeText(getApplicationContext(), "Generated ECC Keypair A", Toast.LENGTH_SHORT).show();
	
	        //
	        // b side
	        //
	        KeyPair     bKeyPair = generator.generateKeyPair();
	        PublicKey   bPub = bKeyPair.getPublic();
	        PrivateKey  bPriv = bKeyPair.getPrivate();
	        Toast.makeText(getApplicationContext(), "Generated ECC Keypair B", Toast.LENGTH_SHORT).show();
	
	        //
	        // stream test
	        //
	        Cipher c1 = Cipher.getInstance("ECIES", "SC");
	        Cipher c2 = Cipher.getInstance("ECIES", "SC");
	
	        IEKeySpec   c1Key = new IEKeySpec(aPriv, bPub);
	        IEKeySpec   c2Key = new IEKeySpec(bPriv, aPub);
	
	        byte[]  d = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
	        byte[]  e = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
	
	        IESParameterSpec param = new IESParameterSpec(d, e, 128);
	
	        c1.init(Cipher.ENCRYPT_MODE, c1Key, param);
	        c2.init(Cipher.DECRYPT_MODE, c2Key, param);
	
	        byte[] message = "requirements are in my blood says the ramiro, this is great!".getBytes();
	        
	        /*
	         * Display the original message 
	         */
	        Toast.makeText(getApplicationContext(), "Original Message: " + new String(message) , Toast.LENGTH_LONG).show();
	        
	        /*
	         * Display the encrypted message
	         */
	        byte[]   out1 = c1.doFinal(message, 0, message.length);
	        Toast.makeText(getApplicationContext(), "Encrypted Message: " + new String(out1), Toast.LENGTH_LONG).show();
	        
	       /*
	        * Display the decrypted message 
	        */
	        byte[]   out2 = c2.doFinal(out1, 0, out1.length);
	        Toast.makeText(getApplicationContext(), "Decrypted Message: " + new String(out2) , Toast.LENGTH_LONG).show();
	
	        if (!same_as(out2, message))
	        {
	        	Toast.makeText(getApplicationContext(), "Stream cipher test FAILED", Toast.LENGTH_SHORT).show();
	        }
	        else
	        {
	        	Toast.makeText(getApplicationContext(), "Stream cipher test PASSED", Toast.LENGTH_SHORT).show();
	        	
	        }
	    }
	    catch (Exception ex)
	    {
	    	Toast.makeText(getApplicationContext(), "stream cipher test exception " + ex.toString(), Toast.LENGTH_SHORT).show();
	    }
	}
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * Use one of the named elliptic curves specified in the NamedCurvedTable
         * http://www.bouncycastle.org/wiki/display/JA1/Supported+Curves+%28ECDSA+and+ECGOST%29
         * @TODO: Research into more secure/larger key sizes or ones that are more optimized such
         * as curve25519: http://cr.yp.to/ecdh.html
         */
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
        
		try
		{
			KeyPairGenerator g = KeyPairGenerator.getInstance("ECIES", "SC");
	        g.initialize(ecSpec, new SecureRandom());
	        
	        /*
	         * Execute a series of tests, generate a public and private keypair
	         * and a simple steam cipher example that encrypts a given string
	         */
	        encryption_test(g);
	        
		} catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}