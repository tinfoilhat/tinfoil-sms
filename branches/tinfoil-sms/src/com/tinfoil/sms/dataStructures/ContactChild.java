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

public class ContactChild {
	
	private String number;
	private boolean trusted;
	private boolean selected;
	
	/**
	 * Create a ContactChild which hold relavent information to display numbers
	 * in the ManageContactActivity.
	 * @param number The number 
	 * @param trusted Whether the number is trusted, a key exchange has taken
	 * place successfully.
	 * @param selected Whether the list item has been selected by the user or not.
	 */
	public ContactChild(String number, boolean trusted, boolean selected)
	{
		this.setNumber(number);
		this.setTrusted(trusted);
		this.setSelected(selected);
	}

	/**
	 * Get the number
	 * @return The number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Set the number
	 * @param number The new number
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * Whether the contact is trusted or not
	 * @return True if trusted, otherwise false
	 */
	public boolean isTrusted() {
		return trusted;
	}

	/**
	 * Set whether the contact is trusted or not.
	 * @param trusted True if trusted, otherwise false.
	 */
	public void setTrusted(boolean trusted) {
		this.trusted = trusted;
	}

	/**
	 * Whether the list item has been selected or not
	 * @return True if the list item has been selected, false otherwise
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Set whether the list item has been selected or not
	 * @param selected True if the item has been selected, false otherwise.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Toggle between selected and not selected.
	 * @return The new value of selected.
	 */
	public boolean toggle() {
		selected = !selected;
		return selected;
	}
}
