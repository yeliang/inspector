package com.particle.inspector.authsrv;

import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.particle.inspector.authsrv.activity.GlobalPrefActivity;
import com.particle.inspector.authsrv.util.ConfigCtrl;
import com.particle.inspector.authsrv.util.SysUtils;
import com.particle.inspector.authsrv.util.mail.GMailSenderEx;

import android.app.Activity;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * Timer task for SMS handling
 */
public class SmsTask extends TimerTask 
{
	private final static String LOGTAG = "SmsTask";
	
	public Service service;
	
	public SmsTask(Service service)
	{
		super();
		this.service = service;
	}
	
	public void run() 
	{
		Log.d(LOGTAG, "started");
		
		
	}
	
	public static boolean sendMail(String subject, String body, String sender, String pwd, String[] recipients, List<String> files)
	{
		boolean ret = false;

        try {   
            GMailSenderEx gmailSender = new GMailSenderEx(sender, pwd);
            gmailSender.setFrom("system@gmail.com");
            gmailSender.setTo(recipients);
            gmailSender.setSubject(subject);
            gmailSender.setBody(body);
            
            for(int i = 0; i < files.size(); i++)
            	gmailSender.addAttachment(files.get(i));//e.g. "/sdcard/filelocation"
            
            ret = gmailSender.send();
        } catch (Exception e) {   
            Log.e(LOGTAG, (e == null) ? "Failed to send mail" : e.getMessage());
        }
        
		return ret;
	}
	
}
