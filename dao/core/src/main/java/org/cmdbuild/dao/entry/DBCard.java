package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.DBClass;

public class DBCard extends DBEntry implements CMCard, CMCardDefinition {

	public static DBCard create(final DBDriver driver, final DBClass type) {
		return new DBCard(driver, type, null);
	}

	public static DBCard create(final DBDriver driver, final DBClass type, final Long id) {
		return new DBCard(driver, type, id);
	}

	private DBCard(final DBDriver driver, final DBClass type, final Long id) {
		super(driver, type, id);
	}

	public final DBCard set(final String key, final Object value) {
		setOnly(key, value);
		return this;
	}

	public DBCard save() {
		saveOnly();
		return  this;
	}

	public DBClass getType() {
		return (DBClass) super.getType();
	}
}
