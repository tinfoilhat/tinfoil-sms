import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.ISAACEngine;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.prng.RandomGenerator;
import org.spongycastle.crypto.StreamCipher;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.math.BigInteger;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
/**
 * 
 * @title	Steganography Research and Proof of Concept
 * 
 * @author	GNU USER
 * 
 */
public class SteganographyActivity
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
	 * Function which quickly reads the lines from the file into an 
	 * array of strings
	 * 
	 * @param filename The path and name of the file to open
	 * @return A string array containing each line of the file
	 */
    public static String[] readLines(String filename)
    		throws IOException
    {
        /*
         * Quickly get the number of lines in the file for creating the string array
         */
        LineNumberReader  lnr = new LineNumberReader(new FileReader(filename));
        lnr.skip(Long.MAX_VALUE);
        String[] fileLines = new String[lnr.getLineNumber()];
        String line = null;
        lnr.close();    	
    	
    	/*
    	 * Open the file for reading as well as a buffered reader
    	 */
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        
        for(int i = 0;; ++i)
        {
        	// Reached the end of the file
        	if ((line = bufferedReader.readLine()) == null)
        	{
        		break;
        	}

        	fileLines[i] = line;
        }
        
        bufferedReader.close();
        return fileLines;
    }

	
	/**
	 * @param args
	 * @throws Exception 
	 */ 
	public static void main(String[] args) throws Exception
	{
		byte[] seed = new byte[32];
		byte[] random1 = new byte[32];
		byte[] random2 = new byte[32];
		
		int aliceKey;
		int bobKey;
		
		int length;
		boolean sequenceMatch = true;
		
		final int NUMBER_RANDOM_ELEMENTS = 1000;
		
		List<BigInteger> randomSequence1 = new ArrayList();
		List<BigInteger> randomSequence2 = new ArrayList();
		
		/*
		 * Create a list of values to be sorted
		 */
		Integer[] unsortedNum = new Integer[NUMBER_RANDOM_ELEMENTS];
		
		// Calculate the time it takes to parse the file
		long startTime = System.currentTimeMillis();
		
		String[] unsortedStr = readLines("src/test_inv_dict.txt");
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Total time to store file in string array: " + (endTime-startTime) + " milliseconds");
		
		CollationKey[] unsortedKeys = new CollationKey[unsortedStr.length];
		
		
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
		 * Simulate two separate users generating a sequence of random numbers using 
		 * a shared seed and the ISAAC stream cipher as the PRNG
		 */
		ISAACRandomGenerator isaac1 = new ISAACRandomGenerator();
		ISAACRandomGenerator isaac2 = new ISAACRandomGenerator();
		
		isaac1.init(new ISAACEngine(), seed);
		isaac2.init(new ISAACEngine(), seed);
		
		
		/*
		 * Generate two separate sequences of random data and verify the two sequences are identical
		 */
		for (int i = 0; i < 3; ++i)
		{
			randomSequence1.add(isaac1.nextBigInteger());
		}
		
		for (int j = 0; j < 3; ++j)
		{
			randomSequence2.add(isaac2.nextBigInteger());
		}

		
		/*
		 * Verify that the two sequences are equal
		 */
		for (int idx  = 0; idx < randomSequence1.size(); ++idx)
		{
			// Have to cast as int otherwise it will always fail as Java arraylist uses Integer object
			// and the comparison is checking if the reference is to the same object
			if (randomSequence1.get(idx).compareTo(randomSequence2.get(idx)) != 0)
			{
				System.out.println(randomSequence1.get(idx) + " and " + randomSequence2.get(idx) +
						" do not match!");
				sequenceMatch = false;
			}
		}
		
		if (sequenceMatch)
		{
			System.out.println("The sequences match, here is the list of random data in the sequence:\n");
			for (BigInteger randomNum : randomSequence1)
			{
				System.out.println(randomNum);
			}
		}
		else
		{
			System.out.println("The sequences DO NOT MATCH, here is the list of random data are space seperated for each " +
					"value in the two sequences, each newline is the next pair of values from two sequences:\n");
			
			for (int idx = 0; idx < randomSequence1.size(); ++idx)
			{
				System.out.println(randomSequence1.get(idx) + " " + randomSequence2.get(idx));
			}
		}
		
		
		
		/*
		 * Simulate a sequence of random numbers to sort with FastQuickSort 
		 */
		Random random = new Random();
		
		for (int i = 0; i < NUMBER_RANDOM_ELEMENTS; ++i)
		{
			unsortedNum[i] = Integer.valueOf(random.nextInt(NUMBER_RANDOM_ELEMENTS));
		}
		
		// Calculate the time it takes to sort the numerical data
		startTime = System.currentTimeMillis();
		
		// Sort the numbers with quicksort
		FastQuickSort.sort(unsortedNum);
		
		endTime = System.currentTimeMillis();
		
		
		/*
		 * Verify that the sequence is actually ordered with a simple linear comparison
		 */
		sequenceMatch = true;
		for (int i  = 0; i < unsortedNum.length - 1; ++i)
		{
			/*
			 * If the current value in the list is greater than the next number, then
			 * sequence of numbers is not sorted correctly
			 */
			if (unsortedNum[i].compareTo(unsortedNum[i+1]) > 0)
			{
				System.out.println("The next value in the sequence: " + unsortedNum[i+1] + 
						" is less than the current value: " + unsortedNum[i]);
				sequenceMatch = false;
				break;
			}
		}
		
		if (sequenceMatch)
		{
			System.out.println("THE NUMBER SEQUENCE IS CORRECTLY SORTED!");
		}
		else
		{
			System.out.println("The number sequence is NOT SORTED PROPERLY!");
		}
		/*for (int i  = 0; i < unsortedNum.length - 1; ++i)
		{
			System.out.println(unsortedNum[i]);
		}*/
		
		System.out.println("Total time to sort numerical data: " + (endTime-startTime) + " milliseconds");
	
		
		/*
		 * Simulate a sequence of random strings to sort with FastQuickSort 
		 */
		
		// Define the collator locale and parameters for sorting strings
		Collator collator = Collator.getInstance(Locale.US);
		
		/* Set the collator decomposition parameters and comparison strength
		 * For more detail on the different decompositions and comparison strengths
		 * 
		 * @see http://docs.oracle.com/javase/1.5.0/docs/api/java/text/Collator.html
		 */
		collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
		collator.setStrength(Collator.PRIMARY);
		
		
		/*
		 * Simulate an unsorted collection of Collation keys for the strings to be sorted 
		 */
		for (int i = 0; i < unsortedStr.length; ++i)
		{
			unsortedKeys[i] = collator.getCollationKey(unsortedStr[i]);
		}
		
		
		// Calculate the time it takes to sort the string data
		startTime = System.currentTimeMillis();
		
		// Sort the strings with quicksort
		//FastQuickSort.sort(unsortedStr, (Comparator)collator);
		
		endTime = System.currentTimeMillis();
		
		
		/*
		 * Verify that the string sequence is actually ordered with a simple linear comparison
		 */
		sequenceMatch = true;
		for (int i  = 0; i < unsortedStr.length - 1; ++i)
		{
			/*
			 * If the current value in the list is greater than the next number, then
			 * sequence of numbers is not sorted correctly
			 */
			if (collator.compare(unsortedStr[i], unsortedStr[i+1]) > 0)
			{
				System.out.println("The next value in the string sequence: " + unsortedStr[i+1] + 
						" is less than the current value: " + unsortedStr[i]);
				sequenceMatch = false;
				break;
			}
		}
		
		if (sequenceMatch)
		{
			System.out.println("THE STRING SEQUENCE IS CORRECTLY SORTED!");
		}
		else
		{
			System.out.println("The string sequence is NOT SORTED PROPERLY!");
		}
		/*for (int i  = 0; i < unsortedStr.length - 1; ++i)
		{
			System.out.println(unsortedStr[i]);
		}*/
		
		System.out.println("Total time to sort STRING data: " + (endTime-startTime) + " milliseconds");
		 
		 
		
		
		/*
		 * COLLATION KEYS TEST, HAS THE SAME UNSORTED VALUES AS STRING TEST
		 */
		
		// Calculate the time it takes to sort the collation keys for the string data
		startTime = System.currentTimeMillis();
		
		// Sort the numbers with quicksort
		FastQuickSort.sort(unsortedKeys);
		
		endTime = System.currentTimeMillis();
		
		
		/*
		 * Verify that the collation keys sequence is actually ordered with a simple linear comparison
		 */
		sequenceMatch = true;
		for (int i  = 0; i < unsortedKeys.length - 1; ++i)
		{
			/*
			 * If the current value in the list is greater than the next number, then
			 * sequence of numbers is not sorted correctly
			 */
			if (unsortedKeys[i].compareTo(unsortedKeys[i+1]) > 0)
			{
				System.out.println("The next value in the collation key sequence: " + unsortedKeys[i+1] + 
						" is less than the current value: " + unsortedKeys[i]);
				sequenceMatch = false;
				break;
			}
		}
		
		if (sequenceMatch)
		{
			System.out.println("THE COLLATION KEY SEQUENCE IS CORRECTLY SORTED!");
		}
		else
		{
			System.out.println("The COLLATION KEY sequence is NOT SORTED PROPERLY!");
		}
		for (int i  = 0; i < unsortedKeys.length - 1; ++i)
		{
			System.out.println(unsortedKeys[i].getSourceString());
		}
		
		System.out.println("Total time to sort COLLATION KEY data: " + (endTime-startTime) + " milliseconds");
		
		
		
		/*
		 * Quick test of the hashtable functions and miller-rabin primality tests
		 */
		
		for (int i = 0; i < 30; ++i)
		{
			aliceKey = isaac1.nextInt();
			bobKey = isaac2.nextInt();
			
			System.out.println("Alice probe: " + HashTable.getProbe(aliceKey, 190366));
			System.out.println("Alice double hash interval: " + HashTable.getInterval(aliceKey));
			
			System.out.println("Bob probe: " + HashTable.getProbe(bobKey, 190366));
			System.out.println("Bog double hash interval: " + HashTable.getInterval(bobKey));

		}
		System.out.println("Co-prime bucketSize " + HashTable.getCoPrime(190366));
	}
}