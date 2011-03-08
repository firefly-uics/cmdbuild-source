package org.cmdbuild.connector;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.cmdbuild.connector.logger.Log;

public class ExternalModuleLoader<T> {

	protected static final Logger logger = Log.CONNECTOR;

	private final String className;

	public ExternalModuleLoader(final String className) {
		Validate.notNull(className, "null name");
		Validate.notEmpty(className, "empty name");
		this.className = className;
	}

	@SuppressWarnings("unchecked")
	public T load() throws ConnectorException {
		logger.debug(System.getProperty("java.class.path"));

		try {
			// TODO check class type
			final Class<?> clazz = Class.forName(className);
			final Object instance = clazz.newInstance();
			return (T) instance;
		} catch (final Throwable e) {
			throw new ConnectorException("unable to load class " + className, e);
		}
	}

}
