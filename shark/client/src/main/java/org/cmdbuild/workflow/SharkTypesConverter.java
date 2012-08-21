package org.cmdbuild.workflow;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.joda.time.DateTime;

public class SharkTypesConverter implements TypesConverter {

	private static final IntegerAttributeType ID_TYPE = new IntegerAttributeType();
	private static final String NO_DESCRIPTION = StringUtils.EMPTY;

	/**
	 * It's needed to convert class names to legacy ids... argh!
	 */
	private final CMDataView dataView;

	public SharkTypesConverter(final CMDataView dataView) {
		this.dataView = dataView;
	}

	@Override
	public Object toWorkflowType(final Object obj) {
		if (obj instanceof Integer) {
			return Integer.class.cast(obj).longValue();
		} else if (obj instanceof DateTime) {
			final long instant = DateTime.class.cast(obj).getMillis();
			return new Date(instant);
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

	private LookupType convertLookup(final CMLookup cml) {
		final LookupType lt = new LookupType();
		lt.setType(cml.getType().getName());
		lt.setId(objectIdToInt(cml.getId()));
		lt.setCode(cml.getCode());
		lt.setDescription(cml.getDescription());
		return lt;
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
		final CMClass refClass = dataView.findClassByName(ref.getClassName());
		final ReferenceType rt = new ReferenceType();
		rt.setId(objectIdToInt(ref.getId()));
		rt.setIdClass(objectIdToInt(refClass.getId()));
		rt.setDescription(ref.getDescription());
		return rt;
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
	 * Thanks to the brilliant use of classId in ReferenceType, restoring
	 * a database makes it inconsistent because the class Id changes.
	 * 
	 * @param classId
	 * @return actual class name or the base class name
	 */
	private String classNameForIdOrBaseClassIfUnknown(final Long classId) {
		final CMClass clazz = dataView.findClassById(classId);
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
