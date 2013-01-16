package com.tinfoil.sms;

public class ContactChild {
	
	private String number;
	private boolean trusted;
	private boolean selected;
	
	public ContactChild(String number, boolean trusted, boolean selected)
	{
		this.setNumber(number);
		this.setTrusted(trusted);
		this.setSelected(selected);
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public boolean isTrusted() {
		return trusted;
	}

	public void setTrusted(boolean trusted) {
		this.trusted = trusted;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean toggle() {
		selected = !selected;
		return selected;
	}
}
