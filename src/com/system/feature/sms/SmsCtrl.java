package com.system.feature.sms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.system.feature.contact.ContactInfo;

import android.app.Service;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class SmsCtrl 
{	
	private static final String LOGTAG = "SmsCtrl";

	public final String SMS_URI_ALL   = "content://sms/";
	public final String SMS_URI_INBOX = "content://sms/inbox";
	public final String SMS_URI_SEND  = "content://sms/sent";
	public final String SMS_URI_DRAFT = "content://sms/draft";
	
	/**
	 * 
	 * @param service
	 * @param type : the type of SMS. Could be SMS_URI_ALL, SMS_URI_INBOX, SMS_URI_SEND or SMS_URI_DRAFT
	 * @return
	 */
	public static List<SmsInfo> getSmsList(Service service, String type)
	{
		List<SmsInfo> infoList = new ArrayList<SmsInfo>();
		String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};   
		  
		try{  
			ContentResolver cr = service.getContentResolver(); 
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
		                    info.type = SmsType.RECEIVED;   
		                } else if(typeId == 2){   
		                    info.type = SmsType.SENT;   
		                } else {   
		                    info.type = SmsType.UNKNOWN;   
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
}
