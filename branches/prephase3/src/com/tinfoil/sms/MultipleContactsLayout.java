package com.tinfoil.sms;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.RelativeLayout;


public class MultipleContactsLayout extends RelativeLayout implements Checkable{
	
	public MultipleContactsLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MultipleContactsLayout(Context context) {
		super(context);
	}

	CheckedTextView checkbox;
	

	public boolean isChecked() {
		if(checkbox != null)
		{
			return checkbox.isChecked();
		}
		return false;
	}

	public void setChecked(boolean checked) {
		if(checkbox != null)
		{
			checkbox.setChecked(checked);
		}
		
	}

	public void toggle() {
		if(checkbox != null)
		{
			checkbox.toggle();
		}		
	}
	
	@Override
    protected void onFinishInflate() {
    	super.onFinishInflate();
    	// find checked text view
		int childCount = getChildCount();
		for (int i = 0; i < childCount; ++i) {
			View v = getChildAt(i);
			if (v instanceof CheckedTextView) {
				checkbox = (CheckedTextView)v;
			}
		}    	
    }

}
