package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.common.annotations.Legacy;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.Activity;
import org.enhydra.jxpdl.elements.Performer;

public class XpdlActivity implements XpdlExtendedAttributesHolder  {

	@Legacy("As in 1.x") private static final String ADMIN_START_XA = "adminStart";

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

	/**
	 * Returns the first performer for this activity. We are not interested
	 * in other performers.
	 * 
	 * @return name of the first and only performer
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
}