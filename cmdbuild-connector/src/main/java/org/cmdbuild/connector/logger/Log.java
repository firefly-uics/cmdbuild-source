package org.cmdbuild.connector.logger;

import org.apache.log4j.Logger;

public final class Log {

	private Log() {
	}

	public static final Logger ROOT = Logger.getRootLogger();
	public static final Logger CONNECTOR = Logger.getLogger("connector");
	public static final Logger PARSER = Logger.getLogger("parser");
	public static final Logger DIFFER = Logger.getLogger("differ");
	public static final Logger SYNC = Logger.getLogger("sync");

}
