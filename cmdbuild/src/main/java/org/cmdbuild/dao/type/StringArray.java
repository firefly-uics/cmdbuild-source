package org.cmdbuild.dao.type;

import java.util.Arrays;
import java.util.StringTokenizer;

public class StringArray {

	private String[] value;

	// Using constructor method to return null
	private StringArray(String[] value) {
		this.value = value;
	}

	public static StringArray valueOf(String[] value) {
		if (value != null) {
			return new StringArray(value);
		} else {
			return null;
		}
	}

	public static StringArray valueOf(String stringValue) {
		StringArray stringArray = null;
		if (stringValue != null && !stringValue.trim().isEmpty()) {
			if(stringValue.indexOf("{") != -1) {
				stringValue = stringValue.replaceAll("\\{", "");
				stringValue = stringValue.replaceAll("\\}", "");
			}
			StringTokenizer tokenizer = new StringTokenizer(stringValue, ",");
			String[] stringArrayValue = new String[tokenizer.countTokens()];
			int i = 0;
	        while (tokenizer.hasMoreTokens()) {
	        	stringArrayValue[i++] = tokenizer.nextToken();
	        }
	        stringArray = new StringArray(stringArrayValue);
		}
		return stringArray;
	}

	public String[] getValue() {
		return value;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (value == null) { // WAS: || value.length == 0
			sb.append("null");
		} else {
			sb.append("'{");
			for (int i=0; i<value.length; ++i) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(value[i]);
			}
			sb.append("}'");
		}
		return sb.toString();
	}

	public int hashCode() {
		return Arrays.hashCode(value);
	}

	public boolean equals(Object o) {
		if (o instanceof StringArray)
			return Arrays.equals(value, ((StringArray) o).getValue());
		return false;
	}
}
