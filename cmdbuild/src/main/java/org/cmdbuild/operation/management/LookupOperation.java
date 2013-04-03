package org.cmdbuild.operation.management;

import java.util.List;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.auth.UserContext;
import org.springframework.beans.factory.annotation.Autowired;

public class LookupOperation {

	private final UserContext userCtx;

	@Autowired
	private final CMBackend backend = CMBackend.INSTANCE;

	public LookupOperation(final UserContext userCtx) {
		this.userCtx = userCtx;
	}

	public Lookup getLookupById(final int lookupId) {
		return backend.getLookup(lookupId);
	}

	public List<Lookup> getLookupList(final String lookupType) throws NotFoundException, ORMException {
		return backend.getLookupList(lookupType, null);
	}

}
