package com.tinfoil.sms.dataStructures;

import java.util.EnumMap;
import java.util.Map;

import com.tinfoil.sms.utility.Walkthrough.Step;

public class WalkthroughStep
{
    private Map<Step, Boolean> steps;
	
	public WalkthroughStep()
	{
	    steps =  new EnumMap<Step, Boolean>(Step.class);
	    
	    steps.put(Step.INTRO, false);
	    steps.put(Step.START_IMPORT, false);
	    steps.put(Step.IMPORT, false);
	    steps.put(Step.START_EXCHANGE, false);
	    steps.put(Step.SET_SECRET, false);
	    steps.put(Step.KEY_SENT, false);
	    steps.put(Step.PENDING, false);
	    steps.put(Step.ACCEPT, false);
	    steps.put(Step.SUCCESS, false);
	    steps.put(Step.CLOSE, false);
	}
	
	public WalkthroughStep(Boolean setOne)
	{
	    steps =  new EnumMap<Step, Boolean>(Step.class);
	    
	    steps.put(Step.INTRO, setOne);
	    steps.put(Step.START_IMPORT, setOne);
	    steps.put(Step.IMPORT, setOne);
	    steps.put(Step.START_EXCHANGE, setOne);
	    steps.put(Step.SET_SECRET, setOne);
	    steps.put(Step.KEY_SENT, setOne);
	    steps.put(Step.PENDING, setOne);
	    steps.put(Step.ACCEPT, setOne);
	    steps.put(Step.SUCCESS, setOne);
	    steps.put(Step.CLOSE, setOne);
	}
	
	/**
	 * Gets whether the specified walkthrough step has been viewed
	 * @param step The walkthrough step
	 * @return True if already viewed
	 */
	public Boolean get(Step step)
	{
	    return steps.get(step);
	}
	
	/**
	 * Sets the specified walkthrough step as viewed or not viewed
	 * @param step The walkthrough steps
	 * @param vale The value, true for viewed, false if not viewed
	 */
	public void set(Step step, boolean value)
	{
	    steps.put(step, value);
	}
	
	/**
     * Sets the specified walkthrough step as viewed or not viewed
     * @param step The walkthrough steps
     * @param vale The value, true for viewed, false if not viewed
     */
    public void set(Step step, int value)
    {
        if (value == 0)
        {
            steps.put(step, false);
        }
        else
        {
            steps.put(step, true);
        }
    }
    
    /**
     * Sets all steps in the walkthrough to the specified value.
     * @param value The value, true for viewed, false if not viewed
     */
    public void setAll(boolean value)
    {
        for (Step step : steps.keySet())
        {
            steps.put(step, value);
        }
    }
}
