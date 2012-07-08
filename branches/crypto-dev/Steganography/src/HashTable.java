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

import java.math.BigInteger;

/**
 * Provides the basic functionality for generating hash tables efficiently
 * using double hashing
 */
public abstract class HashTable
{
	/**
	 *  Constant value used in determining the interval for double hashing
	 *  it MUST be a prime number to minimize collision
	 */
	private static final int CONSTANT_INTERVAL = 5;
	
	
	/**
	 * Returns the initial probe value for generating the hash table given the
	 * key
	 * 
	 * NOTE: The size of the bucket being probed should be a prime number
	 * to ensure that the modulus for the probe and constant interval
	 * are co-prime
	 * 
	 * @param key The key to be hashed
	 * @param bucketSize The size of the bucket(s) being probed, should
	 * be prime number to ensure maximum efficiency
	 * 
	 * @return Returns a probe
	 */
	public static int getProbe(int key, int bucketSize)
	{
		// Avoid negative values as java does negative modulus wrong... fucking hack
		return (key < 0) ? (bucketSize - (Math.abs(key) % bucketSize)) : (key % bucketSize);
	}
	public static BigInteger getProbe(BigInteger key, int bucketSize)
	{
		return key.mod(BigInteger.valueOf(bucketSize));
	}
	
	/**
	 * Returns the interval for incrementing the probe
	 */
	public static int getInterval(int key)
	{
		// Avoid negative values as java does negative modulus wrong... fucking hack
		return (key < 0) ? (CONSTANT_INTERVAL - (((key % CONSTANT_INTERVAL) + CONSTANT_INTERVAL) % CONSTANT_INTERVAL))
				: (CONSTANT_INTERVAL - (key % CONSTANT_INTERVAL));
	}
	public static BigInteger getInterval(BigInteger key)
	{
		// One-liner... fucking hack
		return BigInteger.valueOf(CONSTANT_INTERVAL).subtract(key.mod(BigInteger.valueOf(CONSTANT_INTERVAL)));
	}
	
	
	/**
	 * Returns the nearest co-prime value for the size of the bucket(s), this
	 * is due to a fact of number theory where the number of collisions is
	 * minimized only if both the constant interval and the bucket size are
	 * both prime. The two values are only guaranteed to be relatively prime
	 * if they are BOTH prime.
	 * 
	 * @see http://stackoverflow.com/questions/1145217/why-should-hash-functions-use-a-prime-number-modulus
	 * @see http://mathworld.wolfram.com/RelativelyPrime.html
	 * 
	 * NOTE: If the two bucket is already co-prime with the constant interval
	 * then the size of the bucket is returned.
	 * 
	 * NOTE: By using the nearest prime value of the bucket to ensure that it
	 * is co-prime the number of values selected is less than that of the
	 * original bucket.
	 * 
	 * @param bucketSize The size of the bucket(s)
	 * @returns The bucketSize if it is already co-prime with the constant
	 * interval, or the nearest prime number.  
	 */
	public static int getCoPrime(int bucketSize)
	{
		int a = CONSTANT_INTERVAL;
		int b = bucketSize;
		int c = 0;
		int newBucketSize = 0;
		
		// Quickly apply Euclid's Greatest Common Divisor algorithm, if
		while (b != 0)
		{
			c = b;
			b = a % b;
			a = c;
		}
		
		// If a is 1 then the bucketSize is co-prime with constant interval
		if (b == 1)
		{
			return bucketSize;
		}
		
		/*
		 * Otherwise return the nearest prime number as the co-prime, search for
		 * the next nearest prime incrementally until one is found
		 */
		for (int i = 1; i <= (int)Math.ceil(Math.sqrt(bucketSize)); ++i)
		{
			/*
			 * Use the miller-rabin test to determine if the new bucketSize is a
			 * prime number, return it if it is a prime number
			 * 
			 * @see http://en.wikipedia.org/wiki/Miller%E2%80%93Rabin_primality_test
			 */
			if (MillerRabin32.miller_rabin_32(bucketSize - i))
			{
				newBucketSize = bucketSize - i;
				break;
			}
		}
		return newBucketSize;
	}
}
