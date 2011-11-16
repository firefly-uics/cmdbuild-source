package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.DBClass;

public class DBCard extends DBEntry implements CMCard, CMCardDefinition {

	public static DBCard newInstance(final DBDriver driver, final DBClass type) {
		return new DBCard(driver, type, null);
	}

	public static DBCard newInstance(final DBDriver driver, final DBClass type, final Object id) {
		return new DBCard(driver, type, id);
	}

	private DBCard(final DBDriver driver, final DBClass type, final Object id) {
		super(driver, type, id);
	}

	@Override
	public final DBCard set(final String key, final Object value) {
		setOnly(key, value);
		return this;
	}

	@Override
	public DBCard save() {
		saveOnly();
		return  this;
	}

	@Override
	public DBClass getType() {
		return (DBClass) super.getType();
	}
}
