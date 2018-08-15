package org.pulpdust.lesserpad;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.TargetApi;
import android.util.Base64;

@TargetApi(8)
public class forFroyo {
	
	public String doEncrypt(String key, String text)
	throws GeneralSecurityException, UnsupportedEncodingException {
		SecretKeySpec sks = new SecretKeySpec(key.getBytes(),"AES");
		Cipher cp = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cp.init(Cipher.ENCRYPT_MODE, sks);
		byte[] base = Base64.encode(text.getBytes("UTF-8"), Base64.NO_WRAP);
//		byte[] ected = cp.doFinal(base, 0, base.length);
		byte[] iv = cp.getIV();
		return Base64.encodeToString(iv, Base64.NO_WRAP) + "\n" + Base64.encodeToString(cp.doFinal(base, 0, base.length), Base64.NO_WRAP);
//		return ret;
	}
	public String doDecrypt(String key, String etxt)
	throws GeneralSecurityException, UnsupportedEncodingException {
//		String iv;
//		String body;
		String[] cont = etxt.split("\n{1,}");
//		iv = etxt.replaceFirst("^([^\n]+)\n(.+)", "\1");
//		body = etxt.replaceFirst("^([^\n]+)\n(.+)", "\2");
		IvParameterSpec ivps = new IvParameterSpec(Base64.decode(cont[0], Base64.DEFAULT));
		SecretKeySpec sks = new SecretKeySpec(key.getBytes(), "AES");
		Cipher cp = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cp.init(Cipher.DECRYPT_MODE, sks, ivps);
//		String dump = new String(Base64.decode(cont[1], Base64.DEFAULT), "UTF-8");
		byte[] base = Base64.decode(cont[1], Base64.DEFAULT);
//		byte[] dcted = cp.doFinal(base, 0, base.length);
		return new String(Base64.decode(cp.doFinal(base, 0, base.length), Base64.DEFAULT), "UTF-8");
//		return ret;
	}


}
