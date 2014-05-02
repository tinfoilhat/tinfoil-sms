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

package com.tinfoil.sms.utility;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.Entry;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.settings.QuickPrefsActivity;
import com.tinfoil.sms.sms.ConversationView;
import com.tinfoil.sms.sms.KeyExchangeManager;
import com.tinfoil.sms.sms.SendMessageActivity;

public class MessageService extends Service {
    private DBAccessor dba;
    public static NotificationManager mNotificationManager;
    //private int SIMPLE_NOTFICATION_ID =1;
    public static final String notificationIntent = "com.tinfoil.sms.Notifications";
    public static final String multipleNotificationIntent = "com.tinfoil.sms.MultipleNotifications";
    public static CharSequence contentTitle;
    public static CharSequence contentText;
    public static final int SINGLE = 1;
    public static final int MULTI = 2;
    public static final int KEY = 3;
    
    private SharedPreferences sharedPrefs;

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        dba = new DBAccessor(this);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        
        /*
         * Creates a notification if there is one to be created and if the user set the preferences
         * to allow notifications
         */
        if (contentTitle != null && contentText != null &&
                sharedPrefs.getBoolean(
                QuickPrefsActivity.NOTIFICATION_BAR_SETTING_KEY, true))
        {
            Intent notifyIntent = null;
            PendingIntent in = null;

            final String address = contentTitle.toString();

            if (dba.getUnreadMessageCount() > 1) {
            	
            	MessageService.mNotificationManager.cancel(SINGLE);
                //Might need to change this.
                contentTitle = dba.getRow(address).getName();

                contentTitle = this.getString(R.string.new_message_notification_title);
                contentText = dba.getUnreadMessageCount() + " " +
                		this.getString(R.string.unread_email_message);

                // Extra is added so the user will be brought to the main menu
                notifyIntent = new Intent(this.getApplicationContext(), ConversationView.class);
                //notifyIntent.putExtra(multipleNotificationIntent, true);
                notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                		| Intent.FLAG_ACTIVITY_SINGLE_TOP);

                in = PendingIntent.getActivity(this,
                        0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(in)
	            	.setContentTitle(contentTitle)
	            	.setContentText(contentText)
	            	.setTicker(contentTitle + ": " + contentText)
	            	.setSmallIcon(R.drawable.tinfoil_logo);
                
                mNotificationManager.notify(MULTI, builder.build());
            }
            else
            {
                contentTitle = dba.getRow(address).getName();
                
                notifyIntent = new Intent(this.getApplicationContext(), SendMessageActivity.class);
                notifyIntent.putExtra(ConversationView.MESSAGE_INTENT, ConversationView.MESSAGE_VIEW);
                notifyIntent.putExtra(notificationIntent, address);
                notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                		| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    
                // Adds the back stack
                stackBuilder.addParentStack(SendMessageActivity.class);
                              
                notifyIntent.putExtra(multipleNotificationIntent, false);
                
                // Adds the Intent to the top of the stack
                stackBuilder.addNextIntent(notifyIntent);
                
                in = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                
                builder.setContentIntent(in)
                	.setContentTitle(contentTitle)
                	.setContentText(contentText)
                	.setTicker(contentTitle + ": " + contentText)
                	.setSmallIcon(R.drawable.tinfoil_logo);
                
                mNotificationManager.notify(SINGLE, builder.build());
            }
        }
        
        if(sharedPrefs.getBoolean(
        		QuickPrefsActivity.NOTIFICATION_BAR_SETTING_KEY, true))
        {
        	ArrayList<Entry> keyMessage = dba.getAllKeyExchangeMessages();
	        if(keyMessage != null && keyMessage.size() > 0)
	        {
	            Intent notifyIntent = null;
	            PendingIntent in = null;
	    		
	    		notifyIntent = new Intent(this.getApplicationContext(), KeyExchangeManager.class);
	    		notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                		| Intent.FLAG_ACTIVITY_SINGLE_TOP);
	            
	            // Adds the back stack
                stackBuilder.addParentStack(KeyExchangeManager.class);

                // Adds the Intent to the top of the stack
                stackBuilder.addNextIntent(notifyIntent);
                
                in = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                
                builder.setContentIntent(in)
                	.setContentTitle(this.getString(R.string.pending_key_exchange_notification))
	            	.setContentText(this.getString(R.string.pending_key_exchange_message))
	            	.setTicker(this.getString(R.string.pending_key_exchange_notification))
	            	.setSmallIcon(R.drawable.key_exchange);
                
                mNotificationManager.notify(KEY, builder.build());
	        }
	        else
	        {
	        	MessageService.mNotificationManager.cancel(MessageService.KEY);
	        }
        }

        this.stopSelf();
        return Service.START_NOT_STICKY;
    }
}
