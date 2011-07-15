package org.cmdbuild.dao.driver.postgres;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.dao.entrytype.DBAttribute.AttributeMetadata;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IPAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;

public enum SqlType {
	
	// Missing: POINT, LINESTRING, POLYGON
	// Not used: regclass, bytea, _int4, _varchar

	bool(BooleanAttributeType.class),
	bpchar(CharAttributeType.class),
	date(DateAttributeType.class),
	float8(DoubleAttributeType.class),
	inet(IPAddressAttributeType.class),
	int4(IntegerAttributeType.class),
	numeric(DecimalAttributeType.class) { // precision and scale

		protected Object[] getConstructorParams(String[] stringParams) {
			final Object[] params = new Object[2];
			params[0] = Integer.valueOf(stringParams[0]);
			params[1] = Integer.valueOf(stringParams[1]);
			return params;
		}

		protected Object[] getSqlParams(final CMAttributeType type) {
			final DecimalAttributeType decimalType = (DecimalAttributeType) type;
			final Object[] sqlParams = new Object[2];
			sqlParams[0] = decimalType.precision;
			sqlParams[1] = decimalType.scale;
			return sqlParams;
		}
	},
	text(TextAttributeType.class),
	time(TimeAttributeType.class),
	timestamp(DateTimeAttributeType.class),
	unknown(UndefinedAttributeType.class),
	varchar(StringAttributeType.class) { // length

		protected Object[] getConstructorParams(String[] stringParams) {
			final Object[] params = new Object[1];
			params[0] = Integer.valueOf(stringParams[0]);
			return params;
		}

		protected Object[] getSqlParams(final CMAttributeType type) {
			final StringAttributeType stringType = (StringAttributeType) type;
			final Object[] sqlParams = new Object[1];
			sqlParams[0] = stringType.length;
			return sqlParams;
		}
	};

	// TODO Lookup, Reference, etc. need a different handling
	protected final Class<? extends CMAttributeType> javaType;

	private SqlType(final Class<? extends CMAttributeType> javaType) {
		this.javaType = javaType;
	}

	final CMAttributeType createAttributeType(String[] stringParams, AttributeMetadata meta) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Object[] constructorParams = getConstructorParams(stringParams);
		final Class<?>[] paramTypes = getParamTypes(constructorParams);
		return javaType.getConstructor(paramTypes).newInstance(constructorParams);
	}

	protected Object[] getConstructorParams(String[] stringParams) {
		return (Object[]) stringParams;
	}

	private final Class<?>[] getParamTypes(Object[] constructorParams) {
		final Class<?>[] paramTypes = new Class<?>[constructorParams.length];
		for (int i=0; i<constructorParams.length; ++i) {
			paramTypes[i] = constructorParams[i].getClass();
		}
		return paramTypes;
	}

	protected Object[] getSqlParams(final CMAttributeType type) {
		return null;
	}


	private static final Pattern TYPE_PATTERN = Pattern.compile("(\\w+)(\\((\\d+(,\\d+)*)\\))?");

	public static CMAttributeType createAttributeType(final String sqlTypeString, final AttributeMetadata meta) {
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

	public static String getSqlTypeString(final CMAttributeType type) {
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

	private static SqlType getSqlType(CMAttributeType type) {
		for (SqlType t : SqlType.values()) {
			if (t.javaType == type.getClass()) {
				return t;
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
}
