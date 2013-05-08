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

package com.tinfoil.sms.crypto;

import java.util.ArrayList;

import android.app.ProgressDialog;

import com.tinfoil.sms.dataStructures.ContactParent;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.encryption.Encryption;
import com.tinfoil.sms.utility.MessageService;

/**
 * A class that creates a thread to manage a user's exchange of keys with
 * contacts. It will go through each contact that the user wishes to exchange
 * keys with and queue a key exchange message. This thread only handles the
 * first phase of the key exchange. The second phase is handled in the
 * MessagerReceiver.
 */
public class ExchangeKey implements Runnable {

    public static ProgressDialog keyDialog;
    private ArrayList<String> untrusted;
    private ArrayList<String> trusted;
    private ArrayList<ContactParent> contacts;
    private Number number;

    /**
     * A constructor used by the ManageContactsActivity to set up the key
     * exchange thread
     * 
     * @param contacts The list of contacts
     */
    public void startThread(final ArrayList<ContactParent> contacts)
    {
        //this.c = c;
        this.contacts = contacts;
        this.trusted = null;
        this.untrusted = null;

        /*
         * Start the thread from the constructor
         */
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * TODO comment
     * @param trusted
     * @param untrusted
     */
    public void startThread(final String trusted, final String untrusted)
    {
        //this.c = c;
        this.trusted = new ArrayList<String>();
        this.untrusted = new ArrayList<String>();
        
        if(trusted != null)
        {
        	this.trusted.add(trusted);
        }
        
        if(untrusted != null)
        {
        	this.untrusted.add(untrusted);
        }
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {

        /* 
         * Used by ManageContacts Activity to determine from the 
         * contacts that have been selected need to exchange keys or
         * stop sending secure messages
         */
        if (this.trusted == null && this.untrusted == null)
        {
            this.trusted = new ArrayList<String>();
            this.untrusted = new ArrayList<String>();

            for (int i = 0; i < this.contacts.size(); i++)
            {
                for (int j = 0; j < this.contacts.get(i).getNumbers().size(); j++)
                {
                    if (this.contacts.get(i).getNumber(j).isSelected())
                    {
                        if (!this.contacts.get(i).getNumber(j).isTrusted())
                        {
                            this.trusted.add(this.contacts.get(i).getNumber(j).getNumber());
                        }
                        else
                        {
                            this.untrusted.add(this.contacts.get(i).getNumber(j).getNumber());
                        }
                    }
                }
            }
        }

        /*
         * This is actually how removing contacts from trusted should look since it is just a
         * deletion of keys. We don't care if the contact will now fail to decrypt messages that
         * is the user's problem
         */
        if (this.untrusted != null)
        {
            for (int i = 0; i < this.untrusted.size(); i++)
            {
                //untrusted.get(i).clearPublicKey();
                this.number = MessageService.dba.getNumber(this.untrusted.get(i));
                this.number.clearPublicKey();
                
                //set the initiator flag to false
                this.number.setInitiator(false);
                MessageService.dba.updateKey(this.number);
            }
        }

        /*
         * Start Key exchanges 1 by 1, messages are prepared and then placed in
         * the messaging queue.
         */        
        if (this.trusted != null)
        {
            for (int i = 0; i < this.trusted.size(); i++)
            {
                this.number = MessageService.dba.getNumber(this.trusted.get(i));
                 
                /*
                 * Set the initiator flag since this user is starting the key exchange.
                 */
                number.setInitiator(true);
                                
                MessageService.dba.updateInitiator(number);
             
                /*
                 * Will use MessageService.dba.getUserRow(); to get access to
                 * the user's key. After the user's key has been generated.
                 */
                String keyExchangeMessage = new String(Encryption.generateKey());
                
                MessageService.dba.addMessageToQueue(number.getNumber(), keyExchangeMessage, true);
            }
        }

        //Dismisses the load dialog since the load is finished
        keyDialog.dismiss();
    }

}
