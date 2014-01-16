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

package com.tinfoil.sms.crypto;

import java.security.Security;
import java.util.ArrayList;

import org.strippedcastle.jce.provider.BouncyCastleProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.Message;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.settings.EditNumber;
import com.tinfoil.sms.sms.ConversationView;
import com.tinfoil.sms.utility.OnKeyExchangeResolvedListener;
import com.tinfoil.sms.utility.SMSUtility;

/**
 * A class that creates a thread to manage a user's exchange of keys with
 * contacts. It will go through each contact that the user wishes to exchange
 * keys with and queue a key exchange message. This thread only handles the
 * first phase of the key exchange. The second phase is handled in the
 * MessagerReceiver.
 */
public class ExchangeKey implements Runnable {

    //public static ProgressDialog keyDialog;
    private ArrayList<String> untrusted;
    private ArrayList<String> trusted;
    private Number number;
    
    private Activity activity; 
    private TrustedContact trustedContact;
    
    private OnKeyExchangeResolvedListener listener;
    
    private DBAccessor dba;
    

    /* Register spongycastle as the most preferred security provider */
    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }
    
    /**
     * Used to setup the a key exchange for a single contact or to untrust a
     * single contact.
     * @param trusted The number of the contact to send a key exchange to,
     * do not sent a key exchange if null
     * @param untrusted The number of the contact to un-trust, no contact is
     * untrusted. 
     */
    public void startThread(Activity activity, final String trusted, final String untrusted)
    {
        this.activity = activity;
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
        dba = new DBAccessor(activity);
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
    	
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
                this.number = dba.getNumber(this.untrusted.get(i));
                this.number.clearPublicKey();
                
                //set the initiator flag to false
                this.number.setInitiator(false);
                dba.updateKey(this.number);
            }
        }

        //TODO give a status update for each person sent and received
        /*
         * Start Key exchanges 1 by 1, messages are prepared and then placed in
         * the messaging queue.
         */       
        boolean invalid = false;
        if (this.trusted != null)
        {
            for (int i = 0; i < this.trusted.size(); i++)
            {
                number = dba.getNumber(trusted.get(i));
                
                if(number != null)
                {
	                if (SMSUtility.checksharedSecret(number.getSharedInfo1()) &&
	                		SMSUtility.checksharedSecret(number.getSharedInfo2()))
	                {
                		Log.v("S1", number.getSharedInfo1());
                		Log.v("S2", number.getSharedInfo2());
	                	
                		sendKeyExchange(dba, number, true);
	                }
	                else
	                {
	                	invalid = true;
	                	//Toast.makeText(c, "Invalid shared secrets", Toast.LENGTH_LONG).show();
	                	Log.v("Shared Secret", "Invalid shared secrets");
	                }
                }
                else
                {
                	//TODO give error message
                }
            }
        }
        
        if (invalid && activity != null)
        {
        	trustedContact = dba.getRow(number.getNumber());
        	
        	/*
        	 * Get the shared secrets from the user.
        	 */
        	activity.runOnUiThread(new Runnable() {
        	    public void run() {
    	    		//Toast.makeText(activity, "Shared secrets must be set prior to key exchange", Toast.LENGTH_LONG).show();
    	    		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	    		LinearLayout linearLayout = new LinearLayout(activity);
    	    		linearLayout.setOrientation(LinearLayout.VERTICAL);

    	    		final EditText sharedSecret1 = new EditText(activity);
    	    		sharedSecret1.setHint(R.string.shared_secret_hint_1);
    	    		sharedSecret1.setMaxLines(EditNumber.SHARED_INFO_MAX);
    	    		sharedSecret1.setInputType(InputType.TYPE_CLASS_TEXT);
    	    		linearLayout.addView(sharedSecret1);

    	    		final EditText sharedSecret2 = new EditText(activity);
    	    		sharedSecret2.setHint(R.string.shared_secret_hint_2);
    	    		sharedSecret2.setMaxLines(EditNumber.SHARED_INFO_MAX);
    	    		sharedSecret2.setInputType(InputType.TYPE_CLASS_TEXT);
    	    		linearLayout.addView(sharedSecret2);
    	    		
    	    		builder.setMessage(activity.getString(R.string.set_shared_secrets)
    	    				+ " " + trustedContact.getName() + ", " + number.getNumber())
    	    		       .setCancelable(true)
    	    		       .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
    	    		    	   @Override
    	    		    	   public void onClick(DialogInterface dialog, int id) {
    	    		                //Save the shared secrets
    	    		    		   String s1 = sharedSecret1.getText().toString();
    	    		    		   String s2 = sharedSecret2.getText().toString();
    	    		    		   if (SMSUtility.checksharedSecret(s1) &&
    	    		                		SMSUtility.checksharedSecret(s2))
    	    		               {     	    		    			   
    	    		    			   sendKeyExchange(dba, number, s1, s2, true);
    	    		    		   }
    	    		    		   else
    	    		    		   {
    	    		    			   Toast.makeText(activity, R.string.invalid_secrets, Toast.LENGTH_LONG).show();
    	    		    		   }
    	    		           }})
    	    		       .setOnCancelListener(new OnCancelListener(){

								@Override
								public void onCancel(DialogInterface arg0) {
									//Cancel the key exchange
									Toast.makeText(activity, R.string.key_exchange_cancelled, Toast.LENGTH_LONG).show();
								}
    	    		    	   
    	    		       })
    	    		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    	    		    	   @Override
    	    		    	   public void onClick(DialogInterface arg0, int arg1) {
    	    		    			//Cancel the key exchange
    	    		    		   Toast.makeText(activity, R.string.key_exchange_cancelled, Toast.LENGTH_LONG).show();
    	    		    	   }});
    	    		AlertDialog alert = builder.create();
    	    		
    	    		alert.setView(linearLayout);
    	    		alert.show();
        	    }
        	});
        }

        ConversationView.updateList(activity, ConversationView.messageViewActive);
        if((trusted == null || trusted.size() == 0) && listener != null)
        {
        	Log.v("onKeyExchangeResolved", "TRUE");
        	listener.onKeyExchangeResolved();
        }
        //Dismisses the load dialog since the load is finished
        //keyDialog.dismiss();
    }
    
    public void setOnFinishedTaskListener(OnKeyExchangeResolvedListener listener)
    {
    	this.listener = listener;
    }

    public void removeOnFinishedTaskListener()
    {
    	this.listener = null;
    }
    
    /**
     * Set the share and then make the key exchange. Note you must check whether the
     * share secrets are valid first.
     * @param dba The database interface.
     * @param number The number in use
     * @param s1 The first new shared secret 
     * @param s2 The second new shared secret
     * @param initiator Whether the user is the key exchange initiator.
     */
    public static void sendKeyExchange(DBAccessor dba, Number number, String s1, String s2, boolean initiator)
    {
    	number.setSharedInfo1(s1);
		number.setSharedInfo2(s2);
		dba.updateNumberRow(number, number.getNumber(), number.getId());
		sendKeyExchange(dba, number, initiator);
    }
    
    /**
     * Make the key exchange. Note you must check whether the share secrets are valid first.
     * @param dba The database interface.
     * @param number The number in use
     * @param initiator Whether the user is the key exchange initiator.
     */	
    public static void sendKeyExchange(DBAccessor dba, Number number, boolean initiator)
    {
    	/*
         * Set the initiator flag since this user is starting the key exchange.
         */
		number.setInitiator(initiator);					
        
        dba.updateInitiator(number);
         
        String keyExchangeMessage = KeyExchange.sign(number,
     		   dba, SMSUtility.user);
         
        dba.addMessageToQueue(number.getNumber(), keyExchangeMessage, true);
        
        Message newMessage = new Message(keyExchangeMessage,
					true, Message.SENT_KEY_EXCHANGE_INIT);
		dba.addNewMessage(newMessage, number.getNumber(), false);
    }
}
