package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.DBDomain;

public class DBRelation extends DBEntry implements CMRelation, CMRelationDefinition {

	public static final String _1 = "_1";
	public static final String _2 = "_2";

	private static final String IDOBJ1 = "IdObj1";
	private static final String IDCLASS1 = "IdClass1";
	private static final String IDOBJ2 = "IdObj2";
	private static final String IDCLASS2 = "IdClass2";

	public static DBRelation newInstance(final DBDriver driver, final DBDomain type) {
		return new DBRelation(driver, type, null);
	}

	public static DBRelation newInstance(final DBDriver driver, final DBDomain type, final Long id) {
		return new DBRelation(driver, type, id);
	}

	private DBRelation(final DBDriver driver, final DBDomain type, final Long id) {
		super(driver, type, id);
	}

	@Override
	public DBRelation save() {
		saveOnly();
		return this;
	}

	@Override
	public DBDomain getType() {
		return (DBDomain) super.getType();
	}

	/*
	 * FIXME this is a total mess
	 */

	@Override
	public DBRelation setCard1(final CMCard card) {
		setOnly(IDOBJ1, card.getId());
		setOnly(IDCLASS1, card.getType().getId());
		return this;
	}

	@Override
	public DBRelation setCard2(final CMCard card) {
		setOnly(IDOBJ2, card.getId());
		setOnly(IDCLASS2, card.getType().getId());
		return this;
	}

	@Override
	public final DBRelation set(final String key, final Object value) {
		if (_1.equals(key)) {
			setCard1((CMCard)value);
		} else if (_2.equals(key)) {
			setCard2((CMCard)value);
		} else {
			setOnly(key, value);
		}
		return this;
	}
}
