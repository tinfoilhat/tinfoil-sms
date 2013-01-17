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

package com.tinfoil.sms.contacts;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.tinfoil.sms.R;

public class ManageContactAdapter extends BaseExpandableListAdapter {

    private static class TrustContactHolder
    {
        CheckedTextView name;
        TextView indicator;
    }

    private static class ContactHolder
    {
        TextView name;
        TextView indicator;
    }

    private final LayoutInflater inflater;
    ArrayList<ContactParent> contacts;

    public ManageContactAdapter(final Context context, final ArrayList<ContactParent> contact)
    {
        this.contacts = contact;
        this.inflater = LayoutInflater.from(context);
    }

    public ArrayList<ContactParent> getContacts()
    {
        return this.contacts;
    }

    public Object getChild(final int groupPosition, final int childPosition) {
        return this.contacts.get(groupPosition).getNumbers().get(childPosition);
    }

    public long getChildId(final int groupPosition, final int childPosition) {
        return childPosition;
    }

    public View getChildView(final int groupPosition, final int childPosition,
            final boolean isLastChild, final View convertView, final ViewGroup parent) {

        View row = convertView;
        TrustContactHolder holder = null;

        //if(row == null)
        //{
        row = this.inflater.inflate(R.layout.trusted_contact_manage, parent, false);
        holder = new TrustContactHolder();
        holder.name = (CheckedTextView) row.findViewById(R.id.trust_name);
        holder.indicator = (TextView) row.findViewById(R.id.trust_indicator);

        /*	row.setTag(holder);
        }	
        else
        {
            holder = (TrustContactHolder)row.getTag();
        }*/

        holder.name.setText(this.contacts.get(groupPosition).getNumber(childPosition).getNumber());

        holder.name.setChecked(this.contacts.get(groupPosition).getNumber(childPosition).isSelected());

        holder.indicator.setText(String.valueOf(this.contacts.get(groupPosition).getNumber(childPosition).isTrusted()));

        return row;
    }

    public int getChildrenCount(final int groupPosition) {
        return this.contacts.get(groupPosition).getNumbers().size();
    }

    public Object getGroup(final int groupPosition) {
        return this.contacts.get(groupPosition).getNumbers();
    }

    public int getGroupCount() {
        return this.contacts.size();
    }

    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(final int groupPosition, final boolean isExpanded,
            final View convertView, final ViewGroup parent) {
        View row = convertView;
        ContactHolder holder = null;

        //if(row == null)
        //{
        row = this.inflater.inflate(R.layout.contact_layout, parent, false);
        holder = new ContactHolder();
        holder.name = (TextView) row.findViewById(R.id.contact_name);
        holder.indicator = (TextView) row.findViewById(R.id.contact_indicator);

        //	row.setTag(holder);
        /*}
        else
        {
            holder = (ContactHolder)row.getTag();
        }*/

        holder.name.setText(this.contacts.get(groupPosition).getName());
        holder.indicator.setText(String.valueOf(this.contacts.get(groupPosition).isTrusted()));
        return row;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(final int groupPosition, final int childPosition) {
        return true;
    }
}
