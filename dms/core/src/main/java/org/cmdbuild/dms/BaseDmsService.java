package org.cmdbuild.dms;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dms.DmsConfiguration.NullDmsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDmsService implements DmsService {

	private static final DmsConfiguration NULL_CONFIGURATION = NullDmsConfiguration.newInstance();

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private DmsConfiguration configuration;

	@Override
	public DmsConfiguration getConfiguration() {
		return (configuration == null) ? NULL_CONFIGURATION : configuration;
	}

	@Override
	public void setConfiguration(final DmsConfiguration configuration) {
		Validate.notNull(configuration, "null configuration");
		this.configuration = configuration;
	}

}
