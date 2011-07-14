package org.cmdbuild.dao.entry;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.joda.time.DateTime;

public abstract class DBEntry {

	final DBDriver driver;

	private final DBEntryType type;
	private Object id;
	private final Map<String, Object> values;

	private String user;
	private DateTime beginDate;
	private DateTime endDate;

	protected DBEntry(final DBDriver driver, final DBEntryType type, final Object id) {
		this.driver = driver;
		this.type = type;
		this.id = id;
		this.values = new HashMap<String, Object>();
	}

	public DBEntryType getType() {
		return type;
	}

	public final Object getId() {
		return id;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setBeginDate(final DateTime beginDate) {
		this.beginDate = beginDate;
	}

	public DateTime getBeginDate() {
		return beginDate;
	}

	public void setEndDate(final DateTime endDate) {
		this.endDate = endDate;
	}

	public DateTime getEndDate() {
		return endDate;
	}

	/*
	 * The returned value should be immutable, but it is not a real problem:
	 * the CMEntry interface does not allow a card to be saved, and even with
	 * that, Object would have to be casted to a mutable value. After all,
	 * reflection can do anything, so there is no point in being over-strict.
	 */
	public final Object get(final String key) {
		if (!values.containsKey(key)) {
			if (type.getAttribute(key) != null && !isNew()) {
				// TODO It was lazy loaded: load the remaining values
				throw new UnsupportedOperationException("Not implemented");
			} else {
				throw new IllegalArgumentException();
			}
		}
		return values.get(key);
	}

	private boolean isNew() {
		return (id == null);
	}

	public Iterable<Map.Entry<String, Object>> getValues() {
		return values.entrySet();
	}

	public final void setOnly(final String key, final Object value) {
		if (type.getAttribute(key) == null) {
			throw new IllegalArgumentException();
		}
		// TODO convert value
		values.put(key, value);
	}

	protected void saveOnly() {
		id = driver.create(this);
	}
}
