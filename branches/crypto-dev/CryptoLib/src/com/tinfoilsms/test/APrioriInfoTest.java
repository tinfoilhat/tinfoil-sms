package com.tinfoilsms.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.spongycastle.crypto.DataLengthException;

import com.tinfoilsms.crypto.APrioriInfo;

public class APrioriInfoTest
{
	private APrioriInfo priorInfo;
	private String expectedS1;
	private String expectedS2;
	
	@Before
	public void setUp() throws Exception
	{
		/* Set the default S1 and S2 as defined by the ECG protocol */
		expectedS1 = "initiator";
		expectedS2 = "recipient";
	}

	/**
	 *  Test that the APrioriInfo object created with the default S1 & S2 shared info
	 */
	@Test
	public void defaultSharedInfo()
	{
		priorInfo = new APrioriInfo("initiator", "recipient");
		assertTrue(expectedS1.equals(new String(priorInfo.getS1())));
		assertTrue(expectedS2.equals(new String(priorInfo.getS2())));
	}
	
	
	/**
	 * Test that it throws an exception if no shared info S1 & S2 provided
	 */
	@Test(expected=DataLengthException.class)
	public void noSharedInfo()
	{
		priorInfo = new APrioriInfo("", "");
	}
	
	
	/**
	 * Test that it throws an exception if only one shared info S1/S2 provided
	 */
	@Test(expected=DataLengthException.class)
	public void oneSharedInfo()
	{
		priorInfo = new APrioriInfo("initiator", "");
	}
}
