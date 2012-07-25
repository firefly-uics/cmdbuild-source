package org.cmdbuild.dao.reference;


public interface CMReference {

	public Long getId();
	void accept(CMReferenceVisitor visitor);
}
