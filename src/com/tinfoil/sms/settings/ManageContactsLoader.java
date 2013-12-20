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

package com.tinfoil.sms.settings;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.tinfoil.sms.R;
import com.tinfoil.sms.adapter.ManageContactAdapter;
import com.tinfoil.sms.dataStructures.ContactChild;
import com.tinfoil.sms.dataStructures.ContactParent;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.loader.OnFinishedTaskListener;

/**
 * Loads in the contacts from the database and formats them.
 * 
 */
public class ManageContactsLoader extends AsyncTask<Activity, Void, Boolean> {

	public static final String EMPTYLIST = "emptyListValues";
	public boolean[] trusted;
	private String emptyListValue = "";
	private Activity context;
	private OnFinishedTaskListener listener;

	@Override
	protected Boolean doInBackground(Activity... params) {
		// ((ManageContactsActivity)activity).showProgress(true);
		context = params[0];

		DBAccessor loader = new DBAccessor(context);

		/*
		 * Load the list of contacts
		 */
		if (ManageContactsActivity.exchange) {
			ManageContactsActivity.tc = loader.getAllRows(DBAccessor.UNTRUSTED);
			emptyListValue = context.getString(R.string.empty_loader_value);
		} else {
			ManageContactsActivity.tc = loader.getAllRows(DBAccessor.TRUSTED);
			emptyListValue = context
					.getString(R.string.empty_loader_trusted_value);
		}

		if (ManageContactsActivity.tc != null) {
			ManageContactsActivity.contacts = new ArrayList<ContactParent>();
			int size = 0;

			for (int i = 0; i < ManageContactsActivity.tc.size(); i++) {
				size = ManageContactsActivity.tc.get(i).getNumber().size();

				ManageContactsActivity.contactNumbers = new ArrayList<ContactChild>();

				trusted = loader.isNumberTrusted(ManageContactsActivity.tc.get(
						i).getNumber());

				for (int j = 0; j < size; j++) {
					// TODO change to use primary key from trusted contact table
					ManageContactsActivity.contactNumbers.add(new ContactChild(
							ManageContactsActivity.tc.get(i).getNumber(j),
							trusted[j], false));
				}
				ManageContactsActivity.contacts.add(new ContactParent(
						ManageContactsActivity.tc.get(i).getName(),
						ManageContactsActivity.contactNumbers));
			}
			return true;
		}
		return false;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		Button encry = (Button) context.findViewById(R.id.exchange_keys);
		if (success) {
			// TODO disable the key exchange button until an item is actually
			// selected.

			//((ManageContactsActivity) context).showProgress(false);
			/* Handle UI update once the thread has finished querying the data */

			encry.setEnabled(true);
			ManageContactsActivity.adapter = new ManageContactAdapter(context,
					ManageContactsActivity.contacts);
			ManageContactsActivity.adapter.notifyDataSetChanged();
		}
		else {

			encry.setEnabled(false);

			if (emptyListValue == null) {
				emptyListValue = context.getString(R.string.empty_list_value);
			}
			ManageContactsActivity.arrayAp = new ArrayAdapter<String>(context,
					android.R.layout.simple_list_item_1,
					new String[] { emptyListValue });

			ManageContactsActivity.arrayAp.notifyDataSetChanged();
		}

		if(listener != null)
		{
			listener.onFinishedTaskListener();
		}
	}
	
	public void setOnFinshedTaskListener(OnFinishedTaskListener listener)
	{
		this.listener = listener;
	}

}