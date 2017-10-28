package com.ldyy.tool;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSA {
	private static final String KEY_ALGORITHM = "RSA";
	// RSA最大加密明文大小
	private static final int MAX_ENCRYPT_BLOCK = 117;
	// RSA最大解密密文大小
	private static final int MAX_DECRYPT_BLOCK = 128;

	private RSAPrivateKey priK;
	private RSAPublicKey pubK;

	public RSA(String pri, String pub) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

		if (pri != null) {
			byte[] priBytes = Base64.decode(pri);
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priBytes);
			priK = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
		}

		if (pub != null) {
			byte[] pubBytes = Base64.decode(pub);
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubBytes);
			pubK = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
		}
	}

	public byte[] decrypt(byte[] data) throws Exception {
		Cipher priC = Cipher.getInstance(KEY_ALGORITHM);
		priC.init(Cipher.DECRYPT_MODE, priK);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = priC.doFinal(data, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = priC.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return decryptedData;
	}

	public byte[] encrypt(byte[] data) throws Exception {
		Cipher pubC = Cipher.getInstance(KEY_ALGORITHM);
		pubC.init(Cipher.ENCRYPT_MODE, pubK);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = pubC.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = pubC.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

	public String decrypt(String data) {
		try {
			return new String(decrypt(Base64.decode(data)), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String encrypt(String data) {
		try {
			return Base64.encode(encrypt(data.getBytes("UTF-8")));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	public static void main(String[] args) throws Exception {
//		String msg = "2c541fbc-7a0a-480e-bb54-4a03858639b4测试下小申通不通";
		
		String key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCGEa4RXNCqk23yAb+lyCyIqNdV0LnIzqJpC/01UG4K4PqtCfRx8j6/+JYB5NRofgKTr0ynjGx7BpIsL0IxJdsH91rIehX3lHZbn5mv+V2DJiMNDXpe9aTaIRHOsuLp8Ua4GYyVFrDQ7qSgcktTyCFjjWq/mb4sK4ahsj63DXrJgwIDAQAB";
		byte[] b = Base64.decode(key);
		for (int i = 0; i < b.length; i++) {
			System.out.println(Integer.toHexString(b[i]));
		}
	}
}
