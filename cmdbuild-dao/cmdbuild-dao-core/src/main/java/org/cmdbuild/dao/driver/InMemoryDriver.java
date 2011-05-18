package org.cmdbuild.dao.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;

public class InMemoryDriver implements DBDriver {

	private AtomicInteger idGenerator = new AtomicInteger(42);

	private final Collection<DBClass> allClasses;

	public InMemoryDriver() {
		allClasses = new ArrayList<DBClass>();
	}

	/*
	 * Note: The store has to be cloned, otherwise it could be changed by other components
	 */
	public Collection<DBClass> findAllClasses() {
		return new ArrayList<DBClass>(allClasses);
	}

	@Override
	public DBClass createClass(final String name, final DBClass parent) {
		final DBClass newClass = new DBClass(name, idGenerator.getAndIncrement(), new ArrayList<DBAttribute>(0));
		newClass.setParent(parent);
		allClasses.add(newClass);
		return newClass;
	}

	@Override
	public void deleteClass(final DBClass dbClass) {
		allClasses.remove(dbClass);
		dbClass.setParent(null);
	}

	@Override
	public void update(DBEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DBClass findClassById(Object id) {
		for (DBClass c : allClasses) {
			if (c.getId().equals(id)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

	@Override
	public DBClass findClassByName(final String name) {
		for (DBClass c : allClasses) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		return null;
	}

	@Override
	public Object create(DBEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(DBEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CMQueryResult query(QuerySpecs query) {
		throw new UnsupportedOperationException();
	}
}