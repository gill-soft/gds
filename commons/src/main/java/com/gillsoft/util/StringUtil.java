package com.gillsoft.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class StringUtil {
	
	public static String md5(String st) {
	    try {
			return getMessageDigestHash(st, "MD5");
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}
	
	public static String getMessageDigestHash(String value, String algorithm)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.reset();
        md.update(value.getBytes());
        byte[] bytes = md.digest();
        StringBuilder hash = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            hash.append(hex);
        }
        return hash.toString();
    }

}
