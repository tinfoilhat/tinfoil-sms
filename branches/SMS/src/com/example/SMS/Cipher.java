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
