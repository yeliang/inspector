package com.particle.inspector.common.util.contact;

import java.util.ArrayList;
import java.util.List;
import android.content.res.Resources;
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
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (name.length() <= 8) sb.append(name + "\t\t");
		else sb.append(name + "\t");
		sb.append(StrUtils.toCommaString(phoneNumberList) + "\t");
		sb.append(getAddressString() + "\t");
		sb.append(StrUtils.toCommaString(emailList));
		
		return sb.toString();
	}
}
