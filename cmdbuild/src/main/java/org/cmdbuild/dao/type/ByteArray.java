package org.cmdbuild.dao.type;

import java.util.Arrays;

public class ByteArray {

	private byte[] value;

	private ByteArray(byte[] value) {
		this.value = value;
	}

	public static ByteArray valueOf(byte[] value) {
		if (value != null) {
			return new ByteArray(value);
		} else {
			return null;
		}
	}

	public byte[] getValue() {
		return value;
	}

	public String toString() {
		return value.toString();
	}

	public int hashCode() {
		return Arrays.hashCode(value);
	}

	public boolean equals(Object o) {
		if (o instanceof ByteArray)
			return Arrays.equals(value, ((ByteArray) o).getValue());
		return false;
	}
}
