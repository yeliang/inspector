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
	
	private Button editButton;
	private Button applyButton;
	private Button delButton;
	private Button insertButton;
	private Button cancelButton;
	
	private TKey oldValue;
	
	
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
	  
	  // Initially these are not editable
	  setEditTextStatus(false);
      
      queryKeyButton = (Button) findViewById(R.id.querykeybutton);
      queryKeyButton.setOnClickListener(new OnClickListener() {
          public void onClick(final View v) {
    		  // Query by key
    		  String key = query_editKey.getText().toString().toUpperCase().trim();
    		  if (key == null || key.length() <= 0) {
    			  String title = getResources().getString(R.string.error);
    			  String msg = "Please input a valid key.";
    			  SysUtils.errorDlg(ManageKeyActivity.this, title, msg);
    			  return;
    		  }
    		  
			  // Open database
        	  DbHelper db = new DbHelper(v.getContext());
        	  if (!db.isOpen()) { 
        		  if (!db.createOrOpenDatabase()) {
        			  String title = getResources().getString(R.string.error);
        			  String msg = "Cannot open database.";
        			  SysUtils.errorDlg(ManageKeyActivity.this, title, msg);
        			  return;
        		  }
        	  }
        	  
        	  TKey t = db.findKey(key);
    		  if (t != null) {
    			  oldValue = t;
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
      });
      
      queryDeviceIdButton = (Button) findViewById(R.id.querydeviceidbutton);
      queryDeviceIdButton.setOnClickListener(new OnClickListener() {
         public void onClick(final View v) {
				// Query by device ID
				String deviceId = query_editDeviceId.getText().toString().trim();
				if (deviceId == null || deviceId.length() <= 0) {
					String title = getResources().getString(R.string.error);
					String msg = "Please input a valid device ID.";
					SysUtils.errorDlg(ManageKeyActivity.this, title, msg);
					return;
				}
        	 
				// Open database
				DbHelper db = new DbHelper(v.getContext());
				if (!db.isOpen()) {
					if (!db.createOrOpenDatabase()) {
						String title = getResources().getString(R.string.error);
						String msg = "Cannot open database.";
						SysUtils.errorDlg(ManageKeyActivity.this, title, msg);
						return;
					}
				}
				
				TKey t = db.findDevice(deviceId);
				if (t != null) {
					oldValue = t;
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
      });
      
      editButton = (Button) findViewById(R.id.editbutton);
      editButton.setOnClickListener(new OnClickListener() {
          public void onClick(final View v) {
        	  setEditTextStatus(true);
        	  
        	  applyButton.setEnabled(true);
        	  cancelButton.setEnabled(true);
        	  editButton.setEnabled(false);
          }
       });
      
      applyButton = (Button) findViewById(R.id.applybutton);
      applyButton.setOnClickListener(new OnClickListener() {
          public void onClick(final View v) {
        	  new AlertDialog.Builder(ManageKeyActivity.this)
				.setTitle(getResources().getString(R.string.info))  
				.setIcon(android.R.drawable.ic_dialog_info)  
				.setMessage(getResources().getString(R.string.confirm_apply)) 
				.setPositiveButton(getResources().getString(android.R.string.yes),  
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i){
							// Open database
							DbHelper db = new DbHelper(v.getContext());
							if (!db.isOpen()) {
								if (!db.createOrOpenDatabase()) {
									String title = getResources().getString(R.string.error);
									String msg = "Cannot open database.";
									SysUtils.errorDlg(ManageKeyActivity.this, title, msg);
									return;
								}
							}
							
							// Start to delete
							int id = oldValue.getId();
							TKey newRecord = new TKey(id, 
									value_licenseKey.getText().toString().trim().toUpperCase(),
									LicenseCtrl.strToEnum(value_keyType.getText().toString().trim()),
									value_deviceId.getText().toString().trim(),
									value_phoneNum.getText().toString().trim(),
									value_phoneModel.getText().toString().trim(),
									value_androidVer.getText().toString().trim(),
									value_consumeDate.getText().toString().trim(),
									value_recvMail.getText().toString().trim(),
									value_recvPhoneNum.getText().toString().trim());
							boolean ret = db.updateById(newRecord);
							if (!ret) {
								String title = getResources().getString(R.string.error);
								String msg = String.format("Cannot update it (ID=%d).", id);
								SysUtils.errorDlg(ManageKeyActivity.this, title, msg);
							} else {
								SysUtils.messageBox(context, getResources().getString(R.string.apply_ok));
							}
							
							// Reset status
							setEditTextStatus(false);
							editButton.setEnabled(true);
				        	cancelButton.setEnabled(false);
						}
					}
				)  
				.setNegativeButton(getResources().getString(android.R.string.no), 
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i){
						
						}
					}
				).show();
          }
       });
      
      delButton = (Button) findViewById(R.id.delbutton);
      delButton.setOnClickListener(new OnClickListener() {
          public void onClick(final View v) {
        	  new AlertDialog.Builder(ManageKeyActivity.this)
				.setTitle(getResources().getString(R.string.info))  
				.setIcon(android.R.drawable.ic_dialog_info)  
				.setMessage(getResources().getString(R.string.confirm_delete)) 
				.setPositiveButton(getResources().getString(android.R.string.yes),  
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i){
							// Open database
							DbHelper db = new DbHelper(v.getContext());
							if (!db.isOpen()) {
								if (!db.createOrOpenDatabase()) {
									String title = getResources().getString(R.string.error);
									String msg = "Cannot open database.";
									SysUtils.errorDlg(ManageKeyActivity.this, title, msg);
									return;
								}
							}
							
							// Start to delete
							int id = oldValue.getId();
							int ret = db.deleteById(id);
							if (ret <= 0) {
								String title = getResources().getString(R.string.error);
								String msg = String.format("Cannot delete it (ID=%d).", id);
								SysUtils.errorDlg(ManageKeyActivity.this, title, msg);
							} else {
								SysUtils.messageBox(context, getResources().getString(R.string.delete_ok));
							}
							
							// Reset status
							cleanEditTextValue();
				        	setEditTextStatus(false);
				            editButton.setEnabled(false);
				            applyButton.setEnabled(false);
				            delButton.setEnabled(false);
				            cancelButton.setEnabled(false);
						}
					}
				)  
				.setNegativeButton(getResources().getString(android.R.string.no), 
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i){
						
						}
					}
				).show();
          }
       });
      
      insertButton = (Button) findViewById(R.id.insertbutton);
      insertButton.setOnClickListener(new OnClickListener() {
          public void onClick(final View v) {
        	  new AlertDialog.Builder(ManageKeyActivity.this)
				.setTitle(getResources().getString(R.string.info))  
				.setIcon(android.R.drawable.ic_dialog_info)  
				.setMessage(getResources().getString(R.string.confirm_insert)) 
				.setPositiveButton(getResources().getString(android.R.string.yes),  
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i){
							// Open database
							DbHelper db = new DbHelper(v.getContext());
							if (!db.isOpen()) {
								if (!db.createOrOpenDatabase()) {
									String title = getResources().getString(R.string.error);
									String msg = "Cannot open database.";
									SysUtils.errorDlg(ManageKeyActivity.this, title, msg);
									return;
								}
							}
							
							// Start to insert
							TKey newRecord = new TKey( 
									value_licenseKey.getText().toString().trim().toUpperCase(),
									LicenseCtrl.strToEnum(value_keyType.getText().toString().trim()),
									value_deviceId.getText().toString().trim(),
									value_phoneNum.getText().toString().trim(),
									value_phoneModel.getText().toString().trim(),
									value_androidVer.getText().toString().trim(),
									value_consumeDate.getText().toString().trim(),
									value_recvMail.getText().toString().trim(),
									value_recvPhoneNum.getText().toString().trim());
							boolean ret = db.insert(newRecord);
							if (!ret) {
								String title = getResources().getString(R.string.error);
								String msg = "Cannot insert it.";
								SysUtils.errorDlg(ManageKeyActivity.this, title, msg);
							} else {
								SysUtils.messageBox(context, getResources().getString(R.string.insert_ok));
								
								// Update oldValue
								if (!db.isOpen()) {
									if (!db.createOrOpenDatabase()) {
										String title = getResources().getString(R.string.error);
										String msg = "Cannot open database.";
										SysUtils.errorDlg(ManageKeyActivity.this, title, msg);
										return;
									}
								}
								oldValue = db.findLastRecord(newRecord.getKey());
							}
							
							// Reset status
							setEditTextStatus(false);
				            editButton.setEnabled(true);
				            applyButton.setEnabled(false);
				            delButton.setEnabled(true);
				            cancelButton.setEnabled(false);
						}
					}
				)  
				.setNegativeButton(getResources().getString(android.R.string.no), 
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i){
						
						}
					}
				).show();
          }
       });
      
      cancelButton = (Button) findViewById(R.id.cancelbutton);
      cancelButton.setOnClickListener(new OnClickListener() {
          public void onClick(final View v) {
        	  // Restore values
        	  value_licenseKey.setText(oldValue.getKey());
        	  value_keyType.setText(LicenseCtrl.enumToStr(oldValue.getKeyType()));
        	  value_deviceId.setText(oldValue.getDeviceID());
        	  value_phoneNum.setText(oldValue.getPhoneNum());
        	  value_phoneModel.setText(oldValue.getPhoneModel());
        	  value_androidVer.setText(oldValue.getAndroidVer());
        	  value_consumeDate.setText(oldValue.getConsumeDate());
        	  value_recvMail.setText(oldValue.getRecvMail());
        	  value_recvPhoneNum.setText(oldValue.getRecvPhoneNum());
        	  
        	  setEditTextStatus(false);
              editButton.setEnabled(true);
              cancelButton.setEnabled(false);
          }
      });
      
      // Set buttons status
      editButton.setEnabled(false);
      applyButton.setEnabled(false);
      delButton.setEnabled(false);
      insertButton.setEnabled(false);
      cancelButton.setEnabled(false);
      
   } // end of OnCreate()
   
   private void setEditTextStatus(boolean enabled) {
	   value_licenseKey.setEnabled(enabled);
	   value_keyType.setEnabled(enabled);
	   value_deviceId.setEnabled(enabled);
	   value_phoneNum.setEnabled(enabled);
	   value_phoneModel.setEnabled(enabled);
	   value_androidVer.setEnabled(enabled);
	   value_consumeDate.setEnabled(enabled);
	   value_recvMail.setEnabled(enabled);
	   value_recvPhoneNum.setEnabled(enabled);
   }
   
   private void cleanEditTextValue() {
	   value_licenseKey.setText("");
	   value_keyType.setText("");
	   value_deviceId.setText("");
	   value_phoneNum.setText("");
	   value_phoneModel.setText("");
	   value_androidVer.setText("");
	   value_consumeDate.setText("");
	   value_recvMail.setText("");
	   value_recvPhoneNum.setText("");
   }

   
}