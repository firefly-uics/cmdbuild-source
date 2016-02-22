package org.cmdbuild.dms.cmis;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.MetadataType;

public interface CmisConverter {
	
	public void setConfiguration(final DmsConfiguration configuration);
	
	public Object convertToCmisValue(Session session, PropertyDefinition<?> propertyDefinition, String value);

	public String convertFromCmisValue(Session session, PropertyDefinition<?> propertyDefinition, Object cmisValue);

	public MetadataType getType(PropertyDefinition<?> propertyDefinition);

	public boolean isAsymmetric();

}
