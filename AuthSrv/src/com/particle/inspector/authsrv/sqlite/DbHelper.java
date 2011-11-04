package com.particle.inspector.authsrv.sqlite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.particle.inspector.authsrv.R;
import com.particle.inspector.authsrv.sqlite.metadata.TKey;
import com.particle.inspector.common.util.LANG;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.license.LicenseCtrl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Environment;
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
	
    public final static String DEFAULT_DATABASE_NAME = "inspector.db";
    private final static int DATABASE_VERSION = 1;
    
    public final static String DEFAULT_DATABASE_PATH = "/data/com.particle.inspector.authsrv/databases/inspector.db";
    
    private final static String DEFAULT_KEY_TABLE_NAME = "inspector_auth_key";
    public final static String KEY_FIELD_ID = "_id"; 
    public final static String KEY_FIELD_KEY = "licensekey";
    public final static String KEY_FIELD_DEVICE_ID = "deviceid";
    public final static String KEY_FIELD_PHONE_NUMBER = "phonenum";
    public final static String KEY_FIELD_CONSUME_DATE = "consumedate";
    
    public DbHelper(Context context)
    {
        this.context = context;
    }
    
    public static boolean dbExist() {
    	File dbFile = new File(Environment.getDataDirectory() + DEFAULT_DATABASE_PATH);
        if (dbFile.exists()) return true;
        else return false;
    }
     
    public boolean createOrOpenDatabase() {
    	if (db == null || !db.isOpen()) {
    		db = context.openOrCreateDatabase(DEFAULT_DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);
    	}
    	
    	if (db.isOpen()) {
    		dbPath = db.getPath();
    		return true;
    	} else return false;
    }
    
    public boolean deleteDB() {
    	return context.deleteDatabase(DEFAULT_DATABASE_NAME);
    }
    
    public void resetDbConnection() {
        Log.i(LOGTAG, "resetting database connection (close and re-open).");
        cleanup();
        db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
     }

     public void cleanup() {
        if ((db != null) && db.isOpen()) {
           db.close();
        }
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
        	
        	// Create index
        	sql = context.getResources().getString(R.string.sql_create_index_on_licensekey);
        	db.execSQL(sql);
        	sql = context.getResources().getString(R.string.sql_create_index_on_deviceid);
        	db.execSQL(sql);
        	return true;
        } catch (SQLException e) {
        	Log.e(LOGTAG, e.getMessage());
        	return false;
        }
    }

    public boolean DropKeyTable() 
    {
    	boolean ret = false;
        String sql = String.format(context.getResources().getString(R.string.sql_drop_table), DEFAULT_KEY_TABLE_NAME);
        try {
        	if (db == null) return false;
        	if (!db.isOpen()) {
        		db = SQLiteDatabase.openDatabase(db.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        	}
        	db.execSQL(sql);
        	ret = true;
        } catch (SQLException e) {
        	Log.e(LOGTAG, e.getMessage());
        }
        return ret;
    }

    public Cursor selectAll()
    {
        Cursor cursor = db.query(DEFAULT_KEY_TABLE_NAME, null, null, null, null, null, "_id desc");
        return cursor;
    }
    
    public boolean insert(TKey key)
    {
    	boolean ret = false;
    	try {
    		db.beginTransaction(); 
        	db.execSQL("insert into " + DEFAULT_KEY_TABLE_NAME + "(licensekey,keytype,deviceid,phonenum,phonemodel,androidver,consumedate,receivermailaddress,receiverphonenum) values(?,?,?,?,?,?,?,?,?)",  
            	new Object[] { key.getKey(), LicenseCtrl.enumToStr(key.getKeyType()), key.getDeviceID(), key.getPhoneNum(), key.getPhoneModel(), key.getAndroidVer(),
        			key.getConsumeDate(), key.getRecvMail(), key.getRecvPhoneNum() });
        	db.setTransactionSuccessful();  
        	db.endTransaction();
        	ret = true;
    	} catch (SQLException e) {
    		Log.e(LOGTAG, e.getMessage());
    	} finally {
    		db.close();
    	}
    	return ret;
    }
    
    public int deleteById(long id)
    {
        String where = KEY_FIELD_ID + "=?";
        String[] whereValue = {Long.toString(id)};
        return db.delete(DEFAULT_KEY_TABLE_NAME, where, whereValue);
    }
    
    public int deleteByKey(String key)
    {
        String where = KEY_FIELD_KEY + "=?";
        String[] whereValue = {key};
        return db.delete(DEFAULT_KEY_TABLE_NAME, where, whereValue);
    }
    
    public int deleteByDeviceId(String deviceId)
    {
        String where = KEY_FIELD_DEVICE_ID + "=?";
        String[] whereValue = {deviceId};
        return db.delete(DEFAULT_KEY_TABLE_NAME, where, whereValue);
    }
    
    public int cleanTableKey()
    {
        return db.delete(DEFAULT_KEY_TABLE_NAME, null, null);
    }
    
    // unregister
    public boolean unregister(String key, String deviceId)
    {
    	int count = deleteByKey(key);
    	return (count > 0);
    }
    
    // Update by _id
    public boolean updateById(TKey key)
    {
    	boolean ret = false;
    	try {
    		db.beginTransaction();
    		db.execSQL("update " + DEFAULT_KEY_TABLE_NAME + " set licensekey=?,keytype=?,deviceid=?,phonenum=?,phonemodel=?,androidver=?,consumedate=? where _id=?",  
                new Object[] { key.getKey(), LicenseCtrl.enumToStr(key.getKeyType()), key.getDeviceID(), key.getPhoneNum(), key.getPhoneModel(), key.getAndroidVer(),
        			key.getConsumeDate(), key.getId() });
    		db.setTransactionSuccessful();  
    		db.endTransaction();
    		ret = true;
    	} catch (SQLException ex) {
    		Log.e(LOGTAG, ex.getMessage());
    	} finally {
    		db.close();
    	} 
    	return ret;
    }
    
    // Update by device ID
    public boolean updateByDevice(TKey key)
    {
    	boolean ret = false;
    	try {
    		db.beginTransaction();
    		db.execSQL("update " + DEFAULT_KEY_TABLE_NAME + " set licensekey=?,keytype=?,phonenum=?,androidver=? where deviceid=?",  
                new Object[] { key.getKey(), LicenseCtrl.enumToStr(key.getKeyType()), key.getPhoneNum(), key.getAndroidVer(), key.getDeviceID() });
    		db.setTransactionSuccessful();  
    		db.endTransaction();
    		ret = true;
    	} catch (SQLException ex) {
    		Log.e(LOGTAG, ex.getMessage());
    	} finally {
    		db.close();
    	} 
    	return ret;
    }
    
    // Update by license key
    public boolean updateByKey(TKey key)
    {
    	boolean ret = false;
    	try {
    		db.beginTransaction();
    		db.execSQL("update " + DEFAULT_KEY_TABLE_NAME + " set phonenum=?,androidver=? where licensekey=?",  
                new Object[] { key.getPhoneNum(), key.getAndroidVer(), key.getKey() });
    		db.setTransactionSuccessful();  
    		db.endTransaction();
    		ret = true;
    	} catch (SQLException ex) {
    		Log.e(LOGTAG, ex.getMessage());
    	} finally {
    		db.close();
    	} 
    	return ret;
    }
    
    // Update by device ID to write receiver info (mail address, phone number)
    public boolean updateReceiverInfoByDeviceId(String deviceId, String receiverMailAddress, String receiverPhoneNum, String phoneNum)
    {
    	boolean ret = false;
    	try {
    		db.beginTransaction();
    		db.execSQL("update " + DEFAULT_KEY_TABLE_NAME + " set phonenum=?,receivermailaddress=?,receiverphonenum=? where deviceid=?",  
    				new Object[] { phoneNum, receiverMailAddress, receiverPhoneNum, deviceId });
        	db.setTransactionSuccessful();  
        	db.endTransaction();
        	ret = true;
    	} catch (SQLException ex) {
    		Log.e(LOGTAG, ex.toString());
    	} finally {
    		db.close();
    	}
    	return ret; 
    }
    
    public TKey findId(int id) {  
        Cursor cursor = db.rawQuery("select * from " + DEFAULT_KEY_TABLE_NAME + " where _id=?",  
        		new String[] { String.valueOf(id) });  
        if (cursor.moveToNext()) {  
            return new TKey(cursor.getInt(0), cursor.getString(1), LicenseCtrl.strToEnum(cursor.getString(2)), 
            		cursor.getString(3), cursor.getString(4), cursor.getString(5), 
            		cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9));  
        }  
        return null;  
    }
    
    public TKey findKey(String key) {  
        Cursor cursor = db.rawQuery("select * from " + DEFAULT_KEY_TABLE_NAME + " where licensekey=?",  
        		new String[] { key });  
        if (cursor.moveToNext()) {
        	try {
        		int id = cursor.getInt(0);
        		String licenseKey = cursor.getString(1);
        		LICENSE_TYPE type = LicenseCtrl.strToEnum(cursor.getString(2));
        		String deviceID = cursor.getString(3);
        		String phoneNum = cursor.getString(4);
        		String phoneModel = cursor.getString(5);
        		String androidVer = cursor.getString(6);
        		String consumeDate = cursor.getString(7);
        		String recvMail =  cursor.getString(8);
        		String recvPhoneNum =  cursor.getString(9);
        		return new TKey(id, licenseKey, type, deviceID, phoneNum, phoneModel, androidVer, consumeDate, recvMail, recvPhoneNum);
        	} catch (Exception ex) {
        		Log.e(LOGTAG, ex.getMessage());
        		return null;
        	}
        } else {
        	return null;
        }
    }
    
    public TKey findDevice(String deviceId) {  
        Cursor cursor = db.rawQuery("select * from " + DEFAULT_KEY_TABLE_NAME + " where deviceid=?",  
        		new String[] { deviceId });  
        if (cursor.moveToNext()) {  
            return new TKey(cursor.getInt(0), cursor.getString(1), LicenseCtrl.strToEnum(cursor.getString(2)),
            		cursor.getString(3), cursor.getString(4), cursor.getString(5), 
            		cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9));  
        }  
        return null;  
    }
    
    public TKey findLastRecord(String key) {
    	Cursor cursor = db.rawQuery("select * from " + DEFAULT_KEY_TABLE_NAME + " where licensekey=? order by _id desc",  
        		new String[] { key });  
        if (cursor.moveToNext()) {  
            return new TKey(cursor.getInt(0), cursor.getString(1), LicenseCtrl.strToEnum(cursor.getString(2)),
            		cursor.getString(3), cursor.getString(4), cursor.getString(5), 
            		cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9));  
        }  
        return null;
    }
    
    // If the license key exists but the device is the same one, set exists to true
    public KEY_VALIDATION_RESULT isValidLicenseKey(String key, String deviceID) {
    	TKey foundKey = findKey(key);
    	if (foundKey == null) {
    		return KEY_VALIDATION_RESULT.VALID_AND_NOT_EXIST;
    	} else if (foundKey.getDeviceID().equalsIgnoreCase(deviceID)) {
    		return KEY_VALIDATION_RESULT.VALID_BUT_EXIST;
    	} else return KEY_VALIDATION_RESULT.INVALID;
    }
    
    public String getDefaultValidateFailMsg(String key, LANG lang) {
    	if (lang == LANG.CN)
    		return String.format(context.getResources().getString(R.string.msg_validate_fail_used_key_cn), key);
    	else if (lang == LANG.JP)
    		return String.format(context.getResources().getString(R.string.msg_validate_fail_used_key_jp), key);
    	else 
    		return String.format(context.getResources().getString(R.string.msg_validate_fail_used_key_en), key);
    }
    
    public String getDbPath() { return db.getPath(); }
    
    public boolean isOpen() {
    	if (db == null || !db.isOpen()) return false;
    	else return true;
    }
    
    public boolean TableExist(String tableName) 
    {
    	boolean result = false;
        if(tableName == null){
              return false;
        }
        Cursor cursor = null;
        try {
        	String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='" + tableName.trim() + "' ";
            cursor = db.rawQuery(sql, null);
            if(cursor.moveToNext()){
            	int count = cursor.getInt(0);
                if(count>0){
                	result = true;
                }
            }
        } catch (Exception e) {
        	Log.e(LOGTAG, e.getMessage());
        }                
        return result;
    }
    
    public boolean keyTableExist() { return TableExist(DEFAULT_KEY_TABLE_NAME); }
    
    public boolean backupDatabase(Context context, String fileName) 
    {
    	boolean ret = false;
    	try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = getDbPath();
                String backupDBPath = fileName;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
                ret = true;
            }
        } catch (Exception e) {
        	Log.e(LOGTAG, "");
        } finally {
        	
        }
    	return ret;
    }
    
}