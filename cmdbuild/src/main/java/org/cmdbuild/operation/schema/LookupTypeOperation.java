package org.cmdbuild.operation.schema;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.LookupType;
import org.springframework.beans.factory.annotation.Autowired;

public class LookupTypeOperation {

	@Autowired
	private CMBackend backend = CMBackend.INSTANCE;

	public LookupType getLookupType(String type) {
		LookupType lookupType = backend.getLookupType(type);
		return lookupType;
	}

}
