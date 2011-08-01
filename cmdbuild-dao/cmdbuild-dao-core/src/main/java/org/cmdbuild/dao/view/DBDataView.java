package org.cmdbuild.dao.view;

import static org.cmdbuild.dao.entrytype.Deactivable.IsActivePredicate.filterActive;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.QuerySpecsBuilder;

public class DBDataView implements CMDataView {

	private final DBDriver driver;

	public DBDataView(final DBDriver driver) {
		this.driver = driver;
	}

	@Override
	public final DBClass findClass(final Object idOrName) {
		DBClass c = null;
		if (idOrName instanceof String) {
			c = findClassByName((String) idOrName);
		}
		if (c == null) {
			c = findClassById(idOrName);
		}
		return c;
	}

	@Override
	public DBClass findClassById(Object id) {
		return driver.findClassById(id);
	}

	@Override
	public DBClass findClassByName(String name) {
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
		List<DBDomain> domainsForClass = new ArrayList<DBDomain>();
		for (DBDomain d : findDomains()) {
			if (d.getClass1().isAncestorOf(cmClass) || d.getClass2().isAncestorOf(cmClass)) {
				domainsForClass.add(d);
			}
		}
		return domainsForClass;
	}

	@Override
	public final DBDomain findDomain(final Object idOrName) {
		DBDomain c = null;
		if (idOrName instanceof String) {
			c = findDomainByName((String) idOrName);
		}
		if (c == null) {
			c = findDomainById(idOrName);
		}
		return c;
	}

	@Override
	public DBDomain findDomainById(Object id) {
		return driver.findDomainById(id);
	}

	@Override
	public DBDomain findDomainByName(String name) {
		return driver.findDomainByName(name);
	}

	@Override
	public DBCard newCard(CMClass type) {
		final DBClass dbType = findClassById(type.getId());
		return DBCard.create(driver, dbType);
	}

	@Override
	public DBCard modifyCard(CMCard type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public QuerySpecsBuilder select(final Object... attrDef) {
		return new QuerySpecsBuilder(this).select(attrDef);
	}

	@Override
	public CMQueryResult query(QuerySpecs querySpecs) {
		return driver.query(querySpecs);
	}
}
