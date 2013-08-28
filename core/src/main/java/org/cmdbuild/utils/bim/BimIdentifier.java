package org.cmdbuild.utils.bim;

import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.services.bim.DefaultBimDataModelManager;

public class BimIdentifier implements CMIdentifier {
	
	private String localName;

	public BimIdentifier(){}
	
	public static BimIdentifier newIdentifier() {
		return new BimIdentifier();
	}
	
	public BimIdentifier withName(final String name) {
		this.localName = name;
		return this;
	}
	
	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public String getNameSpace() {
		return DefaultBimDataModelManager.BIM_SCHEMA;
	}
	
	
}
