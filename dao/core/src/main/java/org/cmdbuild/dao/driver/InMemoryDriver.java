package org.cmdbuild.dao.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;

public class InMemoryDriver extends CachingDriver {

	private AtomicInteger idGenerator = new AtomicInteger(42);

	private final Collection<DBClass> allClasses;
	private final Collection<DBDomain> allDomains;

	public InMemoryDriver() {
		allClasses = new ArrayList<DBClass>();
		allDomains = new ArrayList<DBDomain>();
	}

	/*
	 * CachingDriver Classes
	 */

	@Override
	protected Collection<DBClass> findAllClassesNoCache() {
		// Note: The store has to be cloned, otherwise it could be changed by other components
		return new ArrayList<DBClass>(allClasses);
	}

	@Override
	public DBClass createClassNoCache(final String name, final DBClass parent) {
		final DBClass newClass = new DBClass(name, idGenerator.getAndIncrement(), new ArrayList<DBAttribute>(0));
		newClass.setParent(parent);
		allClasses.add(newClass);
		return newClass;
	}

	@Override
	public void deleteClassNoCache(final DBClass dbClass) {
		allClasses.remove(dbClass);
		dbClass.setParent(null);
	}

	/*
	 * CachingDriver Domains
	 */

	@Override
	protected Collection<DBDomain> findAllDomainsNoCache() {
		// Note: The store has to be cloned, otherwise it could be changed by other components
		return new ArrayList<DBDomain>(allDomains);
	}

	@Override
	protected DBDomain createDomainNoCache(String name, DBClass class1, DBClass class2) {
		final DBDomain newDomain = new DBDomain(name, idGenerator.getAndIncrement(), new ArrayList<DBAttribute>(0));
		newDomain.setClass1(class1);
		newDomain.setClass2(class2);
		allDomains.add(newDomain);
		return newDomain;
	}

	@Override
	protected void deleteDomainNoCache(DBDomain dbDomain) {
		allDomains.remove(dbDomain);
	}

	/*
	 * DBDriver
	 */

	@Override
	public Object create(DBEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(DBEntry entry) {
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