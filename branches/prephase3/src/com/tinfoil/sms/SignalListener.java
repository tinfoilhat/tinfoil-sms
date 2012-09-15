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
			}
			else{
				ServiceChecker.signal = true;
			}
		}
		
		super.onSignalStrengthsChanged(signalStrength);
	}
	
}
