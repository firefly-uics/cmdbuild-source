package org.cmdbuild.auth.acl;

public interface CMGroup {

	String getName();
	String getDescription();
	CMSecurityManager getSecurityManager();
}
