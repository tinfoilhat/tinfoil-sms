/** 
 * Copyright (C) 2013 Jonathan Gillett, Joseph Heron
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
