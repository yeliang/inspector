package com.system.feature.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

/**
 * 短信帮助类
 * 
 * @author qianlifeng 2011-3-14
 * */
public class SmsHelper
{
	/**
	 * 获得短信内容
	 * */
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
			tempString += smsMessage[n].getDisplayMessageBody();
		}
		return tempString;
	}

	/**
	 * 获得短信地址
	 * */
	public static String getSmsAddress(Intent intent)
	{
		Bundle bundle = intent.getExtras();
		Object messages[] = (Object[]) bundle.get("pdus");
		return SmsMessage.createFromPdu((byte[]) messages[0])
				.getDisplayOriginatingAddress();
	}

	/**
	 * 发送短信
	 **/
	public static Boolean gendSms(Context context,String addre, String mess)
	{
			try
			{
				PendingIntent mPI = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
				SmsManager.getDefault().sendTextMessage(addre, null, mess, mPI,null);
				return true;
			}
			catch (Exception e)
			{
				return false;
			}
	}
}