package com.particle.inspector.authsrv;

import java.util.Timer;
import java.util.TimerTask;

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
	
	private Timer mGetInfoTimer;
	private GeneralTask mInfoTask;
	private final long mGetInfoDelay  = 10000; // 10 Seconds
	private final long mGetInfoPeriod = 300000; // 300 Seconds

	@Override
	public IBinder onBind(final Intent intent) {
		Log.d(LOGTAG, "onBind"); 
		return null;
	}
	
	@Override
    public void onDestroy() {  
        Log.d(LOGTAG, "onDestroy");  
    }
	
	@Override  
    public boolean onUnbind(Intent intent) {  
        Log.d(LOGTAG, "onUnbind");  
        return super.onUnbind(intent);  
    }

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(LOGTAG, "created");
		
		mGetInfoTimer = new Timer();
		mInfoTask = new GeneralTask(this);
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);
		Log.i(LOGTAG, "started");
		
		// Start timer 
		mGetInfoTimer.scheduleAtFixedRate(mInfoTask, mGetInfoDelay, mGetInfoPeriod);
		
	}
	
	public class IaiaiBinder extends Binder {  
        public BootService getService() {  
            return BootService.this;  
        }  
    }
}