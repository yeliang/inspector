package com.particle.inspector.authsrv.task;

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
import com.particle.inspector.authsrv.sms.SmsCtrl;
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.SysUtils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.util.Log;

/**
 * Timer task for warning that duplicated license key has been registered for more than 3 times
 */
public class WarningTask extends TimerTask 
{
	private final static String LOGTAG = "WarningTask";
	
	public Context context;
	
	public WarningTask(Context context)
	{
		super();
		this.context = context;
	}
	
	public void run() 
	{	
		
	}
	
}
