package com.system.utils.mail;

import java.util.Random;

import com.system.R;
import android.content.Context;

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
