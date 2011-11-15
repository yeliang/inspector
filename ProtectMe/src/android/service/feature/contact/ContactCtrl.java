package android.service.feature.contact;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactCtrl 
{
	private static final String LOGTAG = "ContactCtrl";

	public static List<ContactInfo> getContactList(Context context)
	{
		Cursor cursor = context.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, 
				null, null, null, null);
		
		List<ContactInfo> infoList = new ArrayList<ContactInfo>();
		
        while (cursor.moveToNext()) 
        {
        	// Get contact ID 
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)); 
            
            if (contactId == null || contactId == "") {
            	Log.d(LOGTAG, "Invalid contact ID");
            	continue;
            }
 
            ContactInfo info = new ContactInfo();
            
            // Get contact name 
            info.name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)); 
            if (info.name == null) info.name = "";
            
            // See if the contact has phone number (return "1" if have or "0" if have not) 
            String hasPhoneStr = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)); 
            boolean hasPhone = hasPhoneStr.equalsIgnoreCase("1") ? true : false;
                 
            // Get all phone numbers by the contact ID
            if (hasPhone) { 
                Cursor phones = context.getContentResolver().query( 
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
                        null, 
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID 
                                + " = " + contactId, null, null); 
                
                while (phones.moveToNext()) { 
                    String phoneNumber = phones 
                            .getString(phones 
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)); 
                    if (!info.phoneNumberList.contains(phoneNumber)) info.phoneNumberList.add(phoneNumber); 
                }
                phones.close(); 
            } 
 
            // Get all emails by the contact ID
            Cursor emails = context.getContentResolver().query( 
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
                    null, 
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null); 
            
            while (emails.moveToNext()) { 
                String emailAddress = emails 
                        .getString(emails 
                                .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)); 
                if (!info.emailList.contains(emailAddress)) info.emailList.add(emailAddress);
            }
            emails.close(); 
 
            // Get contact address
            Cursor address = context.getContentResolver().query(
            				ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, 
                            null, 
                            ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = " + contactId, null, null); 
            while (address.moveToNext()) 
            {
                info.poBox = address.getString(address 
                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX)); 
                info.street = address.getString(address 
                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)); 
                info.city = address.getString(address 
                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)); 
                info.state = address.getString(address 
                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)); 
                info.postalCode = address.getString(address 
                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)); 
                info.country = address.getString(address 
                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)); 
                info.type = address.getString(address 
                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)); 
            }
            
            infoList.add(info);
        } 
        
        return infoList;
	}
	
	
}
