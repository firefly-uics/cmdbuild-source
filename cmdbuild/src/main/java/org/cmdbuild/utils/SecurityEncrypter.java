package org.cmdbuild.utils;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.cmdbuild.logger.Log;

@SuppressWarnings("restriction")
public class SecurityEncrypter {

	static final byte[] salt = {
		(byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
		(byte) 0x56, (byte) 0x34, (byte) 0xE3, (byte) 0x03
	};
	static final int uznig = 0x19d15ea;
	static final int iterationCount = 19;

	static private Cipher ecipher;
	static private Cipher dcipher;

	static {
		String pPh = Integer.toString(uznig);
		try {
			KeySpec keySpec = new PBEKeySpec(pPh.toCharArray(), salt, iterationCount);
			SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
			AlgorithmParameterSpec cypherParameters = new PBEParameterSpec(salt, iterationCount);

			ecipher = Cipher.getInstance(key.getAlgorithm());
			ecipher.init(Cipher.ENCRYPT_MODE, key, cypherParameters);

			dcipher = Cipher.getInstance(key.getAlgorithm());
			dcipher.init(Cipher.DECRYPT_MODE, key, cypherParameters);
		} catch (Exception e) {
			Log.OTHER.fatal("Error Preparing Security Encrypter", e);
		}
	}

	public String encrypt(String password) {
		try {
			byte[] passwordBytesAsUTF8Encoding = password.getBytes("UTF8");
			byte[] encryptedPasswordBytes = ecipher.doFinal(passwordBytesAsUTF8Encoding);
			return new sun.misc.BASE64Encoder().encode(encryptedPasswordBytes);
		} catch (Exception e) {
			Log.OTHER.error("Error Encrypting", e);
		}
		return null;
	}

	public String decrypt(String encodedBase64Password) {
		try {
			byte[] encryptedPasswordBytes = new sun.misc.BASE64Decoder().decodeBuffer(encodedBase64Password);
			byte[] passwordBytesAsUTF8Encoding = dcipher.doFinal(encryptedPasswordBytes);
			return new String(passwordBytesAsUTF8Encoding, "UTF8");
		} catch (Exception e) {
			Log.OTHER.error("Error Decrypting", e);
		}
		return null;
	}
}
