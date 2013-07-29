package com.test.androidbugtest;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;


/*
@ReportsCrashes(formKey = "dFllaFBUVXphdkE4RWFnY3JVcGI3REE6MQ",
				logcatArguments = { "-t", "200", "-v", "time"}) 
*/

@ReportsCrashes(
        formKey = "",
        formUri = "http://code4peace.dyndns.org:5984/acra-tinfoilsms/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin="reporter",
        formUriBasicAuthPassword="reportALLthecrashes987",
        // Your usual ACRA configuration
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
        )

public class AndroidBugTest extends Application
{
	  @Override
	  public void onCreate()
	  {
	      /* The following line triggers the initialization of ACRA
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
	      */
	      super.onCreate();
	      ACRA.init(this);
	  }
}