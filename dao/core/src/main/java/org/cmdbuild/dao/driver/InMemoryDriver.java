package org.cmdbuild.dao.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBClass.ClassMetadata;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;

public class InMemoryDriver extends CachingDriver {

	private final AtomicLong idGenerator = new AtomicLong(42);

	private final Collection<DBClass> allClasses;
	private final Collection<DBDomain> allDomains;
	private final Collection<DBFunction> allFunctions;

	public InMemoryDriver() {
		allClasses = new ArrayList<DBClass>();
		allDomains = new ArrayList<DBDomain>();
		allFunctions = new ArrayList<DBFunction>();
	}

	/*
	 * CachingDriver Classes
	 */

	@Override
	protected Collection<DBClass> findAllClassesNoCache() {
		// Note: The store has to be cloned, otherwise it could be changed by
		// other components
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
	public DBClass createSuperClassNoCache(final String name, final DBClass parent) {
		final ClassMetadata classMetadata = new ClassMetadata();
		classMetadata.put(ClassMetadata.SUPERCLASS, Boolean.valueOf(true).toString());
		final DBClass newClass = new DBClass(name, idGenerator.getAndIncrement(), classMetadata,
				new ArrayList<DBAttribute>(0));
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
		// Note: The store has to be cloned, otherwise it could be changed by
		// other components
		return new ArrayList<DBDomain>(allDomains);
	}

	@Override
	protected DBDomain createDomainNoCache(final DomainDefinition domainDefinition) {
		final DBDomain newDomain = DBDomain.newDomain() //
				.withName(domainDefinition.getName()) //
				.withId(idGenerator.getAndIncrement()) //
				.withClass1(domainDefinition.getClass1()) //
				.withClass2(domainDefinition.getClass2()) //
				.build();
		allDomains.add(newDomain);
		return newDomain;
	}

	@Override
	protected void deleteDomainNoCache(final DBDomain dbDomain) {
		allDomains.remove(dbDomain);
	}

	/*
	 * CachingDriver Functions
	 */

	@Override
	public Collection<DBFunction> findAllFunctionsNoCache() {
		return new ArrayList<DBFunction>(allFunctions);
	}

	/*
	 * DBDriver
	 */

	@Override
	public Long create(final DBEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(final DBEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(final DBEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CMQueryResult query(final QuerySpecs query) {
		throw new UnsupportedOperationException();
	}
}