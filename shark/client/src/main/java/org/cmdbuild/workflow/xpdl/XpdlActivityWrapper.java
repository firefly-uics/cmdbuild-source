package org.cmdbuild.workflow.xpdl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.workflow.ActivityPerformer;
import org.cmdbuild.workflow.CMActivity;

public class XpdlActivityWrapper implements CMActivity {

	@Legacy("As in 1.x")
	public static final String ADMIN_START_XA = "AdminStart";

	private final XpdlActivity inner;
	private final XpdlExtendedAttributeVariableFactory variableFactory;
	private final XpdlExtendedAttributeWidgetFactory widgetFactory;

	public XpdlActivityWrapper(final XpdlActivity xpdlActivity,
			final XpdlExtendedAttributeVariableFactory variableFactory,
			final XpdlExtendedAttributeWidgetFactory widgetFactory) {
		Validate.notNull(xpdlActivity, "Wrapped object cannot be null");
		Validate.notNull(variableFactory, "Wrapped object cannot be null");
		Validate.notNull(widgetFactory, "Wrapped object cannot be null");
		this.inner = xpdlActivity;
		this.variableFactory = variableFactory;
		this.widgetFactory = widgetFactory;
	}

	@Override
	public List<ActivityPerformer> getPerformers() {
		List<ActivityPerformer> out = new ArrayList<ActivityPerformer>();
		out.add(getFirstRolePerformer());
		if (isAdminStart()) {
			out.add(ActivityPerformer.newAdminPerformer());
		}
		return out;
	}

	@Legacy("As in 1.x")
	private boolean isAdminStart() {
		return inner.hasExtendedAttributeIgnoreCase(ADMIN_START_XA);
	}

	@Override
	public String getId() {
		return inner.getId();
	}

	@Override
	public String getDescription() {
		return inner.getName();
	}

	@Override
	public String getInstructions() {
		return inner.getDescription();
	}

	@Override
	public ActivityPerformer getFirstRolePerformer() {
		return ActivityPerformer.newRolePerformer(inner.getFirstPerformer());
	}

	@Override
	public List<CMActivityVariableToProcess> getVariables() {
		final List<CMActivityVariableToProcess> vars = new ArrayList<CMActivityVariableToProcess>();
		for (XpdlExtendedAttribute xa : inner.getExtendedAttributes()) {
			final CMActivityVariableToProcess v = variableFactory.createVariable(xa);
			if (v != null) {
				vars.add(v);
			}
		}
		return vars;
	}

	@Override
	public List<CMActivityWidget> getWidgets() {
		List<CMActivityWidget> widgets = new ArrayList<CMActivityWidget>();
		for (XpdlExtendedAttribute xa : inner.getExtendedAttributes()) {
			final CMActivityWidget w = widgetFactory.createWidget(xa);
			if (w != null) {
				widgets.add(w);
			}
		}
		return widgets;
	}

}
