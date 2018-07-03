package com.gillsoft.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
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
		return Base64.getEncoder().encodeToString(value.getBytes());
	}
	
	public static String fromBase64(String value) {
		return new String(Base64.getDecoder().decode(value));
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
		return jsonStringToObject(type, fromBase64(value));
	}
	
	public static String objectToString(Object object) throws IOException {
		if (object == null) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream stream = new ObjectOutputStream(out);
		stream.writeObject(object);
		return out.toString(StandardCharsets.UTF_8.name());
	}
	
	public static Object stringToObject(String value) throws IOException, ClassNotFoundException {
		if (value == null) {
			return null;
		}
		ByteArrayInputStream in = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8.name()));
		ObjectInputStream stream = new ObjectInputStream(in);
		return stream.readObject();
	}

}
