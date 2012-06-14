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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MessageService extends Service {
	public static DBAccessor dba;
		
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	 @Override
     public void onCreate() {
		 //Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		 //super.onCreate();
		 dba = new DBAccessor(this);
     }

     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
    	 //Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
    	 //stopSelf();
    	 return Service.START_STICKY;
     }
}
