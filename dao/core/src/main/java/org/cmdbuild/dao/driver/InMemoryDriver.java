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

public class InMemoryDriver extends AbstractDBDriver {

	private final AtomicLong idGenerator = new AtomicLong(42);

	private final Collection<DBClass> allClasses;
	private final Collection<DBDomain> allDomains;
	private final Collection<DBFunction> allFunctions;

	public InMemoryDriver() {
		allClasses = new ArrayList<DBClass>();
		allDomains = new ArrayList<DBDomain>();
		allFunctions = new ArrayList<DBFunction>();
	}

	@Override
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
	public DBClass createSuperClass(final String name, final DBClass parent) {
		final ClassMetadata classMetadata = new ClassMetadata();
		classMetadata.put(ClassMetadata.SUPERCLASS, Boolean.valueOf(true).toString());
		final DBClass newClass = new DBClass(name, idGenerator.getAndIncrement(), classMetadata,
				new ArrayList<DBAttribute>(0));
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
	public Collection<DBDomain> findAllDomains() {
		return new ArrayList<DBDomain>(allDomains);
	}

	@Override
	public DBDomain createDomain(final DomainDefinition domainDefinition) {
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
	public void deleteDomain(final DBDomain dbDomain) {
		allDomains.remove(dbDomain);
	}

	@Override
	public Collection<DBFunction> findAllFunctions() {
		return new ArrayList<DBFunction>(allFunctions);
	}

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