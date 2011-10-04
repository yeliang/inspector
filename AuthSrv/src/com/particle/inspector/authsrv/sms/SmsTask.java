package com.particle.inspector.authsrv.sms;

import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import com.particle.inspector.authsrv.activity.GlobalPrefActivity;
import com.particle.inspector.authsrv.config.ConfigCtrl;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.mail.GMailSenderEx;


import android.app.Activity;
import android.app.Service;
import android.util.Log;

/**
 * Timer task for SMS handling
 */
public class SmsTask extends TimerTask 
{
	private final static String LOGTAG = "SmsTask";
	
	public Service service;
	
	public SmsTask(Service service)
	{
		super();
		this.service = service;
	}
	
	public void run() 
	{
		Log.v(LOGTAG, "started");
		
		// Clean the received validation SMS ("<Header>,<KEY>,<LANG>,<DEVICEID>,<PHONENUM>,<PHONEMODEL>,<ANDROIDVERSION>") regularly
		int intervalHours = GlobalPrefActivity.getIntervalInfo(service);
		// Firstly we should make sure the time range (>24H by default)
		Date lastDatetime = new Date(ConfigCtrl.getLastCleanSmsDatetime(service));
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR, intervalHours); // Now - interval(Hours)
		Date now_minus_interval = now.getTime();
		if (now_minus_interval.after(lastDatetime)) // Reached the clean time
		{
			SmsCtrl.deleteAllAuthSMS(service);
		} else {
			Log.v(LOGTAG, "Not reached the valid timing yet. Last time: " + lastDatetime.toString());
			return;
		}
	}
	
}
