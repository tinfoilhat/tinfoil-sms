package com.tinfoil.sms;

import java.util.Calendar;

/**
 * A class used to store information from the numbers table
 *
 */
public class Number {
	
	private String number;
	private String lastMessage;
	private int type;
	private long date;
	
	/**
	 * 
	 * @param number
	 * @param type
	 * @param lastMessage
	 * @param date
	 */
	public Number (String number, int type, String lastMessage, long date)
	{
		this.setNumber(number);
		this.setLastMessage(lastMessage);
		this.setType(type);
		this.setDate(date);
	}
	
	/**
	 * Date set to current time
	 * @param number
	 * @param type
	 * @param lastMessage
	 */
	/*public Number (String number, int type, String lastMessage)
	{
		this.setNumber(number);
		this.setLastMessage(lastMessage);
		this.setType(type);
		this.setDate();
	}*/
	
	/**
	 * Date not set
	 * @param number
	 * @param type
	 */
	public Number (String number, int type)
	{
		this.setNumber(number);
		this.setLastMessage(null);
		this.setType(type);
		this.setDate(0);
	}
	
	/**
	 * Date not set
	 * @param number
	 */
	public Number (String number)
	{
		this.setNumber(number);
		this.setLastMessage(null);
		this.setType(DBAccessor.OTHER_INDEX);
		this.setDate(0);
	}
	
	/**
	 * Date set
	 * @param number
	 * @param lastMessage
	 */
	public Number (String number, String lastMessage)
	{
		this.setNumber(number);
		this.setLastMessage(lastMessage);
		this.setType(DBAccessor.OTHER_INDEX);
		this.setDate();
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
	public int getType() {
		if (type > DBAccessor.LENGTH)
		{
			return DBAccessor.OTHER_INDEX;
		}
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the date
	 */
	public long getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(long date) {
		this.date = date;
	}
	
	/**
	 * set the date to the current time
	 */
	public void setDate() {
		Calendar calendar = Calendar.getInstance();
		this.date = calendar.getTimeInMillis();
	}
	
	public static String millisToDate(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        return calendar.getTime().toString();
    }
}
