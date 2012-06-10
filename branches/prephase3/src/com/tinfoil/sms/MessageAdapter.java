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
import android.widget.TextView;

public class MessageAdapter extends ArrayAdapter<String[]>{

    private Context context; 
    private int layoutResourceId;    
    private List<String[]> data = null;
    
    public MessageAdapter(Context context, int layoutResourceId, List<String[]> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
    
    public void addData(List<String[]> data)
    {
    	for (int i = 0; i < data.size(); i++)
    	{
    		this.add(data.get(i));
    	}
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
            holder.c_name = (TextView)row.findViewById(R.id.c_name);
            holder.c_message = (TextView)row.findViewById(R.id.c_message);
            
            row.setTag(holder);
        }
        else
        {
            holder = (MessageHolder)row.getTag();
        }
        
        String contact[] = data.get(position);
        holder.c_name.setText(contact[1]);
        holder.c_message.setText(contact[2]);
        
        return row;
    }
    
    static class MessageHolder
    {
    	TextView c_name;
    	TextView c_message;
    }
}