package com.system.feature.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SmsBroadCast extends BroadcastReceiver 
{
	private static final String LOGTAG = "SmsBroadCast";
	
    @Override 
    public void onReceive(Context context, Intent intent) 
    { 
        String dString = SmsHelper.getSmsBody(intent); 
        String address  = SmsHelper.getSmsAddress(intent); 
        Log.i(LOGTAG, dString+","+address); 
        
        //阻止广播继续传递，如果该receiver比系统的级别高， 
        //那么系统就不会收到短信通知了 
        abortBroadcast(); 
    } 
}