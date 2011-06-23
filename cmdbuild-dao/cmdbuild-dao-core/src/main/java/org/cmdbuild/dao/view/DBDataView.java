package org.cmdbuild.dao.view;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;

public class DBDataView implements CMDataView {

	private final DBDriver driver;

	public DBDataView(final DBDriver driver) {
		this.driver = driver;
	}

	@Override
	public DBClass findClassById(Object id) {
		return driver.findClassById(id);
	}

	@Override
	public DBClass findClassByName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<DBClass> findAllClasses() {
		return driver.findAllClasses();
	}

	@Override
	public Iterable<DBDomain> findAllDomains() {
		return driver.findAllDomains();
	}

	@Override
	public Iterable<DBDomain> findDomains(final CMClass cmClass) {
		List<DBDomain> domainsForClass = new ArrayList<DBDomain>();
		for (DBDomain d : findAllDomains()) {
			if (d.getClass1().isAncestorOf(cmClass) || d.getClass2().isAncestorOf(cmClass)) {
				domainsForClass.add(d);
			}
		}
		return domainsForClass;
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

}
