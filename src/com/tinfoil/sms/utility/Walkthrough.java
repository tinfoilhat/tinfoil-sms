package com.tinfoil.sms.utility;

public abstract class Walkthrough
{
    /* Enumeration of tutorial steps */
    public static enum Type 
    {
        INTRO,
        START_IMPORT,
        IMPORT,
        START_EXCHANGE,
        SET_SECRET,
        KEY_SENT,
        PENDING,
        ACCEPT,
        SUCCESS,
        CLOSE
    }
}
