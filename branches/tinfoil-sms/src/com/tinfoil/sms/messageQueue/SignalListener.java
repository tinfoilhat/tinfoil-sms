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

package com.tinfoil.sms.messageQueue;

import com.tinfoil.sms.sms.ConversationView;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;

public class SignalListener extends PhoneStateListener{

	public void onSignalStrengthsChanged(SignalStrength signalStrength)
	{
		int strength = signalStrength.getGsmSignalStrength();
		//signalStrength.
		if (strength >= 0 && strength <= 31 || strength == 99 )
		{
			Log.v("Strength", ""+strength);
			if (strength == 0 || strength == 99)
			{
				ConversationView.messageSender.setSignal(false);
				/*
				 * Could use wait() and notify() rather then busy waiting
				 */
				//MessageSender.sc.notify();
				ConversationView.messageSender.threadNotify(false);
				
			}
			else{
				ConversationView.messageSender.setSignal(true);
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
