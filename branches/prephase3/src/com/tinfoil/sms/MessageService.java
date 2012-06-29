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

package com.tinfoil.sms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MessageService extends Service {
	public static DBAccessor dba;
	public static NotificationManager mNotificationManager;
	//private int SIMPLE_NOTFICATION_ID =1;
	public static CharSequence contentTitle;
	public static CharSequence contentText;
	public static final int INDEX = 1;
		
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	 @Override
     public void onCreate() {
		 mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
     }

     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
    	// TODO make so that when a user goes to the messages (through pendingIntent or just goes to MessageView) will clear the notifications for that user
    	if (contentTitle != null && contentText != null)
    	{

    		String address = contentTitle.toString();
    		contentTitle = dba.getRow(address).getName();
    		Notification notifyDetails = new Notification(R.drawable.ic_launcher, 
    				contentTitle + ": " + contentText,System.currentTimeMillis());
    		Intent notifyIntent = null;
    		if (MessageReceiver.myActivityStarted)
    		{
    			notifyIntent = new Intent(getApplicationContext(), MessageView.class);
    		}
    		else
    		{
    			notifyIntent = new Intent(getApplicationContext(), Prephase3Activity.class);
    		}
			
			notifyIntent.putExtra("Notification", address);
			PendingIntent in = PendingIntent.getActivity(this,
					0, notifyIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
			
		
			notifyDetails.setLatestEventInfo(this, contentTitle, contentText, in);
			mNotificationManager.notify(INDEX, notifyDetails);
			
     	}
    	stopSelf();
    	return Service.START_NOT_STICKY;
     }
}
