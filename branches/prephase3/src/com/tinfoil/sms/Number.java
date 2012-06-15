package com.tinfoil.sms;

import java.util.Calendar;

public class Number {
	
	private String number;
	private String lastMessage;
	private String type;
	private long date;
	
	public Number (String number, String lastMessage, String type, long date)
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
		this.setDate(0);
	}
	
	public Number (String number, String lastMessage)
	{
		this.setNumber(number);
		this.setLastMessage(lastMessage);
		//this.setType(null);
		this.setType("cell");
		this.setDate(0);
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
	/*public long getDate() {
		return date;
	}*/
	
	/**
	 * @return the date
	 */
	public String getDate() {
		return millisToDate(date);
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(long date) {
		this.date = date;
	}
	
	public static String millisToDate(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        return calendar.getTime().toString();
    }
}
