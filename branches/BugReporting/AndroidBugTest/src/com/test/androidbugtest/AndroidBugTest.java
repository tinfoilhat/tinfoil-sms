package com.test.androidbugtest;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.app.Application;
import android.view.Menu;
import android.widget.Toast;

import org.acra.*;
import org.acra.annotation.*;


@ReportsCrashes(formKey = "dFllaFBUVXphdkE4RWFnY3JVcGI3REE6MQ",
				logcatArguments = { "-t", "200", "-v", "time"}) 

public class AndroidBugTest extends Application
{
	  @Override
	  public void onCreate()
	  {
	      // The following line triggers the initialization of ACRA
	      ACRA.init(this);
	      
	      Map<ReportField, String> bugzillaMap = new HashMap<ReportField, String>();
	      bugzillaMap.put(ReportField.APP_VERSION_NAME, "version");

	      BugzillaSender bugzillaSender = new BugzillaSender("http://code4peace.dyndns.org/bugzilla/xmlrpc.cgi",
    		  												 "heron.joseph@gmail.com",
    		  												 "hatewalid123",
    		  												 bugzillaMap);
	      ACRA.getErrorReporter().setReportSender(bugzillaSender);

	      // Add support to handle exceptions
	      // TODO: Add support for important exceptions, such as crypto related exceptions
	      //ACRA.getErrorReporter().handleException(caughtException);
	      super.onCreate();
	  }
}