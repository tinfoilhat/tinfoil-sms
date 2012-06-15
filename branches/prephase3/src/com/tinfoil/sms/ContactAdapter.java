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

package com.tinfoil.sms;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ContactAdapter extends ArrayAdapter<String>{

    private Context context; 
    private int layoutResourceId;    
    private TrustedContact data = null;
    
    public ContactAdapter(Context context, int layoutResourceId, TrustedContact tc) {
        super(context, layoutResourceId, tc.getNumbers());
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = tc;
        //this.primary = primary;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ContactHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new ContactHolder();
            holder.number = (TextView)row.findViewById(R.id.stored_number);
            holder.type = (TextView)row.findViewById(R.id.primary_number);
            
            row.setTag(holder);
        }
        else
        {
            holder = (ContactHolder)row.getTag();
        }
        
        String number = data.getNumber(position);
        String type = data.getNumber().get(position).getType();
        if (number != null)
        {
        	holder.number.setText(number);
        	holder.type.setText(type);
        }
        return row;
    }
    
    static class ContactHolder
    {
    	TextView number;
    	TextView type;
    }
}