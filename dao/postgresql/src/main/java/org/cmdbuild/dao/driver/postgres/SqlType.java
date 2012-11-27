package org.cmdbuild.dao.driver.postgres;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType.Meta;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.GeometryAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IPAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.dao.reference.EntryTypeReference;

/**
 * Missing DAO types: Lookup, Reference, ForeignKey
 * 
 * Missing SQL types: POINT, LINESTRING, POLYGON (use sqlToJavaValue)
 * 
 * Not used: regclass, bytea, _int4, _varchar
 */
public enum SqlType {

	bool(BooleanAttributeType.class) {
	// nothing to implement, just for keep ordered
	}, //
	bpchar(CharAttributeType.class) {
	// nothing to implement, just for keep ordered
	}, //
	date(DateAttributeType.class) {
		@Override
		public Object javaToSqlValue(final Object value) {
			return dateJavaToSqlValue(value);
		}

		@Override
		public Object sqlToJavaValue(final Object value) {
			return dateSqlToJavaValue(value);
		}
	}, //
	float8(DoubleAttributeType.class) {
	// nothing to implement, just for keep ordered
	}, //
	/**
	 * POINT, LINESTRING, POLYGON
	 */
	geometry(GeometryAttributeType.class) {
		@Override
		public Object javaToSqlValue(final Object value) {
			// TODO
			throw new UnsupportedOperationException("Not implemented yet");
		}

		@Override
		public Object sqlToJavaValue(final Object value) {
			// TODO
			throw new UnsupportedOperationException("Not implemented yet");
		}
	}, //
	inet(IPAddressAttributeType.class) {
	// nothing to implement, just for keep ordered
	}, //
	int4(IntegerAttributeType.class, LookupAttributeType.class) {
		@Override
		protected Class<? extends CMAttributeType<?>> getJavaType(final CMAttributeType.Meta meta) {
			if (meta.isLookup()) {
				return LookupAttributeType.class;
			} else {
				return IntegerAttributeType.class;
			}
		}

		@Override
		protected Object[] getConstructorParams(final String[] stringParams, final CMAttributeType.Meta meta) {
			if (meta.isLookup()) {
				final Object[] params = new Object[1];
				params[0] = meta.getLookupType();
				return params;
			} else {
				return super.getConstructorParams(stringParams, meta);
			}
		}
	}, //
	/**
	 * precision and scale
	 */
	numeric(DecimalAttributeType.class) {
		@Override
		protected Object[] getConstructorParams(final String[] stringParams, final CMAttributeType.Meta meta) {
			if (stringParams.length == 2) {
				return new Object[] { Integer.valueOf(stringParams[0]), Integer.valueOf(stringParams[1]) };
			} else {
				return super.getConstructorParams(stringParams, meta);
			}
		}

		@Override
		protected Object[] getSqlParams(final CMAttributeType<?> type) {
			final DecimalAttributeType decimalType = (DecimalAttributeType) type;
			if (decimalType.precision != null && decimalType.scale != null) {
				return new Object[] { decimalType.precision, decimalType.scale };
			} else {
				return super.getSqlParams(type);
			}
		}
	}, //
	/**
	 * Used by some system tables
	 */
	regclass(EntryTypeAttributeType.class) {
		@Override
		public String sqlCast() {
			return "oid";
		}

		@Override
		public Object sqlToJavaValue(final Object value) {
			final Long classId = (Long) value;
			return EntryTypeReference.newInstance(classId);
		}

	}, //
	text(TextAttributeType.class) {
	// nothing to implement, just for keep ordered
	}, //
	time(TimeAttributeType.class) {
		@Override
		public Object javaToSqlValue(final Object value) {
			return dateJavaToSqlValue(value);
		}

		@Override
		public Object sqlToJavaValue(final Object value) {
			return dateSqlToJavaValue(value);
		}
	}, //
	timestamp(DateTimeAttributeType.class) {
		@Override
		public Object javaToSqlValue(final Object value) {
			return dateJavaToSqlValue(value);
		}

		@Override
		public Object sqlToJavaValue(final Object value) {
			return dateSqlToJavaValue(value);
		}
	}, //
	unknown(UndefinedAttributeType.class) {
	}, //
	/**
	 * length
	 */
	varchar(StringAttributeType.class) {
		@Override
		protected Object[] getConstructorParams(final String[] stringParams, final CMAttributeType.Meta meta) {
			if (stringParams.length == 1) {
				return new Object[] { Integer.valueOf(stringParams[0]) };
			} else {
				return super.getConstructorParams(stringParams, meta);
			}
		}

		@Override
		protected Object[] getSqlParams(final CMAttributeType<?> type) {
			final StringAttributeType stringType = (StringAttributeType) type;
			if (stringType.length != null) {
				return new Object[] { stringType.length };
			} else {
				return super.getSqlParams(type);
			}
		}
	}, //
	;

	private static final CMAttributeType.Meta NO_META = new Meta() {

		@Override
		public boolean isLookup() {
			return false;
		}

		@Override
		public String getLookupType() {
			return null;
		}

		@Override
		public boolean isDisplayableInList() {
			return false;
		}

	};

	/**
	 * DAO attribute types for this SQL type.
	 * 
	 * It should have been Class<? extends CMAttributeType<?>> but Java forbids
	 * array of generic types.
	 * 
	 * TODO Lookup, Reference, etc. need a different handling
	 */
	protected final Class<?> javaTypes[];

	private SqlType(final Class<?>... javaTypes) {
		Validate.notEmpty(javaTypes);
		this.javaTypes = javaTypes;
	}

	public Object javaToSqlValue(final Object value) {
		return value;
	}

	public Object sqlToJavaValue(final Object value) {
		return value;
	}

	public String sqlCast() {
		return null;
	}

	final CMAttributeType<?> createAttributeType(final String[] stringParams, final CMAttributeType.Meta meta)
			throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		final Object[] constructorParams = getConstructorParams(stringParams, meta);
		final Class<?>[] paramTypes = getParamTypes(constructorParams);
		return getJavaType(meta).getConstructor(paramTypes).newInstance(constructorParams);
	}

	@SuppressWarnings("unchecked")
	protected Class<? extends CMAttributeType<?>> getJavaType(final CMAttributeType.Meta meta) {
		return (Class<? extends CMAttributeType<?>>) javaTypes[0];
	}

	protected Object[] getConstructorParams(final String[] stringParams, final CMAttributeType.Meta meta) {
		return new Object[0];
	}

	private final Class<?>[] getParamTypes(final Object[] constructorParams) {
		final Class<?>[] paramTypes = new Class<?>[constructorParams.length];
		for (int i = 0; i < constructorParams.length; ++i) {
			paramTypes[i] = constructorParams[i].getClass();
		}
		return paramTypes;
	}

	protected Object[] getSqlParams(final CMAttributeType<?> type) {
		return null;
	}

	private static final Pattern TYPE_PATTERN = Pattern.compile("(\\w+)(\\((\\d+(,\\d+)*)\\))?");

	/**
	 * Create attribute type from SQL string and no metadata. It is used when
	 * there are no metadata, like in funcion calls.
	 * 
	 * @param sqlTypeString
	 * @return
	 */
	public static CMAttributeType<?> createAttributeType(final String sqlTypeString) {
		return createAttributeType(sqlTypeString, NO_META);
	}

	/**
	 * Create attribute type from SQL string and metadata. It is used for class
	 * and domain attributes, that have comments to define metadata (sigh).
	 * 
	 * @param sqlTypeString
	 * @param meta
	 * @return
	 */
	public static CMAttributeType<?> createAttributeType(final String sqlTypeString, final CMAttributeType.Meta meta) {
		try {
			final Matcher typeMatcher = TYPE_PATTERN.matcher(sqlTypeString);
			if (!typeMatcher.find()) {
				throw new IllegalArgumentException();
			}
			final SqlType type = SqlType.valueOf(typeMatcher.group(1));
			final String[] params = splitParams(typeMatcher.group(3));
			return type.createAttributeType(params, meta);
		} catch (final Throwable t) {
			return new UndefinedAttributeType();
		}
	}

	/**
	 * Returns the full SQL type string (with length and such) for a DAO
	 * attribute type.
	 * 
	 * @param type
	 * @return
	 */
	public static String getSqlTypeString(final CMAttributeType<?> type) {
		final SqlType sqlType = getSqlType(type);
		final Object[] sqlTypeParams = sqlType.getSqlParams(type);
		return buildSqlTypeString(sqlType, sqlTypeParams);
	}

	private static String buildSqlTypeString(final SqlType sqlType, final Object[] sqlTypeParams) {
		final StringBuilder sb = new StringBuilder(sqlType.name());
		if (sqlTypeParams != null) {
			sb.append("(").append(StringUtils.join(sqlTypeParams, ",")).append(")");
		}
		return sb.toString();
	}

	/**
	 * Gets the SQL type from a DAO attribute type.
	 * 
	 * @param type
	 * @return
	 */
	public static SqlType getSqlType(final CMAttributeType<?> type) {
		for (final SqlType t : SqlType.values()) {
			for (final Class<?> javaType : t.javaTypes) {
				if (javaType == type.getClass()) {
					return t;
				}
			}
		}
		return unknown;
	}

	private static String[] splitParams(final String paramsMatch) {
		if (paramsMatch == null) {
			return new String[0];
		}
		return paramsMatch.split(",");
	}

	/*
	 * Utils
	 */

	protected final Object dateJavaToSqlValue(Object value) {
		if (value instanceof org.joda.time.DateTime) {
			value = new java.util.Date(((org.joda.time.DateTime) value).getMillis());
		}
		return value;
	}

	protected final Object dateSqlToJavaValue(Object value) {
		if (value instanceof java.util.Date) {
			value = new org.joda.time.DateTime(((java.util.Date) value).getTime());
		}
		return value;
	}

}
