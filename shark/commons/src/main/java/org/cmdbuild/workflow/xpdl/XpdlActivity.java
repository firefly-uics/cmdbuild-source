package org.cmdbuild.workflow.xpdl;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.Activity;
import org.enhydra.jxpdl.elements.ExtendedAttribute;
import org.enhydra.jxpdl.elements.ExtendedAttributes;
import org.enhydra.jxpdl.elements.ImplementationTypes;
import org.enhydra.jxpdl.elements.Performer;
import org.enhydra.jxpdl.elements.SubFlow;
import org.enhydra.jxpdl.elements.TSScript;
import org.enhydra.jxpdl.elements.TaskTypes;

public class XpdlActivity implements XpdlExtendedAttributesHolder {

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

	public boolean isScriptingType() {
		return inner.getActivityType() == XPDLConstants.ACTIVITY_TYPE_TASK_SCRIPT;
	}

	public ScriptLanguage getScriptLanguage() {
		final TSScript tsScript = getScript();
		final String mimeType = tsScript.getScriptType();
		return ScriptLanguage.of(mimeType);
	}

	public String getScriptExpression() {
		final TSScript tsScript = getScript();
		final String script = tsScript.toValue();
		return script;
	}

	public void setScriptingType(final ScriptLanguage language, final String expression) {
		final TSScript tsScript = getScript();
		tsScript.setScriptType(language.getMimeType());
		tsScript.setValue(expression);
	}

	private TSScript getScript() {
		final ImplementationTypes implementationTypes = inner.getActivityTypes().getImplementation()
				.getImplementationTypes();
		implementationTypes.setTask();
		final TaskTypes taskTypes = implementationTypes.getTask().getTaskTypes();
		taskTypes.setTaskScript();
		final TSScript tsScript = taskTypes.getTaskScript().getScript();
		return tsScript;
	}

	public void setSubProcess(final XpdlProcess subprocess) {
		final ImplementationTypes implementationTypes = inner.getActivityTypes().getImplementation()
				.getImplementationTypes();
		implementationTypes.setSubFlow();
		final SubFlow subflow = implementationTypes.getSubFlow();
		subflow.setId(subprocess.getId());
	}

	/**
	 * Sets the only performer for this activity. We are not interested in more
	 * than one performer.
	 * 
	 * @param name
	 *            of the first and only performer
	 */
	public void setPerformer(final String performerName) {
		doc.turnReadWrite();
		Performer performer = (Performer) inner.getPerformers().generateNewElement();
		performer.setValue(performerName);
		inner.getPerformers().clear();
		inner.getPerformers().add(performer);
	}

	/**
	 * List<CMActivityVariableToProcess> Returns the first performer for this
	 * activity.
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

	public XpdlExtendedAttribute getFirstExtendedAttribute(final String key) {
		final ExtendedAttribute xa = extendedAttributes.getFirstExtendedAttribute(key);
		return XpdlExtendedAttribute.newInstance(xa);
	}

	public List<XpdlExtendedAttribute> getExtendedAttributes() {
		final List<XpdlExtendedAttribute> xxas = new ArrayList<XpdlExtendedAttribute>();
		final ExtendedAttributes xattrs = inner.getExtendedAttributes();
		if (xattrs != null) {
			for (int i = 0; i < xattrs.size(); ++i) {
				final ExtendedAttribute xa = (ExtendedAttribute) xattrs.get(i);
				final XpdlExtendedAttribute xxa = XpdlExtendedAttribute.newInstance(xa);
				if (xxa != null) {
					xxas.add(xxa);
				}
			}
		}
		return xxas;
	}
}