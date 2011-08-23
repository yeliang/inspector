package com.system.feature.contact;

import java.util.ArrayList;
import java.util.List;
import android.content.res.Resources;
import com.system.R;
import com.system.utils.StrUtils;

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
		sb.append(poBox);
		if (poBox.length() > 0) sb.append(", ");
		sb.append(street);
		if (street.length() > 0) sb.append(", ");
		sb.append(city);
		if (city.length() > 0) sb.append(", ");
		sb.append(state);
		if (state.length() > 0) sb.append(", ");
		sb.append(country);
		if (country.length() > 0 && postalCode.length() > 0) sb.append(", ");
		if (postalCode.length() > 0) sb.append("(" + postalCode + ")");
		
		return sb.toString();
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(name + "\r ");
		sb.append(StrUtils.toCommaString(phoneNumberList));
		sb.append(getAddressString());
		sb.append(StrUtils.toCommaString(emailList));
		
		return sb.toString();
	}
}
