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

import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

public class MessageSender extends BroadcastReceiver {
    public static ServiceChecker sc = new ServiceChecker();
    public static byte success = 0;

    @Override
    public void onReceive(final Context c, final Intent intent) {
        final Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.getString(SMSUtility.NUMBER) != null
                && bundle.getString(SMSUtility.MESSAGE) != null)
        {
            final int result = this.getResultCode();
            final long id = bundle.getLong(SMSUtility.ID);

            /*
             * What the end result of this should be:
             * 
             * Case:
             * Message is sent (no messages are in the queue)
             * - Message fails to send since there is no service
             * - Message gets put into the queue. (May change to put in queue prior to sending)
             * - Thread is started and checks if there is service
             * 	- If there is no service it blocks it self
             * 		- The signalListener will wake up the thread once their is service
             * 		- Proceed to line below
             * 	- If there is service the thread sends off the messages (FIFO)
             * 		- If the message successfully sends it is popped off the queue
             * 		- If the message fails to send (because of lack of service) it is not taken out of the queue
             * 			- The thread then blocks itself and waits to be woken up by the signalListener.
             * 
             * Similar case for if there is messages in the queue and a message is sent without service (sill)
             * - Message is added to the queue (upon failing because of lack of service) 
             * - Thread continues as above
             * 
             * The case not yet explained would be when there is a large queue of messages (relatively of course)
             * It could just be a single message. The problem comes with resource allocation in that the database must be
             * accessed exclusively. This is because the database is opened for each operation and then closed upon finishing
             * that operation (on the database). In order to prevent the database from being left open upon program termination.
             * The messaging thread which will be accessing the database.
             *  
             * Solutions, creating semaphores to the database to prevent other actions from competing. 
             */

            /*
             * Currently this only works for when there is one message in the queue.
             * ***Note changes have been made but not tested
             * 
             * The problem:
             * ------------
             * *Please note that tests have consisted only with one contact being messaged multiple times
             * let n be the number of messages in the queue of a single contact,
             * n - 1 of the messages would be sent n times to the contact 
             * current fix attempt: 
             * retrieve the length of the queue once, before the loop,
             * *expected results, n messages will be sent, (unknown if it will be n times)
             * 
             * ***CURRENTLY NOT SUPPORTED***
             */
            if (result == SmsManager.RESULT_ERROR_NO_SERVICE || result == SmsManager.RESULT_ERROR_RADIO_OFF)
            {

                // If id = 0 then the message is being sent for the first time (not yet in the queue)
                if (id == 0) {
                    Toast.makeText(c, "SMS put in queue to send", Toast.LENGTH_SHORT).show();
                    MessageService.dba.addMessageToQueue(bundle.getString(SMSUtility.NUMBER),
                            bundle.getString(SMSUtility.MESSAGE));

                    intent.removeExtra(SMSUtility.NUMBER);
                    intent.removeExtra(SMSUtility.MESSAGE);

                    //**Temporary fix for no signal problem
                    Toast.makeText(c, "No signal, Message Failed to Send", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(c, "No signal", Toast.LENGTH_SHORT).show();

                    //Start the Thread to start checking for messages
                    //sc.startThread(c);

                }
                else {

                    //There was service but not enough/not long enough to send a message from queue
                    Toast.makeText(c, "SMS still in queue", Toast.LENGTH_SHORT).show();

                    //Success variable was intended to identify that the variable was
                    //success = 0 (not in queue)
                    //success = 1 (in the queue)
                    //success = 2 (out of queue)
                    //This was a bad approach in that messages should be put into the queue to begin with
                    //Also it is unable to describe every message in the queue and only works for a single message

                    //success = 1;
                }

            }
            else if (result == Activity.RESULT_OK)
            {
                //Message sent successfully
                if (id > 0)
                {
                    //Message Sent successful from queue 
                    Toast.makeText(c, "Queue message sent", Toast.LENGTH_SHORT).show();
                    MessageService.dba.deleteQueueEntry(id);

                    //TODO Might be good if this section notifies the queue signaling the next message can be send
                    //That would also require the queue to block after sending a message

                    //success = 2;
                }

                Toast.makeText(c, "Message Sent", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
