package com.tinfoil.sms;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class ManageContactAdaptor extends BaseExpandableListAdapter{

	private LayoutInflater inflater;
	ArrayList<ContactParent> contacts;
	
	public ManageContactAdaptor(Context context, ArrayList<ContactParent> contact)
	{
		this.contacts = contact;
		inflater = LayoutInflater.from(context);
	}
	
	public ArrayList<ContactParent> getContacts()
	{
		return contacts;
	}
	
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return contacts.get(groupPosition).getNumbers().get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return (long)childPosition;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		View row = convertView;
		TrustContactHolder holder = null;
		
		//if(row == null)
		//{
			row = inflater.inflate(R.layout.trusted_contact_manage, parent, false);
			holder = new TrustContactHolder();
			holder.name = (CheckedTextView)row.findViewById(R.id.trust_name);
			holder.indicator = (TextView)row.findViewById(R.id.trust_indicator);
			
		/*	row.setTag(holder);
		}	
		else
        {
            holder = (TrustContactHolder)row.getTag();
        }*/

        holder.name.setText(contacts.get(groupPosition).getNumber(childPosition).getNumber());
        
        holder.name.setChecked(contacts.get(groupPosition).getNumber(childPosition).isSelected());

        holder.indicator.setText(String.valueOf(contacts.get(groupPosition).getNumber(childPosition).isTrusted()));
		
		return row;
	}

	public int getChildrenCount(int groupPosition) {
		return contacts.get(groupPosition).getNumbers().size();
	}

	public Object getGroup(int groupPosition) {
		return contacts.get(groupPosition).getNumbers();
	}

	public int getGroupCount() {
		return contacts.size();
	}

	public long getGroupId(int groupPosition) {
		return (long) groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View row = convertView;
        ContactHolder holder = null;
        
        //if(row == null)
        //{
			row = inflater.inflate(R.layout.contact_layout, parent, false);
			holder = new ContactHolder();
			holder.name = (TextView)row.findViewById(R.id.contact_name);
			holder.indicator = (TextView)row.findViewById(R.id.contact_indicator);
			
		//	row.setTag(holder);
		/*}
        else
        {
            holder = (ContactHolder)row.getTag();
        }*/

        holder.name.setText(contacts.get(groupPosition).getName());
      	holder.indicator.setText(String.valueOf(contacts.get(groupPosition).isTrusted()));
      	return row;
	}

	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}
	
	private static class TrustContactHolder
    {
    	CheckedTextView name;
    	TextView indicator;
    	//CheckBox box;
    }
	
	private static class ContactHolder
	{
		TextView name;
    	TextView indicator;
	}

}