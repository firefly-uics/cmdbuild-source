package org.cmdbuild.dao.view;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBClass;

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
	public DBCard newCard(CMClass type) {
		final DBClass dbType = findClassById(type.getId());
		return DBCard.create(driver, dbType);
	}

	@Override
	public DBCard modifyCard(CMCard type) {
		throw new UnsupportedOperationException();
	}
}
