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

package com.tinfoil.sms.sms;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.tinfoil.sms.R;
import com.tinfoil.sms.adapter.ConversationAdapter;
import com.tinfoil.sms.adapter.DefaultListAdapter;
import com.tinfoil.sms.dataStructures.User;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.messageQueue.MessageSender;
import com.tinfoil.sms.messageQueue.SignalListener;
import com.tinfoil.sms.settings.AddContact;
import com.tinfoil.sms.settings.ImportContacts;
import com.tinfoil.sms.settings.QuickPrefsActivity;
import com.tinfoil.sms.utility.MessageReceiver;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;
import com.tinfoil.sms.utility.Walkthrough;
import com.tinfoil.sms.utility.Walkthrough.Step;

/**
 * This activity shows all of the conversations the user has with contacts. The
 * list Will be updated every time a message is received. Upon clicking any of
 * the conversations MessageView activity will be started with selectedNumber =
 * the contacts number From the menu a user can select 'compose' to start
 * SendMessageActivity to start or continue a conversation with a contact. The
 * user can also select 'settings' which will take them to the main settings
 * page.
 */
public class ConversationView extends Activity {

	public static final String selectedNumberIntent = "com.tinfoil.sms.sms.Selected";
	public static final String MESSAGE_INTENT = "com.tinfoil.sms.sms.message";
	
	//public static DBAccessor dba;
    public static final String INBOX = "content://sms/inbox";
    public static final String SENT = "content://sms/sent";
    public static SharedPreferences sharedPrefs;    
    private static ConversationAdapter conversations;
    public static List<String[]> msgList;
    private static ListView list;
    private static DefaultListAdapter ap;
    public static final MessageReceiver boot = new MessageReceiver();
    private final SignalListener pSL = new SignalListener();
    public static boolean messageViewActive = false;
    
    public static MessageSender messageSender = new MessageSender();
      
    //private ProgressDialog dialog;
    private static boolean update = false;
    public static final int LOAD = 0;
    public static final int UPDATE = 1;
    private static ListView emptyList; 
    
    private static ConversationLoader runThread;
    
    public static final int ADD_CONTACT = 0;
    public static final int IMPORT_CONTACT = 1;
    
    public static final int COMPOSE = 0;
    public static final int MESSAGE_VIEW = 1;
    public static final int NEW_KEY_EXCHANGE = 2;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((TelephonyManager) this.getSystemService(TELEPHONY_SERVICE)).listen(this.pSL, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        MessageService.mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        /*
         * Load the shared preferences
         */
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        getEULA();
        
        messageSender.startThread(getApplicationContext());
        
        DBAccessor dba = new DBAccessor(this);
        
        SMSUtility.user = dba.getUserRow();
        
        if(SMSUtility.user == null)
        {
        	//Toast.makeText(context, "New key pair is generating...", Toast.LENGTH_SHORT).show();
        	Log.v("First Launch", "keys are generating...");
        	
	        // Create a default user with a new key pair
        	SMSUtility.user = new User();
	        
	        //Set the user's in the database
	        dba.setUser(SMSUtility.user);
        }
        Log.v("public key", new String(SMSUtility.user.getPublicKey()));
        Log.v("private key", new String(SMSUtility.user.getPrivateKey()));
        
        if (this.getIntent().hasExtra(MessageService.multipleNotificationIntent))
        {
            /*
             * Check if there is the activity has been entered from a notification.
             * This check specifically is to find out if there are multiple pending
             * received messages.
             */
            this.getIntent().removeExtra(MessageService.multipleNotificationIntent);
        }

        if (this.getIntent().hasExtra(MessageService.notificationIntent))
        {
            /*
             * Check if there is the activity has been entered from a notification.
             * This check is to find out if there is a single message received pending.
             * If so then the conversation with that contact will be loaded.
             */
            final Intent intent = new Intent(this, SendMessageActivity.class);
            intent.putExtra(ConversationView.MESSAGE_INTENT, ConversationView.MESSAGE_VIEW);
            intent.putExtra(selectedNumberIntent, this.getIntent().getStringExtra(MessageService.notificationIntent));
            this.getIntent().removeExtra(MessageService.notificationIntent);
            this.startActivity(intent);
        }
        
        ConversationView.messageViewActive = false;
        this.setContentView(R.layout.main);
        
        MessageReceiver.myActivityStarted = true;

        /*
         * Set the list of conversations
         */
        list = (ListView) this.findViewById(R.id.conversation_list);
        emptyList = (ListView) this.findViewById(R.id.empty);
        
        /*this.dialog = ProgressDialog.show(this, "Loading Messages",
                "Please wait...", true, true, new OnCancelListener() {

        	public void onCancel(DialogInterface dialog) {
									
			}
        });*/
        
        update = false;
        runThread = new ConversationLoader(this, update, handler);
       
        //View header = (View)getLayoutInflater().inflate(R.layout.contact_message, null);
        //list.addHeaderView(header);
        
        /*
         * Load the selected conversation thread when clicked
         */
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {

                final Intent intent = new Intent(ConversationView.this.getBaseContext(), SendMessageActivity.class);
                intent.putExtra(ConversationView.MESSAGE_INTENT, ConversationView.MESSAGE_VIEW);
                intent.putExtra(ConversationView.selectedNumberIntent, msgList.get(position)[0]);
                ConversationView.this.startActivity(intent);
            }
        });
        
        emptyList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				switch (position)
				{
					case ADD_CONTACT:
						//Launch add contacts
						AddContact.addContact = true;
		                AddContact.editTc = null;
						ConversationView.this.startActivity(new Intent(
		                		ConversationView.this.getBaseContext(), 
		                		AddContact.class));
					break;
					case IMPORT_CONTACT:
						//Launch import contacts
						ConversationView.this.startActivity(new Intent(
		                		ConversationView.this.getBaseContext(), 
		                		ImportContacts.class));
					break;
				}
			}
		});
    }

    /**
     * Updates the list of the messages in the main inbox and in the secondary
     * inbox that the user last viewed, or is viewing
     * 
     * @param list The ListView for this activity to update the message list
     */
    public static void updateList(final Context context, final boolean messageViewUpdate)
    {
        if (MessageReceiver.myActivityStarted)
        {
        	if(conversations != null)
            {
            	runThread.setUpdate(true);
            }
            
            runThread.setStart(false);
            
            if (messageViewUpdate)
            {
                SendMessageActivity.updateList();
            }
        }
    }

    @Override
    protected void onResume()
    {
    	MessageService.mNotificationManager.cancel(MessageService.MULTI);
    	if(conversations != null || ap != null)
    	{
    		updateList(this, false);
    	}
    	
    	checkDefault();
    	
        super.onResume();
        
        // Display the key exchange instructions if step 1&2 of tutorial already shown
        if ((Walkthrough.hasShown(Step.INTRO, this) && Walkthrough.hasShown(Step.START_IMPORT, this))
                && !Walkthrough.hasShown(Step.START_EXCHANGE, this))
        {
       		Walkthrough.show(Step.START_EXCHANGE, this);
        }

        // Don't show the introduction before the EULA
        PackageInfo versionInfo = getPackageInfo();
        final String eulaKey = "eula_" + versionInfo.versionCode;
        final String betaKey = "beta_notice_" + versionInfo.versionCode;
        if (sharedPrefs.getBoolean(eulaKey, false) && sharedPrefs.getBoolean(betaKey, false))
        {
            // Display the walkthrough tutorial introduction
            displayIntro();
        }
        
        // Display the last step of the tutorial upon successful key exchange
        DBAccessor dba = new DBAccessor(this);
        if ( (Walkthrough.hasShown(Step.ACCEPT, this) || Walkthrough.hasShown(Step.SET_SECRET, this))  
                && (! Walkthrough.hasShown(Step.SUCCESS, this)) && dba.anyTrusted() )
        {
            Walkthrough.show(Step.SUCCESS, this);
            Walkthrough.show(Step.CLOSE, this);
        }
    }

    @Override
    protected void onPause()
    {
    	 super.onPause();
    }
    
    @Override
    protected void onDestroy()
    {
        this.stopService(new Intent(this, MessageService.class));
        
        conversations = null;
        //this.unbindService(null);
        MessageReceiver.myActivityStarted = false;
        
        runThread.setRunner(false);
        messageSender.setRunner(false);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.texting_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
    	
    	Intent i = null;
        switch (item.getItemId()) {
            case R.id.compose:
            	i = new Intent(this, SendMessageActivity.class);
            	i.putExtra(MESSAGE_INTENT, COMPOSE);
                this.startActivity(i);
                return true;
            case R.id.settings:
                this.startActivity(new Intent(this, QuickPrefsActivity.class));
                return true;
            case R.id.exchange:
            	this.startActivity(new Intent(this, KeyExchangeManager.class));
            	return true;
            case R.id.key_exchange:
            	i = new Intent(this, SendMessageActivity.class);
            	i.putExtra(MESSAGE_INTENT, NEW_KEY_EXCHANGE);
                this.startActivity(i);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private PackageInfo getPackageInfo() {
        PackageInfo pi = null;
        try {
             pi = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi;
    }
    
    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    private void setTextColor(TextView text)
    {
    	if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1)
    	{
    		text.setTextColor(this.getResources().getColor(R.color.White));
    	}
    }
    
    public void getEULA()
    {
    	PackageInfo versionInfo = getPackageInfo();
    	final String eulaKey = "eula_" + versionInfo.versionCode;
    	boolean hasBeenShown = sharedPrefs.getBoolean(eulaKey, false);

    	if(hasBeenShown == false){
    		
    		final TextView textBox = new TextView(this);	
    		String licenseMessage = this.getString(R.string.eula_message);
    		final SpannableString license = new SpannableString(licenseMessage);
    		Linkify.addLinks(license, Linkify.WEB_URLS);
    		
    		textBox.setText(license);
    		
    		int horDimen = Math.round(this.getResources().getDimension(R.dimen.activity_horizontal_margin));
    		int verDimen = Math.round(this.getResources().getDimension(R.dimen.activity_vertical_margin));
    		textBox.setPadding(horDimen, verDimen, horDimen, verDimen);

    		textBox.setMovementMethod(LinkMovementMethod.getInstance());
    		setTextColor(textBox);
    		textBox.setTextSize(18);
    		textBox.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
    		

	    	String title = this.getString(R.string.eula_title);
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this)
	        .setTitle(title)
	        .setCancelable(true)
	        .setView(textBox)
	        .setPositiveButton(R.string.accept, new Dialog.OnClickListener() {
	
				@Override
				public void onClick(DialogInterface dialog, int which) {
	                SharedPreferences.Editor editor = sharedPrefs.edit();
	                editor.putBoolean(eulaKey, true);
	                editor.commit();
	                
	                // Display the beta Notice dialog
	                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
	                {
	                    betaNotice();
	                }
	                
	                //If api level > kitkat check if tinfoil-sms is default SMS.
	                //checkDefault();
				}
	        })
	        .setOnCancelListener(new OnCancelListener(){

				@Override
				public void onCancel(DialogInterface arg0) {
					// Close the activity as they have declined the EULA
	                ConversationView.this.finish();
				}	        	
	        })
	        .setNegativeButton(R.string.refuse, new Dialog.OnClickListener() {
	
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                // Close the activity as they have declined the EULA
	                ConversationView.this.finish();
	            }
	        });
	    	builder.create().show();
        }
    	else
    	{
    		// Check if Tinfoil-SMS is default SMS app.
    		//checkDefault();
    	}
    }

	public void checkDefault()
    {
    	if(!SMSUtility.checkDefault(this)) {
	        	
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this)
		        .setTitle(R.string.kitkat_dialog_title)
		        .setCancelable(true)
		        .setMessage(R.string.kitkat_dialog_message)
		        .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
		
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
	                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, 
	                    		getPackageName());
	                    startActivity(intent);
	                    // Display the beta Notice dialog
	                    betaNotice();
					}
		        })
		        .setOnCancelListener(new OnCancelListener(){
	
					@Override
					public void onCancel(DialogInterface arg0) {
						// Close the activity since they refuse to set to default
		                ConversationView.this.finish();
					}	        	
		        })
		        .setNegativeButton(android.R.string.no, new Dialog.OnClickListener() {
		
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		                // Close the activity since they refuse to set to default
		                ConversationView.this.finish();
		            }
		        })
		        .setNeutralButton(R.string.tell_me_more, new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String url = "https://github.com/tinfoilhat/tinfoil-sms/wiki/Android-KitKat-Support";
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						ConversationView.this.startActivity(i);
						
	                    // Display the beta Notice dialog
                        //betaNotice();
					}
		        	
		        });
		    	builder.create().show();
    	}
    }

    /**
     * Display a BETA notice with information about how they can provide feedback, 
     * translations, and other support to help improve the app.
     */
    public void betaNotice()
    {
        PackageInfo versionInfo = getPackageInfo();
        final String betaKey = "beta_notice_" + versionInfo.versionCode;
        boolean hasBeenShown = sharedPrefs.getBoolean(betaKey, false);

        if (hasBeenShown == false)
        {
            final TextView textBox = new TextView(this);    
            String betaMessage = this.getString(R.string.beta_notice_message);
            final SpannableString message = new SpannableString(betaMessage);
            Linkify.addLinks(message, Linkify.ALL);
            
            textBox.setText(message);
            
            int horDimen = Math.round(this.getResources().getDimension(R.dimen.activity_horizontal_margin));
            int verDimen = Math.round(this.getResources().getDimension(R.dimen.activity_vertical_margin));
            textBox.setPadding(horDimen, verDimen, horDimen, verDimen);
    
            textBox.setMovementMethod(LinkMovementMethod.getInstance());
            setTextColor(textBox);
            textBox.setTextSize(18);
            textBox.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
    
            String title = this.getString(R.string.beta_notice_title);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle(title)
            .setCancelable(true)
            .setView(textBox)
            .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
    
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean(betaKey, true);
                    editor.commit();
                    // Display the walkthrough tutorial introduction
                    displayIntro();
                }
            })
            .setOnCancelListener(new OnCancelListener(){
                @Override
                public void onCancel(DialogInterface arg0) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean(betaKey, true);
                    editor.commit();
                    // Display the walkthrough tutorial introduction
                    displayIntro();
                }               
            });
            builder.create().show();
        }
    }
    
    /**
     * Displays the introduction to the walkthrough tutorial after accepting the EULA.
     */
    public void displayIntro()
    {
        // If tutorial enabled display the first two steps of the tutorial
        if (! (Walkthrough.hasShown(Step.INTRO, this) && Walkthrough.hasShown(Step.START_IMPORT, this)) )
        {
            Walkthrough.show(Step.INTRO, this);
            Walkthrough.show(Step.START_IMPORT, this);
        }
    }
    
	/**
	 * The handler class for cleaning up after the loading thread as well as the
	 * update thread.
	 */
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(final android.os.Message msg)
        {
        	if(msgList.isEmpty())
        	{
        		/*
        		 * The user has no contacts show the default list items to allow
        		 * for quick import or addition of contacts.
        		 */
        		List<String> emptyItems = new ArrayList<String>();
        		
        		emptyItems.add(ConversationView.this.getString(R.string.add_contacts_list));
        		emptyItems.add(ConversationView.this.getString(R.string.import_contacts_list));
        		
        		ap = new DefaultListAdapter(ConversationView.this, R.layout.empty_list_item, emptyItems);
        		
        		
                emptyList.setAdapter(ap);
        		emptyList.setVisibility(ListView.VISIBLE);
        		
        		list.setVisibility(ListView.INVISIBLE);
        		//ConversationView.this.dialog.dismiss();
        	}
        	else
        	{
        		//The user has contacts step-up the list view for the conversations
        		emptyList.setVisibility(ListView.INVISIBLE);
        		list.setVisibility(ListView.VISIBLE);
        		
	        	switch (msg.what){
	        	case LOAD:
	        		//First time load, the adapter must be constructed
	        		conversations = new ConversationAdapter(ConversationView.this, R.layout.listview_item_row, msgList);
	        		list.setAdapter(conversations);
	        		/*if(ConversationView.this.dialog.isShowing())
	        		{
	        			ConversationView.this.dialog.dismiss();
	        		}*/
		        	break;
	        	case UPDATE:
	        		//The list's data has changed and needs to be updated
	        		
	        		if(conversations != null)
	        		{
		        		conversations.clear();
		                conversations.addData(msgList);
	        		}
	        		else
	        		{
	        			conversations = new ConversationAdapter(ConversationView.this, R.layout.listview_item_row, msgList);
		        		list.setAdapter(conversations);
	        		}
	        		break;
	        	}
        	}
        }
    };
}
