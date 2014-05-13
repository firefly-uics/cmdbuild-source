package org.cmdbuild.services.sync.store.internal;

import org.cmdbuild.services.sync.store.Type;

public interface Catalog {

	Iterable<Type> getTypes();

}
