package org.cmdbuild.auth.user;

import org.cmdbuild.auth.acl.CMGroup;

public interface CMUser {

	String getName();
	Iterable<CMGroup> getGroups();
}
