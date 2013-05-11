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
package com.tinfoilsms.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.junit.contrib.assumes.Assumes;
import org.junit.contrib.assumes.Corollaries;
import org.junit.runner.RunWith;
import org.spongycastle.util.encoders.Hex;

import com.tinfoilsms.encoding.Ascii85;


@RunWith(Corollaries.class)
public class Ascii85Test
{
    private static final String message = 
            "Man is distinguished, not only by his reason, but by this singular passion from " 
            + "other animals, which is a lust of the mind, that by a perseverance of delight in the "
            + "continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.";
        
    private static final String encoded = 
            "9jqo^BlbD-BleB1DJ+*+F(f,q/0JhKF<GL>Cj@.4Gp$d7F!,L7@<6@)/0JDEF<G%<+EV:2F!,O<DJ+*.@<*K0@<"
            + "6L(Df-\\0Ec5e;DffZ(EZee.Bl.9pF\"AGXBPCsi+DGm>@3BB/F*&OCAfu2/AKYi(DIb:@FD,*)+C]U=@3BN#Ec"
            + "Yf8ATD3s@q?d$AftVqCh[NqF<G:8+EV:.+Cf>-FD5W8ARlolDIal(DId<j@<?3r@:F%a+D58'ATD4$Bl@l3De:"
            + ",-DJs`8ARoFb/0JMK@qB4^F!,R<AKZ&-DfTqBG%G>uD.RTpAKYo'+CT/5+Cei#DII?(E,9)oF*2M7/c";
            

    private static final byte[] encrypted = Hex.decode("bdf866d92817d30746a1ed5a39c0d6fe63f65fa89b9a251f6fd66205b4e23e8924f4a6");
    

    /**
     * Test method for {@link com.tinfoilsms.encoding.Ascii85#encode(byte[])}.
     */
    @Test
    public void testEncode()
    {
        System.out.println("\nEncoded message (bytes): ");
        System.out.println(new String(Ascii85.encode(message.getBytes())));
        
        assertTrue(Arrays.equals(encoded.getBytes(), Ascii85.encode(message.getBytes())));
    }

    
    /**
     * Test method for {@link com.tinfoilsms.encoding.Ascii85#encodeToString(byte[])}.
     */
    @Test
    public void testEncodeToString()
    {
        System.out.println("\nEncoded message (String): ");
        System.out.println(Ascii85.encodeToString(message.getBytes()));
        
        assertEquals(encoded, Ascii85.encodeToString(message.getBytes()));
    }

    
    /**
     * Test method for {@link com.tinfoilsms.encoding.Ascii85#decode(byte[])}.
     */
    @Test
    @Assumes("testEncode")
    public void testDecodeByteArray()
    {
        byte[] encodedBytes = Ascii85.encode(message.getBytes());
        
        System.out.println("\nDecoded message (bytes): ");
        System.out.println(new String(Ascii85.decode(encodedBytes)));
        
        assertTrue(Arrays.equals(message.getBytes(), Ascii85.decode(encodedBytes)));
    }

    
    /**
     * Test method for {@link com.tinfoilsms.encoding.Ascii85#decode(java.lang.String)}.
     */
    @Test
    @Assumes("testEncodeToString")
    public void testDecodeString()
    {
        String encodedBytes = Ascii85.encodeToString(message.getBytes());
        
        System.out.println("\nDecoded message (String): ");
        System.out.println(new String(Ascii85.decode(encodedBytes)));
        
        assertTrue(Arrays.equals(message.getBytes(), Ascii85.decode(encodedBytes)));
    }
    
    
    /**
     * Test which tests the Ascii85 encode/decode methods using a byte array of 
     * encrypted data.
     */
    @Test
    @Assumes({"testEncode", "testDecodeByteArray"})
    public void testEncodeDecodeEncrypted()
    {
        byte[] encodedBytes = Ascii85.encode(encrypted);
        
        System.out.println("\nEncrypted: "+ new String(Hex.encode(encrypted)));
        System.out.println("\nEncoded Encrypted: " + new String(encodedBytes));
        System.out.println("\nDecoded Encrypted: " + new String(Hex.encode(Ascii85.decode(encodedBytes))));
        
        assertTrue(Arrays.equals(encrypted, Ascii85.decode(encodedBytes)));
    }
}
