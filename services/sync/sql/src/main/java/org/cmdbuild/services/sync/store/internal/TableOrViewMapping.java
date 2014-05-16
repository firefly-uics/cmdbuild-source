package org.cmdbuild.services.sync.store.internal;

public interface TableOrViewMapping {

	String getName();

	Iterable<TypeMapping> getTypeMappings();

}
