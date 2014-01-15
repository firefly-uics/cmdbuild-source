package org.cmdbuild.logic.email.rules;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.cmdbuild.logic.email.rules.StartWorkflow.Mapper;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class PropertiesMapper implements Mapper {

	private static final Marker marker = MarkerFactory.getMarker(PropertiesMapper.class.getName());

	private final Properties properties;

	public PropertiesMapper(final String mapping) {
		final Reader reader = new StringReader(mapping);
		properties = new Properties();
		try {
			properties.load(reader);
		} catch (final IOException e) {
			logger.warn(marker, "error reading properties", e);
		}
		IOUtils.closeQuietly(reader);
	}

	@Override
	public Object getValue(final String name) {
		return properties.getProperty(name);
	}

}
