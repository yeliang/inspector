package system.service.config;

import system.service.activity.GlobalPrefActivity;
import android.content.Context;

public class MailCfg 
{
	public static String getSender(Context context)
	{
		return GlobalPrefActivity.getSenderMail(context);
	}
	
	public static String getSenderPwd(Context context)
	{	
		return GlobalPrefActivity.getSenderPassword(context);
	}

	public static String getHost(Context context) 
	{
		String[] parts = GlobalPrefActivity.getSenderMail(context).split("@");
		if (parts.length > 1) return "smtp." + parts[1].trim().toLowerCase();
		else return null;
	}

}
