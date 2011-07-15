package org.cmdbuild.dao.driver.postgres;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	bool {
		@Override
		CMAttributeType createAttributeType(int[] params, AttributeMetadata meta) {
			return new BooleanAttributeType();
		}
	}, bpchar {
		@Override
		CMAttributeType createAttributeType(int[] params, AttributeMetadata meta) {
			return new CharAttributeType();
		}
	}, date {
		@Override
		CMAttributeType createAttributeType(int[] params, AttributeMetadata meta) {
			return new DateAttributeType();
		}
	}, float8 {
		@Override
		CMAttributeType createAttributeType(int[] params, AttributeMetadata meta) {
			return new DoubleAttributeType();
		}
	}, inet {
		@Override
		CMAttributeType createAttributeType(int[] params, AttributeMetadata meta) {
			return new IPAddressAttributeType();
		}
	}, int4 {
		@Override
		CMAttributeType createAttributeType(int[] params, AttributeMetadata meta) {
			return new IntegerAttributeType();
		}
	}, numeric {
		@Override
		CMAttributeType createAttributeType(int[] params, AttributeMetadata meta) {
			final int precision = params[0];
			final int scale = params[1];
			return new DecimalAttributeType(precision, scale);
		}
	}, text {
		@Override
		CMAttributeType createAttributeType(int[] params, AttributeMetadata meta) {
			return new TextAttributeType();
		}
	}, time {
		@Override
		CMAttributeType createAttributeType(int[] params, AttributeMetadata meta) {
			return new TimeAttributeType();
		}
	}, timestamp {
		@Override
		CMAttributeType createAttributeType(int[] params, AttributeMetadata meta) {
			return new DateTimeAttributeType();
		}
	}, varchar {
		@Override
		CMAttributeType createAttributeType(int[] params, AttributeMetadata meta) {
			final int length = params[0];
			return new StringAttributeType(length);
		}
	};

	abstract CMAttributeType createAttributeType(int[] params, AttributeMetadata meta);

	private static final Pattern TYPE_PATTERN = Pattern.compile("(\\w+)(\\((\\d+(,\\d+)*)\\))?");

	public static CMAttributeType createAttributeType(final String sqlTypeString, final AttributeMetadata meta) {
		try {
			final Matcher typeMatcher = TYPE_PATTERN.matcher(sqlTypeString);
			if (!typeMatcher.find()) {
				throw new IllegalArgumentException();
			}
			final SqlType type = SqlType.valueOf(typeMatcher.group(1));
			final int[] params = typeArray(typeMatcher.group(3));
			return type.createAttributeType(params, meta);
		} catch (Throwable t) {
			return new UndefinedAttributeType();
		}
	}

	private static int[] typeArray(final String paramsMatch) {
		if (paramsMatch == null) {
			return new int[0];
		}
		final String[] stringParams = paramsMatch.split(",");
		final int[] intParams = new int[stringParams.length];
		for (int i=0; i<stringParams.length; ++i) {
			intParams[i] = Integer.parseInt(stringParams[i]);
		}
		return intParams;
	}
}
