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

package com.tinfoil.sms.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tinfoil.sms.R;

public class DefaultListAdapter extends ArrayAdapter<String>{

	private static class MessageHolder
    {
    	TextView displayMessage;
    }
	
    private Context context; 
    private int layoutResourceId;    
    private List<String> data = null;
    
    public DefaultListAdapter(Context context, int layoutResourceId, List<String> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MessageHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new MessageHolder();
            holder.displayMessage = (TextView)row.findViewById(R.id.empty_item);
            
            row.setTag(holder);
        }
        else
        {
            holder = (MessageHolder)row.getTag();
        }
        
        String contact = data.get(position);
        
        holder.displayMessage.setText(contact);

        //holder.displayMessage.setTextColor(Color.BLACK);
        
        return row;
    }
}