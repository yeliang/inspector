package com.particle.inspector.keygen;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.particle.inspector.common.util.SysUtils;

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
	
}
