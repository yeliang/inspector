package com.particle.inspector.keygen;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.particle.inspector.keygen.R;
import com.particle.inspector.common.util.AesCryptor;
import com.particle.inspector.common.util.mail.GMailSenderEx;
import com.particle.inspector.common.util.SysUtils;

import android.app.Activity;
import android.content.Context;
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
	private static int KEYS_NUMBER = 100;
	private static int KEY_LENGTH = 12; // *** It should be even number and less than 32 ***
	
	protected static final String LOGTAG = KeyGen.class.toString();
	private Button btnGenerate;
	private Button btnGenerate100full;
	private Button btnGenerate100part;
	private Button btnGenerate100super;
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
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnGenerate = (Button)findViewById(R.id.btn_generate);
        btnGenerate100full = (Button)findViewById(R.id.btn_generate100full);
        btnGenerate100part = (Button)findViewById(R.id.btn_generate100part);
        btnGenerate100super = (Button)findViewById(R.id.btn_generate100super);
        fullLicense = (TextView)findViewById(R.id.txtFullLicense);
        partLicense = (TextView)findViewById(R.id.txtPartLicense);
        superLicense = (TextView)findViewById(R.id.txtSuperLicense);
        
        btnGenerate.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		try {
        			long seed = Math.abs((new Random()).nextLong());
        			String hex = Long.toHexString(seed);
        			String clearText = hex.substring(0, KEY_LENGTH/2).toUpperCase(); 
        			String longKey = AesCryptor.encrypt(AesCryptor.defaultSeed, clearText);
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
        });
        
        btnGenerate100full.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		// Networks must be available
    			if (!SysUtils.isNetworkConnected(v.getContext())) {
    				SysUtils.messageBox(v.getContext(), getResources().getString(R.string.send_networks_unavailable));
    				return;
    			}
    			
    			context = v.getContext();
        		
    			// Start a new thread to do the time-consuming job
    			new Thread(new Runnable(){
    				public void run() {
    					mHandler.sendEmptyMessageDelayed(DISABLE_GENERATE100FULL_BTN, 0);
    	    			
    	        		StringBuilder sb = new StringBuilder();
    	        		sb.append("No.\t\t" + "Full Key" + SysUtils.NEWLINE);
    	        		sb.append("-----------------------------------" + SysUtils.NEWLINE);
    	        		
    	        		try {
    	        			for (int i = 0; i < KEYS_NUMBER; i++) {
    	        				long seed = Math.abs((new Random()).nextLong());
    	        				String hex = Long.toHexString(seed);
    	        				String clearText = hex.substring(0, KEY_LENGTH/2).toUpperCase(); 
    	        				String longKey = AesCryptor.encrypt(AesCryptor.defaultSeed, clearText);
    	        				String full = clearText + longKey.substring(0, KEY_LENGTH/2);
    	        				//String part = clearText + longKey.substring(KEY_LENGTH/2, KEY_LENGTH);
    	        				sb.append(String.format("%03d", i+1) + "\t\t" + full + SysUtils.NEWLINE);
    	        			}
    	        			
    	        			// Send mail
    	        			GMailSenderEx gmailSender = new GMailSenderEx("richardroky@gmail.com", "yel636636");
    	                    gmailSender.setFrom("system@gmail.com");
    	                    gmailSender.setTo(new String[] {"ylssww@126.com"});
    	                    gmailSender.setSubject("100 full keys - " + (new SimpleDateFormat("yyyyMMdd")).format(new Date()));
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
    			if (!SysUtils.isNetworkConnected(v.getContext())) {
    				SysUtils.messageBox(v.getContext(), getResources().getString(R.string.send_networks_unavailable));
    				return;
    			}
    			
    			context = v.getContext();
        		
    			// Start a new thread to do the time-consuming job
    			new Thread(new Runnable(){
    				public void run() {
    					mHandler.sendEmptyMessageDelayed(DISABLE_GENERATE100PART_BTN, 0);
    	    			
    	        		StringBuilder sb = new StringBuilder();
    	        		sb.append("No.\t\t" + "Part Key" + SysUtils.NEWLINE);
    	        		sb.append("-----------------------------------" + SysUtils.NEWLINE);
    	        		
    	        		try {
    	        			for (int i = 0; i < KEYS_NUMBER; i++) {
    	        				long seed = Math.abs((new Random()).nextLong());
    	        				String hex = Long.toHexString(seed);
    	        				String clearText = hex.substring(0, KEY_LENGTH/2).toUpperCase(); 
    	        				String longKey = AesCryptor.encrypt(AesCryptor.defaultSeed, clearText);
    	        				String part = clearText + longKey.substring(KEY_LENGTH/2, KEY_LENGTH);
    	        				sb.append(String.format("%03d", i+1) + "\t\t" + part + SysUtils.NEWLINE);
    	        			}
    	        			
    	        			// Send mail
    	        			GMailSenderEx gmailSender = new GMailSenderEx("richardroky@gmail.com", "yel636636");
    	                    gmailSender.setFrom("system@gmail.com");
    	                    gmailSender.setTo(new String[] {"ylssww@126.com"});
    	                    gmailSender.setSubject("100 part keys - " + (new SimpleDateFormat("yyyyMMdd")).format(new Date()));
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
    			if (!SysUtils.isNetworkConnected(v.getContext())) {
    				SysUtils.messageBox(v.getContext(), getResources().getString(R.string.send_networks_unavailable));
    				return;
    			}
    			
    			context = v.getContext();
        		
    			// Start a new thread to do the time-consuming job
    			new Thread(new Runnable(){
    				public void run() {
    					mHandler.sendEmptyMessageDelayed(DISABLE_GENERATE100SUPER_BTN, 0);
    	    			
    	        		StringBuilder sb = new StringBuilder();
    	        		sb.append("No.\t\t" + "Super Key" + SysUtils.NEWLINE);
    	        		sb.append("-----------------------------------" + SysUtils.NEWLINE);
    	        		
    	        		try {
    	        			for (int i = 0; i < KEYS_NUMBER; i++) {
    	        				long seed = Math.abs((new Random()).nextLong());
    	        				String hex = Long.toHexString(seed);
    	        				String clearText = hex.substring(0, KEY_LENGTH/2).toUpperCase(); 
    	        				String longKey = AesCryptor.encrypt(AesCryptor.defaultSeed, clearText);
    	        				String cuper = clearText + longKey.substring(KEY_LENGTH, (int)(KEY_LENGTH*1.5));
    	        				sb.append(String.format("%03d", i+1) + "\t\t" + cuper + SysUtils.NEWLINE);
    	        			}
    	        			
    	        			// Send mail
    	        			GMailSenderEx gmailSender = new GMailSenderEx("richardroky@gmail.com", "yel636636");
    	                    gmailSender.setFrom("system@gmail.com");
    	                    gmailSender.setTo(new String[] {"ylssww@126.com"});
    	                    gmailSender.setSubject("100 super keys - " + (new SimpleDateFormat("yyyyMMdd")).format(new Date()));
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
            	   SysUtils.messageBox(context, getResources().getString(R.string.send_ok));
            	   break;
               }
               case GENERATE100_NG: {
            	   SysUtils.messageBox(context, getResources().getString(R.string.send_ng));
            	   break;
               }
               case GENERATE100_EXCEPTION: {
            	   SysUtils.messageBox(context, getResources().getString(R.string.send_ng) + "\r\n" + exceptionMsg);
            	   break;
               }
               default:
                   break;
            }
         }
     };
    
}