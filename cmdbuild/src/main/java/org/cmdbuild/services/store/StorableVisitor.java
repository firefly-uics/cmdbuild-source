package org.cmdbuild.services.store;

import org.cmdbuild.model.Menu;

public interface StorableVisitor {

	void visit(Menu s);

}
