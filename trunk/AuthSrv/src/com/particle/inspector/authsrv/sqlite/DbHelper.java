package com.particle.inspector.authsrv.sqlite;

import java.text.DateFormat;
import java.util.Date;

import com.particle.inspector.authsrv.R;
import com.particle.inspector.authsrv.sqlite.metadata.TKey;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

/**
 * The database path will be: /data/data/com.particle.inspector.authsrv/databases/inspector
 */
public class DbHelper 
{
	private static final String LOGTAG = "DbHelper";
	
	private Context context;
	private SQLiteDatabase db;
	private String dbPath;
	
    private final static String DATABASE_NAME = "inspector.db";
    private final static int DATABASE_VERSION = 1;
    
    private final static String DEFAULT_KEY_TABLE_NAME = "inspector_auth_key";
    public final static String KEY_FIELD_ID = "_id"; 
    public final static String KEY_FIELD_KEY = "licensekey";
    public final static String KEY_FIELD_DEVICE_ID = "deviceid";
    public final static String KEY_FIELD_PHONE_NUMBER = "phonenum";
    public final static String KEY_FIELD_BUY_DATE = "buydate";
    public final static String KEY_FIELD_CONSUME_DATE = "consumedate";
    public final static String KEY_FIELD_LAST_ACTIVATE_DATE = "lastactivatedate";
	
    public DbHelper(Context context)
    {
        this.context = context;
    }
     
    public boolean createOrOpenDatabase() {
    	db = context.openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);
    	if (db.isOpen()) {
    		dbPath = db.getPath();
    		return true;
    	}
    	else return false;
    }
    
    public boolean deleteDB() {
    	// TODO
    	return false;
    }
    
    public boolean createKeyTable() 
    {
        String sql = context.getResources().getString(R.string.sql_create_table_key);
        try {
        	if (db == null) return false;
        	if (!db.isOpen()) {
        		db = SQLiteDatabase.openDatabase(db.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        	}
        	db.execSQL(sql);
        	return true;
        } catch (SQLException e) {
        	Log.e(LOGTAG, e.getMessage());
        	return false;
        }
    }

    public boolean DropKeyTable() 
    {
        String sql = String.format(context.getResources().getString(R.string.sql_drop_table), DEFAULT_KEY_TABLE_NAME);
        try {
        	if (db == null) return false;
        	if (!db.isOpen()) {
        		db = SQLiteDatabase.openDatabase(db.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        	}
        	db.execSQL(sql);
        	return true;
        } catch (SQLException e) {
        	Log.e(LOGTAG, e.getMessage());
        	return false;
        }
    }

    public Cursor selectAll()
    {
        Cursor cursor = db.query(DEFAULT_KEY_TABLE_NAME, null, null, null, null, null, "_id desc");
        return cursor;
    }
    
    public boolean insert(TKey key)
    {
    	try {
    		db.beginTransaction(); 
        	db.execSQL("insert into " + DEFAULT_KEY_TABLE_NAME + "(licensekey,deviceid,phonenum,buydate,consumedate,lastactivatedate) values(?,?,?,?,?,?)",  
            	new Object[] { key.getKey(), key.getDeviceID(), key.getPhoneNum(), 
        			key.getBuyDate(), key.getConsumeDate(), key.getLastActivateDate() });
        	db.setTransactionSuccessful();  
        	db.endTransaction();
        	db.close();
        	return true;
    	} catch (SQLException e) {
    		Log.e(LOGTAG, e.getMessage());
    		return false;
    	}
    }
    
    public int deleteFromKey(long id)
    {
        String where = KEY_FIELD_ID + "=?";
        String[] whereValue = {Long.toString(id)};
        return db.delete(DEFAULT_KEY_TABLE_NAME, where, whereValue);
    }
    
    public int deleteFromKey(String key)
    {
        String where = KEY_FIELD_KEY + "=?";
        String[] whereValue = {key};
        return db.delete(DEFAULT_KEY_TABLE_NAME, where, whereValue);
    }
    
    public int cleanTableKey()
    {
        return db.delete(DEFAULT_KEY_TABLE_NAME, null, null);
    }
    
    public void update(TKey key)
    {
        db.beginTransaction();
        db.execSQL("update " + DEFAULT_KEY_TABLE_NAME + " set key=?,deviceid=?,phonenum=?,buydate=?,consumedate=?,lastactivatedate=? where _id=?",  
                new Object[] { key.getKey(), key.getDeviceID(), key.getPhoneNum(),
        			key.getBuyDate(), key.getConsumeDate(), key.getLastActivateDate(), key.getId() });
        db.setTransactionSuccessful();  
        db.endTransaction();
        db.close(); 
    }
    
    public TKey find(int id) {  
        Cursor cursor = db.rawQuery("select * from " + DEFAULT_KEY_TABLE_NAME + " where _id=?",  
        		new String[] { String.valueOf(id) });  
        if (cursor.moveToNext()) {  
            return new TKey(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
            	new Date(cursor.getString(4)), new Date(cursor.getString(5)), new Date(cursor.getString(6)));  
        }  
        return null;  
    }
    
    public TKey find(String key) {  
        Cursor cursor = db.rawQuery("select * from " + DEFAULT_KEY_TABLE_NAME + " where licensekey=?",  
        		new String[] { key });  
        if (cursor.moveToNext()) {
        	try {
        		int id = cursor.getInt(0);
        		String licenseKey = cursor.getString(1);
        		String deviceID = cursor.getString(2);
        		String phoneNum = cursor.getString(3);
        		String buyDate = cursor.getString(4);
        		String consumeDate = cursor.getString(5);
        		String lastActivateDate = cursor.getString(6);
        		DateFormat f = DateFormat.getDateInstance();
        		Date buy = f.parse(buyDate);
        		Date consume = f.parse(consumeDate);
        		Date lastActivate = f.parse(lastActivateDate);
        		return new TKey(id, licenseKey, deviceID, phoneNum, buy, consume, lastActivate);
        	} catch (Exception ex) {
        		Log.e(LOGTAG, ex.getMessage());
        		return null;
        	}
        } else {
        	return null;
        }
    }
    
    public boolean isValidLicenseKey(String key) {
    	if (find(key) == null) {
    		return true;
    	} else return false;
    }
    
    public String getDefaultValidateFailMsg(String key) {
    	return String.format(context.getResources().getString(R.string.msg_validate_fail_used_key_cn), key);
    }
    
    public String getDbPath() { return db.getPath(); }
    
}