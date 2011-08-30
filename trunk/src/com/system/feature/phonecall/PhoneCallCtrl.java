package com.system.feature.phonecall;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

/**
 * Phone Call Control
 *
 */
public class PhoneCallCtrl
{
	// Get phone call info list
	public static List<PhoneCallInfo> getPhoneCallHistory(Context context)
	{
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, 
				new String[]{CallLog.Calls.NUMBER,      // 0 (phone number)
							 CallLog.Calls.CACHED_NAME, // 1 (associated contact name)
							 CallLog.Calls.TYPE,        // 2 (phone call type: incoming, outgoing or missed)
							 CallLog.Calls.DATE,        // 3 (milliseconds since the epoch)
							 CallLog.Calls.DURATION},   // 4 (seconds)
				null, null,	CallLog.Calls.DEFAULT_SORT_ORDER);
		
		List<PhoneCallInfo> list = new ArrayList<PhoneCallInfo>();
		
		for (int i = 0; i < cursor.getCount(); i++) 
		{   
			cursor.moveToPosition(i);
			
			PhoneCallInfo info = new PhoneCallInfo();
			
			info.number = cursor.getString(0);
			info.contactName = cursor.getString(1);
			info.type = cursor.getInt(2);
			info.date = new Date(Long.parseLong(cursor.getString(3)));
			info.duration = cursor.getInt(4);
			
			//SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			//String time = sfd.format(date);
			
			list.add(info);
		}
		
		return list;
	}
	
	// Get the description of phone call type
	public static String getCallTypeDescription(int type)
	{
		switch (type) 
		{
			case CallLog.Calls.INCOMING_TYPE:
				return "Received";
			case CallLog.Calls.OUTGOING_TYPE:
				return "Called";
			case CallLog.Calls.MISSED_TYPE:
				return "Missed";
			default:
				return "";
		}
	}
	
}
