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
package com.tinfoilsms.crypto;

import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.DerivationParameters;

 /**
 * Seed Derivative Function (SDF) parameter, takes the shared information 
 * S1 & S2 as constructor arguments which are used as parameters for 
 * deriving the shared seed
 */
public class SDFParameters implements DerivationParameters
{

    byte[]  S1;
    byte[]  S2;
	
	/**
	 * Defines the two parameters for the SDF which are the shared
	 * information S1 & S2, the values cannot be empty
	 * 
	 * @param S1 Shared information S1
	 * @param S2 Shared information S2
	 * 
	 * @throws DataLengthException if shared information is empty
	 */
	public SDFParameters(String S1, String S2)
			throws DataLengthException
	{
		if (S1.length() == 0 || S2.length() == 0)
		{
		    throw new DataLengthException("You must specify a value for the shared information!");
		}
		
		this.S1 = S1.getBytes();
		this.S2 = S2.getBytes();
	}


	/**
	 * Gets the shared information S1
	 * @return shared information, S1
	 */
    public byte[] getS1()
    {
        return S1;
    }
    
	/**
	 * Gets the shared information S2
	 * @return shared information, S2
	 */
    public byte[] getS2()
    {
        return S2;
    }
}
