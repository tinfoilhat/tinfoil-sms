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

package com.tinfoil.sms.adapter;

import com.tinfoil.sms.R;
import com.tinfoil.sms.R.id;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class TrustedAdapter extends ArrayAdapter<String> {
	
	private int layoutResourceId;
	private Context context;
	private String[] names;
	private boolean[] trusted;
	
	/*
	 * Need list of names and whether they are trusted or not
	 */
	public TrustedAdapter(Context context, int layoutResourceId, String[] names, boolean[] trusted) {
        super(context, layoutResourceId, names);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.names = names;
        this.trusted = trusted;
    }
	
	 @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View row = convertView;
	        TrustContactHolder holder = null;
	        
	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	            row = inflater.inflate(layoutResourceId, parent, false);
	            
	            holder = new TrustContactHolder();
	            holder.name = (CheckedTextView)row.findViewById(R.id.trust_name);
	            holder.indicator = (TextView)row.findViewById(R.id.trust_indicator);
	            //holder.box = (CheckBox)row.findViewById(R.id.trusted_checkBox);
	            //holder.box = (CheckBox)row.findViewById(R.id.checkBox1);
	            
	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (TrustContactHolder)row.getTag();
	        }
	        
	        holder.name.setText(names[position]);
	      	holder.indicator.setText(String.valueOf(trusted[position]));
	        //holder.box.setText(names[position]);
	      	
	      	/*if(holder.box.isChecked())
	      	{
	      		holder.box.setChecked(false);
	      	}
	      	else
	      	{
	      		holder.box.setChecked(true);
	      	}*/
	      	
	      	return row;
	    }
	    
	    static class TrustContactHolder
	    {
	    	CheckedTextView name;
	    	TextView indicator;
	    	//CheckBox box;
	    }
	
}
