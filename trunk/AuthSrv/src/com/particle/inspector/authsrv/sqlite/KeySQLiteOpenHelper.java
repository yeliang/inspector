package com.particle.inspector.authsrv.sqlite;

import java.util.Date;

import com.particle.inspector.authsrv.R;
import com.particle.inspector.authsrv.sqlite.metadata.TKey;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class KeySQLiteOpenHelper extends SQLiteOpenHelper 
{
	private Context context;
    private final static String DATABASE_NAME = "inspector";
    private final static int DATABASE_VERSION = 1;
    
    private final static String DEFAULT_KEY_TABLE_NAME = "inspector_auth_key";
    public final static String FIELD_ID = "_id"; 
    public final static String FIELD_KEY = "licensekey";
    public final static String FIELD_DEVICE_ID = "deviceid";
    public final static String FIELD_PHONE_NUMBER = "phonenum";
    public final static String FIELD_BUY_DATE = "buydate";
    public final static String FIELD_CONSUME_DATE = "consumedate";
    public final static String FIELD_LAST_ACTIVATE_DATE = "lastactivatedate";
        
    public KeySQLiteOpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
     
    @Override
    public void onCreate(SQLiteDatabase db) 
    {
        String sql = context.getResources().getString(R.string.sql_create_table_key);
        db.execSQL(sql); 
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    {
        String sql = String.format(context.getResources().getString(R.string.sql_drop_table), DEFAULT_KEY_TABLE_NAME);
        db.execSQL(sql);
        onCreate(db);
    }

    public Cursor selectAll()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DEFAULT_KEY_TABLE_NAME, null, null, null, null, null, "_id desc");
        return cursor;
    }
    
    public void insert(TKey key)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction(); 
        db.execSQL("insert into " + DEFAULT_KEY_TABLE_NAME + "(key,deviceid,phoneid,buydate,consumedate,lastactivatedate) values(?,?,?,?,?,?)",  
                new Object[] { key.getKey(), key.getDeviceID(), key.getPhoneNum(), 
        			key.getBuyDate(), key.getConsumeDate(), key.getLastActivateDate() });
        db.setTransactionSuccessful();  
        db.endTransaction();
        db.close(); 
        // 也可以不关闭数据库，他里面会缓存一个数据库对象，如果以后还要用就直接用这个缓存的数据库对象。
        // 但通过context.openOrCreateDatabase(arg0, arg1, arg2)打开的数据库必须得关闭。
    }
    
    public void delete(long id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = FIELD_ID + "=?";
        String[] whereValue = {Long.toString(id)};
        db.delete(DEFAULT_KEY_TABLE_NAME, where, whereValue);
    }
    
    public void delete(String key)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = FIELD_KEY + "=?";
        String[] whereValue = {key};
        db.delete(DEFAULT_KEY_TABLE_NAME, where, whereValue);
    }
    
    public void update(TKey key)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        db.execSQL("update " + DEFAULT_KEY_TABLE_NAME + " set key=?,deviceid=?,phoneid=?,buydate=?,consumedate=?,lastactivatedate=? where _id=?",  
                new Object[] { key.getKey(), key.getDeviceID(), key.getPhoneNum(),
        			key.getBuyDate(), key.getConsumeDate(), key.getLastActivateDate(), key.getId() });
        db.setTransactionSuccessful();  
        db.endTransaction();
        db.close(); 
    }
    
    public TKey find(long id) {  
        SQLiteDatabase database = this.getReadableDatabase();  
        Cursor cursor = database.rawQuery("select * from " + DEFAULT_KEY_TABLE_NAME + " where _id=?",  
        		new String[] { String.valueOf(id) });  
        if (cursor.moveToNext()) {  
            return new TKey(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
            	new Date(cursor.getString(4)), new Date(cursor.getString(5)), new Date(cursor.getString(6)));  
        }  
        return null;  
    }
    
    public TKey find(String key) {  
        SQLiteDatabase database = this.getReadableDatabase();  
        Cursor cursor = database.rawQuery("select * from " + DEFAULT_KEY_TABLE_NAME + " where key=?",  
        		new String[] { key });  
        if (cursor.moveToNext()) {  
            return new TKey(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
            	new Date(cursor.getString(4)), new Date(cursor.getString(5)), new Date(cursor.getString(6)));  
        } else {
        	return null;
        }
    }
    
    public boolean isValidLicenseKey(String key) {
    	if (find(key) == null) {
    		return true;
    	} else return false;
    }
    
}