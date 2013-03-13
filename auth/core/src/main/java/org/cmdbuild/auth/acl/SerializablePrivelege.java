package org.cmdbuild.auth.acl;

public interface SerializablePrivelege extends CMPrivilegedObject {

	Long getId();

	String getName();

	String getDescription();
}
