package org.cmdbuild.service.rest.cxf.service;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jasypt.commons.CommonUtils.STRING_OUTPUT_TYPE_HEXADECIMAL;
import static org.joda.time.DateTime.now;

import java.util.UUID;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class RandomTokenGenerator implements TokenGenerator {

	private final StandardPBEStringEncryptor encryptor;

	public RandomTokenGenerator() {
		encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(randomAlphanumeric(42));
		encryptor.setStringOutputType(STRING_OUTPUT_TYPE_HEXADECIMAL);
	}

	@Override
	public String generate(final String username) {
		final String key = new StringBuilder(username) //
				.append("|").append(now().getMillis()) //
				.append("|").append(UUID.randomUUID().toString()) //
				.toString();
		return encryptor.encrypt(key);
	}

}
