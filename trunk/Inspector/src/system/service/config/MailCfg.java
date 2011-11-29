package system.service.config;

import java.util.Random;

import system.service.R;
import system.service.activity.GlobalPrefActivity;
import android.content.Context;

public class MailCfg 
{
	private final static String HOST_GMAIL = "smtp.gmail.com";
	
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

	public static String getHost(Context context) 
	{
		// If use system default GMail sender
		if (!GlobalPrefActivity.getUseSelfSender(context)) {
			return HOST_GMAIL;
		}
		
		// Otherwise, use customer's mail sender host
		String[] parts = GlobalPrefActivity.getSenderMail(context).split("@");
		if (parts.length > 1) return "smtp." + parts[1].toLowerCase();
		else return null;
	}

}
