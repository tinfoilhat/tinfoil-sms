package com.tinfoil.sms.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.tinfoil.sms.R;

public class DonationPreferenceDialog extends DialogPreference{

	private Context context;
	
	public DonationPreferenceDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context;
		//this.onBindDialogView(view)
	}

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		//Default implementation. If bound to a listener, invokes the handler.
		super.onDialogClosed(positiveResult);
    }
	
	@Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		builder.setTitle(R.string.donate);
		builder.setIcon(R.drawable.donate);
		builder.setMessage(R.string.donate_dialog_message);
		builder.setCancelable(true);
		builder.setIcon(R.drawable.donate);
        builder.setPositiveButton(R.string.paypal_option, 
        		new DialogInterface.OnClickListener() {
    	   @Override
    	   public void onClick(DialogInterface dialog, int id) {
    		   Intent i = new Intent(Intent.ACTION_VIEW);
    		   i.setData(Uri.parse(context.getString(R.string.paypal_url)));
    		   context.startActivity(i);
    	   }
    	});
        builder.setNeutralButton(R.string.flattr_option,
        		new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(context.getString(R.string.flattr_url)));
				context.startActivity(i);
			}
		});
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
    	   @Override
    	   public void onClick(DialogInterface arg0, int arg1) {
    		   // Cancel the donation
    	   }
	    });
        super.onPrepareDialogBuilder(builder);  
    }
	
	/*@Override
	protected void onBindDialogView(View view)
	{
		view.
		super.onBindDialogView(view);
	}*/
}
