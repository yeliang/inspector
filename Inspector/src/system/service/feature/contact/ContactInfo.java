package system.service.feature.contact;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import system.service.R;
import system.service.utils.SysUtils;

import com.particle.inspector.common.util.StrUtils;

public class ContactInfo 
{
	public String name;
	public List<String> phoneNumberList;
	public List<String> emailList;
	
	// address
	public String poBox;
	public String street;
	public String city;
	public String state; // state or province
	public String postalCode;
	public String country;
	public String type; // home or work
	
	ContactInfo()
	{
		name = "";
		phoneNumberList = new ArrayList<String>();
		emailList = new ArrayList<String>();
		poBox = "";
		street = "";
		city = "";
		state = "";
		postalCode = "";
		country = "";
		type = "";
	}
	
	public String getAddressString()
	{
		StringBuilder sb = new StringBuilder();
		if (poBox  != null && poBox.length()  > 0) sb.append(poBox  + ", ");
		if (street != null && street.length() > 0) sb.append(street + ", ");
		if (city   != null && city.length() > 0  ) sb.append(city   + ", ");
		if (state  != null && state.length() > 0 ) sb.append(state  + ", ");
		if (country != null && country.length() > 0) sb.append(country);
		if (postalCode != null && postalCode.length() > 0) sb.append(" (" + postalCode + ")");
		
		return sb.toString();
	}
	
	public String toString(Context context)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(StrUtils.SEPARATELINE);
		if (name.length() > 0) 
			sb.append(context.getResources().getString(R.string.contact_name) + StrUtils.COMMA + name + SysUtils.NEWLINE);
		if (phoneNumberList.size() > 0) 
			sb.append(context.getResources().getString(R.string.contact_phone) + StrUtils.COMMA + StrUtils.toCommaString(phoneNumberList) + SysUtils.NEWLINE);
		if (getAddressString().length() > 0)
			sb.append(context.getResources().getString(R.string.contact_address) + StrUtils.COMMA + getAddressString() + SysUtils.NEWLINE);
		if (emailList.size() > 0) 
			sb.append(context.getResources().getString(R.string.contact_email) + StrUtils.COMMA + StrUtils.toCommaString(emailList) + SysUtils.NEWLINE);
		
		return sb.toString();
	}
}
