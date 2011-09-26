package com.cryptor;

import java.util.Date;
import java.util.Random;

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
	protected static final String LOGTAG = KeyGen.class.toString();
	private Button btnGenerate;
	private TextView fullLicense;
	private TextView partLicense;
	private int KEY_LENGTH = 12; // *** It should be even number and less than 32 ***
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnGenerate = (Button)findViewById(R.id.btn_generate);
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
    }
    
}