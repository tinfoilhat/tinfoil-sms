package com.tinfoil.sms.dataStructures;

public class ValueHolder {
	
	private boolean intro;
	private boolean startImport;
	private boolean endImport;
	private boolean startExchange;
	private boolean setSecret;
	private boolean keySend;
	private boolean pending;
	private boolean accept;
	private boolean success;
	private boolean close;

	
	public ValueHolder()
	{
		intro = false;
		startImport = false;
		endImport = false;
		startExchange = false;
		setSecret = false;
		keySend = false;
		pending = false;
		accept = false;
		success = false;
		close = false;
	}

	
	private boolean getValue (int value)
	{
		if(value == 0)
		{
			return false;
		}
		return true;
	}

	/**
	 * @return the intro
	 */
	public boolean isIntro() {
		return intro;
	}


	/**
	 * @param intro the intro to set
	 */
	public void setIntro(int intro) {
		this.intro = getValue(intro);
	}


	/**
	 * @return the startImport
	 */
	public boolean isStartImport() {
		return startImport;
	}


	/**
	 * @param startImport the startImport to set
	 */
	public void setStartImport(int startImport) {
		this.startImport = getValue(startImport);
	}


	/**
	 * @return the endImport
	 */
	public boolean isEndImport() {
		return endImport;
	}


	/**
	 * @param endImport the endImport to set
	 */
	public void setEndImport(int endImport) {
		this.endImport = getValue(endImport);
	}


	/**
	 * @return the startExchange
	 */
	public boolean isStartExchange() {
		return startExchange;
	}


	/**
	 * @param startExchange the startExchange to set
	 */
	public void setStartExchange(int startExchange) {
		this.startExchange = getValue(startExchange);
	}


	/**
	 * @return the setSecret
	 */
	public boolean isSetSecret() {
		return setSecret;
	}


	/**
	 * @param setSecret the setSecret to set
	 */
	public void setSetSecret(int setSecret) {
		this.setSecret = getValue(setSecret);
	}


	/**
	 * @return the keySend
	 */
	public boolean isKeySend() {
		return keySend;
	}


	/**
	 * @param keySend the keySend to set
	 */
	public void setKeySend(int keySend) {
		this.keySend = getValue(keySend);
	}


	/**
	 * @return the pending
	 */
	public boolean isPending() {
		return pending;
	}


	/**
	 * @param pending the pending to set
	 */
	public void setPending(int pending) {
		this.pending = getValue(pending);
	}


	/**
	 * @return the accept
	 */
	public boolean isAccept() {
		return accept;
	}


	/**
	 * @param accept the accept to set
	 */
	public void setAccept(int accept) {
		this.accept = getValue(accept);
	}


	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}


	/**
	 * @param success the success to set
	 */
	public void setSuccess(int success) {
		this.success = getValue(success);
	}


	/**
	 * @return the close
	 */
	public boolean isClose() {
		return close;
	}


	/**
	 * @param close the close to set
	 */
	public void setClose(int close) {
		this.close = getValue(close);
	}
}
