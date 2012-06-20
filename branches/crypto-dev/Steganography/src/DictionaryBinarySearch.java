import java.text.Collator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongycastle.crypto.DataLengthException;

/**
 * A custom variant of binary search used for the steganography dictionaries which
 * handles searching a multi-language dictionary where the data is "almost" sorted,
 * as there are some words with different accents, etc. from other languages which 
 * cause it not to be sorted properly.
 * 
 * Currently only handles Germanic, and Romance based languages such as English, 
 * French, German, Spanish (anything that is latin character based)
 */
public abstract class DictionaryBinarySearch
{
	
	/*
	 * The number of steps to take when performing the divergent search,
	 * if you find that values which exist are still not being found, try
	 * increasing the number of steps
	 */
	private static final int DIVERGENT_SEARCH_STEPS = 100; 

	
	/*
	 * Searches for the value specified and returns the index where the first 
	 * instance of the value provided is found in the sorted array of data.
	 * 
	 * A custom variant of binary search is used, which is tailored to searching
	 * almost-sorted data, where the data is from multiple latin-based languages.
	 * 
	 * @param value The value to search for in the sorted data provided
	 * @param sortedData The sorted data to search through
	 * @param c	The comparator to use for performing comparisons 
	 * 
	 * @return The index of the first instance of the value in the data if found,
	 * -1 if the value is not found
	 * 
	 * @throws IllegalArgumentException If the data is not sorted or the value is null
	 * 
	 * TODO Add support to check if the data provided is not sorted and throw an exception
	 * if it isn't 
	 * 
	 */
	public static int search(String value, String[] sortedData, Collator c)
			throws IllegalArgumentException
	{
		// TODO Add a statistical test instead of just checking the middle value perhaps?
		if (c.compare(sortedData[(sortedData.length/2)], 
				sortedData[(sortedData.length/2) +1]) > 0)
		{
			throw new IllegalArgumentException("The data provided MUST be sorted!");
		}
		// Error if the key is null 
		if (value == null)
		{
			throw new IllegalArgumentException("The value to search for cannot be NULL!");
		}
		
		return multiLangSearch(value, sortedData, c);
	}
	
	
	/*
	 * Searches for the value specified and returns the index where the first 
	 * instance of the value provided is found in the sorted array of data.
	 * 
	 * A custom variant of binary search is used, which is tailored to searching
	 * almost-sorted data, where the data is from multiple latin-based languages.
	 * 
	 * @param value The value to search for in the sorted data provided
	 * @param sortedData The sorted data to search through
	 * @param p The pattern to refine searching for the value within the data
	 * @param c	The comparator to use for performing comparisons 
	 * 
	 * @return The index of the first instance of the value in the data if found,
	 * -1 if the value is not found
	 * 
	 * @throws IllegalArgumentException If the data is not sorted or the value is null
	 * 
	 * TODO Add support to check if the data provided is not sorted and throw an exception
	 * if it isn't 
	 * 
	 */
	public static int search(String value, String[] sortedData, Pattern p, Collator c)
			throws IllegalArgumentException
	{
		// TODO Add a statistical test instead of just checking the middle value perhaps?
		if (c.compare(sortedData[(sortedData.length/2)], 
				sortedData[(sortedData.length/2) +1]) > 0)
		{
			throw new IllegalArgumentException("The data provided MUST be sorted!");
		}
		// Error if the key is null 
		if (value == null)
		{
			throw new IllegalArgumentException("The value to search for cannot be NULL!");
		}		
		
		return multiLangSearch(value, sortedData, p, c);
	}
	
	
	/*
	 * @see #search(String, String[], Collator)
	 * 
	 * @param value The value to search for in the sorted data provided
	 * @param sortedData The sorted data to search through
	 * @param p The pattern to refine searching for the value within the data
	 * @param c	The comparator to use for performing comparisons 
	 * 
	 * @return The index of the first instance of the value in the data if found,
	 * -1 if the value is not found
	 */
	private static int multiLangSearch(String value, String[] sortedData, Collator c)
	{
		return -1;
	}
	
	
	/*
	 * @see #search(String, String[], Pattern, Collator)
	 * 
	 * @param value The value to search for in the sorted data provided
	 * @param sortedData The sorted data to search through
	 * @param p The pattern to refine searching for the value within the data
	 * @param c	The comparator to use for performing comparisons 
	 * 
	 * @return The index of the first instance of the value in the data if found,
	 * -1 if the value is not found
	 */
	private static int multiLangSearch(String value, String[] sortedData, Pattern p, Collator c)
	{
		int low = 0, high = sortedData.length -1;
		int idx = 0;
		Matcher valueMatcher;
		String curValue = "";
		
		while (low <= high)
		{
			idx = (low + high) / 2;
			valueMatcher = p.matcher(sortedData[idx]);
			
			// TODO This should raise an exception if it cannot match the value, possible dictionary corruption
			// if this is used for searching the steganography dictionaries
			if (valueMatcher.find())
			{
				curValue = valueMatcher.group(1);
			}
			
			// Value is less than the current value in the data, decrease the upper bound
			if (c.compare(value, curValue) < 0)
			{
				high = idx - 1;
			}
			// Value is greater than current value in the data, increase lower bound
			else if (c.compare(value, curValue) > 0)
			{
				low = idx + 1;
			}
			// Value found, return the index of the value
			else
			{
				return idx;
			}
		}
		
		// Value not found using binary search, possibly because it is multi-language
		// and not sorted exactly, try finding the value using divergent search
		return divergentSearch(value, sortedData, idx, p, c);
	}
	
	
	/*
	 * 
	 * Performs a divergent search which searches for the value in opposite
	 * directions from the index given, the number of steps in each direction
	 * to take is determined by the constant DIVERGENT_SEARCH_STEPS.
	 *
	 * TODO There is probably some elegent way of determining the actual maximum
	 * value for step size to ensure that the value is always found
	 * 
	 * @param value The value to search for in the sorted data provided
	 * @param sortedData The sorted data to search through
	 * @param idx The index to start the divergent search from
	 * @param c	The comparator to use for performing comparisons 
	 * 
	 * @return The index of the first instance of the value in the data if found,
	 * -1 if the value is not found
	 */
	private static int divergentSearch(	String value, 
										String[] sortedData, 
										int idx,
										Collator c)
	{
		return -1;
	}
	
	
	/*
	 * 
	 * Performs a divergent search which searches for the value in opposite
	 * directions from the index given, the number of steps in each direction
	 * to take is determined by the constant DIVERGENT_SEARCH_STEPS.
	 *
	 * TODO There is probably some elegent way of determining the actual maximum
	 * value for step size to ensure that the value is always found
	 * 
	 * @param value The value to search for in the sorted data provided
	 * @param sortedData The sorted data to search through
	 * @param idx The index to start the divergent search from
	 * @param p The pattern to refine searching for the value within the data
	 * @param c	The comparator to use for performing comparisons 
	 * 
	 * @return The index of the first instance of the value in the data if found,
	 * -1 if the value is not found
	 */
	private static int divergentSearch(	String value, 
										String[] sortedData, 
										int idx,
										Pattern p, 
										Collator c)
	{
		int step;
		Matcher valueAboveMatcher, valueBelowMatcher;
		String curValueAbove = "", curValueBelow = "";
		
		for (step = 1; step <= DIVERGENT_SEARCH_STEPS; ++step)
		{
			// Search for the value steps above and below the index
			valueAboveMatcher = p.matcher(sortedData[idx + step]);
			valueBelowMatcher = p.matcher(sortedData[idx - step]);
			
			// TODO This should raise an exception if it cannot match the value, possible dictionary corruption
			// if this is used for searching the steganography dictionaries
			if (valueAboveMatcher.find() && valueBelowMatcher.find())
			{
				curValueAbove = valueAboveMatcher.group(1);
				curValueBelow = valueBelowMatcher.group(1);
			}
			
			// If the value is found above or below return the index
			if (c.compare(value, curValueAbove) == 0)
			{
				return idx + step;
			}
			if (c.compare(value, curValueBelow) == 0)
			{
				return idx - step;
			}
		}
		
		// The value was never found within the search steps
		return -1;
	}
}