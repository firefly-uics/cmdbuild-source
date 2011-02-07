package org.cmdbuild.portlet;

import org.apache.log4j.Logger;

public final class Log {

	private Log() {
	};

	public static final Logger ROOT = Logger.getRootLogger();
	public static final Logger PORTLET = Logger.getLogger("portlet");

}
