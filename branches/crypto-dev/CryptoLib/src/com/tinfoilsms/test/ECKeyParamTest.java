package com.tinfoilsms.test;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;

import org.junit.Before;
import org.junit.Test;

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.spec.ECParameterSpec;

import com.tinfoilsms.crypto.ECKeyParam;

public class ECKeyParamTest
{
	private ECParameterSpec expectedCurve;
	private ECKeyParam keyParam;
	
	@Before
	public void setUp() throws Exception
	{
		expectedCurve = ECNamedCurveTable.getParameterSpec("secp256r1");
	}

	/**
	 * Test that the correct curve is being created by the default
	 * constructor.
	 * 
	 * NOTE: If you changed the defNamedCurve constant in the class then you must
	 * update the setUp() for this test to match the named curve.
	 */
	@Test
	public void defNamedCurve()
	{
		keyParam = new ECKeyParam();
		assertTrue(expectedCurve.getCurve().equals(keyParam.getCurve()));
		assertTrue(expectedCurve.getG().equals(keyParam.getG()));
		assertTrue(expectedCurve.getN().equals(keyParam.getN()));
	}
	
	
	/**
	 * Test that an exception is thrown if an invalid curve name is provided
	 */
	@Test(expected=InvalidParameterException.class)
	public void invalidNamedCurve()
	{
		keyParam = new ECKeyParam("derp");
	}
}