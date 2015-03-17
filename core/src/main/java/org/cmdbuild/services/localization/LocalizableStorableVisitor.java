package org.cmdbuild.services.localization;

import org.cmdbuild.data.store.lookup.Lookup;


public interface LocalizableStorableVisitor {

	void visit(Lookup lookupImpl);

}
