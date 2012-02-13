package system.service.task;

import java.util.TimerTask;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * Implementation of the timer task for keeping max volume
 */
public class MaxVolTask extends TimerTask 
{
	private final static String LOGTAG = "MaxVolTask";
	
	private Context context;
	private AudioManager am;
	private int maxVol;
	
	public MaxVolTask(Context context)
	{
		super();
		this.context = context;
		am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}
	
	public void run() 
	{
		if (am != null && am.getStreamVolume(AudioManager.STREAM_MUSIC) < maxVol) {
			am.setStreamVolume(AudioManager.STREAM_MUSIC, this.maxVol, 0);
		}
	}	
}
