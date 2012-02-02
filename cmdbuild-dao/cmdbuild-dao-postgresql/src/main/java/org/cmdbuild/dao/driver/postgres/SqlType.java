package org.cmdbuild.dao.driver.postgres;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.DBAttribute.AttributeMetadata;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.GeometryAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IPAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;

public enum SqlType {

	bool(BooleanAttributeType.class),
	bpchar(CharAttributeType.class),
	date(DateAttributeType.class) {
		@Override
		public Object javaToSqlValue(Object value) {
			return dateJavaToSqlValue(value);
		}
	},
	float8(DoubleAttributeType.class),
	geometry(GeometryAttributeType.class) { // POINT, LINESTRING, POLYGON
		@Override
		public Object javaToSqlValue(Object value) {
			// TODO
			throw new UnsupportedOperationException("Not implemented yet");
		}
		@Override
		public Object sqlToJavaValue(Object value) {
			// TODO
			throw new UnsupportedOperationException("Not implemented yet");
		}
	},
	inet(IPAddressAttributeType.class),
	int4(IntegerAttributeType.class, LookupAttributeType.class,
			ReferenceAttributeType.class, ForeignKeyAttributeType.class) {
		@Override
		protected Class<? extends CMAttributeType<?>> getJavaType(AttributeMetadata meta) {
			// TODO ReferenceAttributeType, ForeignKeyAttributeType
			if (meta.isLookup()) {
				return LookupAttributeType.class;
			} else {
				return IntegerAttributeType.class;
			}
		}
		@Override
		protected Object[] getConstructorParams(String[] stringParams, AttributeMetadata meta) {
			// TODO ReferenceAttributeType, ForeignKeyAttributeType
			if (meta.isLookup()) {
				final Object[] params = new Object[1];
				params[0] = meta.getLookupType(); // lookup type
				return params;
			} else {
				return super.getConstructorParams(stringParams, meta);
			}
		}
	},
	numeric(DecimalAttributeType.class) {
		@Override
		protected Object[] getConstructorParams(String[] stringParams, AttributeMetadata meta) {
			final Object[] params = new Object[2];
			params[0] = Integer.valueOf(stringParams[0]); // precision
			params[1] = Integer.valueOf(stringParams[1]); // scale
			return params;
		}
		@Override
		protected Object[] getSqlParams(final CMAttributeType<?> type) {
			final DecimalAttributeType decimalType = (DecimalAttributeType) type;
			final Object[] sqlParams = new Object[2];
			sqlParams[0] = decimalType.precision;
			sqlParams[1] = decimalType.scale;
			return sqlParams;
		}
	},
	regclass(EntryTypeAttributeType.class) { // Used by some system tables
		public String sqlCast() {
			return "oid";
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
	varchar(StringAttributeType.class) {
		@Override
		protected Object[] getConstructorParams(String[] stringParams, AttributeMetadata meta) {
			final Object[] params = new Object[1];
			params[0] = Integer.valueOf(stringParams[0]); // length
			return params;
		}
		@Override
		protected Object[] getSqlParams(final CMAttributeType<?> type) {
			final StringAttributeType stringType = (StringAttributeType) type;
			final Object[] sqlParams = new Object[1];
			sqlParams[0] = stringType.length;
			return sqlParams;
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

	public String sqlCast() {
		return null;
	}

	final CMAttributeType<?> createAttributeType(String[] stringParams, AttributeMetadata meta) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Object[] constructorParams = getConstructorParams(stringParams, meta);
		final Class<?>[] paramTypes = getParamTypes(constructorParams);
		return getJavaType(meta).getConstructor(paramTypes).newInstance(constructorParams);
	}

	protected Class<? extends CMAttributeType<?>> getJavaType(AttributeMetadata meta) {
		return javaTypes[0];
	}
	
	protected Object[] getConstructorParams(String[] stringParams, AttributeMetadata meta) {
		return new Object[0];
	}

	private Class<?>[] getParamTypes(Object[] constructorParams) {
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

	public static CMAttributeType<?> createAttributeType(final String sqlTypeString, final AttributeMetadata meta) {
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
