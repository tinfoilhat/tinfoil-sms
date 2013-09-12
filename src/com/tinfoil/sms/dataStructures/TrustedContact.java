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

package com.tinfoil.sms.dataStructures;

import java.util.ArrayList;

import com.tinfoil.sms.utility.SMSUtility;

/**
 * A class for storing information retrieved or to be stored in the database.
 * 
 * Through out tinfoil-sms TrustedContact is used to describe the class where
 * Trusted Contact is used to describe a contact that a key exchange has 
 * successfully taken place and secure messages are sent and expected.
 * A TrustedContact != Trusted Contact but a TrustedContact can be a
 * Trusted Contact, if key != null
 */
public class TrustedContact {
	
	private String name;
	private ArrayList<Number> numbers;	
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 */
	public TrustedContact ()
	{
		this.name = null;
		this.numbers = new ArrayList<Number>();
	}
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 * @param numbers A list of numbers that are associated to the contact.
	 */
	public TrustedContact (String name, ArrayList<Number> numbers)
	{
		this.name = name;
		this.numbers = new ArrayList<Number>();
		for (int i = 0; i<numbers.size(); i++)
		{
			this.numbers.add(numbers.get(i));
		}
	}
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * @param numbers The contact's Number object
	 */
	public TrustedContact (Number numbers)
	{
		this.name = numbers.getNumber();
		this.numbers = new ArrayList<Number>();
		this.numbers.add(numbers);
	}
	
	/**
	 * A class for storing information retrieved or to be stored in the database. 
	 * 
	 * @param name The contact's name 
	 */
	public TrustedContact (String name)
	{
		this.name = name;
		this.numbers = new ArrayList<Number>();
	}
	
	/**
	 * Access the contact's name
	 * @return The contact's name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Set the contact's name
	 * @param name The contact's new name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Get any of the numbers for the contact.
	 * This is used for retrieving a single number to be used. 
	 * *NOTE: this should not be used unless it is to just get a single number
	 * to delete the row.
	 * @return The first non-null number found
	 */
	public String getANumber()
	{
		for (int i = 0; i < numbers.size(); i++)
		{
			if (numbers.get(i) != null)
			{
				return numbers.get(i).getNumber();
			}
		}
		return null;
	}
	
	/**
	 * Set a number in the contact's numbers list
	 * @param index The index of the number
	 * @param number The new number
	 */
	public void setNumber(int index, String number)
	{
		this.numbers.get(index).setNumber(number);
	}
	
	/**
	 * Set a number in the contact's numbers list
	 * @param number The new number
	 */
	public void setNumber(String number)
	{
		if(this.numbers.size() < 1)
		{
			this.addNumber(number);
		}
		else
		{
			this.numbers.get(0).setNumber(number);
		}
	}
	
	/**
	 * Add a number to the contact's numbers list
	 * @param number The new number to add
	 */
	public void addNumber(String number)
	{
		this.numbers.add(new Number(number));
	}
	
	/**
	 * Add a number to the contact's numbers list
	 * @param number The new Number to add 
	 */
	public void addNumber(Number number)
	{
		this.numbers.add(number);
	}
	
	/**
	 * Access a contact's number from their contact list
	 * @param index The number's index
	 * @return The number at the given index.
	 */
	public String getNumber(int index)
	{
		return numbers.get(index).getNumber();
	}
	
	/**
	 * Access a contact's number from their contact list
	 * @param number The number to look for
	 * @return The Number that contains the given number
	 */
	public Number getNumber(String number)
	{
		for (int i = 0; i < numbers.size(); i++)
		{
			if (numbers.get(i).getNumber().equalsIgnoreCase(SMSUtility.format(number)))
			{
				return numbers.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Access a contact's number from their contact list
	 * @return The list of Numbers owned by the contact.
	 */
	public ArrayList<Number> getNumber()
	{
		return numbers;
	}
	
	/**
	 * Access a contact's number from their contact list
	 * @return : ArrayList<String>
	 */
	public ArrayList<String> getNumbers()
	{
		ArrayList<String> num = new ArrayList<String>();
		
		for (int i =0; i < numbers.size(); i++)
		{
			num.add(numbers.get(i).getNumber());
		}
		return num;
	}
	
	/**
	 * Delete the number object who's number is given.
	 * @param number
	 */
	public void deleteNumber(String number)
	{
		this.numbers.remove(getNumber(number));
	}

	
	/**
	 * Whether the contact has numbers or not
	 * @return True if the contact has no numbers, false otherwise
	 */
	public boolean isNumbersEmpty()
	{
		return numbers.isEmpty();
	}
	
	/**
	 * Check whether another contact has the given number
	 * @param tc The list of the user's trusted contacts 
	 * @param number The number to check if there is a duplicate of
	 * @return Whether the number is already used. If it is the return will be
	 * true, otherwise it will be false.
	 */
	public static boolean isNumberUsed(ArrayList<TrustedContact> tc, String number)
	{
		for (int i = 0; i < tc.size(); i++)
		{
			for (int h = 0; h < tc.get(i).getNumber().size(); h++)
			{
				if (number.equalsIgnoreCase(tc.get(i).getNumber(h)))
				{
					return true;
				}
			}			
		}		
		return false;
	}
	
}
