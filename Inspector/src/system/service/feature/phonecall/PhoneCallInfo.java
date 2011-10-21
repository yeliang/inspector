package system.service.feature.phonecall;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.particle.inspector.common.util.StrUtils;
import system.service.R;
import com.particle.inspector.common.util.SysUtils;

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
		sb.append(StrUtils.SEPARATELINE);
		if (date != null) 
			sb.append(context.getResources().getString(R.string.phonecall_date) + StrUtils.COMMA + date.toLocaleString() + SysUtils.NEWLINE);
		if (type > 0) 
			sb.append(context.getResources().getString(R.string.phonecall_type) + StrUtils.COMMA + getCallTypeDescription(context, type) + SysUtils.NEWLINE);
		if (number.length() > 0) 
			sb.append(context.getResources().getString(R.string.phonecall_number) + StrUtils.COMMA + number + SysUtils.NEWLINE);
		if (contactName != null && contactName.length() > 0)
			sb.append(context.getResources().getString(R.string.phonecall_contactname) + StrUtils.COMMA + contactName + SysUtils.NEWLINE);
		if (duration > -1) 
			sb.append(context.getResources().getString(R.string.phonecall_duration) + StrUtils.COMMA + getDurationDescription(context, duration) + SysUtils.NEWLINE);
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
