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

package com.tinfoil.sms;

import java.security.Security;

import android.app.Application;
import android.preference.PreferenceManager;

import com.bugsense.trace.BugSenseHandler;
import com.tinfoil.sms.settings.QuickPrefsActivity;

import org.spongycastle.jce.provider.BouncyCastleProvider;


/**
 * The main application class which is simply used to initialize Bugsense, which is
 * used to report crashes and other bugs in the application.
 */
public class TinfoilSMS extends Application
{
	public static boolean threadable = false;
	
    /* Register spongycastle as the most preferred security provider */
    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }
	
    @Override
    public void onCreate()
    {
        super.onCreate();
        
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
        		QuickPrefsActivity.BUGSENSE_ENABLE_SETTING_KEY, true))
        {
            BugSenseHandler.initAndStartSession(this, "169095e2");
        }
    }
}