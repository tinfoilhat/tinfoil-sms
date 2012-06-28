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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
	
	// Number of random elements
	private static final int NUMBER_RANDOM_ELEMENTS = 10000;
	
	// Minimum size of the dictionary
	private static final int MIN_DICT_SIZE = 65537; 
	
	/**
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
			

	/**
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
	 * Simple function which returns a hexadecimal string with a minimum fixed
	 * width given a value to convert to hex.
	 */
	public static String fixedWidthHex(int value, int minWidth)
	{
		String leadingZeros = "";
		
		for (int i = Integer.toHexString(value).length(); i < minWidth; ++i)
		{
			leadingZeros += "0";
		}
		
		return leadingZeros + Integer.toHexString(value).toUpperCase();
	}
    
	
	/**
	 * Simple function to test that the random number generators generate the
	 * same sequence of values give a shared seed from the SDF
	 */
	public static void randomSequenceTest(byte[] shared_seed)
	{
		boolean sequenceMatch = true;
		
		List<BigInteger> randomSequence1 = new ArrayList();
		List<BigInteger> randomSequence2 = new ArrayList();
		
		/*
		 * Simulate two separate users generating a sequence of random numbers using 
		 * a shared seed and the ISAAC stream cipher as the PRNG
		 */
		ISAACRandomGenerator isaac1 = new ISAACRandomGenerator();
		ISAACRandomGenerator isaac2 = new ISAACRandomGenerator();
		
		isaac1.init(new ISAACEngine(), shared_seed);
		isaac2.init(new ISAACEngine(), shared_seed);
		
		/*
		 * Generate two separate sequences of random data and verify the two sequences are identical
		 */
		for (int i = 0; i < NUMBER_RANDOM_ELEMENTS; ++i)
		{
			randomSequence1.add(isaac1.nextBigInteger());
		}
		
		for (int j = 0; j < NUMBER_RANDOM_ELEMENTS; ++j)
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
			/*for (BigInteger randomNum : randomSequence1)
			{
				System.out.println(randomNum);
			}*/
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
		
		System.out.println("LENGTH: " + shared_seed.length);
		System.out.println("SEED: " + new String(Hex.encode(shared_seed)));
	}
	
	
	/**
	 * Function which tests the FastQuickSort implementation by verifying that
	 * it properly sorts a collection of random numbers
	 */
	public static void randomNumSortTest() throws Exception
	{
		boolean sequenceMatch = true;
		Integer[] unsortedNum = new Integer[NUMBER_RANDOM_ELEMENTS];
	
		// Used for calculating execution times
		long startTime = 0;
		long endTime = 0;
		
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
	
	}
	
	
	/**
	 * Function which tests the FastQuickSort implementation by verifying that
	 * it properly sorts a collection of strings
	 */
	@SuppressWarnings("unchecked")
	public static void randomStringSortTest(String[] unsortedStr, Collator collator) throws Exception
	{
		boolean sequenceMatch = true;
		
		// Used for calculating execution times
		long startTime = 0;
		long endTime = 0;
		
		// Calculate the time it takes to sort the string data
		startTime = System.currentTimeMillis();
		
		// Sort the strings with quicksort
		FastQuickSort.sort(unsortedStr, (Comparator)collator);
		
		endTime = System.currentTimeMillis();
		
		
		/*
		 * Verify that the string sequence is actually ordered with a simple linear comparison
		 */

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
		
			for (int i  = 0; i < unsortedStr.length; ++i)
			{
				System.out.println(unsortedStr[i]);
			}
		}
		
		System.out.println("Total time to sort STRING data: " + (endTime-startTime) + " milliseconds");
	}

	
	
	/**
	 * Function which tests the FastQuickSort implementation by verifying that
	 * it properly sorts a collection of strings stored as CollationKeys
	 */
	@SuppressWarnings("unchecked")
	public static void randomKeysSortTest(CollationKey[] unsortedKeys) throws Exception
	{
		boolean sequenceMatch = true;
		
		// Used for calculating execution times
		long startTime = 0;
		long endTime = 0;
		
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
			
			for (int i  = 0; i < unsortedKeys.length; ++i)
			{
				System.out.println(unsortedKeys[i].getSourceString());
			}
		}
		
		System.out.println("Total time to sort COLLATION KEY data: " + (endTime-startTime) + " milliseconds");
	}
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */ 
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		byte[] seed = new byte[32];
		byte[] random1 = new byte[32];
		byte[] random2 = new byte[32];
	
		int aliceKey;
		int bobKey;
		
		int length;
		boolean sequenceMatch = true;
		
		byte[] contentBefore = new byte[64];
		byte[] contentAfter = new byte[64];
		String stegotext;
		
		// Steganography dictionaries
		String[] uniqueDictionary = new String[MIN_DICT_SIZE];
		String[] inverseDictString = new String[MIN_DICT_SIZE];
		CollationKey[] inverseDictionary = new CollationKey[MIN_DICT_SIZE];
		
		
		// Calculate the time it takes to parse the file
		long startTime = System.currentTimeMillis();
		
		String[] masterDictionary = readLines("src/master_dictionary2.txt");
		
		
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Total time to store file in string array: " + (endTime-startTime) + " milliseconds");
		
		CollationKey[] unsortedKeys = new CollationKey[masterDictionary.length];
		
		
		/*
		 *  Inititialize the seed derivative function parameters with the
		 *  parameters S1 & S2
		 */
		SDFParameters paramSDF = new SDFParameters("test1", "test2");
		
		// Generate the seed using SHA256 digest
		SDFGenerator generatorSDF = new SDFGenerator(new SHA256Digest());
		generatorSDF.init(paramSDF);
		length = generatorSDF.generateBytes(seed, 0, 0);
		
		System.out.println("LENGTH: " + seed.length);
		System.out.println("SEED: " + new String(Hex.encode(seed)));
		System.out.println("HEX LENGTH: " + new String(Hex.encode(seed)).length());
		
		ISAACRandomGenerator isaac1 = new ISAACRandomGenerator();
		ISAACRandomGenerator isaac2 = new ISAACRandomGenerator();
		
		isaac1.init(new ISAACEngine(), seed);
		isaac2.init(new ISAACEngine(), seed);
		
		//byte[] test = Hex.encode(seed);
		
		byte[] subTest = new byte[2];
		
		System.arraycopy(seed, 0, subTest, 0, 2);
		
		ByteBuffer buffer = ByteBuffer.wrap(subTest);
		buffer.order(ByteOrder.BIG_ENDIAN);  // if you want little-endian
		int result = buffer.getShort() & 0xFFFF;
		
		System.out.println(result);
		//System.out.println(new String(subTest));
		//System.out.println(new BigInteger(subTest).intValue());
		
		
		/*
		 * Simulate a sequence of random strings to sort with FastQuickSort 
		 */
		
		// Define the collator locale and parameters for sorting strings
		//Collator collator = Collator.getInstance(Locale.US);
		
		/* Set the collator decomposition parameters and comparison strength
		 * For more detail on the different decompositions and comparison strengths
		 * 
		 * @see http://docs.oracle.com/javase/1.5.0/docs/api/java/text/Collator.html
		 */
		//collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
		//collator.setStrength(Collator.PRIMARY);
		
		
		// Define a strict collator for sorting and matching words in the dictionary
		Collator strictCollator = Collator.getInstance(Locale.US);
		
		/* Set the collator decomposition parameters and comparison strength
		 * For more detail on the different decompositions and comparison strengths
		 * 
		 * @see http://docs.oracle.com/javase/1.5.0/docs/api/java/text/Collator.html
		 */
		strictCollator.setDecomposition(Collator.FULL_DECOMPOSITION);
		strictCollator.setStrength(Collator.IDENTICAL);
		
		
		
		
		/*
		 * Simulate an unsorted collection of Collation keys for the strings to be sorted 
		 
		for (int i = 0; i < masterDictionary.length; ++i)
		{
			unsortedKeys[i] = collator.getCollationKey(masterDictionary[i]);
		}*/
		
		
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
		
		
		/*
		 * Test that the Steganography class properly generates the unique dictionary and
		 * inverse dictionary given the master dictionary
		 */
		startTime = System.currentTimeMillis();
		Steganography.generateDict(uniqueDictionary, inverseDictionary, masterDictionary, (Comparator)strictCollator, isaac1);
		endTime = System.currentTimeMillis();
		
		// Display results
		for (int i  = 0; i < uniqueDictionary.length - 1; ++i)
		{
			System.out.println(uniqueDictionary[i]);

		}
		
		for (int i = 0; i < inverseDictionary.length; ++i)
		{
			System.out.println(inverseDictionary[i].getSourceString());
		}
		
		System.out.println("Total time to generate the unique dictionary and inverse dictionary " + (endTime-startTime) + " milliseconds");
		
		
		// Convert the inverseDictionary from collationkey to string array as that is how it will be stored on disk
		for (int i =0; i < inverseDictionary.length; ++i)
		{
			inverseDictString[i] = inverseDictionary[i].getSourceString();
		}
		
		/*
		 * Test the steganography obfuscate and deobfuscate, as well as the average length of messages given
		 * a simulated encrypted content using random generated input
		 * 
		 *  Use the seed value to generate a sequence of random bytes as a GOOD test to simulate the encrypted
		 *  content, concatenate two random numbers to get a 64byte string as a test (since we will support
		 *  messages of at least 60 chars which is 64 bytes when using block cipher
		 */

		for (int i = 0; i < 100000; ++i)
		{
			// Generate a random "simulated" encrypted message, this is the initial content
			isaac1.nextBytes(contentBefore);
		
			// Test string, breaks because of daemons conflict
			//contentBefore = Hex.decode("b904caa129f7a41c28300087d662e00c56cb2f121deac2f9226aea29ec5d4e972e83d41a3377438996b6e97602e0c226f1df6c6266b28cfb218b2040552b33b0");
			
			stegotext = Steganography.obfuscate(contentBefore, uniqueDictionary);
			contentAfter = Steganography.deObfuscate(stegotext, inverseDictString, strictCollator);
			
			// Verify that the content is THE EXACT SAME after it has had steganography applied (obfuscated) and then has
			// been de-obfuscated by the recipient, if they are not the same then the steganography has a SERIOUS FLAW!
			if (strictCollator.compare(new String(Hex.encode(contentBefore)), new String(Hex.encode(contentAfter))) != 0)
			{
				System.out.println("STEGANOGRAPHY FAILED, ORIGINAL CONTENT: " + new String(Hex.encode(contentBefore)) +
						" AND CONTENT AFTER DE-OBFUSCATE DO NOT MATCH: " + new String(Hex.encode((contentAfter))));
				sequenceMatch = false;
			}

			 //System.out.println("Encrypted message before OBFUSCATE: " + new String(Hex.encode(contentBefore)));
			 //System.out.println("STEGOTEXT: " + stegotext.length() + " : " + stegotext);
			 //System.out.println("Encrypted message after DE-OBFUSCATE: " + new String(Hex.encode(contentAfter)));
		}
		if (sequenceMatch)
		{
			System.out.println("STEGANOGRAPHY SUCCEEDED!");
		}
		
	}
}