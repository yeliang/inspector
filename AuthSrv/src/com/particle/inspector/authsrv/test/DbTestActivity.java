package com.particle.inspector.authsrv.test;

import java.util.Date;
import java.util.Random;

import com.particle.inspector.authsrv.R;
import com.particle.inspector.authsrv.R.id;
import com.particle.inspector.authsrv.R.layout;
import com.particle.inspector.authsrv.sqlite.DbHelper;
import com.particle.inspector.authsrv.sqlite.metadata.TKey;
import com.particle.inspector.authsrv.util.SysUtils;

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
	private Button btnTest_CleanTableKey;
	private Button btnTest_IsValidKey;

	@SuppressWarnings("unused")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dbtest);
        
        btnTest_CreateDB = (Button)findViewById(R.id.btn_db_test_createdb);
        btnTest_CreateDB.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		DbHelper db = new DbHelper(v.getContext());
        		boolean ret = db.createOrOpenDatabase();
        		String dbPath = db.getDbPath();
        	}
        });
        
        btnTest_DeleteDB = (Button)findViewById(R.id.btn_db_test_deletedb);
        btnTest_DeleteDB.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		DbHelper db = new DbHelper(v.getContext());
        		boolean ret = db.deleteDB();
        	}
        });
        
        btnTest_CreateKeyTable = (Button)findViewById(R.id.btn_db_test_createkeytable);
        btnTest_CreateKeyTable.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		DbHelper db = new DbHelper(v.getContext());
        		boolean ret = db.createOrOpenDatabase();
        		ret = db.createKeyTable();
        	}
        });
        
        btnTest_DropKeyTable = (Button)findViewById(R.id.btn_db_test_dropkeytable);
        btnTest_DropKeyTable.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		DbHelper db = new DbHelper(v.getContext());
        		boolean ret = db.createOrOpenDatabase();
        		ret = db.DropKeyTable();
        	}
        });
        
        btnTest_InsertKey = (Button)findViewById(R.id.btn_db_test_insertkey);
        btnTest_InsertKey.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		DbHelper db = new DbHelper(v.getContext());
        		boolean ret = db.createOrOpenDatabase();
        		TKey key =  new TKey("TheKey4Test", "TheDeviceID4Test", "ThePhoneNumber4Test", 
        				SysUtils.getUniformDatetimeStr(new Date()), 
        				SysUtils.getUniformDatetimeStr(new Date()), 
        				SysUtils.getUniformDatetimeStr(new Date()));
        		ret = db.insert(key);
        	}
        });
        
        btnTest_DeleteKey = (Button)findViewById(R.id.btn_db_test_deletekey);
        btnTest_DeleteKey.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		DbHelper db = new DbHelper(v.getContext());
        		boolean ret = db.createOrOpenDatabase();
        		int count = db.deleteFromKey("TheKey4Test");
        	}
        });
        
        btnTest_CleanTableKey = (Button)findViewById(R.id.btn_db_test_cleantablekey);
        btnTest_CleanTableKey.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		DbHelper db = new DbHelper(v.getContext());
        		boolean ret = db.createOrOpenDatabase();
        		int count = db.cleanTableKey();
        	}
        });
        
        btnTest_IsValidKey = (Button)findViewById(R.id.btn_db_test_isvalidkey);
        btnTest_IsValidKey.setOnClickListener(new OnClickListener() {
        	public void onClick(View v)
        	{
        		DbHelper db = new DbHelper(v.getContext());
        		boolean ret = db.createOrOpenDatabase();
        		ret = db.isValidLicenseKey("TheKey4Test");
        	}
        });
    }
    
}