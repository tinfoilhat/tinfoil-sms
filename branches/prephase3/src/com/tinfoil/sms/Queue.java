package com.tinfoil.sms;

public class Queue {
	private String number;
	private String message;
	private long id;
	
	public Queue (String number, String message, long id)
	{
		this.setNumber(number);
		this.setMessage(message);
		this.setId(id);
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
}
