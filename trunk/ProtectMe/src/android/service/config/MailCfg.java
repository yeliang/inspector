package android.service.config;

import java.util.Random;

import android.content.Context;
import android.service.R;
import android.service.activity.GlobalPrefActivity;

public class MailCfg 
{
	
	public static String getSender(Context context)
	{
		// If user prefer to use his own GMail sender
		if (GlobalPrefActivity.getUseSelfSender(context)) {
			return GlobalPrefActivity.getSenderMail(context);
		}
		
		// Otherwise, use system default sender at random
		String[] list = context.getResources().getStringArray(R.array.senderlist);
		int i = (new Random()).nextInt(list.length);
		return list[i];
	}
	
	public static String getSenderPwd(Context context)
	{
		if (GlobalPrefActivity.getUseSelfSender(context)) {
			return GlobalPrefActivity.getSenderPassword(context);
		} else {
			return context.getResources().getString(R.string.mail_sender_pwd);
		}
	}

}
