package org.cmdbuild.workflow.xpdl;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.Activity;
import org.enhydra.jxpdl.elements.ExtendedAttribute;
import org.enhydra.jxpdl.elements.ExtendedAttributes;
import org.enhydra.jxpdl.elements.Performer;

public class XpdlActivity implements XpdlExtendedAttributesHolder  {

	@Legacy("As in 1.x")
	private static final String ADMIN_START_XA = "adminStart";

	public static final String VARIABLE_PREFIX = "VariableToProcess_";

	public enum XpdlVariableSuffix {
		VIEW {
			@Override
			public Type toGlobalType() {
				return CMActivityVariableToProcess.Type.READ_ONLY;
			}
		},
		UPDATE {
			@Override
			public Type toGlobalType() {
				return CMActivityVariableToProcess.Type.READ_WRITE;
			}
		},
		UPDATEREQUIRED {
			@Override
			public Type toGlobalType() {
				return CMActivityVariableToProcess.Type.READ_WRITE_REQUIRED;
			}
		};

		public abstract Type toGlobalType();
	}

	private static class XpdlActivityVariableToProcess implements CMActivityVariableToProcess {

		private final String name;
		private final Type type;

		XpdlActivityVariableToProcess(final String name, final Type type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Type getType() {
			return type;
		}

		/**
		 * Create a new instance if the passed extended attribute is a
		 * variable to process.
		 * 
		 * @param activity extended attribute
		 * @return a new instance or null if not a valid extended attribute
		 */
		static XpdlActivityVariableToProcess newInstance(final ExtendedAttribute xa) {
			final String key = xa.getName();
			final String name = xa.getVValue();
			if (key == null || name == null) {
				return null;
			}
			if (isVariableKey(key)) {
				final Type type = extractType(key);
				return new XpdlActivityVariableToProcess(name, type);
			} else {
				return null;
			}
		}

		private static boolean isVariableKey(final String key) {
			return key.startsWith(VARIABLE_PREFIX);
		}

		private static Type extractType(final String key) {
			final String suffix = key.substring(VARIABLE_PREFIX.length());
			final XpdlVariableSuffix xpdlType = XpdlVariableSuffix.valueOf(suffix);
			return xpdlType.toGlobalType();
		}
	}

	final XpdlDocument doc;
	final XpdlProcess process;
	final Activity inner;

	private final XpdlExtendedAttributes extendedAttributes;

	XpdlActivity(final XpdlProcess process, final Activity activity) {
		this.doc = process.getDocument();
		this.process = process;
		this.inner = activity;
		this.extendedAttributes = new XpdlActivityExtendedAttributes(this);
	}

	public String getId() {
		return inner.getId();
	}

	public String getName() {
		return inner.getName();
	}

	public String getDescription() {
		return inner.getDescription();
	}

	public boolean isManualType() {
		return inner.getActivityType() == XPDLConstants.ACTIVITY_TYPE_NO;
	}

	public void setBlockType(final XpdlActivitySet activitySet) {
		if (activitySet != null) {
			inner.getActivityTypes().setBlockActivity();
			inner.getActivityTypes().getBlockActivity().setActivitySetId(activitySet.inner.getId());
		}
	}

	public boolean isBlockType() {
		return inner.getActivityType() == XPDLConstants.ACTIVITY_TYPE_BLOCK;
	}

	public XpdlActivitySet getBlockActivitySet() {
		if (isBlockType()) {
			final String blockId = inner.getActivityTypes().getBlockActivity().getActivitySetId();
			return process.findActivitySet(blockId);
		} else {
			return null;
		}
	}

	/**
	 * Sets the only performer for this activity. We are not interested
	 * in more than one performer.
	 * 
	 * @param name of the first and only performer
	 */
	public void setPerformer(final String performerName) {
		doc.turnReadWrite();
		Performer performer = (Performer) inner.getPerformers().generateNewElement();
		performer.setValue(performerName);
		inner.getPerformers().clear();
		inner.getPerformers().add(performer);
	}

	/**List<CMActivityVariableToProcess>
	 * Returns the first performer for this activity.
	 * 
	 * @return name of the first performer
	 */
	public String getFirstPerformer() {
		if (inner.getPerformers().isEmpty()) {
			return null;
		} else {
			Performer p = (Performer) inner.getPerformers().get(0);
			return p.toValue();
		}
	}

	@Override
	public void addExtendedAttribute(final String key, final String value) {
		extendedAttributes.addExtendedAttribute(key, value);
	}

	@Override
	public String getFirstExtendedAttributeValue(final String key) {
		return extendedAttributes.getFirstExtendedAttributeValue(key);
	}

	@Legacy("As in 1.x")
	public boolean isAdminStart() {
		return (extendedAttributes.getFirstExtendedAttribute(ADMIN_START_XA) != null);
	}

	public List<CMActivityVariableToProcess> getVariablesToProcess() {
		final List<CMActivityVariableToProcess> vars = new ArrayList<CMActivityVariableToProcess>();
		final ExtendedAttributes xattrs = inner.getExtendedAttributes();
		if (xattrs != null) {
			for (int i = 0; i < xattrs.size(); ++i) {
				final ExtendedAttribute xa = (ExtendedAttribute) xattrs.get(i);
				final CMActivityVariableToProcess v = XpdlActivityVariableToProcess.newInstance(xa);
				if (v != null) {
					vars.add(v);
				}
			}
		}
		return vars;
	}
}