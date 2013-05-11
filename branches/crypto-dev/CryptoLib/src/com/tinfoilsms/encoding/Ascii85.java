/** 
 * Copyright (C) 2013 Jonathan Gillett, Joseph Heron
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
package com.tinfoilsms.encoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


/**
 * Provides support for encoding and decoding using the ASCII85 (Base85) encoding
 * scheme, which encodes date using radix-85 to encode data as 85 of the possible
 * ASCII printable characters. The main advantages of Base85 to Base64 is that it
 * results in less overhead encoding by every 4 bytes into 5 bytes (4/5) in
 * comparison to Base64 which encodes every 3 bytes into 4 bytes (3/4).
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Binary-to-text_encoding"></a>
 * @see <a href="http://en.wikipedia.org/wiki/Ascii85"></a>
 */
public abstract class Ascii85
{
    private static final String CHARSET = "ascii";
    
    
    public static void main(String[] args)
    {
        String message = "Man is distinguished, not only by his reason, but by this singular passion from " 
                + "other animals, which is a lust of the mind, that by a perseverance of delight in the "
                + "continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.";
        
        byte[] encodedBytes = encode(message.getBytes());
        String encodedStr = encodeToString(message.getBytes());

        System.out.println("Encoded message (bytes): ");
        System.out.println(new String(encodedBytes));
        
        System.out.println("\nEncoded message (String): ");
        System.out.println(encodedStr);
        
        System.out.println("\nDecoded message (bytes): ");
        System.out.println(new String(decode(encodedBytes)));
        
        System.out.println("\nDecoded message (String): ");
        System.out.println(new String(decode(encodedStr)));
    }
    
    
    
    /**
     * Encodes input data in bytes into Ascii85 encoded data, and
     * returns the encoded data in bytes. The Ascii85 information can
     * easily be transmitted and stored, similarly to Bas64 encoded
     * data.
     * 
     * @param input The input to encode as Ascii85 in bytes
     * @return A byte array of the encoded data
     */
    public static byte[] encode(byte[] input)
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Ascii85OutputStream ascii85 = new Ascii85OutputStream(buffer);
        
        try
        {
            ascii85.write(input);
            ascii85.flush();
            ascii85.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return removeIdentifiers(buffer.toByteArray());
    }
    
    
    /**
     * Encodes input data in bytes into Ascii85 encoded data, and
     * returns the encoded data in bytes. The Ascii85 information can
     * easily be transmitted and stored, similarly to Bas64 encoded
     * data.
     * 
     * @param input The input to encode as Ascii85 in bytes
     * @return A String representation of the encoded data
     */
    public static String encodeToString(byte[] input)
    {
        return new String(encode(input));
    }
    
    
    /**
     * Decodes the Ascii85 encoded input into bytes, and returns the 
     * original data in bytes.
     * 
     * @param input The encode as Ascii85 data in bytes
     * @return A byte array of the original decoded data
     */
    public static byte[] decode(byte[] input)
    {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(addIdentifiers(input));
        Ascii85InputStream ascii85 = new Ascii85InputStream(inputStream);
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        
        try
        {
            int b = ascii85.read();
            
            while (b != -1000)
            {
                bytes.add(new Byte((byte)b));
                b = ascii85.read();
            }
            ascii85.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        /* Convert the Ascii85 Byte representation to byte */
        byte[] output = new byte[bytes.size()];
        for (int i = 0; i < output.length; ++i)
        {
            output[i] = bytes.get(i).byteValue();
        }
        
        return output;
    }
    
    
    /**
     * Decodes the Ascii85 encoded input as a String, and returns the 
     * original data in bytes.
     * 
     * @param input The encode as Ascii85 data in bytes
     * @return A byte array of the original decoded data
     */
    public static byte[] decode(String input)
    {
        ByteArrayInputStream inputStream = null;
        Ascii85InputStream ascii85 = null;
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        
        /* Initialize the streams used */
        try
        {
            inputStream = new ByteArrayInputStream(addIdentifiers(input).getBytes(CHARSET));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
  
        ascii85 = new Ascii85InputStream(inputStream);
        
        try
        {
            int b = ascii85.read();
            
            while (b != -1000)
            {
                bytes.add(new Byte((byte)b));
                b = ascii85.read();
            }
            ascii85.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        /* Convert the Ascii85 Byte representation to byte */
        byte[] output = new byte[bytes.size()];
        for (int i = 0; i < output.length; ++i)
        {
            output[i] = bytes.get(i).byteValue();
        }
        
        return output;
    }
    
    
    /**
     * Adds back the redundant <~ and ~>, which are part of the Ascii85
     * encoding scheme so that it can be properly decoded.
     * 
     * @param input The Ascii85 input to add the redundant encoding to
     * @return The input with the redundant encoding added back
     */
    private static byte[] addIdentifiers(byte[] input)
    {
        byte[] output = new byte[input.length + 4];
        output[0] = (byte) '<';
        output[1] = (byte) '~';
        
        System.arraycopy(input, 0, output, 2, input.length);
        
        /* Add the ending encoding, arraycopy appears to null terminate remaining length */
        output[input.length] = (byte) '~';
        output[input.length + 1] = (byte) '>';

        System.out.println("\nDEBUG: ");
        System.out.println(new String(output));
        return output;
    }
    
    
    /**
     * Adds back the redundant <~ and ~>, which are part of the Ascii85
     * encoding scheme so that it can be properly decoded.
     * 
     * @param input The Ascii85 input to add the redundant encoding to
     * @return The input with the redundant encoding added back
     */
    private static String addIdentifiers(String input)
    {
        return new String(addIdentifiers(input.getBytes()));        
    }
    
    
    /**
     * Removes the redundant <~ and ~>, which are part of the Ascii85
     * encoding scheme, I know this breaks the standard, but to hell with
     * standards!
     * 
     * @param input The Ascii85 input to remove the redundant encoding from
     * @return The input with the redundant encoding removed
     */
    private static byte[] removeIdentifiers(byte[] input)
    {
        byte[] output = new byte[input.length - 4];
        
        System.arraycopy(input, 2, output, 0, input.length - 6);
        return output;
    }
}
