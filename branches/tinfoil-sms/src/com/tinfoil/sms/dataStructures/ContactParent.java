/** 
 * Copyright (C) 2011 Tinfoilhat
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

package com.tinfoil.sms.dataStructures;

import java.util.ArrayList;

public class ContactParent {

	private String name;
	private ArrayList<ContactChild> numbers;
	
	/**
	 * Create a ContactParent which holds relevant information for displaying
	 * the contacts in ManageContactsActivity.
	 * @param name The name of the contact
	 * @param numbers The list of COntactChild containing relevant information
	 * about the numbers owned by the contact.
	 */
	public ContactParent(String name, ArrayList<ContactChild> numbers)
	{
		this.setName(name);
		this.numbers = numbers;
	}

	/**
	 * Whether the contact is trusted or not. To be trusted the contact must
	 * have at least 1 trusted number.
	 * @return True if at least 1 number is trusted, otherwise false
	 */
	public boolean isTrusted() {
		for(int i = 0; i < numbers.size();i++)
		{
			if(numbers.get(i).isTrusted())
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the name
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name
	 * @param name The new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the list of numbers
	 * @return The list of numbers
	 */
	public ArrayList<ContactChild> getNumbers() {
		return numbers;
	}
	
	/**
	 * Get a number
	 * @param index The index of the number that is desired.
	 * @return The number at the given index
	 */
	public ContactChild getNumber(int index)
	{
		return numbers.get(index);
	}

	/**
	 * Set the list of Numbers
	 * @param numbers The list of numbers
	 */
	public void setNumbers(ArrayList<ContactChild> numbers) {
		this.numbers = numbers;
	}
}
