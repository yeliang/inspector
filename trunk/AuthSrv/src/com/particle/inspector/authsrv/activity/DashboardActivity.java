package com.particle.inspector.authsrv.activity;

import java.util.Date;
import java.util.Random;

import com.particle.inspector.authsrv.R;
import com.particle.inspector.authsrv.R.id;
import com.particle.inspector.authsrv.R.layout;

import android.app.Activity;
import android.content.Intent;
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
	private Button btnManageDB;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        
        btnSetting = (Button)findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		Intent intent = new Intent().setClass(getApplicationContext(), GlobalPrefActivity.class);
            	startActivity(intent);
        	}
        });
        
        btnManageDB = (Button)findViewById(R.id.btn_managedb);
        btnManageDB.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		Intent intent = new Intent().setClass(getApplicationContext(), ManageDatabaseActivity.class);
            	startActivity(intent);
        	}
        });
    }
    
}