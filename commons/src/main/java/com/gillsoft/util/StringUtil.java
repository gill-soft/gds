package com.gillsoft.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}
	
	public static String toBase64(String value) {
		return toBase64(value.getBytes());
	}
	
	public static String toBase64(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	public static String fromBase64AsString(String value) {
		return new String(fromBase64(value));
	}
	
	public static byte[] fromBase64(String value) {
		return Base64.getDecoder().decode(value);
	}
	
	public static String objectToJsonString(Object value) throws JsonProcessingException {
		return new ObjectMapper().writerFor(value.getClass()).writeValueAsString(value);
	}
	
	public static <T> T jsonStringToObject(Class<?> type, String value) throws IOException {
		return new ObjectMapper().readerFor(type).readValue(value);
	}
	
	public static String objectToJsonBase64String(Object value) throws JsonProcessingException {
		return toBase64(objectToJsonString(value));
	}
	
	public static <T> T jsonBase64StringToObject(Class<?> type, String value) throws IOException {
		return jsonStringToObject(type, fromBase64AsString(value));
	}
	
	public static String objectToBase64String(Object object) throws IOException {
		if (object == null) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream stream = new ObjectOutputStream(out);
		stream.writeObject(object);
		return toBase64(out.toByteArray());
	}
	
	public static Object base64StringToObject(String value) throws IOException, ClassNotFoundException {
		if (value == null) {
			return null;
		}
		ByteArrayInputStream in = new ByteArrayInputStream(fromBase64(value));
		ObjectInputStream stream = new ObjectInputStream(in);
		return stream.readObject();
	}

}
