package org.cmdbuild.logic.files;

import com.google.common.hash.HashFunction;

public class DefaultHashing implements Hashing {

	private final HashFunction hashFunction;

	public DefaultHashing() {
		hashFunction = com.google.common.hash.Hashing.md5();
	}

	@Override
	public String hash(final String value) {
		return hashFunction.hashBytes(value.getBytes()).toString();
	}

}