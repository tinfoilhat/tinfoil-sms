/** 
 * Copyright (C) 2012 Tinfoilhat
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

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
 * TODO Do more research and add support to incorporate information theory and
 * natural language processing operations so that stegotext appears as information
 * rather than just textual data.
 */
public abstract class Steganography
{
	
	// Minimum size of the dictionary
	private static final int MIN_DICT_SIZE = 65537; 
	
	/**
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
					uniqueDictionary[i] = DictionaryUtil.fixedWidthHexString(i, 4) + ":" + uniqueWord;
					
					/*
					 * Add the word from the master dictionary to the inverse lookup for the unique
					 * dictionary in the format <unique word>:<dictionary index in hex> (ie. fizzle:003F)
					 */
					inverseDictionary[i] = ((Collator)c).getCollationKey(uniqueWord + ":"
										+ DictionaryUtil.fixedWidthHexString(i, 4));
					
					// Mark the unique word in the master dictionary as null as it has been used
					masterDictionary[newProbe] = null;
					
					break;
				}
			}
		}
		// Finally, sort the inverseDictionary alphabetically to make lookups using the unique word faster
		FastQuickSort.sort(inverseDictionary);
	}
	
	
	/**
	 * Obfuscates the content by generating a stegotext using unique words from the dictionary
	 * provided. Currently this only provides a minimal level of security-through-obscurity 
	 * as it only generates a primitive stegotext, which is merely textual data ("words")
	 * without any evident information.
	 * 
	 * @param content The content to be transformed into an obfuscated stegotext
	 * @param uniqueDictionary A unique dictionary mapping hex values (keys) to words
	 * 
	 * @return The stegotext, which is the obfuscated content
	 * 
	 * @throws DataLengthException If the content in bytes is not divisible by two, or if
	 * the unique dictionary does not contain 65,537 or more words, 
	 * 
	 * TODO Research into adding NLP/Information theory support to generate
	 * stegotext that appears as information rather than just textual data
	 */
	public static String obfuscate(byte[] content, String[] uniqueDictionary)
		throws DataLengthException
	{
		if (content.length % 2 == 1)
		{
			throw new DataLengthException("The number of bytes in the content must be a multiple of two!");
		}
		if (uniqueDictionary.length < MIN_DICT_SIZE)
		{
			throw new DataLengthException("Dictionary and inverse dictionary MUST have at least 65,537 elements or more!");
		}
		

		byte[] key = new byte[2];
		int keyIndex;
		String word = "";
		String stegotext = "";

		/*
		 * For each two bytes of the content look up a unique word in the dictionary 
		 * matches the key
		 */
		for (int i = 0; i < content.length; i += 2)
		{
			/*
			 * Create a byte buffer to convert from bytes to unisgned integer, the bytes are
			 * ordered as big endian as message content is generated as array of bytes in big endian
			 */
			System.arraycopy(content, i, key, 0, 2);
			ByteBuffer buffer = ByteBuffer.wrap(key);
			buffer.order(ByteOrder.BIG_ENDIAN);
			
			// Get the key as an unsigned short index to lookup in the dictionary
			keyIndex = buffer.getShort() & 0xFFFF;
			
			/*
			 * Get the unique word value for the key, remove the key and ":" delimeter 
			 * from the string and add the word to the stegotext
			 */
			word = uniqueDictionary[keyIndex];
			word = word.replaceAll(Pattern.compile("^\\w{4}:").toString(), "");
			
			if (i < content.length -2)
			{
				stegotext += word + " ";
			}
			else
			{
				stegotext += word;
			}
		}
		return stegotext;
	}
	
	
	/**
	 * De-obfuscates the stegotext back into the original content by using the unique
	 * words INVERSE dictionary provided. Currently this only provides a minimal level 
	 * of security-through-obscurity as the stegotext to be de-obfuscated is merely 
	 * textual data ("words") without any evident information.
	 * 
	 * @param stegotext The obfuscated stegotext to be transformed into the original content
	 * @param inverseDictionary The INVERSE unique dictionary for mapping words to hex values (keys)
	 * @param c	The comparator to use for performing comparisons when looking up words in the 
	 * inverse dictionary
	 * 
	 * @return The original content, which is the de-obfuscated stegotext
	 * 
	 * @throws DataLengthException If the inverse dictionary does not contain 65,537 or more words
	 * @throws Exception If there is an error converting from the stegotext to the original content 
	 * 
	 * TODO Research into adding NLP/Information theory support to generate stegotext that appears
	 * as information rather than just textual data
	 */
	public static byte[] deObfuscate(String stegotext, String[] inverseDictionary, Collator c)
		throws DataLengthException, IllegalArgumentException, Exception
	{
		if (inverseDictionary.length < MIN_DICT_SIZE)
		{
			throw new DataLengthException("Dictionary and inverse dictionary MUST have at least 65,537 elements or more!");
		}
		
		/*
		 * Divide the stegotext into a collection of n-grams or words and then map each
		 * word to the original hex values
		 */
		String[] words = stegotext.split("\\s+");
		byte[] origContent = new byte[words.length * 2];
		byte[] value;
		int idx = 0;
		
		for (String word : words)
		{
			value = DictionaryUtil.getInverseValue(word, inverseDictionary, c);
			System.arraycopy(value, 0, origContent, idx, value.length);
			idx += value.length;
		}
		return origContent;
	}

	
}
