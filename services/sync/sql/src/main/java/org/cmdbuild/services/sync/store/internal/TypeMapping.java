package org.cmdbuild.services.sync.store.internal;

import org.cmdbuild.services.sync.store.ClassType;

public interface TypeMapping {

	ClassType getType();

	Iterable<AttributeMapping> getAttributeMappings();

}
