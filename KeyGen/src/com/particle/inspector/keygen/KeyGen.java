package com.particle.inspector.keygen;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.particle.inspector.keygen.R;
import com.particle.inspector.keygen.util.AesCryptor;
import com.particle.inspector.keygen.util.GMailSenderEx;
import com.particle.inspector.keygen.util.SysUtils;

import android.app.Activity;
import android.os.Bundle;
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
	private Button btnGenerate100;
	private TextView fullLicense;
	private TextView partLicense;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnGenerate = (Button)findViewById(R.id.btn_generate);
        btnGenerate100 = (Button)findViewById(R.id.btn_generate100);
        fullLicense = (TextView)findViewById(R.id.txtFullLicense);
        partLicense = (TextView)findViewById(R.id.txtSmsLicense);
        
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
        			
        			fullLicense.setText(full);
        			partLicense.setText(part);
        		} catch (Exception e) {
        			Log.e(LOGTAG, e.getMessage());
        		}
        	}
        });
        
        btnGenerate100.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		// Networks must be available
    			if (!SysUtils.isNetworkConnected(v.getContext())) {
    				SysUtils.messageBox(v.getContext(), getResources().getString(R.string.send_networks_unavailable));
    				return;
    			}
        		
        		StringBuilder sb = new StringBuilder();
        		sb.append("Full Key" + "\t" + "Part Key" + SysUtils.NEWLINE);
        		sb.append("------------------------------------------------");
        		
        		try {
        			for (int i = 0; i < KEYS_NUMBER; i++) {
        				long seed = Math.abs((new Random()).nextLong());
        				String hex = Long.toHexString(seed);
        				String clearText = hex.substring(0, KEY_LENGTH/2).toUpperCase(); 
        				String longKey = AesCryptor.encrypt(AesCryptor.defaultSeed, clearText);
        				String full = clearText + longKey.substring(0, KEY_LENGTH/2);
        				String part = clearText + longKey.substring(KEY_LENGTH/2, KEY_LENGTH);
        				sb.append(full + "\t" + part + SysUtils.NEWLINE);
        			}
        			
        			// Send mail
        			GMailSenderEx gmailSender = new GMailSenderEx("richardroky@gmail.com", "yel636636");
                    gmailSender.setFrom("system@gmail.com");
                    gmailSender.setTo(new String[] {"ylssww@126.com"});
                    gmailSender.setSubject("100 spector keys (full and part) - " + (new SimpleDateFormat("yyyyMMdd")).format(new Date()));
                    gmailSender.setBody(sb.toString());
                    
                    if (gmailSender.send()) {
                    	SysUtils.messageBox(v.getContext(), getResources().getString(R.string.send_ok));
                    } else {
                    	SysUtils.messageBox(v.getContext(), getResources().getString(R.string.send_ng));
                    }
        		} catch (Exception e) {
        			Log.e(LOGTAG, e.getMessage());
        		}
        	}
        });
    }
    
}