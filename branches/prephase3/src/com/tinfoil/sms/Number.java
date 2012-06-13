package com.tinfoil.sms;

public class Number {
	
	private String number;
	private String lastMessage;
	private String type;
	private String date;
	
	public Number (String number, String lastMessage, String type, String date)
	{
		this.setNumber(number);
		this.setLastMessage(lastMessage);
		this.setType(type);
		this.setDate(date);
	}
	
	public Number (String number)
	{
		this.setNumber(number);
		this.setLastMessage(null);
		//this.setType(null);
		this.setType("cell");
		this.setDate(null);
	}
	
	public Number (String number, String lastMessage)
	{
		this.setNumber(number);
		this.setLastMessage(lastMessage);
		//this.setType(null);
		this.setType("cell");
		this.setDate(null);
	}

	/**
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * @param number the number to set
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * @return the lastMessage
	 */
	public String getLastMessage() {
		return lastMessage;
	}

	/**
	 * @param lastMessage the lastMessage to set
	 */
	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}
	
	

}
