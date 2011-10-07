package com.particle.inspector.authsrv.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.particle.inspector.authsrv.R;
import com.particle.inspector.authsrv.sqlite.DbHelper;
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.SysUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Simplified import/export DB activity.
 *
 */
public class ManageDatabaseActivity extends Activity 
{
	private static final String LOGTAG = "ManageDatabaseActivity";
	private static final String DEFAULT_BACKUP_DIR = "DB_BACKUP";
	
	private Button exportDbToSdButton;
	private Button importDbFromSdButton;
	
   @Override
   public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.managedb);

      exportDbToSdButton = (Button) findViewById(R.id.exportdbtosdbutton);
      exportDbToSdButton.setOnClickListener(new OnClickListener() {
         public void onClick(final View v) {
            Log.i(LOGTAG, "exporting database to external storage");
            new AlertDialog.Builder(ManageDatabaseActivity.this).setMessage(
                     "Are you sure?").setPositiveButton("Yes",
                     new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                           if (FileCtrl.isExternalStorageAvail()) {
                              Log.i(LOGTAG, "importing database from external storage, and resetting database");
                              new BackupDatabaseTask().execute();
                              ManageDatabaseActivity.this.startActivity(new Intent(ManageDatabaseActivity.this, DashboardActivity.class));
                           } else {
                              Toast.makeText(ManageDatabaseActivity.this,
                                       "External storage is not available, unable to export data.", Toast.LENGTH_SHORT)
                                       .show();
                           }
                        }
                     }).setNegativeButton("No", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface arg0, int arg1) {
               }
            }).show();
         }
      });

      importDbFromSdButton = (Button) findViewById(R.id.importdbfromsdbutton);
      importDbFromSdButton.setOnClickListener(new OnClickListener() {
         public void onClick(final View v) {
            new AlertDialog.Builder(ManageDatabaseActivity.this).setMessage(
                     "Are you sure (this will overwrite existing current data)?").setPositiveButton("Yes",
                     new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                           if (FileCtrl.isExternalStorageAvail()) {
                              Log.i(LOGTAG, "importing database from external storage, and resetting database");
                              new RestoreDatabaseTask().execute();
                              // sleep momentarily so that database reset stuff has time to take place (else Main reloads too fast)
                              SystemClock.sleep(500);
                              ManageDatabaseActivity.this.startActivity(new Intent(ManageDatabaseActivity.this, DashboardActivity.class));
                           } else {
                              SysUtils.messageBox(v.getContext(), "External storage is not available, unable to export data.");
                           }
                        }
                     }).setNegativeButton("No", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface arg0, int arg1) {
               }
            }).show();
         }
      });
      
   }

   private class BackupDatabaseTask extends AsyncTask<Void, Void, String> {
      private final ProgressDialog dialog = new ProgressDialog(ManageDatabaseActivity.this);

      // can use UI thread here
      @Override
      protected void onPreExecute() {
         dialog.setMessage("Backing up database...");
         dialog.show();
      }

      // automatically done on worker thread (separate from UI thread)
      @Override
      protected String doInBackground(final Void... args) 
      { 
         File dbFile = new File(Environment.getDataDirectory() + DbHelper.DEFAULT_DATABASE_PATH);
         if (!dbFile.exists()) {
        	 String errMsg = "ERROR: database file '" + (Environment.getDataDirectory() + DbHelper.DEFAULT_DATABASE_PATH) + "' does not exist.";
        	 Log.e(LOGTAG, errMsg);
             return errMsg;
         }
         
         // Create backup directory in SD-CARD
         File exportDir = new File(Environment.getExternalStorageDirectory(), DEFAULT_BACKUP_DIR);
         if (!exportDir.exists()) {
            exportDir.mkdirs();
         }
         
         // The backup db file format: inspector.db_yyyy-MM-dd_HH:mm:ss
         String now = DatetimeUtil.format2.format(new Date());
         File file = new File(exportDir, dbFile.getName() + "_" + now);

         try {
            file.createNewFile();
            FileCtrl.copyFile(dbFile, file);
            return null;
         } catch (IOException e) {
            Log.e(LOGTAG, e.getMessage(), e);
            return e.getMessage();
         }
      }

      // can use UI thread here
      @Override
      protected void onPostExecute(final String errMsg) {
         if (dialog.isShowing()) {
            dialog.dismiss();
         }
         if (errMsg == null) {
            SysUtils.messageBox(getApplicationContext(), "Backup successful!");
         } else {
        	SysUtils.messageBox(getApplicationContext(), "ERROR: backup failed: " + errMsg);
         }
      }
   }

   private class RestoreDatabaseTask extends AsyncTask<Void, Void, String> {
      private final ProgressDialog dialog = new ProgressDialog(ManageDatabaseActivity.this);

      @Override
      protected void onPreExecute() {
         dialog.setMessage("Restoring database...");
         dialog.show();
      }

      // could pass the params used here in AsyncTask<String, Void, String> - but not being re-used
      @Override
      protected String doInBackground(final Void... args) 
      {
    	 // The source file path
         File dbBackupFile = new File(Environment.getExternalStorageDirectory() + "/" 
        		 + DEFAULT_BACKUP_DIR + "/" + DbHelper.DEFAULT_DATABASE_NAME);
         if (!dbBackupFile.exists()) {
            return "Database backup file does not exist, cannot restore.";
         } else if (!dbBackupFile.canRead()) {
            return "Database backup file exists, but is not readable, cannot restore.";
         }

         // The restore target path
         File dbFile = new File(Environment.getDataDirectory() + DbHelper.DEFAULT_DATABASE_PATH);
         if (dbFile.exists()) {
            dbFile.delete();
         }

         try {
            dbFile.createNewFile();
            FileCtrl.copyFile(dbBackupFile, dbFile);
            //ManageDatabaseActivity.this.application.getDataHelper().resetDbConnection();
            return null;
         } catch (IOException e) {
            Log.e(LOGTAG, e.getMessage(), e);
            return e.getMessage();
         }
      }

      @Override
      protected void onPostExecute(final String errMsg) {
         if (dialog.isShowing()) {
            dialog.dismiss();
         }
         if (errMsg == null) {
            SysUtils.messageBox(getApplicationContext(), "Restore successful!");
         } else {
        	SysUtils.messageBox(getApplicationContext(), "ERROR: restore failed: " + errMsg);
         }
      }
   }
}