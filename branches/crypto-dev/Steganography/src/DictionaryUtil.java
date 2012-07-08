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

import java.text.Collator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.spongycastle.crypto.DataLengthException;

/**
 * Provides the dictionary and inverse dictionary utility functions such as for
 * looking up the values from the dictionary or handling keys/values that are bytes
 */
public abstract class DictionaryUtil
{
	/**
	 * Returns the hex value for the the unique word which is the key in the 
	 * inverse dictionary. It uses binary search to efficiently look up the
	 * hex value as the dictionary is already sorted, if the inverse dictionary
	 * provided is NOT sorted the lookup will fail.
	 * 
	 * @param key The unique word which is the key to lookup the value for in the inverse dictionary
	 * @param inverseDictionary The INVERSE unique dictionary for mapping words to hex values (keys)
	 * @param c	The comparator to use for performing comparisons when looking up words in the  
	 * 
	 * @return The value for the key
	 * 
	 * @throws IllegalArgumentException If the key is null
	 * @throws Exception If the value is not found in the inverse dictionary
	 * 
	 */
	public static byte[] getInverseValue(String key, String[] inverseDictionary, Collator c)
			throws IllegalArgumentException, Exception
	
	{
		// Error if the key is null 
		if (key == null)
		{
			throw new IllegalArgumentException("The key to search for cannot be NULL, invalid key!");
		}

		// Regular expressions to match the key and value from this delimited string
		Pattern keyPattern = Pattern.compile("(^.*):");
		Pattern valuePattern = Pattern.compile(":(.*$)");
		Matcher valueMatcher;
		
		byte[] hexValue = null;
		int valueIndex;
		
		// Search for the unique word in the inverse dictionary
		valueIndex = DictionaryBinarySearch.search(key, inverseDictionary, keyPattern, c);
		
		// If the unique word was found get hex value
		if (valueIndex != -1)
		{
			valueMatcher = valuePattern.matcher(inverseDictionary[valueIndex]);
				
			// TODO This should raise an exception if it cannot match the word, possible dictionary corruption
			if (valueMatcher.find())
			{
				hexValue = hexStringToByteArray(valueMatcher.group(1));
			}
		}
		// Unique word was not found, SERIOUS ISSUE WITH DICTIONARIES
		else
		{
			throw new Exception("FATAL ERROR: Unable to find unique word in dictionary, de-obfuscate failed");
		}
		
		return hexValue;
	}
	
	
	/**
	 * Simple function which returns a hexadecimal string with a minimum fixed
	 * width given a value to convert to hex.
	 * 
	 * @param value The integer value to convert to a hex string
	 * @param minWidth The width of the hex string, the width must be a multiple
	 * of two such as four, leading 0's will be added to match the width
	 *   
	 * @return A fixed width hex string of the value
	 * 
	 * @throws DataLengthException If the width is not a multiple of two
	 */
	public static String fixedWidthHexString(int value, int width)
			throws DataLengthException
	{
		if (width % 2 != 0)
		{
			throw new DataLengthException("The width must be a multiple of two!");
		}
		
		String leadingZeros = "";
		
		for (int i = Integer.toHexString(value).length(); i < width; ++i)
		{
			leadingZeros += "0";
		}
		
		return leadingZeros + Integer.toHexString(value).toUpperCase();
	}
	
	
	/**
	 * Simple utility function which takes a hex string and returns a byte
	 * array containing the hex values.
	 * 
	 * @param hexString A string containing hex values, must have a length that
	 * is a multiple of two
	 * 
	 * @return A byte array containing the hex values
	 * 
	 * @throws DataLengthException If the length of hexString is not a multiple of two
	 * @throws IllegalArgumentException If the hex string does not contain hex values
	 */
	public static byte[] hexStringToByteArray(String hexString)
			throws DataLengthException, IllegalArgumentException
	{
		if (hexString.length() % 2 != 0)
		{
			throw new DataLengthException("The hexString length must be a multiple of two!");
		}
		if (Pattern.matches("[^a-f0-9]", hexString))
		{
			throw new IllegalArgumentException("The hexString contains values that are not hexadecimal!");
		}
		
	    int len = hexString.length();
	    byte[] hexValues = new byte[len / 2];
	    
	    for (int i = 0; i < len; i += 2)
	    {
	    	hexValues[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
	                             + Character.digit(hexString.charAt(i+1), 16));
	    }
	    
	    return hexValues;
	}	
}