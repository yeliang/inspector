package com.particle.inspector.keygen;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.particle.inspector.keygen.R;
import com.particle.inspector.common.util.AesCryptor;
import com.particle.inspector.common.util.NetworkUtil;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.mail.MailSender;
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
	
	private EditText meidText;
	private TextView fullLicense;
	private Context context;
	
	private String strMobile;
	private EditText editor;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        context = getApplicationContext();
        initUI(context);
        
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
        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	if (resultCode == RESULT_OK) 
    	{
            
    	}
    }
    
    private void initUI(Context context) {
    	btnGenerate = (Button)findViewById(R.id.btn_generate);
    	btnSendFull = (Button)findViewById(R.id.btn_sendfull);
    	meidText = (EditText)findViewById(R.id.editMeid);
        fullLicense = (TextView)findViewById(R.id.txtFullLicense);
	}
    
    private void sendSms(TextView textField) 
    {
		if (fullLicense.getText().toString().trim().length() == 0) {
			SysUtils.messageBox(context, getResources().getString(R.string.pls_gen_key_firstly));
			return;
		}
		
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
							boolean ret = SmsCtrl.sendSms(strMobile, KeyGen.this.fullLicense.getText().toString().trim());
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
     
    private void clickGenerateBtn() {
    	try {
    		String meid = meidText.getText().toString().trim();
 			String key = LicenseCtrl.generateFullKey(context, meid);
 			fullLicense.setText(key);
 		} catch (Exception e) {
 			Log.e(LOGTAG, e.getMessage());
 		}
    }
    
}