package org.cmdbuild.workflow;

import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.BOOLEAN_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.DATETIME_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.EXTERNAL_REFERENCE_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.FLOAT_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.INTEGER_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.STRING_TYPE;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.Reference;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.services.SchemaCache;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.xpdl.XPDLAttributeType;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;

/**
 * Provides a way to map an xpdl attribute with the cmdbuild counterpart.
 */
public enum WorkflowAttributeType {

	BOOLEAN(XPDLAttributeType.BOOLEAN, BOOLEAN_TYPE, AttributeType.BOOLEAN),
	STRING(XPDLAttributeType.STRING, STRING_TYPE, AttributeType.STRING, AttributeType.TEXT, AttributeType.CHAR),
	DOUBLE(XPDLAttributeType.DOUBLE, FLOAT_TYPE, AttributeType.DECIMAL, AttributeType.DOUBLE) {
		@Override
		public Object get(Object value) {
			if (value != null && value instanceof BigDecimal) {
				return ((BigDecimal)value).doubleValue();
			} else {
				return (Double) value;
			}
		}
	},
	INTEGER(XPDLAttributeType.INT, INTEGER_TYPE, AttributeType.INTEGER, AttributeType.REGCLASS) {
		@Override
		protected void set(AttributeValue arg0, WMAttribute arg1) {
			Long value = (Long) arg1.getValue();
			if (value != null) {
				arg0.setValue((Integer) value.intValue()); // can't cast Long to
															// Integer
			} else {
				arg0.setValue((Integer) null);
			}
		}
		@Override
		public Object get(Object value) {
			Integer intValue = (Integer) value;
			if (intValue == null) {
				return null;
			} else {
				return Long.valueOf(intValue.longValue());
			}
		}
	},
	LOOKUP(XPDLAttributeType.LOOKUP, EXTERNAL_REFERENCE_TYPE, AttributeType.LOOKUP) {
		@Override
		protected void set(AttributeValue arg0, WMAttribute arg1) {
			org.cmdbuild.workflow.type.LookupType lookupType = (org.cmdbuild.workflow.type.LookupType) arg1.getValue();
			if (lookupType != null && lookupType.checkValidity()) {
				Lookup lookup = SchemaCache.getInstance().getLookup(lookupType.getId());
				arg0.setValue(lookup);
			} else {
				arg0.setValue(null);
			}
		}
		@Override
		public Object get(Object value) {
			Lookup lkp = (Lookup) value;
			if (lkp != null) {
				return new org.cmdbuild.workflow.type.LookupType(lkp.getId(), lkp.getType(),
						lkp.getDescription(), lkp.getCode());
			} else {
				return new org.cmdbuild.workflow.type.LookupType();
			}
		}
	},
	REFERENCE(XPDLAttributeType.REFERENCE, EXTERNAL_REFERENCE_TYPE, AttributeType.REFERENCE) {
		@Override
		protected void set(AttributeValue arg0, WMAttribute arg1) {
			org.cmdbuild.workflow.type.ReferenceType refType = (org.cmdbuild.workflow.type.ReferenceType) arg1
					.getValue();
			if (refType != null && refType.checkValidity()) {
				arg0.setValue(new Reference(arg0.getSchema().getReferenceDirectedDomain(), refType.getId(), refType
						.getDescription()));
			} else {
				arg0.setValue(null);
			}
		}
		@Override
		public Object get(Object value) {
			Reference ref = (Reference) value;
			if (ref != null) {
				String descr = ref.getDescription();
				if (descr == null) {
					descr = UserContext.systemContext().tables().get(ref.getClassId()).cards().get(ref.getId())
							.getDescription();
				}
				return new org.cmdbuild.workflow.type.ReferenceType(ref.getId(), ref.getClassId(), ref.getDescription());
			} else {
				return new org.cmdbuild.workflow.type.ReferenceType();
			}
		}
	},
	TIMESTAMP(XPDLAttributeType.DATETIME, DATETIME_TYPE, AttributeType.TIMESTAMP) {
		@Override
		protected void set(AttributeValue arg0, WMAttribute arg1) {
			Calendar sharkCal = (Calendar) arg1.getValue();
			if (sharkCal != null) {
				arg0.setValue(sharkCal.getTime());
			} else {
				arg0.setValue(null);
			}
		}
		@Override
		public Object get(Object value) {
			Date dateValue = (Date) value;
			if (dateValue != null) {
				Calendar cmdbuildCal = Calendar.getInstance();
				long timeinmillis = dateValue.getTime();
				cmdbuildCal.setTimeInMillis(timeinmillis);
				return cmdbuildCal;
			} else {
				return null;
			}
		}
	},
	DATE(XPDLAttributeType.DATETIME, DATETIME_TYPE, AttributeType.DATE) {
		@Override
		protected void set(AttributeValue arg0, WMAttribute arg1) {
			Calendar sharkCal = (Calendar) arg1.getValue();
			if (sharkCal != null) {
				arg0.setValue(sharkCal.getTime());
			} else {
				arg0.setValue(null);
			}
		}
		@Override
		public Object get(Object value) {
			Date dateValue = (Date) value;
			if (dateValue != null) {
				Calendar cmdbuildCal = Calendar.getInstance();
				long timeinmillis = dateValue.getTime();
				// TODO HACK FOR DATE TYPE BUG #562
				// There's something wrong on the shark side,
				// so we fake that the time zone is GMT
				// HACK HACK HACK HACK HACK HACK HACK
				timeinmillis += cmdbuildCal.getTimeZone().getOffset(timeinmillis);
				cmdbuildCal.setTimeInMillis(timeinmillis);
				return cmdbuildCal;
			} else {
				return null;
			}
		}
	};

	private WorkflowAttributeType(XPDLAttributeType xpdl, int sharkType, AttributeType... cmdbType) {
		this.xpdlType = xpdl;
		this.sharkType = sharkType;
		this.cmdbType = EnumSet.copyOf(Arrays.asList(cmdbType));
	}

	/**
	 * String used in XPDL documents
	 */
	XPDLAttributeType xpdlType;

	/**
	 * XPDL defined attribute type
	 */
	int sharkType;
	/**
	 * Mapped cmdbuild attribute type(s)
	 */
	Set<AttributeType> cmdbType;

	/**
	 * Set the shark value in WMAttribute attr in the CMDBuild AttributeValue
	 * 
	 * @param av
	 * @param attr
	 */
	protected void set(AttributeValue av, WMAttribute attr) {
		av.setValue(attr.getValue());
	}

	/**
	 * Get the Shark value object from the AttributeValue THIS IS NOT THE
	 * CMDBUILD OBJECT, BUT THE ONE THAT SHARK CAN HANDLE!
	 * 
	 * @param av
	 * @return
	 */
	public Object get(Object value) {
		return value;
	}

	public int getSharkType() {
		return sharkType;
	}

	public boolean isType(AttributeValue av, WMAttribute attr) {
		return (attr.getType() == this.sharkType && cmdbType.contains(av.getSchema().getType()));
	}

	public boolean isType(AttributeValue av) {
		return cmdbType.contains(av.getSchema().getType());
	}

	public void setInAttributeValue(AttributeValue av, WMAttribute attr) {
		if (isType(av, attr)) {
			set(av, attr);
		} else {
			System.err.println("WorkflowAttributeType mismatch type! " + name());
		}
	}

	public static WorkflowAttributeType search(AttributeValue av, WMAttribute attr) {
		for (WorkflowAttributeType wat : values()) {
			if (wat.isType(av, attr))
				return wat;
		}
		return null;
	}

	public static XPDLAttributeType getXpdlType(IAttribute attr) {
		for (WorkflowAttributeType wat : values()) {
			if (wat.cmdbType.contains(attr.getType()))
				return wat.xpdlType;
		}
		return null;
	}

	public static int getSharkType(IAttribute attr) {
		for (WorkflowAttributeType wat : values()) {
			if (wat.cmdbType.contains(attr.getType()))
				return wat.sharkType;
		}
		return -1;
	}

	/**
	 * Set in AttributeValue the value stored in WMAttribute, is a
	 * WorkflowAttributeType matching the tuple is found.
	 * 
	 * @param av
	 * @param attr
	 * @return
	 */
	public static boolean setIfFound(AttributeValue av, WMAttribute attr) {
		WorkflowAttributeType wat = search(av, attr);
		if (wat != null) {
			wat.setInAttributeValue(av, attr);
			return true;
		}
		return false;
	}

	public static Object getValue(AttributeValue av) {
		for (WorkflowAttributeType wat : values()) {
			if (wat.cmdbType.contains(av.getSchema().getType())) {
				return wat.get(av.getObject());
			}
		}
		return null;
	}
}
