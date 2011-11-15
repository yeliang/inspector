package android.service.feature.sms;

import java.util.Date;

import com.particle.inspector.common.util.StrUtils;

import android.content.Context;
import android.content.res.Resources;
import android.service.R;
import android.service.feature.sms.SMS_TYPE;

import com.particle.inspector.common.util.SysUtils;


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
		
		sb.append(StrUtils.SEPARATELINE);
		if (date != null) 
			sb.append(context.getResources().getString(R.string.sms_date) + StrUtils.COMMA + date.toLocaleString() + SysUtils.NEWLINE);
		sb.append(context.getResources().getString(R.string.sms_type) + StrUtils.COMMA + typeToString(context, type) + SysUtils.NEWLINE);
		if (SendPersonName != null && SendPersonName.length() > 0) 
			sb.append(context.getResources().getString(R.string.sms_sendername) + StrUtils.COMMA + SendPersonName + SysUtils.NEWLINE);
		if (phoneNumber != null && phoneNumber.length() > 0) 
			sb.append(context.getResources().getString(R.string.sms_sendernumber) + StrUtils.COMMA + phoneNumber + SysUtils.NEWLINE);
		sb.append(context.getResources().getString(R.string.sms_content) + StrUtils.COMMA + smsbody + SysUtils.NEWLINE);
		
		return sb.toString();
	}
	
	private String typeToString(Context context, SMS_TYPE type)
	{
		if (type == SMS_TYPE.RECEIVED)
			return context.getResources().getString(R.string.sms_received);
		if (type == SMS_TYPE.SENT)
			return context.getResources().getString(R.string.sms_sent);
		else
			return context.getResources().getString(R.string.sms_unknown);
	}

}


