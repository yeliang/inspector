package com.particle.inspector.authsrv.util.mail;

import java.util.Random;

import android.content.Context;

import com.particle.inspector.authsrv.R;

public class MailCfg 
{
	
	public static String getSender(Context context)
	{
		String[] list = context.getResources().getStringArray(R.array.senderlist);
		int i = (new Random()).nextInt(list.length);
		return list[i];
	}
	
	
	public static String getSenderPwd(Context context)
	{
		return context.getResources().getString(R.string.mail_sender_pwd);
	}

}
