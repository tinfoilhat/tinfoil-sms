package com.tinfoil.sms.sms;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.tinfoil.sms.dataStructures.Entry;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.SMSUtility;

public class QuickSMSSendingService extends Service {

	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!TelephonyManager.ACTION_RESPOND_VIA_MESSAGE.equals(intent.getAction())) {
			//Intent unknown
			return START_NOT_STICKY;
		}
		
		String message = intent.getStringExtra(Intent.EXTRA_TEXT);
		
		Uri uri = intent.getData();
		String number = uri.getSchemeSpecificPart();
		
		DBAccessor dba = new DBAccessor(this);
		
		// TODO fix to use queue.
		SMSUtility.sendMessage(dba, this, new Entry(number, message));
		SMSUtility.addMessageToDB(dba, number, message);

		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		return null;
	}
}
