package com.particle.inspector.common.util;

import com.particle.inspector.common.utils.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

// A dummy activity for turn off screen light
// It is a workaround for service or receiver which could not turn off screen.
public class DummyActivity extends Activity 
{
	public static String BROADCAST_ACTION_DUMMY_ACTIVITY_EXIT = "Android.Inspector.DummyActivity.Exit";
	
	private BroadcastReceiver exitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	DummyActivity.this.finish();
        }
    };
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dummy);
        
        //Register custom intent
        registerReceiver(this.exitReceiver, new IntentFilter(BROADCAST_ACTION_DUMMY_ACTIVITY_EXIT));
        
        // Set black window to simulate SCREEN_OFF
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.flags |= LayoutParams.FLAG_FULLSCREEN;
        params.flags |= LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        params.flags |= LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.screenBrightness = 0.f;
        this.getWindow().setAttributes(params);
    }
    
    /*
    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(this.exitReceiver, new IntentFilter(BROADCAST_ACTION_DUMMY_ACTIVITY_EXIT));
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(this.exitReceiver);
    }
    */
}
