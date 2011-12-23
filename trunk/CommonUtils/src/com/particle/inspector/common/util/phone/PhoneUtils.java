package com.particle.inspector.common.util.phone;  
  
import java.lang.reflect.Field;  
import java.lang.reflect.Method;  
import android.telephony.TelephonyManager;   

import android.util.Log;  
  
public class PhoneUtils {  
      
    static public com.android.internal.telephony.ITelephony getITelephony(TelephonyManager telMgr) throws Exception {  
        Method getITelephonyMethod = telMgr.getClass().getDeclaredMethod("getITelephony");  
        getITelephonyMethod.setAccessible(true);
        return (com.android.internal.telephony.ITelephony)getITelephonyMethod.invoke(telMgr);  
    }  
      
    static public void printAllInform(Class clsShow) {    
        try {    
            // Get all methods   
            Method[] hideMethod = clsShow.getDeclaredMethods();    
            int i = 0;    
            for (; i < hideMethod.length; i++) {    
                Log.e("method name", hideMethod[i].getName());    
            }    
            // Get all constants    
            Field[] allFields = clsShow.getFields();    
            for (i = 0; i < allFields.length; i++) {    
                Log.e("Field name", allFields[i].getName());    
            }    
        } catch (Exception e) {
        }    
    }    
} 