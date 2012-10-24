package org.cmdbuild.dao.view;

import static org.cmdbuild.dao.entrytype.Deactivable.IsActivePredicate.filterActive;

import java.util.List;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;

import com.google.common.collect.Lists;

public class DBDataView extends QueryExecutorDataView {

	private final DBDriver driver;

	public DBDataView(final DBDriver driver) {
		this.driver = driver;
	}

	@Override
	public DBClass findClassById(final Long id) {
		return driver.findClassById(id);
	}

	@Override
	public DBClass findClassByName(final String name) {
		return driver.findClassByName(name);
	}

	@Override
	public Iterable<DBClass> findClasses() {
		return filterActive(findAllClasses());
	}

	@Override
	public Iterable<DBClass> findAllClasses() {
		return driver.findAllClasses();
	}

	@Override
	public Iterable<DBDomain> findDomains() {
		return filterActive(findAllDomains());
	}

	@Override
	public Iterable<DBDomain> findAllDomains() {
		return driver.findAllDomains();
	}

	@Override
	public Iterable<DBDomain> findDomainsFor(final CMClass cmClass) {
		final List<DBDomain> domainsForClass = Lists.newArrayList();
		for (final DBDomain d : findDomains()) {
			if (d.getClass1().isAncestorOf(cmClass) || d.getClass2().isAncestorOf(cmClass)) {
				domainsForClass.add(d);
			}
		}
		return domainsForClass;
	}

	@Override
	public DBDomain findDomainById(final Long id) {
		return driver.findDomainById(id);
	}

	@Override
	public DBDomain findDomainByName(final String name) {
		return driver.findDomainByName(name);
	}

	@Override
	public Iterable<? extends CMFunction> findAllFunctions() {
		return driver.findAllFunctions();
	}

	@Override
	public CMFunction findFunctionByName(final String name) {
		return driver.findFunctionByName(name);
	}

	@Override
	public DBCard newCard(final CMClass type) {
		final DBClass dbType = findClassById(type.getId());
		return DBCard.newInstance(driver, dbType);
	}

	@Override
	public DBCard modifyCard(final CMCard type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CMQueryResult executeNonEmptyQuery(final QuerySpecs querySpecs) {
		return driver.query(querySpecs);
	}

}
