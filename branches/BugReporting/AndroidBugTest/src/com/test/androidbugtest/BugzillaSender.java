package com.test.androidbugtest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ACRAConfiguration;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import android.util.Log;

import com.j2bugzilla.base.Bug;
import com.j2bugzilla.base.BugFactory;
import com.j2bugzilla.base.BugzillaConnector;
import com.j2bugzilla.base.BugzillaException;
import com.j2bugzilla.base.ConnectionException;
import com.j2bugzilla.rpc.BugSearch;
import com.j2bugzilla.rpc.BugzillaVersion;
import com.j2bugzilla.rpc.LogIn;
import com.j2bugzilla.rpc.ReportBug;
import com.j2bugzilla.rpc.BugSearch.SearchLimiter;
import com.j2bugzilla.rpc.BugSearch.SearchQuery;


public class BugzillaSender implements ReportSender
{
	private final String server;
	private final String username;
	private final String password;
	/* Mapping of ReportField entries to the equivalent Bugzilla field */
	private Map<ReportField, String> mapping;
	
	/**
	 * Constructor to initialize the Bugzilla specific sender for ARCA
	 * 
	 * @param server The Bugzilla XML-RPC server, usually ends in xmlrpc.cgi 
	 * @param username The username of the Bugzilla user
	 * @param password The Bugzilla user password
	 * @param mapping A mapping of the ReportField values to the corresponding
	 * Bugzilla XML-RPC fields
	 */
	public BugzillaSender(	String server, 
							String username, 
							String password, 
							Map<ReportField, String> mapping)
	{
		this.server = server;
		this.username = username;
		this.password = password;
		this.mapping = mapping;
	}
	
	/**
	 * Send a crash report to the Bugzilla server
	 * 
	 * @param report The CrashReportData object which is mapped to
	 * the corresponding Bugzilla values to submit in the bug report.
	 */
	public void send(CrashReportData report) 
		throws ReportSenderException
	{
		
		/* Connect to the bugzilla server */
		BugzillaConnector conn = new BugzillaConnector();
		try
		{
			conn.connectTo(this.server, this.username, this.password);
		}
		catch (ConnectionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* Login to the Bugzilla server */
		LogIn logIn = new LogIn(this.username, this.password);
		try
		{
			conn.executeMethod(logIn);
		}
		catch (BugzillaException e)
		{
			// TODO Auto-generated catch block
			Log.v("Login ID: ", String.valueOf(logIn.getUserID()));
			e.printStackTrace();
		}
		/* Map the ReportField values to the Bugzilla fields using the mapping */
		//TODO IMPLEMENT
		/*
        BugFactory factory = new BugFactory();
        
        Map<String, Object> bugMap = new HashMap<String, Object>();

        bugMap.put("product", "Tinfoil-SMS");
        bugMap.put("component", "Other");
        bugMap.put("summary", "Android Test Bug Report");
        bugMap.put("description", "A long test bug report description");
        bugMap.put("version", "1.0");
        bugMap.put("cf_android_ver", "2.2 - Froyo");
        bugMap.put("op_sys", "Android");
        bugMap.put("rep_platform", "Phone");
        bugMap.put("priority", "Normal");
        bugMap.put("severity", "normal");
        
        Bug bug = factory.createBug(bugMap);
        ReportBug bugReport = new ReportBug(bug);
        try
        {
			conn.executeMethod(bugReport);
		} 
        catch (BugzillaException e)
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	
	/**
	 * Remaps the CrashReportData mapping to the corresponding Bugzilla mapping
	 * using the mapping member to translate from CrashReportData to a final
	 * mapping of Bugzilla fields to values. 
	 * 
	 * @param report The CrashReportData object to be remapped to the Bugzilla mapping
	 * @return
	 */
    private Map<String, String> remap(Map<ReportField, String> report)
    {
        ReportField[] fields = ACRA.getConfig().customReportContent();
        /*if (fields.length == 0)
        {
            fields = ACRAConstants.DEFAULT_REPORT_FIELDS;
        }*/

        final Map<String, String> finalReport = new HashMap<String, String>(report.size());
        for (ReportField field : fields)
        {
            if (mapping.get(field) != null)
            {
                finalReport.put(mapping.get(field), report.get(field));
            }
        }
        return finalReport;
    }
}
