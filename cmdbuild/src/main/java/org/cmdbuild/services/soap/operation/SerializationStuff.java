package org.cmdbuild.services.soap.operation;

import static java.lang.String.format;
import static org.cmdbuild.common.Constants.Webservices.BOOLEAN_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.CHAR_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.DATE_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.DECIMAL_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.DOUBLE_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.FOREIGNKEY_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.INET_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.INTEGER_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.LOOKUP_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.REFERENCE_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.STRING_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.TEXT_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.TIMESTAMP_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.TIME_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.UNKNOWN_TYPE_NAME;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;

class SerializationStuff {

	private static final Logger logger = SoapLogicHelper.logger;
	private static final Marker marker = MarkerFactory.getMarker(SerializationStuff.class.getName());

	public static class Functions {

		private Functions() {
			// prevents instantiation
		}

		private static Function<CMAttribute, AttributeSchema> CMATTRTIBUTE_TO_ATTRIBUTESCHEMA = new Function<CMAttribute, AttributeSchema>() {
			@Override
			public AttributeSchema apply(final CMAttribute input) {
				return serialize(input);
			}
		};

		public static Function<CMAttribute, AttributeSchema> toAttributeSchema() {
			logger.debug(marker, "converting from '{}' to '{}'", CMAttribute.class, AttributeSchema.class);
			return CMATTRTIBUTE_TO_ATTRIBUTESCHEMA;
		}

	}

	public static AttributeSchema serialize(final CMAttribute attribute) {
		return serialize(attribute, attribute.getIndex());
	}

	public static AttributeSchema serialize(final CMAttribute attribute, final int index) {
		final AttributeSchema schema = new AttributeSchema();
		attribute.getType().accept(new CMAttributeTypeVisitor() {

			@Override
			public void visit(final BooleanAttributeType attributeType) {
				schema.setType(BOOLEAN_TYPE_NAME);
			}

			@Override
			public void visit(final CharAttributeType attributeType) {
				schema.setType(CHAR_TYPE_NAME);
			}

			@Override
			public void visit(final DateAttributeType attributeType) {
				schema.setType(DATE_TYPE_NAME);
			}

			@Override
			public void visit(final DateTimeAttributeType attributeType) {
				schema.setType(TIMESTAMP_TYPE_NAME);
			}

			@Override
			public void visit(final DecimalAttributeType attributeType) {
				schema.setType(DECIMAL_TYPE_NAME);
				schema.setPrecision(attributeType.precision);
				schema.setScale(attributeType.scale);
			}

			@Override
			public void visit(final DoubleAttributeType attributeType) {
				schema.setType(DOUBLE_TYPE_NAME);
			}

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
				schema.setType(UNKNOWN_TYPE_NAME);
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				schema.setType(LOOKUP_TYPE_NAME);
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				schema.setType(FOREIGNKEY_TYPE_NAME);
			}

			@Override
			public void visit(final IntegerAttributeType attributeType) {
				schema.setType(INTEGER_TYPE_NAME);
			}

			@Override
			public void visit(final IpAddressAttributeType attributeType) {
				schema.setType(INET_TYPE_NAME);
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				schema.setType(REFERENCE_TYPE_NAME);
			}

			@Override
			public void visit(final StringArrayAttributeType attributeType) {
				schema.setType(UNKNOWN_TYPE_NAME);
			}

			@Override
			public void visit(final StringAttributeType attributeType) {
				schema.setType(STRING_TYPE_NAME);
				schema.setLength(attributeType.length);
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
				schema.setType(TEXT_TYPE_NAME);
			}

			@Override
			public void visit(final TimeAttributeType attributeType) {
				schema.setType(TIME_TYPE_NAME);
			}

		});
		schema.setIdClass(attribute.getOwner().getId().intValue());
		schema.setName(attribute.getName());
		schema.setDescription(attribute.getDescription());
		schema.setBaseDSP(attribute.isDisplayableInList());
		schema.setUnique(attribute.isUnique());
		schema.setNotnull(attribute.isMandatory());
		schema.setInherited(attribute.isInherited());
		schema.setIndex(index);
		schema.setFieldmode(serialize(attribute.getMode()));
		schema.setDefaultValue(attribute.getDefaultValue());
		schema.setClassorder(attribute.getClassOrder());
		return schema;
	}

	public static String serialize(final Mode mode) {
		switch (mode) {
		case WRITE:
			return "write";
		case READ:
			return "read";
		case HIDDEN:
			return "hidden";
		}
		throw new IllegalArgumentException(format("invalid mode '%s'", mode));
	}

}
