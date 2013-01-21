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

package com.tinfoil.sms.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.tinfoil.sms.dataStructures.Message;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.encryption.Encryption;
import com.tinfoil.sms.messageQueue.MessageSender;
import com.tinfoil.sms.sms.Prephase3Activity;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Toast;

/**
 * An abstract class used to retrieve contacts information from the native
 * database and format the data going into tinfoil-sms's database.
 */
public abstract class SMSUtility {
    private static final Pattern p = Pattern.compile("^[+]1.{10}");
    private static final Pattern numOnly = Pattern.compile("\\W");
    private static final SmsManager sms = SmsManager.getDefault();
    public static final String SENT = "content://sms/sent";

    public static String NUMBER = "com.tinfoil.sms.number";
    public static String MESSAGE = "com.tinfoil.sms.message";
    public static String ID = "com.tinfoil.sms.id";

    public static final int ENCRYPTED_MESSAGE_LENGTH = 128;
    public static final int MESSAGE_LENGTH = 160;
    public static final int LIMIT = 50;
    public static final boolean saveMessage = false;

    private static MessageSender MS = new MessageSender();

    /**
     * Create an array of Strings to display for the auto-complete
     * 
     * @param tc
     *            : List<TrustedContact> all the TrustedContacts
     * @return : List<String> a list of all the contacts and their numbers for
     *         the auto complete list.
     */
    public static List<String> contactDisplayMaker(final List<TrustedContact> tc)
    {
        final List<String> contacts = new ArrayList<String>();
        for (int i = 0; i < tc.size(); i++)
        {
            for (int j = 0; j < tc.get(i).getNumber().size(); j++)
            {
                contacts.add(tc.get(i).getName() + ", " + tc.get(i).getNumber(j));
            }
        }

        return contacts;
    }

    /**
     * Removes the preceding '1' or '+1' for the given number
     * 
     * @param number
     *            : String, the number of the contact
     * @return : String, the number without the preceding '1' or '+1'
     */
    public static String format(String number)
    {
        if (number.matches("^1.{10}"))
        {
            number = number.substring(1);
        }
        else if (number.matches(p.pattern()))
        {
            number = number.substring(2);
        }
        number = number.replaceAll(numOnly.pattern(), "");

        return number;
    }

    /**
     * Sends the given message to the phone with the given number
     * 
     * @param number
     *            : String, the number of the phone that the message is sent to
     * @param message
     *            : String, the message, encrypted that will be sent to the
     *            contact
     */
    public static void sendSMS(final Context c, final String number, final String message)
    {
        final String SENT = "SMS_SENT";

        final Intent intent = new Intent(SENT);
        intent.putExtra(NUMBER, number);
        intent.putExtra(MESSAGE, message);
        intent.putExtra(ID, 0);
        final PendingIntent sentPI = PendingIntent.getBroadcast(c, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        c.registerReceiver(MS, new IntentFilter(SENT));

        //SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(number, null, message, sentPI, null);
        /*if(ServiceChecker.signal){
        	sms.sendTextMessage(number, null, message, sentPI, null);   
        }*/
        c.unregisterReceiver(MS);
    }

    /**
     * Sends the given message to the phone with the given number
     * 
     * @param number
     *            : String, the number of the phone that the message is sent to
     * @param message
     *            : String, the message, encrypted that will be sent to the
     *            contact
     */
    public static void sendSMS(final Context c, final String number, final String message, final long id)
    {
        final String SENT = "SMS_SENT";

        final Intent intent = new Intent(SENT);
        intent.putExtra(NUMBER, number);
        intent.putExtra(MESSAGE, message);
        intent.putExtra(ID, id);
        final PendingIntent sentPI = PendingIntent.getBroadcast(c, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Toast.makeText(c, message, Toast.LENGTH_SHORT).show();

        c.registerReceiver(MS, new IntentFilter(SENT));

        //SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(number, null, message, sentPI, null);
        /*if(ServiceChecker.signal){
        	sms.sendTextMessage(number, null, message, sentPI, null);
        }*/
        c.unregisterReceiver(MS);
    }

    /**
     * Drops the message into the in-box of the default SMS program. Tricks the
     * in-box to think the message was send by the original sender. If the
     * user's settings are such to prevent messages from being saved to the
     * native sms client as well the method will do nothing, there is NO need to
     * check the user's settings outside.
     * 
     * @param srcNumber
     *            : String, the number of the contact that sent the message
     * @param decMessage
     *            : String, the message sent from the contact
     * @param dest
     *            : String, the folder in the android database that the message
     *            will be stored in
     */
    public static void sendToSelf(final Context c, final String srcNumber, final String decMessage, final String dest) {
        //Prevent message from doing to native client given user settings
        if (Prephase3Activity.sharedPrefs.getBoolean("native_save", false))
        {
            final ContentValues values = new ContentValues();
            values.put("address", srcNumber);
            values.put("body", decMessage);

            //Stops native sms client from reading messages as new.
            values.put("read", true);
            values.put("seen", true);

            /* Sets used to determine who sent the message, 
             * if type == 2 then it is sent from the user
             * if type == 1 it has been sent by the contact
             */
            if (dest.equalsIgnoreCase(SENT))
            {
                values.put("type", "2");
            }
            else
            {
                values.put("type", "1");
            }

            c.getContentResolver().insert(Uri.parse(dest), values);
        }
    }

    /**
     * Sends a message as encrypted or plain text based on the contact's state.
     * 
     * @param number
     *            : String the number the text message is being sent to
     * @param text
     *            : String the text message
     * @param context
     *            : Context the context of the class
     * @return : boolean whether the message sent or not
     */
    public static boolean sendMessage(final String number, final String text, final Context context) {
        try
        {
            if (MessageService.dba.isTrustedContact(number) &&
                    Prephase3Activity.sharedPrefs.getBoolean("enable", true))
            {
                //Send an encrypted message
                final String encrypted = Encryption.aes_encrypt(MessageService.dba.getRow(
                        format(number)).getNumber(format(number)).getPublicKey(), text);

                sendSMS(context, number, encrypted);

                if (Prephase3Activity.sharedPrefs.getBoolean("showEncrypt", true))
                {
                    sendToSelf(context, number, encrypted, Prephase3Activity.SENT);
                    MessageService.dba.addNewMessage(new Message
                            (encrypted, true, true), number, false);
                }

                sendToSelf(context, number, text, Prephase3Activity.SENT);

                MessageService.dba.addNewMessage(new Message(text, true, true), number, false);

                Toast.makeText(context, "Encrypted Message sent", Toast.LENGTH_SHORT).show();
            }
            else
            {
                //Sending a plain text message
                sendSMS(context, number, text);
                sendToSelf(context, number, text, Prephase3Activity.SENT);

                MessageService.dba.addNewMessage(new Message(text, true, true), number, true);

                Toast.makeText(context, "Message sent", Toast.LENGTH_SHORT).show();
            }
            return true;
        } catch (final Exception e)
        {
            Toast.makeText(context, "FAILED TO SEND", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Identifies if the given String is a valid Long
     * 
     * @param number
     *            A number in string format
     * @return Whether the given String is a valid Long number.
     */
    public static boolean isANumber(final String number)
    {
        //TODO use regular expressions
        try {
            Long.valueOf(number);
        } catch (final NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

}
