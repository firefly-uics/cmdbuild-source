package org.cmdbuild.dao.driver;

import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.function.DBFunction;

public abstract class AbstractDBDriver implements DBDriver {

	@Override
	public final DBClass findClassById(final Long id) {
		for (final DBClass entry : findAllClasses()) {
			if (entry.getId().equals(id)) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public final DBClass findClassByName(final String name) {
		for (final DBClass entry : findAllClasses()) {
			if (entry.getName().equals(name)) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public DBDomain findDomainById(final Long id) {
		for (final DBDomain entry : findAllDomains()) {
			if (entry.getId().equals(id)) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public DBDomain findDomainByName(final String name) {
		for (final DBDomain entry : findAllDomains()) {
			if (entry.getName().equals(name)) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public DBFunction findFunctionByName(final String name) {
		for (final DBFunction entry : findAllFunctions()) {
			if (entry.getName().equals(name)) {
				return entry;
			}
		}
		return null;
	}

}