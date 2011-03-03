package org.cmdbuild.dao.type;

import java.util.Arrays;
import java.util.StringTokenizer;

public class IntArray {

	private int[] value;

	// Using constructor method to return null
	private IntArray(int[] value) {
		this.value = value;
	}

	public static IntArray valueOf(int[] value) {
		if (value != null) {
			return new IntArray(value);
		} else {
			return null;
		}
	}

	public static IntArray valueOf(String stringValue) {
		IntArray intArray = null;
		if (stringValue != null && !stringValue.trim().isEmpty()) {
			if(stringValue.indexOf("{") != -1) {
				stringValue = stringValue.replaceAll("\\{", "");
				stringValue = stringValue.replaceAll("\\}", "");
			}
			StringTokenizer tokenizer = new StringTokenizer(stringValue, ",");
			int[] intArrayValue = new int[tokenizer.countTokens()];
			int i = 0;
	        while (tokenizer.hasMoreTokens()) {
	        	intArrayValue[i++] = Integer.parseInt(tokenizer.nextToken());
	        }
	        intArray = new IntArray(intArrayValue);
		}
		return intArray;
	}

	public static IntArray valueOf(Integer[] integerArrayValue) {
		IntArray intArray = null;
		if (integerArrayValue != null) {
			int[] intArrayValue = new int[integerArrayValue.length];
			int i = 0;
			for (int val : integerArrayValue) {
				intArrayValue[i++] = val;
			}
			intArray = new IntArray(intArrayValue);
		}
		return intArray;
	}

	public int[] getValue() {
		return value;
	}

	public String toString() {
		return value.toString();
	}

	public int hashCode() {
		return Arrays.hashCode(value);
	}

	public boolean equals(Object o) {
		if (o instanceof IntArray) {
			return Arrays.equals(value, ((IntArray) o).getValue());
		} else {
			return false;
		}
	}
}
