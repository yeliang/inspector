package com.particle.inspector.keygen;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.particle.inspector.keygen.R;
import com.particle.inspector.common.util.AesCryptor;
import com.particle.inspector.common.util.NetworkUtil;
import com.particle.inspector.common.util.mail.GMailSenderEx;
import com.particle.inspector.common.util.SysUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class KeyGen extends Activity 
{   
	private static int KEY_LENGTH = 12; // *** It should be even number and less than 32 ***
	
	protected static final String LOGTAG = KeyGen.class.toString();
	private Button btnGenerate;
	private Button btnSendFull;
	private Button btnSendPart;
	private Button btnSendSuper;
	private Button btnGenerate100full;
	private Button btnGenerate100part;
	private Button btnGenerate100super;
	private Button btnSetting;
	private TextView fullLicense;
	private TextView partLicense;
	private TextView superLicense;
	private Context context;
	private String exceptionMsg;
	
	private final int DISABLE_GENERATE100FULL_BTN = 0;
	private final int ENABLE_GENERATE100FULL_BTN  = 1;
	private final int DISABLE_GENERATE100PART_BTN = 2;
	private final int ENABLE_GENERATE100PART_BTN  = 3;
	private final int DISABLE_GENERATE100SUPER_BTN = 4;
	private final int ENABLE_GENERATE100SUPER_BTN  = 5;
	private final int GENERATE100_OK          = 6;
	private final int GENERATE100_NG          = 7;
	private final int GENERATE100_EXCEPTION   = 8;
	
	private String strMobile;
	private EditText editor;
	
	private ProgressDialog progressDialog;

	private TextView textField;
	
	private static AesCryptor cryptor = new AesCryptor();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        context = getApplicationContext();
        initUI(context);
        
        btnSetting.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		Intent intent = new Intent().setClass(getApplicationContext(), GlobalPrefActivity.class);
        		startActivityForResult(intent, R.layout.main);
        	}
        });
        
        btnGenerate.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		clickGenerateBtn();
        	}
        });
        
        btnSendFull.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		sendSms(fullLicense);
        	}
        });
        
        btnSendPart.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		sendSms(partLicense);
        	}
        });
        
        btnSendSuper.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		sendSms(superLicense);
        	}
        });
        
        btnGenerate100full.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		// Networks must be available
    			if (!NetworkUtil.isNetworkConnected(v.getContext())) {
    				SysUtils.messageBox(v.getContext(), getResources().getString(R.string.send_networks_unavailable));
    				return;
    			}
    			
    			progressDialog = ProgressDialog.show(KeyGen.this, getResources().getString(R.string.processing), 
            			getResources().getString(R.string.waiting), true, false); 
    			
    			context = v.getContext();
        		
    			// Start a new thread to do the time-consuming job
    			new Thread(new Runnable(){
    				public void run() {
    					mHandler.sendEmptyMessageDelayed(DISABLE_GENERATE100FULL_BTN, 0);
    					
    					int keyCount = GlobalPrefActivity.getKeyCount(context) > 0 ? GlobalPrefActivity.getKeyCount(context) : GlobalPrefActivity.DEFAULT_KEYS_NUMBER;
    					String senderMailAddr = GlobalPrefActivity.getSenderMail(context);
    					String senderPwd = GlobalPrefActivity.getSenderPwd(context);
    					String receiverMailAddr = GlobalPrefActivity.getReceiverMail(context);
    	    			
    	        		StringBuilder sb = new StringBuilder();
    	        		sb.append("No.\t\t" + "Full Key" + SysUtils.NEWLINE);
    	        		sb.append("-----------------------------------" + SysUtils.NEWLINE);
    	        		
    	        		try {
    	        			for (int i = 0; i < keyCount; i++) {
    	        				long seed = Math.abs((new Random()).nextLong());
    	        				String hex = Long.toHexString(seed);
    	        				String clearText = hex.substring(0, KEY_LENGTH/2).toUpperCase(); 
    	        				String longKey = cryptor.encrypt(clearText);
    	        				String full = clearText + longKey.substring(0, KEY_LENGTH/2);
    	        				sb.append(String.format("%03d", i+1) + "\t\t" + full + SysUtils.NEWLINE);
    	        			}
    	        			
    	        			// Send mail
    	        			GMailSenderEx gmailSender = new GMailSenderEx(senderMailAddr, senderPwd);
    	                    gmailSender.setFrom(GMailSenderEx.DEFAULT_SENDER);
    	                    gmailSender.setTo(new String[] {receiverMailAddr});
    	                    gmailSender.setSubject(String.valueOf(keyCount) + " full keys - " + (new SimpleDateFormat("yyyyMMdd")).format(new Date()));
    	                    gmailSender.setBody(sb.toString());
    	                    
    	                    if (gmailSender.send()) {
    	                    	mHandler.sendEmptyMessageDelayed(GENERATE100_OK, 0);
    	                    } else {
    	                    	mHandler.sendEmptyMessageDelayed(GENERATE100_NG, 0);
    	                    }
    	        		} catch (Exception e) {
    	        			Log.e(LOGTAG, e.getMessage());
    	        			exceptionMsg = e.getMessage();
    	        			mHandler.sendEmptyMessageDelayed(GENERATE100_EXCEPTION, 0);
    	        		} finally {
    	        			mHandler.sendEmptyMessageDelayed(ENABLE_GENERATE100FULL_BTN, 0);
    	        		}
    				}
    			}).start();
    			
    			
        	}
        });
        
        btnGenerate100part.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		// Networks must be available
    			if (!NetworkUtil.isNetworkConnected(v.getContext())) {
    				SysUtils.messageBox(v.getContext(), getResources().getString(R.string.send_networks_unavailable));
    				return;
    			}
    			
    			progressDialog = ProgressDialog.show(KeyGen.this, getResources().getString(R.string.processing), 
            			getResources().getString(R.string.waiting), true, false); 
    			
    			context = v.getContext();
        		
    			// Start a new thread to do the time-consuming job
    			new Thread(new Runnable(){
    				public void run() {
    					mHandler.sendEmptyMessageDelayed(DISABLE_GENERATE100PART_BTN, 0);
    					
    					int keyCount = GlobalPrefActivity.getKeyCount(context) > 0 ? GlobalPrefActivity.getKeyCount(context) : GlobalPrefActivity.DEFAULT_KEYS_NUMBER;
    					String senderMailAddr = GlobalPrefActivity.getSenderMail(context);
    					String senderPwd = GlobalPrefActivity.getSenderPwd(context);
    					String receiverMailAddr = GlobalPrefActivity.getReceiverMail(context);
    	    			
    	        		StringBuilder sb = new StringBuilder();
    	        		sb.append("No.\t\t" + "Part Key" + SysUtils.NEWLINE);
    	        		sb.append("-----------------------------------" + SysUtils.NEWLINE);
    	        		
    	        		try {
    	        			for (int i = 0; i < keyCount; i++) {
    	        				long seed = Math.abs((new Random()).nextLong());
    	        				String hex = Long.toHexString(seed);
    	        				String clearText = hex.substring(0, KEY_LENGTH/2).toUpperCase(); 
    	        				String longKey = cryptor.encrypt(clearText);
    	        				String part = clearText + longKey.substring(KEY_LENGTH/2, KEY_LENGTH);
    	        				sb.append(String.format("%03d", i+1) + "\t\t" + part + SysUtils.NEWLINE);
    	        			}
    	        			
    	        			// Send mail
    	        			GMailSenderEx gmailSender = new GMailSenderEx(senderMailAddr, senderPwd);
    	                    gmailSender.setFrom(GMailSenderEx.DEFAULT_SENDER);
    	                    gmailSender.setTo(new String[] {receiverMailAddr});
    	                    gmailSender.setSubject(String.valueOf(keyCount) + " part keys - " + (new SimpleDateFormat("yyyyMMdd")).format(new Date()));
    	                    gmailSender.setBody(sb.toString());
    	                    
    	                    if (gmailSender.send()) {
    	                    	mHandler.sendEmptyMessageDelayed(GENERATE100_OK, 0);
    	                    } else {
    	                    	mHandler.sendEmptyMessageDelayed(GENERATE100_NG, 0);
    	                    }
    	        		} catch (Exception e) {
    	        			Log.e(LOGTAG, e.getMessage());
    	        			exceptionMsg = e.getMessage();
    	        			mHandler.sendEmptyMessageDelayed(GENERATE100_EXCEPTION, 0);
    	        		} finally {
    	        			mHandler.sendEmptyMessageDelayed(ENABLE_GENERATE100PART_BTN, 0);
    	        		}
    				}
    			}).start();
    			
    			
        	}
        });
        
        btnGenerate100super.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		// Networks must be available
    			if (!NetworkUtil.isNetworkConnected(v.getContext())) {
    				SysUtils.messageBox(v.getContext(), getResources().getString(R.string.send_networks_unavailable));
    				return;
    			}
    			
    			progressDialog = ProgressDialog.show(KeyGen.this, getResources().getString(R.string.processing), 
            			getResources().getString(R.string.waiting), true, false); 
    			
    			context = v.getContext();
        		
    			// Start a new thread to do the time-consuming job
    			new Thread(new Runnable(){
    				public void run() {
    					mHandler.sendEmptyMessageDelayed(DISABLE_GENERATE100SUPER_BTN, 0);
    					
    					int keyCount = GlobalPrefActivity.getKeyCount(context) > 0 ? GlobalPrefActivity.getKeyCount(context) : GlobalPrefActivity.DEFAULT_KEYS_NUMBER;
    					String senderMailAddr = GlobalPrefActivity.getSenderMail(context);
    					String senderPwd = GlobalPrefActivity.getSenderPwd(context);
    					String receiverMailAddr = GlobalPrefActivity.getReceiverMail(context);
    	    			
    	        		StringBuilder sb = new StringBuilder();
    	        		sb.append("No.\t\t" + "Super Key" + SysUtils.NEWLINE);
    	        		sb.append("-----------------------------------" + SysUtils.NEWLINE);
    	        		
    	        		try {
    	        			for (int i = 0; i < keyCount; i++) {
    	        				long seed = Math.abs((new Random()).nextLong());
    	        				String hex = Long.toHexString(seed);
    	        				String clearText = hex.substring(0, KEY_LENGTH/2).toUpperCase(); 
    	        				String longKey = cryptor.encrypt(clearText);
    	        				String cuper = clearText + longKey.substring(KEY_LENGTH, (int)(KEY_LENGTH*1.5));
    	        				sb.append(String.format("%03d", i+1) + "\t\t" + cuper + SysUtils.NEWLINE);
    	        			}
    	        			
    	        			// Send mail
    	        			GMailSenderEx gmailSender = new GMailSenderEx(senderMailAddr, senderPwd);
    	                    gmailSender.setFrom(GMailSenderEx.DEFAULT_SENDER);
    	                    gmailSender.setTo(new String[] {receiverMailAddr});
    	                    gmailSender.setSubject(String.valueOf(keyCount) + " super keys - " + (new SimpleDateFormat("yyyyMMdd")).format(new Date()));
    	                    gmailSender.setBody(sb.toString());
    	                    
    	                    if (gmailSender.send()) {
    	                    	mHandler.sendEmptyMessageDelayed(GENERATE100_OK, 0);
    	                    } else {
    	                    	mHandler.sendEmptyMessageDelayed(GENERATE100_NG, 0);
    	                    }
    	        		} catch (Exception e) {
    	        			Log.e(LOGTAG, e.getMessage());
    	        			exceptionMsg = e.getMessage();
    	        			mHandler.sendEmptyMessageDelayed(GENERATE100_EXCEPTION, 0);
    	        		} finally {
    	        			mHandler.sendEmptyMessageDelayed(ENABLE_GENERATE100SUPER_BTN, 0);
    	        		}
    				}
    			}).start();
    			
    			
        	}
        });
        
        // Execute one click on Generate button on initialization
        clickGenerateBtn();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	if (resultCode == RESULT_OK) 
    	{
            if (GlobalPrefActivity.getKeyCount(context) > 0 &&
                GlobalPrefActivity.getSenderMail(context).length() > 0 &&
                GlobalPrefActivity.getSenderPwd(context).length() > 0 &&
                GlobalPrefActivity.getReceiverMail(context).length() > 0)
            {
              	btnGenerate100full.setEnabled(true);
                btnGenerate100part.setEnabled(true);
                btnGenerate100super.setEnabled(true);
            }
    	}
    }
    
    private void initUI(Context context) {
    	btnGenerate = (Button)findViewById(R.id.btn_generate);
    	btnSendFull = (Button)findViewById(R.id.btn_sendfull);
    	btnSendPart = (Button)findViewById(R.id.btn_sendpart);
    	btnSendSuper = (Button)findViewById(R.id.btn_sendsuper);
        btnGenerate100full = (Button)findViewById(R.id.btn_generate100full);
        btnGenerate100part = (Button)findViewById(R.id.btn_generate100part);
        btnGenerate100super = (Button)findViewById(R.id.btn_generate100super);
        btnSetting =  (Button)findViewById(R.id.btn_setting);
        fullLicense = (TextView)findViewById(R.id.txtFullLicense);
        partLicense = (TextView)findViewById(R.id.txtPartLicense);
        superLicense = (TextView)findViewById(R.id.txtSuperLicense);
        
        if (GlobalPrefActivity.getKeyCount(context) <= 0 ||
            GlobalPrefActivity.getSenderMail(context).length() == 0 ||
            GlobalPrefActivity.getSenderPwd(context).length() == 0 ||
            GlobalPrefActivity.getReceiverMail(context).length() == 0)
        {
        	btnGenerate100full.setEnabled(false);
            btnGenerate100part.setEnabled(false);
            btnGenerate100super.setEnabled(false);
        }
        
	}
    
    private void sendSms(TextView textField) 
    {
		if (textField.getText().toString().length() == 0) {
			SysUtils.messageBox(context, getResources().getString(R.string.pls_gen_key_firstly));
			return;
		}
		
		this.textField = textField;
		
		// Pop up dialog for inputing target mobile number 
		strMobile = "";
		editor = new EditText(context);
		Builder dlg = new AlertDialog.Builder(KeyGen.this)
				.setTitle(getResources().getString(R.string.input_title))  
				.setIcon(android.R.drawable.ic_dialog_info)  
				.setView(editor)  
				.setPositiveButton(getResources().getString(R.string.input_ok),  
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i){
							strMobile = editor.getText().toString();
							if (strMobile.length() == 0) return;
							boolean ret = SmsCtrl.sendSms(strMobile, KeyGen.this.textField.getText().toString());
							if (ret) {
								SysUtils.messageBox(context, getResources().getString(R.string.send_ok));
							} else {
								SysUtils.messageBox(context, getResources().getString(R.string.send_ng));
							}
						}
					}
				)  
				.setNegativeButton(getResources().getString(R.string.input_cancel), 
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i){
						
						}
					}
				); 
		dlg.show();
    }

	// Update UI 
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
               case DISABLE_GENERATE100FULL_BTN: {
            	   btnGenerate100full.setEnabled(false);
                   break;
               }
               case ENABLE_GENERATE100FULL_BTN: {
            	   btnGenerate100full.setEnabled(true);
            	   break;
               }
               case DISABLE_GENERATE100PART_BTN: {
            	   btnGenerate100part.setEnabled(false);
                   break;
               }
               case ENABLE_GENERATE100PART_BTN: {
            	   btnGenerate100part.setEnabled(true);
            	   break;
               }
               case DISABLE_GENERATE100SUPER_BTN: {
            	   btnGenerate100super.setEnabled(false);
                   break;
               }
               case ENABLE_GENERATE100SUPER_BTN: {
            	   btnGenerate100super.setEnabled(true);
            	   break;
               }
               case GENERATE100_OK: {
            	   progressDialog.dismiss();
            	   SysUtils.messageBox(context, getResources().getString(R.string.send_ok));
            	   break;
               }
               case GENERATE100_NG: {
            	   progressDialog.dismiss();
            	   SysUtils.messageBox(context, getResources().getString(R.string.send_ng_ex));
            	   break;
               }
               case GENERATE100_EXCEPTION: {
            	   progressDialog.dismiss();
            	   SysUtils.messageBox(context, getResources().getString(R.string.send_ng_ex) + "\r\n" + exceptionMsg);
            	   break;
               }
               default:
                   break;
            }
         }
     };
     
     private void clickGenerateBtn() {
    	 try {
 			long seed = Math.abs((new Random()).nextLong());
 			String hex = Long.toHexString(seed);
 			String clearText = hex.substring(0, KEY_LENGTH/2).toUpperCase(); 
 			String longKey = cryptor.encrypt(clearText);
 			String full = clearText + longKey.substring(0, KEY_LENGTH/2);
 			String part = clearText + longKey.substring(KEY_LENGTH/2, KEY_LENGTH);
 			String cuper = clearText + longKey.substring(KEY_LENGTH, (int)(KEY_LENGTH*1.5));
 			
 			fullLicense.setText(full);
 			partLicense.setText(part);
 			superLicense.setText(cuper);
 		} catch (Exception e) {
 			Log.e(LOGTAG, e.getMessage());
 		}
     }
    
}