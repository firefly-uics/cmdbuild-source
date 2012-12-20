package org.cmdbuild.dao.driver;

import org.cmdbuild.dao.entrytype.DBEntryType;

/**
 * Marks the driver as handling versioning itself, thus needing special
 * handling.
 */
public interface SelfVersioningDBDriver extends DBDriver {

	void clearEntryType(final DBEntryType type);

}
