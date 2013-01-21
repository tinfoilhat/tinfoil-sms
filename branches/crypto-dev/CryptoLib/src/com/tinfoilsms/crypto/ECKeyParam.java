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

import java.math.BigInteger;
import java.security.spec.KeySpec;
import java.security.InvalidParameterException;

import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;


/**
 * A class which acts as a wrapper for the Elliptic Curve specifications
 * (ECParamSpec) and domain parameters (ECDomainParameters) making it
 * easier to simply define one object that can be used to specify the
 * parameters of the Elliptic Curve Asymmetric keys.
 */
public class ECKeyParam implements CipherParameters, KeySpec
{	
	/**
	 * The default named curve if none specified, the default curve is SEC
	 * secp256r1, which is also referred to as P-256 by NIST. For a list
	 * of supported curves and more informations on named curves see the
	 * following.
	 * 
	 * @see http://www.secg.org/collateral/sec2_final.pdf
	 * @see http://www.bouncycastle.org/wiki/display/JA1/Supported+Curves+%28ECDSA+and+ECGOST%29
	 */
	private static final String defNamedCurve = "secp256r1";
	private ECParameterSpec ECParamSpec;
	private ECDomainParameters ECDomainParam;

	/**
	 * The default constructor, creates an instance of ECKeyParam object using the
	 * default named curve which at the present moment is secp256r1.
	 */
	public ECKeyParam()
	{
		/* Instantiate the ECParameterSpec and ECDomainParameters objects */
		this.ECParamSpec = ECNamedCurveTable.getParameterSpec(defNamedCurve);
		this.ECDomainParam = new ECDomainParameters(
				ECParamSpec.getCurve(),		// Curve
				ECParamSpec.getG(),			// G
				ECParamSpec.getN());		// N	
	}

	/**
	 * Creates an instance of the ECKeyParam object with using the named curve
	 * specified for the elliptic curve.
	 * 
	 * For a list of supported curves and more informations on named curves see the
	 * following.
	 *
	 * @see http://www.secg.org/collateral/sec2_final.pdf
	 * @see http://www.bouncycastle.org/wiki/display/JA1/Supported+Curves+%28ECDSA+and+ECGOST%29
	 * 
	 * @param namedCurve The named elliptic curve to use
	 * 
	 * @throws InvalidParameterException if the named curve provided is not supported
	 */
	public ECKeyParam(String namedCurve) throws InvalidParameterException
	{
		/* Instantiate the ECParameterSpec and ECDomainParameters objects */
		this.ECParamSpec = ECNamedCurveTable.getParameterSpec(namedCurve);
		
		if (this.ECParamSpec == null)
		{
			throw new InvalidParameterException("Invalid named elliptic curve provided!");
		}
		
		this.ECDomainParam = new ECDomainParameters(
				ECParamSpec.getCurve(),		// Curve
				ECParamSpec.getG(),			// G
				ECParamSpec.getN());		// N		
	}
	
	/**
	 * @return The object's ECParameterSpec
	 */
	public ECParameterSpec getECParamSpec()
	{
		return ECParamSpec;
	}
	
	/**
	 * @return The object's ECDomainParameters
	 */
	public ECDomainParameters getECDomainParam()
	{
		return ECDomainParam;
	}
	
	/**
	 * Wrapper for getCurve()
	 * @return the curve along which the base point lies.
	 */
	public ECCurve getCurve()
	{
		return ECDomainParam.getCurve();
	}
	
	/**
	 * Wrapper for getG()
	 * @return the base point we are using for these domain parameters.
	 */
	public ECPoint getG()
	{
		return ECDomainParam.getG();
	}
	
	/**
	 * Wrapper for getH()
	 * @return the cofactor H to the order of G.
	 */
	public BigInteger getH()
	{
		return ECDomainParam.getH();
	}
	
	/**
	 * Wrapper for getN()
	 * @return the order N of G
	 */
	public BigInteger getN()
	{
		return ECDomainParam.getN();
	}
	
	/**
	 * Wrapper for getSeed()
	 * @return the seed used to generate this curve (if available).
	 */
	public byte[] getSeed()
	{
		return ECDomainParam.getSeed();
	}
}