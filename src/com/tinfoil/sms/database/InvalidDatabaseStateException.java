package com.tinfoil.sms.database;

public class InvalidDatabaseStateException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5051327928528504697L;

	/**
	 * Thrown when the database is in a state that is illegal. An example
	 * would be the public and private key of the user no longer existing
	 * which cannot happen unless the phone's memory is deleted while
	 * the app is running or more serious issues.
	 * @param message The Error message
	 */
	public InvalidDatabaseStateException(String message){
	     super(message);
	}

}
