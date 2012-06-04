import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;

import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.prng.RandomGenerator;

/**
 * Provides the steganography operations for Tinfoil-SMS, currently supports
 * generating the dictionaries which are used to transform the encrypted message
 * into a stegotext.
 * 
 * Currently this class only provides a minimal level of security-through-obscurity
 * as the obfuscate and deobfuscate methods only generate a primitive stegotext
 * which is merely textual data instead of pertaining any information.
 * 
 * @see hipstego-proc-fki25205.pdf under research/steganography/
 * 
 * In the Alice and Bob prison scenario as discussed in the paper referenced this
 * stegosystem only provides security against a PRIMITIVE computerized analysis
 * that merely looks for textual data, (just looking for words instead of
 * random encrypted charaters). The stegotext currently produced will fail against
 * any form of statistical analysis or primitive natural language processing by
 * an adversarial computer system.
 * 
 * @TODO Do more research and add support to incorporate information theory and
 * natural language processing operations so that stegotext appears as information
 * rather than just textual data.
 */
public abstract class Steganography
{
	
	// Minimum size of the dictionary
	private static final int MIN_DICT_SIZE = 65537; 
	
	/*
	 * Generates the unique dictionary and it's inverse dictionary as a subset of the
	 * master dictionary given the random generator engine. For the comparator it is
	 * recommended that Collator is used with the master dictionary language locale
	 * specified.
	 * 
	 * NOTE: dictionary and inverseDictionary must contain 65,537 or more words and 
	 * must be the same size and half the size of the master dictionary, this is due 
	 * to performance issues inherent in generating hash tables when the bucket of 
	 * values is depleted past 70%
	 * 
	 * @param uniqueDictionary An array which will be populated with a dictionary of unique words
	 * for values, this is used in obfuscating encrypted messages, must be >= 65,537 elements
	 * 
	 * @param inverseDictionary An array which will be populated with the inverse values of
	 * the uniquely generated dictionary, this is used in de-obfuscating stegotext, 
	 * must be >= 65,537 elements
	 * 
	 * @param masterDictionary An array containing each of the unique words in the master
	 * dictionary, must be twice size of the dictionaries
	 * 
	 * @param c The comparator to use for performing comparisons on the words in the dictionary,
	 * using Collator with the language locale specified is recommended
	 * 
	 * @param engine The random generator engine, ISAACEngine is currently the only accepted engine 
	 * as it supports a reproducible random number sequence using a seed value given by the seed 
	 * derivative function
	 * 
	 * @throws DataLengthException If The dictionary and inverseDictionary are not the same size
	 * or if the master dictionary is not at least twice the size of the dictionaries
	 * 
	 * @throws IllegalArgumentException If a comparator other than Collator is provided or if
	 * a random number generator other than ISAACRandomGenerator is provided
	 */
	public static <T extends Comparable<? super T>> void generateDict(	String[] uniqueDictionary, 
																		CollationKey[] inverseDictionary, 
																		String[] masterDictionary,
																		Comparator<T> c,
																		ISAACRandomGenerator engine)
	throws Exception, DataLengthException, IllegalArgumentException
	{
		/*
		 * Checks to ensure that the requirements for the dictionaries are met
		 */
		if (uniqueDictionary.length < MIN_DICT_SIZE)
		{
			throw new DataLengthException("Dictionary and inverse dictionary MUST have at least 65,537 elements or more!");
		}
		if (uniqueDictionary.length != inverseDictionary.length)
		{
			throw new DataLengthException("Dictionary and inverse dictionary MUST be the same size!");
		}
		if (masterDictionary.length < (uniqueDictionary.length * 2))
		{
			throw new DataLengthException("Master dictionary MUST be at least TWICE the size of the dictionaries!");
		}
		if (! (c instanceof Collator))
		{
			throw new IllegalArgumentException("Steganography currently only supports string values, the comparator must be a Collator object!");
		}
			

		/*
		 * Get the nearest co-prime value for the size of the bucket(s), as the number of collisions is minimized 
		 * only if both the constant interval and the bucket size are both prime. 
		 */
		int key;
		int probe;
		int newProbe;
		String uniqueWord;
		int bucketSize = HashTable.getCoPrime(masterDictionary.length);

		
		/*
		 * Populate the unique dictionary, the sequence of keys are the hex value of the dictionary index and the
		 * sequence of values are probed from the master dictionary. If the value has already been selected, apply
		 * double hashing to get the next value
		 */
		for (int i = 0; i < uniqueDictionary.length; ++i)
		{
			key = engine.nextInt();
			probe = HashTable.getProbe(key, bucketSize);
			
			/*
			 * Probe the value using double hashing, if there is a collision, probe the bucket until
			 * a unique value is found. Words that have already been used are marked as null.
			 */
			for (int j = 0; j < bucketSize; ++j)
			{
				newProbe = (probe + (j * HashTable.getInterval(key))) % bucketSize;
				if ((uniqueWord = masterDictionary[newProbe]) != null)
				{
					/*
					 * Add the word from master dictionary to the unique dictionary in the
					 * format <dictionary index in hex>:<unique word> (ie. 003F:fizzle)
					 */
					uniqueDictionary[i] = fixedWidthHex(i, 4) + ":" + uniqueWord;
					
					/*
					 * Add the word from the master dictionary to the inverse lookup for the unique
					 * dictionary in the format <unique word>:<dictionary index in hex> (ie. fizzle:003F)
					 */
					inverseDictionary[i] = ((Collator)c).getCollationKey(uniqueWord + ":" + fixedWidthHex(i, 4));
					
					// Mark the unique word in the master dictionary as null as it has been used
					masterDictionary[newProbe] = null;
					
					break;
				}
			}
		}
		// Finally, sort the inverseDictionary alphabetically to make lookups using the unique word faster
		FastQuickSort.sort(inverseDictionary);
	}
	
	
	/*
	 * Simple function which returns a hexadecimal string with a minimum fixed
	 * width given a value to convert to hex.
	 */
	private static String fixedWidthHex(int value, int minWidth)
	{
		String leadingZeros = "";
		
		for (int i = Integer.toHexString(value).length(); i < minWidth; ++i)
		{
			leadingZeros += "0";
		}
		
		return leadingZeros + Integer.toHexString(value).toUpperCase();
	}
	
}
