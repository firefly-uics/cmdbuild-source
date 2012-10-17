package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.DBClass;

public class DBCard extends DBEntry implements CMCard, CMCardDefinition {

	private DBCard(final DBDriver driver, final DBClass type, final Long id) {
		super(driver, type, id);
	}

	@Override
	public final DBCard set(final String key, final Object value) {
		setOnly(key, value);
		return this;
	}

	@Override
	public DBClass getType() {
		return (DBClass) super.getType();
	}

	@Override
	public Object getCode() {
		return get(getType().getCodeAttributeName());
	}

	@Override
	public CMCardDefinition setCode(Object value) {
		return set(getType().getCodeAttributeName(), value);
	}

	@Override
	public Object getDescription() {
		return get(getType().getDescriptionAttributeName());
	}

	@Override
	public CMCardDefinition setDescription(Object value) {
		return set(getType().getDescriptionAttributeName(), value);
	}

	@Override
	public DBCard save() {
		saveOnly();
		return this;
	}

	public static DBCard newInstance(final DBDriver driver, final DBClass type) {
		return new DBCard(driver, type, null);
	}

	public static DBCard newInstance(final DBDriver driver, final DBClass type, final Long id) {
		return new DBCard(driver, type, id);
	}

}
