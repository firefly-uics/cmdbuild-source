package org.cmdbuild.auth;

import java.util.Map;

import org.cmdbuild.auth.acl.CMGroup;

public interface GroupFetcher {

	/**
	 * Retrieves all groups stored in the database
	 * 
	 * @return
	 */
	Iterable<CMGroup> fetchAllGroups();

	/**
	 * Retrieves a map of all groups stored in the database.
	 * 
	 * @return a map where the key is the id of the group and the value is a
	 *         group object
	 */
	Map<Long, CMGroup> fetchAllGroupIdToGroup();

}
