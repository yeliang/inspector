package com.particle.inspector.authsrv;

import java.util.Timer;
import java.util.TimerTask;

import com.particle.inspector.authsrv.sms.SmsTask;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Boot service that schedules a timer task
 */
public class BootService extends Service 
{
	private final String LOGTAG = "BootService";
	
	private Timer mTimer;
	private SmsTask mSmsTask;
	private final long mDelay  = 30000; // 10 Seconds
	private final long mPeriod = 300000; // 300 Seconds

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
		
		mTimer = new Timer();
		mSmsTask = new SmsTask(this.getApplicationContext());
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		super.onStart(intent, startId);
		
		// Start timer 
		// TODO this feature has not been tested yet
		//mTimer.scheduleAtFixedRate(mSmsTask, mDelay, mPeriod);
	}
	
	public class IaiaiBinder extends Binder {  
        public BootService getService() {  
            return BootService.this;  
        }  
    }
}