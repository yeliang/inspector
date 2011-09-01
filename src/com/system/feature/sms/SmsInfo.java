package com.system.feature.sms;

import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import com.system.R;

public class SmsInfo
{
	public String SendPersonName;
	public String phoneNumber;
	public String smsbody;
	public Date date;
	public SmsType type;
	
	public String toString(Context context)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(date.toLocaleString() + "\t ");
		sb.append(typeToString(context, type) + "\t ");
		sb.append(SendPersonName);
		sb.append("(" + phoneNumber + "):\t ");
		sb.append(smsbody);
		
		return sb.toString();
	}
	
	private String typeToString(Context context, SmsType type)
	{
		if (type == SmsType.RECEIVED)
			return context.getResources().getString(R.string.sms_received);
		if (type == SmsType.SENT)
			return context.getResources().getString(R.string.sms_sent);
		else
			return context.getResources().getString(R.string.sms_unknown);
	}

}


