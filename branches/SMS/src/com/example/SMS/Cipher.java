/** 
 * Copyright (C) 2011 Tinfoilhat
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


package com.example.SMS;

/*
 * A simple class that implements a ROT13 cipher using a number
 */
abstract class Cipher
{
	/*
	 * Function which returns rot13 encrypted ciphertext
	 * @param plaintext The plaintext to be encrypted
	 */
	public static String rot13(String plaintext)
	{
		String ciphertext = "";
		
		// Shift each character by 13
        for (int i = 0; i < plaintext.length(); ++i)
        {
            char c = plaintext.charAt(i);
            if       (c >= 'a' && c <= 'm') c += 13;
            else if  (c >= 'n' && c <= 'z') c -= 13;
            else if  (c >= 'A' && c <= 'M') c += 13;
            else if  (c >= 'A' && c <= 'Z') c -= 13;
            ciphertext += c;
        }
        return ciphertext;
	}
}
