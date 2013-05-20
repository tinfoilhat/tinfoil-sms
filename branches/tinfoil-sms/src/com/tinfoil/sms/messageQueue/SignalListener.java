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
		/*
		 * When the signal strength changes the flag that indicates whether the
		 * phone has service or not will change.
		 * 
		 * This might be better to implement on a broadcast receiver level since
		 * this is only has to do with changes of state. The phone may launch in
		 * a no service state but there would be no way to identify that is the
		 * case.
		 * 
		 * For a broadcast receiver a message that fails to send would be caught
		 * If it didn't send because of lack of service, then the message could
		 * be placed back on the top of the queue and the thread could sleep
		 * until this flag is updated 
		 */
		int strength = signalStrength.getGsmSignalStrength();
		//signalStrength.
		if (strength >= 0 && strength <= 31 || strength == 99 )
		{
			Log.v("Strength", "" + strength);
			
			/*
			 * If the phone has 0 or 99 signal strength then they do not have
			 * enough strength to send a message and all messages sent will be
			 * queued and wait till more service is found.
			 */
			if (strength == 0 || strength == 99)
			{
				ConversationView.messageSender.setSignal(false);
				/*
				 * Could use wait() and notify() rather then busy waiting
				 */
				//ConversationView.messageSender.threadNotify(false);
			}
			else
			{
				/*
				 * Once enough service is found the flag is updated.
				 */
				ConversationView.messageSender.setSignal(true);
				
				ConversationView.messageSender.threadNotify(false);
			}
			
		}
		
		super.onSignalStrengthsChanged(signalStrength);
	}
	
}
