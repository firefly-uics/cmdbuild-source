package org.cmdbuild.dms;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dms.properties.DmsProperties;
import org.cmdbuild.dms.properties.NullDmsPropertiesProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDmsService implements DmsService {

	private static final DmsProperties NULL_DMS_PROPERTIES = NullDmsPropertiesProxy.getDmsProperties();

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private DmsProperties properties;

	public DmsProperties getProperties() {
		return (properties == null) ? NULL_DMS_PROPERTIES : properties;
	}

	public void setProperties(final DmsProperties properties) {
		Validate.notNull(properties, "null properties");
		this.properties = properties;
	}

}
