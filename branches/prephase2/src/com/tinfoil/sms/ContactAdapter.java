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

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class ContactAdapter extends ArrayAdapter<String>{

    Context context; 
    int layoutResourceId;    
    List<String> data = null;
    //private String primary;
    
    public ContactAdapter(Context context, int layoutResourceId, List<String> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
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
            //holder.radioButton = (RadioButton)row.findViewById(R.id.primary_number);
            
            row.setTag(holder);
        }
        else
        {
            holder = (ContactHolder)row.getTag();
        }
        
        String number = data.get(position);
        if (number != null)
        {
        	holder.number.setText(number);
        	//holder.radioButton.setChecked(true);
        	/*if (number.equalsIgnoreCase(primary))
        	{
        		holder.radioButton.setChecked(true);
        	}
        	//else
        	{
        		holder.radioButton.setChecked(false);
        	}*/
        }
        return row;
    }
    
    static class ContactHolder
    {
    	TextView number;
    	//RadioButton radioButton;
    }
}