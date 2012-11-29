package org.cmdbuild.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * @deprecated use Apache Commons instead.
 */
@Deprecated
public class FileUtils {

	/**
	 * @deprecated use Apache Commons instead.
	 */
	@Deprecated
	public static String getContents(final String file) {
		final File aFile = new File(file);

		try {
			return org.apache.commons.io.FileUtils.readFileToString(aFile);
		} catch (final IOException ex) {
			// TODO log me please!
			return StringUtils.EMPTY;
		}
	}

}
