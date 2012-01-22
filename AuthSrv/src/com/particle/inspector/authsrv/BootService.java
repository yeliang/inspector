package com.particle.inspector.authsrv;

import java.util.Timer;
import java.util.TimerTask;

import com.particle.inspector.authsrv.activity.GlobalPrefActivity;
import com.particle.inspector.authsrv.task.SmsTask;
import com.particle.inspector.authsrv.task.WarningTask;

import android.app.Service;
import android.content.Context;
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
	
	private Context context;
	
	private Timer mSmsTimer;
	private Timer mWarningTimer;
	private SmsTask mSmsTask;
	private WarningTask mWarningTask;
	
	private final long mDelay  = 30000; // 10 Seconds

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
		
		this.context = getApplicationContext();
		
		mSmsTimer = new Timer();
		mSmsTask = new SmsTask(this.context);
		
		mWarningTimer = new Timer();
		mWarningTask = new WarningTask(this.context);
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		super.onStart(intent, startId);
		
		// -------------------------------------------------------------------------
		// Start timer 
		
		// TODO this feature has not been tested yet
		//int intervalHours4Warning = GlobalPrefActivity.getWarningIntervalHour(context);
		//mWarningTimer.scheduleAtFixedRate(mSmsTask, mDelay, intervalHours4Warning*3600*1000);
		
		// TODO this feature has not been tested yet
		//int intervalHours4Sms = GlobalPrefActivity.getSmsIntervalHour(context);
		//mSmsTimer.scheduleAtFixedRate(mSmsTask, mDelay, intervalHours4Sms*3600*1000);
	}
	
	public class IaiaiBinder extends Binder {  
        public BootService getService() {  
            return BootService.this;  
        }  
    }
}