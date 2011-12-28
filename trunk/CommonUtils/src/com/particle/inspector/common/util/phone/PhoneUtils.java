package com.particle.inspector.common.util.phone;  
  
import java.lang.reflect.Method;  

import com.android.internal.telephony.ITelephony;

import android.telephony.TelephonyManager;   
  
public class PhoneUtils {  
      
    static public ITelephony getITelephony(TelephonyManager telMgr) throws Exception {  
        Method getITelephonyMethod = telMgr.getClass().getDeclaredMethod("getITelephony");  
        getITelephonyMethod.setAccessible(true);
        return (ITelephony)getITelephonyMethod.invoke(telMgr);  
    }  
      
    /*
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
    */   
} 