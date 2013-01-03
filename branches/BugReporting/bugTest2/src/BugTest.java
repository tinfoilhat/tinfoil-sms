import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.j2bugzilla.base.Bug;
import com.j2bugzilla.base.BugFactory;
import com.j2bugzilla.base.BugzillaConnector;
import com.j2bugzilla.base.BugzillaException;
import com.j2bugzilla.base.ConnectionException;
import com.j2bugzilla.rpc.BugSearch;
import com.j2bugzilla.rpc.BugSearch.SearchLimiter;
import com.j2bugzilla.rpc.BugSearch.SearchQuery;
import com.j2bugzilla.rpc.BugzillaVersion;
import com.j2bugzilla.rpc.LogIn;
import com.j2bugzilla.rpc.ReportBug;

public class BugTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String user = "heron.joseph@gmail.com";
		String pass = "hatewalid123";
		
		/* Connect to the bugzilla server */
		BugzillaConnector conn = new BugzillaConnector();
		try
		{
			conn.connectTo("http://code4peace.dyndns.org/bugzilla/xmlrpc.cgi", user, pass );
		
			/* Display the Bugzilla Server Version */
			BugzillaVersion versionCheck = new BugzillaVersion();
			conn.executeMethod(versionCheck);
			System.out.println("Bugzilla Version: " + versionCheck.getVersion());
			
			/* Test logging in and displaying the user's id */
			LogIn logIn = new LogIn(user, pass);
			conn.executeMethod(logIn);
			System.out.println("Login ID: " + logIn.getUserID());
			
			/* Test searching for a bug and displaying the results */
			BugSearch search = new BugSearch(new SearchQuery(SearchLimiter.SUMMARY, "MULTIPLE"));
			conn.executeMethod(search);
			
			ArrayList<Bug> results = (ArrayList<Bug>) search.getSearchResults();
			for(Bug bug : results)
			{
			    System.out.println(bug.getSummary());
			}
			
			/* Test checking if a bug has already been reported */
			BugSearch searchExisting = new BugSearch(new SearchQuery(SearchLimiter.SUMMARY, "Test Bug Report"));
			conn.executeMethod(searchExisting);	
			
			ArrayList<Bug> existingBugs = (ArrayList<Bug>) search.getSearchResults();
			if (existingBugs.size() > 0)
			{
				System.out.println("Bug Already Exists!");
			    System.exit(0);
			}
			
			/* Test submitting a bug report */
	        BugFactory factory = new BugFactory();
	        
	        Map<String, Object> bugMap = new HashMap<String, Object>();

	        bugMap.put("product", "Tinfoil-SMS");
	        bugMap.put("component", "Other");
	        bugMap.put("summary", "Test Bug Report");
	        bugMap.put("description", "A long test bug report description");
	        bugMap.put("version", "1.0");
	        bugMap.put("cf_android_ver", "2.2 - Froyo");
	        bugMap.put("op_sys", "Android");
	        bugMap.put("rep_platform", "Phone");
	        bugMap.put("priority", "Normal");
	        bugMap.put("severity", "normal");
	        
	        Bug bug = factory.createBug(bugMap);
	        ReportBug report = new ReportBug(bug);
	        conn.executeMethod(report);
	        System.out.println("Bug Report ID: " + report.getID());
	        
		}
		catch (BugzillaException | ConnectionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
