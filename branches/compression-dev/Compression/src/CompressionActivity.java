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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.Collator;
import java.util.Locale;


public class CompressionActivity
{

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
	 * Performs a Burrows Wheeler Transform (BWT) test on the array of strings
	 * provided by verifying that the transformed strings and then inverse
	 * transformed string back to the original string matches the original
	 * string. Basically verifies that the forward and inverse BWT works for
	 * all strings provided.
	 * @param messages An array of strings to be used for the test
	 * @param collator A collator used to fine-tune the string comparison
	 */
	public static void BWTTest(String[] messages, Collator collator)
	{
		boolean messageMatch = true;
		
		for (String message : messages)
		{
			byte[] original = message.getBytes();
			byte[] fwdTransform = new byte[original.length];
			byte[] invTransform = new byte[original.length];
			BWT bwt1 = new BWT();
			
			// Perform the forward transform
			fwdTransform = bwt1.forward(original, 0);
			
			// Perform the inverse transform back to the original message,
			// note you need to set the primary index to perform the inverse
			BWT bwt2  = new BWT();
			invTransform = bwt2.inverse(fwdTransform, 0);
			
			// Verify that the BWT worked and that the final inverse transform's
			// output matches the original message
			if (collator.compare(new String(original), new String(invTransform)) > 0)
			{
				System.out.println("BWT Failed! The original string: " + new String(original)
				+ "\n\tand the inverse transformed string: " + new String(invTransform)
				+ " DO NOT MATCH!");
				messageMatch = false;
				break;
			}
			// Display information about each transform
			else
			{
		        System.out.println("Original string:\t\t" + new String(original));            
		        System.out.println("Transformed string:\t\t" + new String(fwdTransform));
		        System.out.println("Inverse transformed string:\t" + new String(invTransform));
		        // The primary index is the last byte of the transformed string
		        System.out.println("Primary index:\t\t\t" + (int) (fwdTransform[fwdTransform.length -1] & 0xFF));
			}
		}
		
		if (messageMatch)
		{
			System.out.println("\nBWT TEST PASSED!");
		}
		else
		{
			System.out.println("\nBWT TEST FAILED, SOME STRINGS NOT TRANSFORMED BACK PROPERLY!");
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		
		// Get a collection of text messages to use as a test
		String messages[] = readLines("sms_messages.txt");
		
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
		 * Perform a test of the Burrows Wheeler Transform (BWT) on an array of strings,
		 * using a strict collator to handle non-ascii characters and to ensure that the
		 * original and final strings are IDENTICAL
		 */
		BWTTest(messages, strictCollator);
	}
}
