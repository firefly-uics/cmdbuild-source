package utils;

import static org.hamcrest.Matchers.containsString;

import org.hamcrest.Matcher;

public abstract class JsonMatchers {

	public static Matcher<String> containsPair(String key, Object value) {
		String valueString;
		if (value == null) {
			valueString = "null";
		} else if (value instanceof String) {
			valueString = String.format("\"%s\"", value);
		} else {
			valueString = value.toString();
		}
		return containsString(String.format("\"%s\":%s", key, valueString));
	}

	public static Matcher<String> containsKey(String key) {
		return containsString(String.format("\"%s\":", key));
	}

	public static Matcher<String> containsArrayWithKey(String array, String key) {
		return containsString(String.format("\"%s\":%s", key, array));
	}

	public static Matcher<String> containsObjectWithKey(String object, String key) {
		return containsString(String.format("\"%s\":%s", key, object));
	}
}
