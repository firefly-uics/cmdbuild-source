package org.cmdbuild.dao.driver.postgres.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface LoggingSupport {

	Logger logger = LoggerFactory.getLogger("sql");

}
