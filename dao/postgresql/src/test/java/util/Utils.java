package util;

import org.apache.commons.lang.SystemUtils;

public class Utils {

	private Utils() {
		// prevents instantiation
	}

	public static String clean(final String sql) {
		return sql //
				.replace(SystemUtils.LINE_SEPARATOR, " ") //
				.replaceAll("[ ]+", " ") //
		;
	}

}
