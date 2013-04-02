package org.cmdbuild.workflow;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.math.BigDecimal;
import java.util.Date;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.GeometryAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class SharkTypesConverter implements WorkflowTypesConverter {

	private static final Marker marker = MarkerFactory.getMarker(SharkTypesConverter.class.getName());
	private static final Logger logger = Log.WORKFLOW;

	private class ToSharkTypesConverter implements CMAttributeTypeVisitor {

		private final Object input;
		private Object output;

		private ToSharkTypesConverter(final Object input) {
			this.input = input;
		}

		@Override
		public void visit(final BooleanAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultBoolean();
			} else {
				output = input;
			}
		}

		@Override
		public void visit(final CharAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultString();
			} else {
				output = input;
			}
		}

		@Override
		public void visit(final DateTimeAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultDate();
			} else {
				output = convertDateTime(input);
			}
		}

		@Override
		public void visit(final DateAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultDate();
			} else {
				output = convertDateTime(input);
			}
		}

		@Override
		public void visit(final DecimalAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultDouble();
			} else {
				output = BigDecimal.class.cast(input).doubleValue();
			}
		}

		@Override
		public void visit(final DoubleAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultDouble();
			} else {
				output = input;
			}
		}

		@Override
		public void visit(final EntryTypeAttributeType attributeType) {
			notifyIllegalType(attributeType);
		}

		@Override
		public void visit(final ForeignKeyAttributeType attributeType) {
			throwIllegalType(attributeType);
		}

		@Override
		public void visit(final GeometryAttributeType attributeType) {
			throwIllegalType(attributeType);
		}

		@Override
		public void visit(final IntegerAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultInteger();
			} else {
				output = input;
			}
		}

		@Override
		public void visit(final IpAddressAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultString();
			} else {
				output = input.toString();
			}
		}

		@Override
		public void visit(final LookupAttributeType attributeType) {
			output = convertLookup(attributeType.convertValue(input), attributeType.getLookupTypeName());
		}

		@Override
		public void visit(final ReferenceAttributeType attributeType) {
			output = convertReference(attributeType.convertValue(input));
		}

		@Override
		public void visit(final StringArrayAttributeType attributeType) {
			notifyIllegalType(attributeType);
		}

		@Override
		public void visit(final StringAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultString();
			} else {
				output = input;
			}
		}

		@Override
		public void visit(final TextAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultString();
			} else {
				output = input;
			}
		}

		@Override
		public void visit(final TimeAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultDate();
			} else {
				output = convertDateTime(input);
			}
		}

		private void notifyIllegalType(final CMAttributeType<?> attributeType) {
			logger.warn(marker, illegalTypeMessage(attributeType));
		}

		private void throwIllegalType(final CMAttributeType<?> attributeType) {
			throw new IllegalArgumentException(illegalTypeMessage(attributeType));
		}

		private String illegalTypeMessage(final CMAttributeType<?> attributeType) {
			return format("cannot send a '%s' to Shark", attributeType.getClass());
		}

	}

	private final IntegerAttributeType ID_TYPE = new IntegerAttributeType();
	private final String NO_DESCRIPTION = EMPTY;

	/**
	 * It's needed to convert class names to legacy ids... argh!
	 */
	private final CMDataView dataView;

	public SharkTypesConverter(final CMDataView dataView) {
		this.dataView = dataView;
	}

	@Override
	public Object fromWorkflowType(final Object obj) {
		if (obj instanceof LookupType) {
			final LookupType lt = LookupType.class.cast(obj);
			return convertLookup(lt);
		} else if (obj instanceof ReferenceType) {
			final ReferenceType ref = ReferenceType.class.cast(obj);
			return convertReference(ref);
		} else {
			return obj;
		}
	}

	@Override
	public Object toWorkflowType(final CMAttributeType<?> attributeType, final Object obj) {
		if (attributeType != null) {
			return convertCMDBuildVariable(attributeType, obj);
		} else if (obj != null) {
			return convertSharkOnlyVariable(obj);
		} else {
			return null;
		}
	}

	private Object convertCMDBuildVariable(final CMAttributeType<?> attributeType, final Object obj) {
		final ToSharkTypesConverter converter = new ToSharkTypesConverter(obj);
		attributeType.accept(converter);
		return converter.output;
	}

	/**
	 * Tries to convert the values that are present only in Shark, so the
	 * attributeType is null. We can only guess the type when the value is not
	 * null.
	 * 
	 * @param native value
	 * @return shark value
	 */
	private Object convertSharkOnlyVariable(final Object obj) {
		if (obj instanceof Integer) {
			return Integer.class.cast(obj).longValue();
		} else if (obj instanceof DateTime) {
			return convertDateTime(obj);
		} else if (obj instanceof BigDecimal) {
			return BigDecimal.class.cast(obj).doubleValue();
		} else if (obj instanceof CMLookup) {
			final CMLookup cml = CMLookup.class.cast(obj);
			return convertLookup(cml);
		} else if (obj instanceof CardReference) {
			final CardReference ref = CardReference.class.cast(obj);
			return convertReference(ref);
		} else if (obj instanceof CardReference[]) {
			final CardReference[] refArray = CardReference[].class.cast(obj);
			return convertReferenceArray(refArray);
		} else {
			return obj;
		}
	}

	private Object convertDateTime(final Object obj) {
		final long instant = DateTime.class.cast(obj).getMillis();
		return new Date(instant);
	}

	private LookupType convertLookup(final Long id, final String type) {
		logger.error("getting lookup with id '{}'", id);
		if (id == null) {
			return SharkTypeDefaults.defaultLookup();
		}
		try {
			// TODO a lookup store must be used absolutely
			final CMClass lookupClass = dataView.findClass("LookUp");
			final CMCard card = dataView.select(anyAttribute(lookupClass)) //
					.from(lookupClass) //
					.where(condition(attribute(lookupClass, ID_ATTRIBUTE), eq(id))) //
					.run() //
					.getOnlyRow() //
					.getCard(lookupClass);
			final LookupType lookupType = new LookupType();
			lookupType.setType(card.get("Type", String.class));
			lookupType.setId(objectIdToInt(id));
			lookupType.setCode(String.class.cast(card.getCode()));
			lookupType.setDescription(String.class.cast(card.getDescription()));
			return lookupType;
		} catch (final Exception e) {
			logger.error("cannot get lookup", e);
			return SharkTypeDefaults.defaultLookup();
		}
	}

	private LookupType convertLookup(final CMLookup cml) {
		if (cml != null) {
			final LookupType lt = new LookupType();
			lt.setType(cml.getType().getName());
			lt.setId(objectIdToInt(cml.getId()));
			lt.setCode(cml.getCode());
			lt.setDescription(cml.getDescription());
			return lt;
		} else {
			return SharkTypeDefaults.defaultLookup();
		}
	}

	private Integer convertLookup(final LookupType lt) {
		if (lt.checkValidity()) {
			return lt.getId();
		} else {
			return null;
		}
	}

	private ReferenceType[] convertReferenceArray(final CardReference[] refArray) {
		final ReferenceType[] rtArray = new ReferenceType[refArray.length];
		for (int i = 0; i < refArray.length; ++i) {
			rtArray[i] = convertReference(refArray[i]);
		}
		return rtArray;
	}

	private ReferenceType convertReference(final CardReference ref) {
		if (ref != null) {
			final ReferenceType rt = new ReferenceType();
			final CMClass refClass = dataView.findClass(ref.getClassName());
			rt.setId(objectIdToInt(ref.getId()));
			rt.setIdClass(objectIdToInt(refClass.getId()));
			rt.setDescription(ref.getDescription());
			return rt;
		} else {
			return SharkTypeDefaults.defaultReference();
		}
	}

	private ReferenceType convertReference(final Long id) {
		if (id == null) {
			return SharkTypeDefaults.defaultReference();
		}
		try {
			// TODO improve performances
			final CMClass queryClass = dataView.findClass("Class");
			final CMCard card = dataView.select(anyAttribute(queryClass)) //
					.from(queryClass) //
					.where(condition(attribute(queryClass, ID_ATTRIBUTE), eq(id))) //
					.run() //
					.getOnlyRow() //
					.getCard(queryClass);
			final ReferenceType referenceType = new ReferenceType();
			referenceType.setId(objectIdToInt(card.getId()));
			referenceType.setIdClass(objectIdToInt(card.getType().getId()));
			referenceType.setDescription(String.class.cast(card.getDescription()));
			return referenceType;
		} catch (final Exception e) {
			logger.error("cannot get reference", e);
			return SharkTypeDefaults.defaultReference();
		}
	}

	private CardReference convertReference(final ReferenceType ref) {
		if (ref.checkValidity()) {
			final Long cardId = Long.valueOf(ref.getId());
			final Long classId = Long.valueOf(ref.getIdClass());
			final String className = classNameForIdOrBaseClassIfUnknown(classId);
			return CardReference.newInstance(className, cardId, NO_DESCRIPTION);
		} else {
			return null;
		}
	}

	/**
	 * Thanks to the brilliant use of classId in ReferenceType, restoring a
	 * database makes it inconsistent because the class Id changes.
	 * 
	 * @param classId
	 * @return actual class name or the base class name
	 */
	private String classNameForIdOrBaseClassIfUnknown(final Long classId) {
		final CMClass clazz = dataView.findClass(classId);
		if (clazz != null) {
			return clazz.getName();
		} else {
			return Constants.BASE_CLASS_NAME;
		}
	}

	/**
	 * Converts an object identifier to the integer representation or -1 if it
	 * is null (YEAH!)
	 * 
	 * @return legacy id standard
	 */
	private int objectIdToInt(final Long objId) {
		final Integer id = ID_TYPE.convertValue(objId);
		if (id == null) {
			return -1;
		} else {
			return id;
		}
	}
}
