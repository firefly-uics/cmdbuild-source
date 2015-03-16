package org.cmdbuild.data.store.lookup;

import org.cmdbuild.data.store.Storable;

public interface Lookup extends Storable {

	String code();

	String description();

	String notes();

	LookupType type();

	Integer number();

	boolean active();

	boolean isDefault();

	Long parentId();

	Lookup parent();

	String uuid();

	@Override
	String getIdentifier();

	String getTranslationUuid();

	@Override
	String toString();

	Long getId();

	void setId(Long id);

	// FIXME Do I really need it?
	String getDescription();

}