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
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.SysUtils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.util.Log;

/**
 * Timer task for SMS handling
 */
public class SmsTask extends TimerTask 
{
	private final static String LOGTAG = "SmsTask";
	
	public Context context;
	
	public SmsTask(Context context)
	{
		super();
		this.context = context;
	}
	
	public void run() 
	{	
		// Clean the received validation SMS ("<Header>,<KEY>,<LANG>,<DEVICEID>,<PHONENUM>,<PHONEMODEL>,<ANDROIDVERSION>") regularly
		int intervalHours = GlobalPrefActivity.getIntervalInfo(context);
		
		// Firstly we should make sure the time range (>24H by default)
		Date lastDatetime = null;
		String lastDatetimeStr = ConfigCtrl.getLastCleanSmsDatetime(context);
		if (lastDatetimeStr.length() > 0) {
			try {
				lastDatetime = DatetimeUtil.format.parse(lastDatetimeStr);
			} catch (Exception ex) {}
		}
		
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR, intervalHours); // Now - interval(Hours)
		Date now_minus_interval = now.getTime();
		if (lastDatetime == null || (lastDatetime != null && now_minus_interval.after(lastDatetime))) // Reached the clean time
		{
			SmsCtrl.deleteAllCheckiinSMS(context);
		} else {
			//Log.v(LOGTAG, "Not reached the valid timing yet. Last time: " + lastDatetime.toString());
			return;
		}
	}
	
}
