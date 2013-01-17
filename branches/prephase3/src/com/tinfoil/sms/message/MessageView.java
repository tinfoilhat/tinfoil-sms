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

package com.tinfoil.sms.message;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tinfoil.sms.DBAccessor;
import com.tinfoil.sms.ExchangeKey;
import com.tinfoil.sms.Prephase3Activity;
import com.tinfoil.sms.R;
import com.tinfoil.sms.SMSUtility;
import com.tinfoil.sms.contacts.TrustedContact;

/**
 * MessageView activity allows the user to view through all the messages from or
 * to the defined contact. selectedNumber will equal the contact that the
 * messages belong to. If a message is sent or received the list of messages
 * will be updated and Prephase3Activity's messages will be updated as well.
 */
public class MessageView extends Activity {
    private static Button sendSMS;
    private EditText messageBox;
    private static ListView list2;
    private static List<String[]> msgList2;
    private static MessageAdapter messages;
    private static MessageBoxWatcher messageEvent;
    private static final String[] options = new String[] { "Delete message", "Copy message", "Forward message" };
    private static String contact_name;
    private ArrayList<TrustedContact> tc;
    private static AutoCompleteTextView phoneBox;
    private AlertDialog popup_alert;
    private static ExchangeKey keyThread = new ExchangeKey();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Finds the number of the recently sent message attached to the notification
        if (this.getIntent().hasExtra(MessageService.notificationIntent))
        {
            Prephase3Activity.selectedNumber = this.getIntent().getStringExtra(MessageService.notificationIntent);
        }
        else if (this.getIntent().hasExtra(Prephase3Activity.selectedNumberIntent))
        {
            Prephase3Activity.selectedNumber = this.getIntent().getStringExtra(Prephase3Activity.selectedNumberIntent);
        }
        else
        {
            this.finish();
        }

        this.setContentView(R.layout.messageviewer);

        //Sets the keyboard to not pop-up until a text area is selected 
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        MessageService.dba = new DBAccessor(this);
        Prephase3Activity.messageViewActive = true;
        final boolean isTrusted = MessageService.dba.isTrustedContact(Prephase3Activity.selectedNumber);

        messageEvent = new MessageBoxWatcher(this, R.id.word_count, isTrusted);

        /*
         * Create a list of messages sent between the user and the contact
         */
        list2 = (ListView) this.findViewById(R.id.message_list);

        msgList2 = MessageService.dba.getSMSList(Prephase3Activity.selectedNumber);
        final int unreadCount = MessageService.dba.getUnreadMessageCount(Prephase3Activity.selectedNumber);

        Toast.makeText(this, String.valueOf(unreadCount), Toast.LENGTH_SHORT).show();
        messages = new MessageAdapter(this, R.layout.listview_full_item_row, msgList2,
                unreadCount);

        list2.setAdapter(messages);
        list2.setItemsCanFocus(false);

        /*
         * Reset the number of unread messages for the contact to 0
         */
        if (MessageService.dba.getUnreadMessageCount(Prephase3Activity.selectedNumber) > 0)
        {
            //All messages are now read since the user has entered the conversation.
            MessageService.dba.updateMessageCount(Prephase3Activity.selectedNumber, 0);
            if (MessageService.mNotificationManager != null)
            {
                MessageService.mNotificationManager.cancel(MessageService.INDEX);
            }
        }

        //Retrieve the name of the contact from the database
        contact_name = MessageService.dba.getRow(Prephase3Activity.selectedNumber).getName();

        //Set an action for when a user clicks on a message
        list2.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {

                final int item_num = position;

                final AlertDialog.Builder popup_builder = new AlertDialog.Builder(MessageView.this);
                popup_builder.setTitle(contact_name)
                        .setItems(options, new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int which) {

                                final String[] messageValue = (String[]) list2.getItemAtPosition(item_num);

                                //Toast.makeText(MessageView.this, messageValue[1], Toast.LENGTH_SHORT).show();
                                if (which == 0)
                                {
                                    //option = Delete
                                    MessageService.dba.deleteMessage(Long.valueOf(messageValue[3]));
                                    updateList(MessageView.this);
                                }
                                else if (which == 1)
                                {
                                    //TODO implement
                                    //option = Copy message
                                    Toast.makeText(MessageView.this.getBaseContext(), "implement me", Toast.LENGTH_SHORT).show();
                                }
                                else if (which == 2)
                                {

                                    //option = Forward message
                                    phoneBox = new AutoCompleteTextView(MessageView.this.getBaseContext());

                                    List<String> contact = null;
                                    if (MessageView.this.tc == null)
                                    {
                                        MessageView.this.tc = MessageService.dba.getAllRows();
                                    }

                                    if (MessageView.this.tc != null)
                                    {
                                        if (contact == null)
                                        {
                                            contact = SMSUtility.contactDisplayMaker(MessageView.this.tc);
                                        }
                                    }
                                    else
                                    {
                                        contact = null;
                                    }
                                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MessageView.this.getBaseContext(), R.layout.auto_complete_list_item, contact);

                                    phoneBox.setAdapter(adapter);

                                    final AlertDialog.Builder contact_builder = new AlertDialog.Builder(MessageView.this);

                                    contact_builder.setTitle("Input contact number")
                                            .setCancelable(true)
                                            .setView(phoneBox)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {

                                                public void onClick(final DialogInterface dialog, final int which) {
                                                    final String[] info = phoneBox.getText().toString().split(", ");

                                                    boolean invalid = false;
                                                    //TODO identify whether a forwarded message has a special format
                                                    if (info != null)
                                                    {

                                                        if (info.length == 2 && info[1] != null)
                                                        {
                                                            if (SMSUtility.isANumber(info[1]))
                                                            {
                                                                MessageView.this.sendMessage(info[1], messageValue[1]);
                                                            }
                                                            else
                                                            {
                                                                invalid = true;
                                                            }
                                                        }
                                                        else
                                                        {
                                                            final String num = phoneBox.getText().toString();
                                                            if (SMSUtility.isANumber(num))
                                                            {
                                                                MessageView.this.sendMessage(num, messageValue[1]);
                                                            }
                                                            else
                                                            {
                                                                invalid = true;
                                                            }
                                                        }
                                                    }

                                                    if (invalid)
                                                    {
                                                        Toast.makeText(MessageView.this.getBaseContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                            });
                                    final AlertDialog contact_alert = contact_builder.create();

                                    MessageView.this.popup_alert.cancel();
                                    contact_alert.show();
                                }
                            }

                        })
                        .setCancelable(true);
                MessageView.this.popup_alert = popup_builder.create();
                MessageView.this.popup_alert.show();
                /**
                 * TODO implement Going to add a menu of things the user can do
                 * with the messages: 2. Copy the details i. Message ii. Number
                 * 4. View Information i. Time Received/Sent ii. Number
                 */
            }
        });

        /*
         * Link the GUI items to the xml layout
         */
        sendSMS = (Button) this.findViewById(R.id.send);
        this.messageBox = (EditText) this.findViewById(R.id.message);

        final InputFilter[] FilterArray = new InputFilter[1];

        if (isTrusted)
        {
            FilterArray[0] = new InputFilter.LengthFilter(SMSUtility.ENCRYPTED_MESSAGE_LENGTH);
        }
        else
        {
            FilterArray[0] = new InputFilter.LengthFilter(SMSUtility.MESSAGE_LENGTH);
        }

        this.messageBox.setFilters(FilterArray);

        this.messageBox.addTextChangedListener(messageEvent);
        /*
         * Set an action for when the user clicks on the sent button
         */
        sendSMS.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(final View v)
            {
                MessageView.this.sendMessage(Prephase3Activity.selectedNumber, MessageView.this.messageBox.getText().toString());
            }
        });

    }

    public void sendMessage(final String number, final String text)
    {
        if (number.length() > 0 && text.length() > 0)
        {
            //Sets so that a new message sent from the user will not show up as bold
            messages.setCount(0);
            this.messageBox.setText("");
            messageEvent.resetCount();

            //Encrypt the text message before sending it	
            SMSUtility.sendMessage(number, text, this.getBaseContext());
            updateList(this.getBaseContext());
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    public static void updateList(final Context context)
    {
        if (Prephase3Activity.selectedNumber != null)
        {
            msgList2 = MessageService.dba.getSMSList(Prephase3Activity.selectedNumber);
            messages.clear();
            messages.addData(msgList2);

            //TODO fix so when entering MessageView from any state the new messages will show in bold. (until exited message view or user sends out a message)
            MessageService.dba.updateMessageCount(Prephase3Activity.selectedNumber, 0);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.exchange).setChecked(true);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.message_view_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exchange:
                /*
                 * TODO update so that this launches the exchange keys thread.
                 */
                final TrustedContact tc = MessageService.dba.getRow(SMSUtility.format
                        (Prephase3Activity.selectedNumber));

                ExchangeKey.keyDialog = ProgressDialog.show(this, "Exchanging Keys",
                        "Exchanging. Please wait...", true, false);

                if (tc != null)
                {
                    if (!MessageService.dba.isTrustedContact(SMSUtility.format
                            (Prephase3Activity.selectedNumber)))
                    {
                        keyThread.startThread(this, SMSUtility.format(Prephase3Activity.selectedNumber), null);
                    }
                    else
                    {
                        keyThread.startThread(this, null, SMSUtility.format(Prephase3Activity.selectedNumber));
                    }
                }

                return true;
            case R.id.delete:
                /*
                 * TODO add Delete Thread and another option to delete groups of messages within the thread
                 */
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
