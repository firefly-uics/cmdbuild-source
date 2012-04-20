package org.cmdbuild.dao.reference;


public interface CMReference {

	public Object getId();
	void accept(CMReferenceVisitor visitor);
}
