import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.ISAACEngine;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.prng.RandomGenerator;
import org.spongycastle.crypto.StreamCipher;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * 
 * @title	Steganography Research and Proof of Concept
 * 
 * @author	GNU USER
 * 
 */
public class Steganography
{
	static {
		// Set bouncy castle as the most preferred security provider
	    Security.insertProviderAt(new BouncyCastleProvider(), 1);
	}
	
	/*
	 * are_same A function which checks if two array of bytes are identical
	 * 
	 * @param a first array of bytes
	 * @param b first array of bytes
	 * 
	 * @return boolean, true if identical
	 */
	public boolean are_same(
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
	 * Generates a random number given an input value and populates the random output buffer
	 * NOTE: Both the input and output byte arrays must be the same size, ideally you should just
	 * 
	 * @param engine The ISAAC engine object to use
	 * @param output The output buffer to populate with the random number
	 
	public void nextBytes(ISAACEngine engine, byte[] output)
	{
		byte[] in = new byte[output.length];
		
		// Generate the random number and store in output
		engine.processBytes(in, 0, in.length, output, 0);
	}*/
	
	
    /*private void doTest(ISAACEngine engine, byte[] seed, byte[] output)
    {
        byte[] in = new byte[output.length];
        byte[]  = new byte[output.length];
        engine.init(true, new KeyParameter(key));
        engine.processBytes(in, 0, in.length, enc, 0);
        if (!areEqual(enc, output))
        {
            fail("ciphertext mismatch");
        }
        engine.init(false, new KeyParameter(key));
        engine.processBytes(enc, 0, enc.length, enc, 0);
        if (!areEqual(enc, in))
        {
            fail("plaintext mismatch");
        }
    }*/
	
	
	/**
	 * @param args
	 * @throws InterruptedException 
	 */ 
	public static void main(String[] args) throws InterruptedException
	{
		byte[] seed = new byte[32];
		byte[] random1 = new byte[32];
		byte[] random2 = new byte[32];
		int length = 0;
		boolean sequenceMatch = true;
		
		List<byte[]> randomSequence1 = new ArrayList();
		List<byte[]> randomSequence2 = new ArrayList();
		
		
		/*
		 *  Inititialize the seed derivative function parameters with the
		 *  parameters S1 & S2
		 */
		SDFParameters paramSDF = new SDFParameters("test1", "test");
		
		// Generate the seed using SHA256 digest
		SDFGenerator generatorSDF = new SDFGenerator(new SHA256Digest());
		generatorSDF.init(paramSDF);
		length = generatorSDF.generateBytes(seed, 0, 0);
		
		System.out.println("LENGTH: " + length);
		System.out.println("SEED: " + new String(Hex.encode(seed)));
		
		/*
		 *  Simulate two seperate users generating a sequence of PRNs using the same shared seed

		SecureRandom random1 = new SecureRandom(seed);
		SecureRandom random2 = new SecureRandom(seed);
		
		random1.setSeed(seed);
		random2.setSeed(seed);
		
		// Display random generator info
		System.out.println(random1.getProvider());
		System.out.println(random1.getAlgorithm());
		
		*/
		
		/*
		 * Simulate two separate users generating a sequence of random numbers using 
		 * a shared seed and the ISAAC stream cipher as the PRNG
		 */
		ISAACRandomGenerator isaac1 = new ISAACRandomGenerator();
		ISAACRandomGenerator isaac2 = new ISAACRandomGenerator();
		
		isaac1.init(new ISAACEngine(), seed);
		isaac2.init(new ISAACEngine(), seed);
		
		/*
		StreamCipher isaac1 = new ISAACEngine();
		StreamCipher isaac2 = new ISAACEngine();
		
		isaac1.init(true, new KeyParameter(seed));
		isaac2.init(true, new KeyParameter(seed));
		*/
		
		for (int i = 0; i < 100000; ++i)
		{
			isaac1.nextBytes(random1);
			isaac2.nextBytes(random2);
	
			System.out.println("Random BYTES " + i + " BOB: " + new String(Hex.encode(random1)));
			System.out.println("Random BYTES " + i + " ALICE: " + new String(Hex.encode(random2)));
			
			System.out.println("Random number " + i + " BOB: " + new BigInteger(random1));
			System.out.println("Random number " + i + " ALICE: " + new BigInteger(random2));
			
			System.out.println("Random BIGINT " + i + " BOB: " + isaac1.nextBigInteger());
			System.out.println("Random BIGINT " + i + " ALICE: " + isaac2.nextBigInteger());
		}
	}
}
