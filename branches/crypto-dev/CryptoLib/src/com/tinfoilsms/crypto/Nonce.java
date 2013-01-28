/** 
 * Copyright (C) 2013 Tinfoilhat
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

import java.util.Arrays;

import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.prng.RandomGenerator;
import org.spongycastle.crypto.MaxBytesExceededException;

import com.tinfoilsms.csprng.ISAACRandomGenerator;


/**
 * Provides support for generating numbers used once (nonce) which can be used as
 * IVs for block cipher modes of operation such as CBC or CTR. The class uses a
 * deterministic CSPRNG such as ISAAC to generate pseudo-random nonces which can
 * then be used as IVs.
 * 
 * This class functions as an external iterator generating a unique nonce each
 * time the next operation is performed. Additionally, this class can also be
 * initialized to any prior state given that the deterministic CSPRNG is provided
 * with the same seed value it had prior and the IV of the last state.
 */
public class Nonce implements CipherParameters
{
	/* CSPRNG and current nonce */
	private ISAACRandomGenerator csprng;
	private byte[] nonce;
	
	/* The maximum number of nonces that can be generated before the CSPRNG needs
	 * to be re-seeded or a CSPRNG with a new seed must be used for the Nonce.
	 */
	private static final int MAXCYCLES = 100000;
	private int cycle;
	
	
	/**
	 * Construct the nonce with a deterministic CSPRNG, at the moment only ISAAC
	 * engine is supported, but in the future other CSPRNG such as ISAAC+ will 
	 * also be supported.
	 * 
	 * Note, as the CSPRNG is deterministic if you have already used the CSPRNG and
	 * initialized the Nonce object with the same seed value to generate nonces 
	 * previously, you must create this object using the last nonce state in order
	 * to prevent reusing nonces/IVs, which is a security vulnerability.
	 *  
	 * @param csprng Deterministic CSPRNG, currently only ISAAC is supported
	 * @throws IllegalArgumentException if the CSPRNG is not ISAAC
	 */
	public Nonce(RandomGenerator csprng)
		throws IllegalArgumentException
	{
		if (! (csprng instanceof ISAACRandomGenerator))
		{
			throw new IllegalArgumentException("Invalid CSPRNG, at the moment ONLY ISAAC is supported!");
		}
		
		this.csprng = (ISAACRandomGenerator)csprng;			
	}

	
	/**
	 * Construct the nonce with a deterministic CSPRNG, at the moment only ISAAC
	 * engine is supported, but in the future other CSPRNG such as ISAAC+ will 
	 * also be supported.
	 * 
	 * Note, as the CSPRNG is deterministic if you have already used the CSPRNG and
	 * initialized the Nonce object with the same seed value to generate nonces 
	 * previously, you must create this object using the last nonce state in order
	 * to prevent reusing nonces/IVs, which is a security vulnerability.
	 *  
	 * @param csprng Deterministic CSPRNG, currently only ISAAC is supported
	 * @param lastNonce The last nonce generated during an existing state of the
	 * Nonce object given the current CSPRNG, providing the last nonce state prevents
	 * security vulnerabilities of re-using nonce/IVs.
	 * 
	 * @throws IllegalArgumentException if the CSPRNG is not ISAAC
	 */
	public Nonce(RandomGenerator csprng, byte[] lastNonce)
		throws IllegalArgumentException
	{
		if (! (csprng instanceof ISAACRandomGenerator))
		{
			throw new IllegalArgumentException("Invalid CSPRNG, at the moment ONLY ISAAC is supported!");
		}
		
		this.csprng = (ISAACRandomGenerator)csprng;
		this.nonce = lastNonce;
	}
	
	
	/**
	 * Initialize the CSPRNG used by the nonce with the seed value provided so
	 * that it is deterministic in generating random data.
	 * 
	 * Again, because the CSPRNG is deterministic if you have already used the 
	 * CSPRNG and initialized the Nonce object with the same seed value to 
	 * generate nonces previously, you must create this object using the last 
	 * nonce state in order to prevent reusing nonces/IVs, which is a security 
	 * vulnerability.
	 * 
	 * Lastly, if you initialize this object with incorrect seed value needed
	 * to generate the last nonce state the CSPRNG will eventually exhaust
	 * generating random numbers when it reaches MAXCYCLES and throw an
	 * exception, this prevents having to deal with the halting problem, where
	 * the program will run indefinitely generating random data.
	 * 
	 * @param seed The seed value for the CSPRNG, must be specified
	 * 
	 * @throws MaxBytesExceededException If CSPRNG reaches MAXCYCLES trying to
	 * return to the initial Nonce state, usually when the seed data provided is
	 * different than the previous seed data used to generate the last nonce state
	 */
	public void init(byte[] seed)
		throws MaxBytesExceededException
	{
		/* Initialize CSPRNG */
		csprng.init(seed);
		
		/* if nonce state specified initialize nonce to last state */
		if (nonce != null)
		{
			byte[] curNonce = new byte[nonce.length];
			
			for (cycle = 1; cycle <= MAXCYCLES; ++cycle)
			{
				csprng.nextBytes(curNonce);
				
				if (Arrays.equals(nonce, curNonce))
				{
					System.arraycopy(curNonce, 0, nonce, 0, curNonce.length);
					break;
				}
			}
			
			/* Exception thrown if MAXCYCLES reached */
			if (cycle == MAXCYCLES)
			{
				throw new MaxBytesExceededException("Max cycles reached trying to return" +
						"nonce to previous state!");
			}
		}
	}
	
	
	/**
	 * Generates the next nonce which can be accessed using the getNonce()
	 * method.
	 */
	public void nextNonce()
	{
		csprng.nextBytes(nonce);
	}
	
	
	/**
	 * Accesses the current nonce, you should always execute the nextNonce()
	 * method before accessing a nonce to ensure that a uniqe nonce has been
	 * generated.
	 * 
	 * @return The nonce
	 */
	public byte[] getNonce()
	{
		return nonce;
	}
	
	
	/**
	 * Re-seeds the CSPRNG with the seed value specified, if your are using
	 * this Nonce in a cryptographic system with more than one user/system 
	 * all systems must re-seed at the same state with the same seed value
	 * in order to have the same sequence of nonces. Furthermore, if you
	 * re-create this Nonce object, you must initialize the object with
	 * this seed value to return to this state!
	 * 
	 * @param seed The seed value for the CSPRNG, must be specified
	 */
	public void reSeed(byte[] seed)
	{
		csprng.addSeedMaterial(seed);
	}
	
	
	/**
	 * Resets the CSPRNG
	 * 
	 * TODO: IMPLEMENT, must also be implemented in ISAACRandomGenerator
	 */
	public void reset()
	{
	}
}
