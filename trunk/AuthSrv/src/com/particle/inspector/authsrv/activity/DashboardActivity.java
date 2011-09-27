package com.particle.inspector.authsrv.activity;

import java.util.Date;
import java.util.Random;

import com.particle.inspector.authsrv.R;
import com.particle.inspector.authsrv.R.id;
import com.particle.inspector.authsrv.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DashboardActivity extends Activity 
{   
	protected static final String LOGTAG = "Dashboard";
	private Button btnSetting;
	private int KEY_LENGTH = 12; // *** It should be even number and less than 32 ***
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnSetting = (Button)findViewById(R.id.btn_setting);
        
        btnSetting.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		
        	}
        });
    }
    
}