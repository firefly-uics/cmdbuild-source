package org.cmdbuild.dao.driver.postgres;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IPAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType.Meta;

public enum SqlType {

	// Missing cmdbuild types: Lookup, Reference, ForeignKey
	// Missing sql types: POINT, LINESTRING, POLYGON (use sqlToJavaValue)
	// Not used: regclass, bytea, _int4, _varchar

	bool(BooleanAttributeType.class),
	bpchar(CharAttributeType.class),
	date(DateAttributeType.class) {
		@Override
		public Object javaToSqlValue(Object value) {
			return dateJavaToSqlValue(value);
		}
	},
	float8(DoubleAttributeType.class),
	inet(IPAddressAttributeType.class),
	int4(IntegerAttributeType.class, LookupAttributeType.class) {
		@Override
		protected Class<? extends CMAttributeType<?>> getJavaType(CMAttributeType.Meta meta) {
			if (meta.isLookup()) {
				return LookupAttributeType.class;
			} else {
				return IntegerAttributeType.class;
			}
		}
		@Override
		protected Object[] getConstructorParams(String[] stringParams, CMAttributeType.Meta meta) {
			if (meta.isLookup()) {
				final Object[] params = new Object[1];
				params[0] = meta.getLookupType();
				return params;
			} else {
				return super.getConstructorParams(stringParams, meta);
			}
		}
	},
	numeric(DecimalAttributeType.class) { // precision and scale
		@Override
		protected Object[] getConstructorParams(String[] stringParams, CMAttributeType.Meta meta) {
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
	},
	text(TextAttributeType.class),
	time(TimeAttributeType.class) {
		@Override
		public Object javaToSqlValue(Object value) {
			return dateJavaToSqlValue(value);
		}
	},
	timestamp(DateTimeAttributeType.class) {
		@Override
		public Object javaToSqlValue(Object value) {
			return dateJavaToSqlValue(value);
		}
	},
	unknown(UndefinedAttributeType.class),
	varchar(StringAttributeType.class) { // length
		@Override
		protected Object[] getConstructorParams(String[] stringParams, CMAttributeType.Meta meta) {
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
	};

	private static final CMAttributeType.Meta NO_META = new Meta() {
		@Override
		public boolean isLookup() {
			return false;
		}
		@Override
		public String getLookupType() {
			return null;
		}
	};

	// TODO Lookup, Reference, etc. need a different handling
	protected final Class<? extends CMAttributeType<?>> javaTypes[];

	private SqlType(final Class<? extends CMAttributeType<?>>... javaTypes) {
		Validate.notEmpty(javaTypes);
		this.javaTypes = javaTypes;
	}

	public Object javaToSqlValue(Object value) {
		return value;
	}

	public Object sqlToJavaValue(Object value) {
		return value;
	}

	final CMAttributeType<?> createAttributeType(String[] stringParams, CMAttributeType.Meta meta) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Object[] constructorParams = getConstructorParams(stringParams, meta);
		final Class<?>[] paramTypes = getParamTypes(constructorParams);
		return getJavaType(meta).getConstructor(paramTypes).newInstance(constructorParams);
	}

	protected Class<? extends CMAttributeType<?>> getJavaType(CMAttributeType.Meta meta) {
		return javaTypes[0];
	}
	
	protected Object[] getConstructorParams(String[] stringParams, CMAttributeType.Meta meta) {
		return new Object[0];
	}

	private final Class<?>[] getParamTypes(Object[] constructorParams) {
		final Class<?>[] paramTypes = new Class<?>[constructorParams.length];
		for (int i=0; i<constructorParams.length; ++i) {
			paramTypes[i] = constructorParams[i].getClass();
		}
		return paramTypes;
	}

	protected Object[] getSqlParams(final CMAttributeType<?> type) {
		return null;
	}


	private static final Pattern TYPE_PATTERN = Pattern.compile("(\\w+)(\\((\\d+(,\\d+)*)\\))?");

	public static CMAttributeType<?> createAttributeType(final String sqlTypeString) {
		return createAttributeType(sqlTypeString, NO_META);
	}

	public static CMAttributeType<?> createAttributeType(final String sqlTypeString, final CMAttributeType.Meta meta) {
		try {
			final Matcher typeMatcher = TYPE_PATTERN.matcher(sqlTypeString);
			if (!typeMatcher.find()) {
				throw new IllegalArgumentException();
			}
			final SqlType type = SqlType.valueOf(typeMatcher.group(1));
			final String[] params = splitParams(typeMatcher.group(3));
			return type.createAttributeType(params, meta);
		} catch (Throwable t) {
			return new UndefinedAttributeType();
		}
	}

	public static String getSqlTypeString(final CMAttributeType<?> type) {
		final SqlType sqlType = getSqlType(type);
		final Object[] sqlTypeParams = sqlType.getSqlParams(type);
		return buildSqlTypeString(sqlType, sqlTypeParams);
	}

	private static String buildSqlTypeString(SqlType sqlType, Object[] sqlTypeParams) {
		StringBuilder sb = new StringBuilder(sqlType.name());
		if (sqlTypeParams != null) {
			sb.append("(").append(StringUtils.join(sqlTypeParams, ",")).append(")");
		}
		return sb.toString();
	}

	public static SqlType getSqlType(CMAttributeType<?> type) {
		for (SqlType t : SqlType.values()) {
			for (Class<? extends CMAttributeType<?>> javaType : t.javaTypes) {
				if (javaType == type.getClass()) {
					return t;
				}
			}
		}
		throw new IllegalArgumentException();
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
}
