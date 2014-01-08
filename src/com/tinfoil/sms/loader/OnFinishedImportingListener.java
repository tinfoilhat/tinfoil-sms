package com.tinfoil.sms.loader;

import java.util.ArrayList;

import com.tinfoil.sms.dataStructures.TrustedContact;

public interface OnFinishedImportingListener {
	
    /**
     * onFinishedTaskListener Called when the task finishes loading.
     */
    public void onFinishedImportingListener(boolean success, 
    		ArrayList<TrustedContact> tc, ArrayList<Boolean> inDb);
}
