package com.system.feature.phonecall;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.system.utils.StrUtils;
import com.system.R;

import android.content.Context;
import android.provider.CallLog;

public class PhoneCallInfo 
{
	public String number;
	public String contactName;
	public int type;
	public Date date;
	public int duration; //seconds
	
	public String toString(Context context)
	{
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		sb.append(dateFormat.format(date) + "\t");
		sb.append(getCallTypeDescription(context, type) + "\t");
		sb.append(number + "\t");
		if (contactName.length() <= 4) sb.append(contactName + "\t\t");
		else if (contactName.length() <= 8) sb.append(contactName + "\t");
		else sb.append(contactName + "\t");
		sb.append(getDurationDescription(context, duration));
		return sb.toString();
	}
	
	// Get the description of phone call type
	private static String getCallTypeDescription(Context context, int type) 
	{
		switch (type) {
		case CallLog.Calls.INCOMING_TYPE:
			return context.getResources().getString(R.string.phonecall_incoming);
		case CallLog.Calls.OUTGOING_TYPE:
			return context.getResources().getString(R.string.phonecall_outgoing);
		case CallLog.Calls.MISSED_TYPE:
			return context.getResources().getString(R.string.phonecall_missed);
		default:
			return "";
		}
	}
	
	private static String getDurationDescription(Context context, int duration)
	{
		int hour = duration/3600;
		int min  = (duration%3600)/60;
		int sec  = duration%60;
		StringBuilder sb = new StringBuilder();
		if (hour > 0) sb.append(String.format("%d", hour) + context.getResources().getString(R.string.phonecall_hour));
		if (min  > 0) sb.append(String.format("%d", min ) + context.getResources().getString(R.string.phonecall_minute));
		sb.append(String.format("%d", sec ) + context.getResources().getString(R.string.phonecall_second));
		return sb.toString();
	}
}
