package org.cmdbuild.dao.driver;

public interface CachingDriver extends DBDriver {

	void clearCache();

	void clearClassesCache();

	void clearDomainsCache();

	void clearFunctionsCache();

}
