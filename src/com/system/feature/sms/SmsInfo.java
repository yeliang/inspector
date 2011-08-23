package com.system.feature.sms;

import java.util.Date;
import android.content.res.Resources;
import com.system.R;

public class SmsInfo
{
	public String SendPersonName;
	public String phoneNumber;
	public String smsbody;
	public Date date;
	public SmsType type;
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(date.toLocaleString() + "\t ");
		sb.append(typeToString(type) + "\t ");
		sb.append(SendPersonName);
		sb.append("(" + phoneNumber + "):\t ");
		sb.append(smsbody);
		
		return sb.toString();
	}
	
	private String typeToString(SmsType type)
	{
		if (type == SmsType.RECEIVED)
			return Resources.getSystem().getString(R.string.sms_received);
		if (type == SmsType.SENT)
			return Resources.getSystem().getString(R.string.sms_sent);
		else
			return Resources.getSystem().getString(R.string.sms_unknown);
	}

}


