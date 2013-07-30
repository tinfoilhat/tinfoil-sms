/** 
 * Copyright (C) 2013 Jonathan Gillett, Joseph Heron
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

import com.tinfoil.sms.utility.SMSUtility;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

/**
 * Broadcast Receiver for the message sent.
 */
public class MessageBroadcastReciever extends BroadcastReceiver {
    public static byte success = 0;

    @Override
    public void onReceive(final Context c, final Intent intent) {
        final Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.getString(SMSUtility.NUMBER) != null
                && bundle.getString(SMSUtility.MESSAGE) != null)
        {
            final int result = this.getResultCode();
            final long id = bundle.getLong(SMSUtility.ID);

            if (result == SmsManager.RESULT_ERROR_NO_SERVICE || result == SmsManager.RESULT_ERROR_RADIO_OFF)
            {
                Toast.makeText(c, "SMS put in queue to send", Toast.LENGTH_SHORT).show();
                Toast.makeText(c, bundle.getString(SMSUtility.MESSAGE), Toast.LENGTH_LONG).show();
            }
            else if (result == Activity.RESULT_OK)
            {
                //Message sent successfully
                if (id > 0)
                {
                    //Message Sent successful from queue 
                    Toast.makeText(c, "Queue message sent", Toast.LENGTH_SHORT).show();
                    //MessageService.dba.deleteQueueEntry(id);

                    //TODO Might be good if this section notifies the queue signaling the next message can be send
                    //That would also require the queue to block after sending a message

                    //success = 2;
                }

                Toast.makeText(c, "Message Sent", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
