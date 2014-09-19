package org.cmdbuild.common.template;

import static com.google.common.reflect.Reflection.getPackageName;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

public interface LoggingSupport {

	static Logger logger = getLogger(getPackageName(LoggingSupport.class));

}
