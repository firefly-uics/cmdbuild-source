package org.cmdbuild.dao.query.clause.where;

public class OperatorAndValues {

	public static OperatorAndValue beginsWith(final Object value) {
		return BeginsWithOperatorAndValue.beginsWith(value);
	}

	public static OperatorAndValue contains(final Object value) {
		return ContainsOperatorAndValue.contains(value);
	}

	public static OperatorAndValue containsOrEquals(final Object value) {
		return new ContainsOrEquals(value);
	}

	public static OperatorAndValue emptyArray() {
		return EmptyArrayOperatorAndValue.emptyArray();
	}

	public static OperatorAndValue endsWith(final Object value) {
		return EndsWithOperatorAndValue.endsWith(value);
	}

	public static OperatorAndValue eq(final Object value) {
		return EqualsOperatorAndValue.eq(value);
	}

	public static OperatorAndValue gt(final Object value) {
		return GreaterThanOperatorAndValue.gt(value);
	}

	public static OperatorAndValue in(final Object... objects) {
		return InOperatorAndValue.in(objects);
	}

	public static OperatorAndValue isContainedWithinOrEquals(final Object value) {
		return new IsContainedWithinOrEquals(value);
	}

	public static OperatorAndValue lt(final Object value) {
		return LessThanOperatorAndValue.lt(value);
	}

	public static OperatorAndValue isNull() {
		return NullOperatorAndValue.isNull();
	}

	public static OperatorAndValue stringArrayOverlap(final Object value) {
		return StringArrayOverlapOperatorAndValue.stringArrayOverlap(value);
	}

	private OperatorAndValues() {
		// prevents instantiation
	}

}
