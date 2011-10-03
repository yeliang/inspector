package com.particle.inspector.authsrv.test;

import java.util.Date;
import java.util.Random;

import com.particle.inspector.authsrv.R;
import com.particle.inspector.authsrv.R.id;
import com.particle.inspector.authsrv.R.layout;
import com.particle.inspector.common.util.sms.AuthSms;
import com.particle.inspector.common.util.sms.SMS_RESULT;
import com.particle.inspector.authsrv.sms.SmsCtrl;
import com.particle.inspector.authsrv.sqlite.DbHelper;
import com.particle.inspector.authsrv.sqlite.metadata.TKey;
import com.particle.inspector.common.util.SysUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SmsTestActivity extends Activity 
{   
	protected static final String LOGTAG = "SmsTestActivity";
	private Button btnTest_Send;

	@SuppressWarnings("unused")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smstest);
        
        btnTest_Send = (Button)findViewById(R.id.btn_sms_test_send);
        btnTest_Send.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		// Success reply
        		AuthSms replySms = new AuthSms("123456ABCDEF", SMS_RESULT.OK, null);
				String reply = replySms.serverSms2Str();
				SmsCtrl.sendSms("18792991610", reply); // *** Cannot send SMS to self ***
        	}
        });
        
        
    }
    
}