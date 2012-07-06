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
	public static final String notificationIntent = "com.tinfoil.sms.Notifications";
	public static final String multipleNotificationIntent = "com.tinfoil.sms.MultipleNotifications";
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
    	 
    	 if (contentTitle != null && contentText != null)
    	 {
    		//TODO when there is multiple messages the notification Intent will contain either nothing or 
     		//a word to identify it is to be sent to prephase3Activity only.
     		
     		//TODO find out more about intents and figure out to:
     		//get to a neutral state from any position in program so then it can follow the same procedure each time, 
     		//(such as closing the program and then starting from a closed state).
     		//I could consider the main application page (Prephase3Activity) to be the neutral point in the program
    		Intent notifyIntent = null;
    		PendingIntent in = null;
    		Notification notifyDetails = null;

    		
    		String address = contentTitle.toString();

    		if (dba.getUnreadMessageCount() > 1) {
    			//Might need to change this.
    			contentTitle = dba.getRow(address).getName();
    			notifyDetails = new Notification(R.drawable.ic_launcher, 
						contentTitle + ": " + contentText, System.currentTimeMillis());

				contentTitle = "New Messages";
				contentText = dba.getUnreadMessageCount() + " unread messages";
				
    			//No extra is added so the user will be brought to the main menu
    			notifyIntent = new Intent(getApplicationContext(), Prephase3Activity.class);
    			notifyIntent.putExtra(multipleNotificationIntent, true);
    			in = PendingIntent.getActivity(this,
    					0, notifyIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
    		}
    		else 
    		{
    			contentTitle = dba.getRow(address).getName();
				notifyDetails = new Notification(R.drawable.ic_launcher, 
						contentTitle + ": " + contentText,System.currentTimeMillis());
	    		if (MessageReceiver.myActivityStarted)
	    		{
	    			notifyIntent = new Intent(getApplicationContext(), MessageView.class);
	    			notifyIntent.putExtra(notificationIntent, address);
	    			in = PendingIntent.getActivity(this,
	    					0, notifyIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
	    		}
	    		else
	    		{
	    			notifyIntent = new Intent(getApplicationContext(), Prephase3Activity.class);
	    			notifyIntent.putExtra(notificationIntent, address);
	    			in = PendingIntent.getActivity(this,
	    					0, notifyIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
	    		}
	    		notifyIntent.putExtra(multipleNotificationIntent, false);
    		}
			/*notifyIntent.putExtra("Notification", address);
			PendingIntent in = PendingIntent.getActivity(this,
					0, notifyIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);*/
			
			notifyDetails.setLatestEventInfo(this, contentTitle, contentText, in);
			mNotificationManager.notify(INDEX, notifyDetails);
			
     	}
    	stopSelf();
    	return Service.START_NOT_STICKY;
     }
}
