package com.particle.inspector.common.util;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class AesCryptor {

    Cipher ecipher;
    //Cipher dcipher;
    
    private final static String key = "Particle";

    // 8-byte Salt
    byte[] salt = { 1, 2, 4, 5, 7, 8, 3, 6 };

    // Iteration count
    int iterationCount = 3;

    public AesCryptor() {
        try {
            // Create the key
            KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, iterationCount);
            SecretKey key = SecretKeyFactory.getInstance(
                    "PBEWITHSHA256AND128BITAES-CBC-BC").generateSecret(keySpec);
            ecipher = Cipher.getInstance(key.getAlgorithm());
            //dcipher = Cipher.getInstance(key.getAlgorithm());

            // Prepare the parameter to the ciphers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

            // Create the ciphers
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            //dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        } catch (Exception e) {
        }
    }

    public String encrypt(String str) {
        String rVal = "";
        try {
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes("UTF8");

            // Encrypt
            byte[] enc = ecipher.doFinal(utf8);

            // Encode bytes to base64 to get a string
            rVal = toHex(enc);
        } catch (Exception e) {
            
        }
        return rVal;
    }

    /*
    public String decrypt(String str) {
        String rVal = "";
        try {
            // Decode base64 to get bytes
            byte[] dec = toByte(str);

            // Decrypt
            byte[] utf8 = dcipher.doFinal(dec);

            // Decode using utf-8
            rVal = new String(utf8, "UTF8");
        } catch (Exception e) {
            
        }
        return rVal;
    }
    */

    private static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    private static String toHex(byte[] buf) {
        if (buf == null) return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}