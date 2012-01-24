package org.cmdbuild.auth.password;

public interface PasswordHandler {

	String decrypt(String encodedBase64Password);

	String encrypt(String password);
	
}
