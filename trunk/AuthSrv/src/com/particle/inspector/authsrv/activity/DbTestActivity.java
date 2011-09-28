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

public class DbTestActivity extends Activity 
{   
	protected static final String LOGTAG = "DbTestActivity";
	private Button btnTest_CreateDB;
	private Button btnTest_DeleteDB;
	private Button btnTest_CreateKeyTable;
	private Button btnTest_DropKeyTable;
	private Button btnTest_InsertKey;
	private Button btnTest_DeleteKey;
	private Button btnTest_IsValidKey;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dbtest);
        
        btnTest_CreateDB = (Button)findViewById(R.id.btn_db_test_createdb);
        btnTest_CreateDB.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		
        	}
        });
        
        btnTest_DeleteDB = (Button)findViewById(R.id.btn_db_test_deletedb);
        btnTest_DeleteDB.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		
        	}
        });
        
        btnTest_CreateKeyTable = (Button)findViewById(R.id.btn_db_test_createkeytable);
        btnTest_CreateKeyTable.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		
        	}
        });
        
        btnTest_DropKeyTable = (Button)findViewById(R.id.btn_db_test_dropkeytable);
        btnTest_DropKeyTable.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		
        	}
        });
        
        btnTest_InsertKey = (Button)findViewById(R.id.btn_db_test_insertkey);
        btnTest_InsertKey.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		
        	}
        });
        
        btnTest_DeleteKey = (Button)findViewById(R.id.btn_db_test_deletekey);
        btnTest_DeleteKey.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		
        	}
        });
        
        btnTest_IsValidKey = (Button)findViewById(R.id.btn_db_test_isvalidkey);
        btnTest_IsValidKey.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		
        	}
        });
    }
    
}