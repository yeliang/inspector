package com.particle.inspector.authsrv.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.particle.inspector.authsrv.R;
import com.particle.inspector.authsrv.sqlite.DbHelper;
import com.particle.inspector.authsrv.sqlite.metadata.TKey;
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LicenseCtrl;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class ManageKeyActivity extends Activity 
{
	private static final String LOGTAG = "ManageKeyActivity";
	Context context;
	
	private Button queryKeyButton;
	private Button queryDeviceIdButton;
	private EditText query_editKey;
	private EditText query_editDeviceId;
	
	private EditText value_licenseKey;
	private EditText value_keyType;
	private EditText value_deviceId;
	private EditText value_phoneNum;
	private EditText value_phoneModel;
	private EditText value_androidVer;
	private EditText value_consumeDate;
	private EditText value_recvMail;
	private EditText value_recvPhoneNum;
	
	
   @Override
   public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.managekey);
      
      context = getApplicationContext();
      
      query_editKey      = (EditText) findViewById(R.id.edit_key);
      query_editDeviceId = (EditText) findViewById(R.id.edit_deviceid);
      
	  value_licenseKey  = (EditText) findViewById(R.id.value_licensekey);
	  value_keyType     = (EditText) findViewById(R.id.value_keytype);
	  value_deviceId    = (EditText) findViewById(R.id.value_deviceid);
	  value_phoneNum    = (EditText) findViewById(R.id.value_phonenum);
	  value_phoneModel  = (EditText) findViewById(R.id.value_phonemodel);
	  value_androidVer  = (EditText) findViewById(R.id.value_androidver);
	  value_consumeDate = (EditText) findViewById(R.id.value_consumedate);
	  value_recvMail    = (EditText) findViewById(R.id.value_recvmail);
	  value_recvPhoneNum= (EditText) findViewById(R.id.value_recvphonenum);
      
      queryKeyButton = (Button) findViewById(R.id.querykeybutton);
      queryKeyButton.setOnClickListener(new OnClickListener() {
          public void onClick(final View v) {
    		  // Query by key
    		  String key = query_editKey.getText().toString().toUpperCase().trim();
    		  if (key == null || key.length() <= 0) {
    			  String title = getResources().getString(R.string.error);
    			  String msg = "Please input a valid license key.";
    			  SysUtils.errorDlg(context, title, msg);
    			  return;
    		  }
    		  
			  // Open database
        	  DbHelper db = new DbHelper(v.getContext());
        	  if (!db.isOpen()) { 
        		  if (!db.createOrOpenDatabase()) {
        			  String title = getResources().getString(R.string.error);
        			  String msg = "Cannot open database.";
        			  SysUtils.errorDlg(context, title, msg);
        			  return;
        		  }
        		  
        		  TKey t = db.findKey(key);
        		  if (t != null) {
        			  value_licenseKey.setText(t.getKey());
        			  value_keyType.setText(LicenseCtrl.enumToStr(t.getKeyType()));
        			  value_deviceId.setText(t.getDeviceID());
        			  value_phoneNum.setText(t.getPhoneNum());
        			  value_phoneModel.setText(t.getPhoneModel());
        			  value_androidVer.setText(t.getAndroidVer());
        			  value_consumeDate.setText(t.getConsumeDate());
        			  value_recvMail.setText(t.getRecvMail());
        			  value_recvPhoneNum.setText(t.getRecvPhoneNum());
        		  }
        		  else {
        			  String title = getResources().getString(R.string.warning);
        			  String msg = "Cannot find result.";
        			  SysUtils.warningDlg(context, title, msg);
        		  }
        	  }
          
          }
      });
      
      queryDeviceIdButton = (Button) findViewById(R.id.querydeviceidbutton);
      queryDeviceIdButton.setOnClickListener(new OnClickListener() {
         public void onClick(final View v) {
				// Query by device ID
				String deviceId = query_editDeviceId.getText().toString().trim();
				if (deviceId == null || deviceId.length() <= 0) {
					String title = getResources().getString(R.string.error);
					String msg = "Please input a valid device ID.";
					SysUtils.errorDlg(context, title, msg);
					return;
				}
        	 
				// Open database
				DbHelper db = new DbHelper(v.getContext());
				if (!db.isOpen()) {
					if (!db.createOrOpenDatabase()) {
						String title = getResources().getString(R.string.error);
						String msg = "Cannot open database.";
						SysUtils.errorDlg(context, title, msg);
						return;
					}

					TKey t = db.findDevice(deviceId);
					if (t != null) {
						value_licenseKey.setText(t.getKey());
						value_keyType.setText(LicenseCtrl.enumToStr(t.getKeyType()));
						value_deviceId.setText(t.getDeviceID());
						value_phoneNum.setText(t.getPhoneNum());
						value_phoneModel.setText(t.getPhoneModel());
						value_androidVer.setText(t.getAndroidVer());
						value_consumeDate.setText(t.getConsumeDate());
						value_recvMail.setText(t.getRecvMail());
						value_recvPhoneNum.setText(t.getRecvPhoneNum());
					} else {
						String title = getResources().getString(R.string.warning);
						String msg = "Cannot find result.";
						SysUtils.warningDlg(context, title, msg);
					}
				}
         }
      });
      
      
   }

   
}