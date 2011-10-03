package com.system.feature.sms;

import java.util.Date;

import android.content.Context;
import android.content.res.Resources;

import com.particle.inspector.common.util.sms.SMS_TYPE;

import com.system.R;

public class SmsInfo
{
	public String SendPersonName;
	public String phoneNumber;
	public String smsbody;
	public Date date;
	public SMS_TYPE type;
	
	public String toString(Context context)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(date.toLocaleString() + "\t");
		sb.append(typeToString(context, type) + "\t");
		if (SendPersonName != null && SendPersonName.length() > 0) sb.append(SendPersonName);
		if (phoneNumber != null && phoneNumber.length() > 0) sb.append("(" + phoneNumber + "):\t");
		sb.append(smsbody);
		
		return sb.toString();
	}
	
	private String typeToString(Context context, SMS_TYPE type)
	{
		if (type == SMS_TYPE.CLIENT)
			return context.getResources().getString(R.string.sms_received);
		if (type == SMS_TYPE.SERVER)
			return context.getResources().getString(R.string.sms_sent);
		else
			return context.getResources().getString(R.string.sms_unknown);
	}

}

