package org.cmdbuild.elements.filters;

import java.util.Map;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.interfaces.IAbstractElement;
import org.cmdbuild.elements.interfaces.IAttribute;

@OldDao
@Deprecated
public class AttributeFilter extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	public enum AttributeFilterType {
		EQUALS("=", "IN"), IN("IN", "IN"), DIFFERENT("<>", "NOT IN"), MINOR("<="), STRICT_MINOR("<"), MAJOR(">="), STRICT_MAJOR(
				">"), LIKE("ILIKE", "", ""), CONTAINS("ILIKE", "%", "%"), DONTCONTAINS("NOT ILIKE", "%", "%"), BEGIN(
				"ILIKE", "", "%"), DONTBEGIN("NOT ILIKE", "", "%"), END("ILIKE", "%", ""), DONTEND("NOT ILIKE", "%", ""), NULL(
				"IS NULL"), NOTNULL("IS NOT NULL"), BETWEEN(null, "BETWEEN"), NOTBETWEEN(null, "NOT BETWEEN");

		private final String operatorSingle;
		private final String operatorMultiple;
		private final String beforeStr;
		private final String afterStr;
		private final boolean ilikeEscaping;

		AttributeFilterType(final String operatorSingle) {
			this.operatorSingle = operatorSingle;
			this.operatorMultiple = null;
			this.beforeStr = "";
			this.afterStr = "";
			this.ilikeEscaping = false;
		}

		AttributeFilterType(final String operatorSingle, final String operatorMultiple) {
			this.operatorSingle = operatorSingle;
			this.operatorMultiple = operatorMultiple;
			this.beforeStr = "";
			this.afterStr = "";
			this.ilikeEscaping = false;
		}

		AttributeFilterType(final String operatorSingle, final String beforeStr, final String afterStr) {
			this.operatorSingle = operatorSingle;
			this.operatorMultiple = null;
			this.beforeStr = beforeStr;
			this.afterStr = afterStr;
			this.ilikeEscaping = true;
		}

		@Override
		public String toString() {
			return operatorSingle;
		}

		public String beforeValue() {
			return beforeStr;
		}

		public String afterValue() {
			return afterStr;
		}

		public String operatorSingle() {
			return operatorSingle;
		}

		public String operatorMultiple() {
			return operatorMultiple;
		}

		public boolean allowsMultiple() {
			return (operatorMultiple != null);
		}

		public String escapeValue(final String value) { // ILIKE NEEDS MORE
														// ESCAPING!
			if (ilikeEscaping)
				return value.replaceAll("\\\\", "\\\\\\\\");
			else
				return value;
		}
	}

	private final IAttribute attribute;
	private AttributeFilterType filterType;
	protected Object[] values;

	public AttributeFilter(final IAttribute attribute, final AttributeFilterType operator, final Object... values) {
		this.attribute = attribute;
		this.filterType = operator;
		setValues(values);
	}

	private Object[] valuesToNative(final Object[] values) {
		final Object[] nativeValues = new Object[values.length];
		for (int i = 0; i < values.length; ++i) {
			Object v = values[i];
			if (v instanceof String) {
				v = filterType.beforeValue() + v + filterType.afterValue();
			}
			nativeValues[i] = attribute.readValue(v);
		}
		return nativeValues;
	}

	public static AttributeFilter getEquals(final IAbstractElement element, final String attrName,
			final String attrValue) {
		return new AttributeFilter(element.getSchema().getAttribute(attrName), AttributeFilterType.EQUALS, attrValue);
	}

	@Override
	public String toString(final Map<String, QueryAttributeDescriptor> queryMapping) {
		if (queryMapping != null && queryMapping.containsKey(attribute.getName())) {
			return toString(queryMapping.get(attribute.getName()).getValueName());
		} else {
			return toString("\"" + attribute.getSchema().getDBName() + "\".\"" + attribute.getName() + "\"");
		}
	}

	private String toString(final String fullName) {
		if (isNullFilter() || equalsANullValue()) {
			return "(" + fullName + " " + AttributeFilterType.NULL + ")";
		}
		if (isNotNullFilter() || differentThanANullValue()) {
			return "(" + fullName + " " + AttributeFilterType.NOTNULL + ")";
		}
		if (values.length == 1 || !filterType.allowsMultiple()) {
			return "(" + fullName + " " + filterType.operatorSingle() + " " + valueToString(values[0]) + ")";
		}
		return "(" + fullName + " " + filterType.operatorMultiple() + " " + valuesToString() + ")";
	}

	private boolean isNullFilter() {
		return filterType == AttributeFilterType.NULL;
	}

	private boolean equalsANullValue() {
		return filterType == AttributeFilterType.EQUALS && isASingleNullValue();
	}

	private boolean isNotNullFilter() {
		return filterType == AttributeFilterType.NOTNULL;
	}

	private boolean differentThanANullValue() {
		return filterType == AttributeFilterType.DIFFERENT && isASingleNullValue();
	}

	private boolean isASingleNullValue() {
		return values == null || (values.length == 1 && values[0] == null);
	}

	public String getAttributeName() {
		return this.attribute.getName();
	}

	public void setFilterType(final AttributeFilterType fltType) {
		this.filterType = fltType;
	}

	public void setValues(final Object... values) {
		this.values = valuesToNative(values);
	}

	private boolean isBetween() {
		return filterType == AttributeFilterType.BETWEEN || filterType == AttributeFilterType.NOTBETWEEN;
	}

	private String valuesToString() {
		final String valSeparator = (isBetween()) ? " AND " : ",";
		final int size = values.length;
		final StringBuffer buffer = new StringBuffer(size * 8);
		if (!isBetween())
			buffer.append("(");
		if (size > 0) {
			buffer.append(valueToString(values[0]));
			for (int i = 1; i < size; ++i) {
				buffer.append(valSeparator).append(valueToString(values[i]));
			}
		}
		if (!isBetween())
			buffer.append(")");
		return buffer.toString();
	}

	private String valueToString(final Object value) {
		final String dbValue = attribute.valueToDBFormat(value);
		return filterType.escapeValue(dbValue);
	}
}
