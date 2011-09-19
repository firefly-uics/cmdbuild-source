package org.cmdbuild.dms;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.cmdbuild.dms.properties.DmsProperties;
import org.cmdbuild.dms.properties.NullDmsPropertiesProxy;

public abstract class BaseDmsService implements DmsService {

	private static final DmsProperties NULL_DMS_PROPERTIES = NullDmsPropertiesProxy.getDmsProperties();

	protected final Logger logger = Logger.getLogger(getClass());

	private DmsProperties properties;

	public DmsProperties getProperties() {
		return (properties == null) ? NULL_DMS_PROPERTIES : properties;
	}

	public void setProperties(final DmsProperties properties) {
		Validate.notNull(properties, "null properties");
		this.properties = properties;
	}

}
