package org.cmdbuild.dao.entry;

import static java.lang.String.format;

import java.util.Map;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public abstract class DBEntry implements CMValueSet {

	private final DBDriver driver;

	private final DBEntryType type;
	private final Map<String, Object> values;

	private Long id;
	private String user;
	private DateTime beginDate;
	private DateTime endDate;

	protected DBEntry(final DBDriver driver, final DBEntryType type, final Long id) {
		this.driver = driver;
		this.type = type;
		this.values = Maps.newHashMap();
		this.id = id;
	}

	public DBEntryType getType() {
		return type;
	}

	public final Long getId() {
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
	 * The returned value should be immutable, but it is not a real problem: the
	 * CMEntry interface does not allow a card to be saved, and even with that,
	 * Object would have to be casted to a mutable value. After all, reflection
	 * can do anything, so there is no point in being over-strict.
	 */
	@Override
	public final Object get(final String key) {
		if (!values.containsKey(key)) {
			if ((type.getAttribute(key) != null) && !isNew()) {
				// TODO It was lazy loaded: load the remaining values
				throw new UnsupportedOperationException("Not implemented");
			} else {
				throw newAttributeInexistent(key);
			}
		}
		return values.get(key);
	}

	private boolean isNew() {
		return (id == null);
	}

	@Override
	public Iterable<Map.Entry<String, Object>> getValues() {
		return values.entrySet();
	}

	public final void setOnly(final String key, final Object value) {
		if (type.getAttribute(key) != null) {
			values.put(key, toNative(key, value));
		}
	}

	private Object toNative(final String key, final Object value) {
		// TODO: ugly solution to convertNotNullValue with CardReference problem
		// if (type.getAttribute(key).getType() instanceof
		// ForeignKeyAttributeType) {
		// ForeignKeyAttributeType foreignKeyAttributeType =
		// (ForeignKeyAttributeType)type.getAttribute(key).getType();
		// DBDataView view = new DBDataView(driver);
		// DBClass referencedClass =
		// view.findClass(foreignKeyAttributeType.getForeignKeyDestinationClassName());
		// CMQueryRow row =
		// view.select(AnyAttribute.anyAttribute(referencedClass)) //
		// .from(referencedClass) //
		// .where(SimpleWhereClause.condition(QueryAliasAttribute.attribute(referencedClass,
		// "Id"), EqualsOperatorAndValue.eq(value))) //
		// .run().getOnlyRow();
		// return
		// foreignKeyAttributeType.convertValue(CardReference.newInstance(row.getCard(referencedClass)));
		// }
		return type.getAttribute(key).getType().convertValue(value);
	}

	protected void saveOnly() {
		id = driver.create(this);
	}

	protected void delete() {
		driver.delete(this);
	}

	protected void updateOnly() {
		driver.update(this);
	}

	private RuntimeException newAttributeInexistent(final String key) {
		final String message = format("attribute '%s' does not exist for type '%s' within namespace '%s'", //
				key, type.getIdentifier().getLocalName(), type.getIdentifier().getNamespace());
		return new IllegalArgumentException(message);
	}

}
