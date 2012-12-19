package com.tinfoil.sms;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

public class SignalListener extends PhoneStateListener{

	public void onSignalStrengthsChanged(SignalStrength signalStrength)
	{
		int strength = signalStrength.getGsmSignalStrength();
		//signalStrength.
		if (strength >= 0 && strength <= 31 || strength == 99 )
		{
			if (strength == 0 || strength == 99)
			{
				ServiceChecker.signal = false;
				/*
				 * Could use wait() and notify() rather then busy waiting
				 */
				//MessageSender.sc.notify();
				
			}
			else{
				ServiceChecker.signal = true;
				/*try {
					MessageSender.sc.wait();
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}*/
			}
		}
		
		super.onSignalStrengthsChanged(signalStrength);
	}
	
}
