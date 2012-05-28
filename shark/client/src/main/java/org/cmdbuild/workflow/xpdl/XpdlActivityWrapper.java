package org.cmdbuild.workflow.xpdl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.workflow.ActivityPerformer;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;
import org.cmdbuild.workflow.xpdl.XpdlActivity.XpdlVariableSuffix;

public class XpdlActivityWrapper implements CMActivity {

	@Legacy("As in 1.x")
	public static final String ADMIN_START_XA = "adminStart";
	public static final String VARIABLE_PREFIX = "VariableToProcess_";

	private final XpdlActivity inner;

	public XpdlActivityWrapper(final XpdlActivity xpdlActivity) {
		Validate.notNull(xpdlActivity, "Wrapped object cannot be null");
		this.inner = xpdlActivity;
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
		return (inner.getFirstExtendedAttribute(ADMIN_START_XA) != null);
	}

	@Override
	public String getName() {
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
			final CMActivityVariableToProcess v = newVariableToProcess(xa);
			if (v != null) {
				vars.add(v);
			}
		}
		return vars;
	}


	private CMActivityVariableToProcess newVariableToProcess(XpdlExtendedAttribute xa) {
		final String key = xa.getKey();
		final String name = xa.getValue();
		if (key == null || name == null) {
			return null;
		}
		if (isVariableKey(key)) {
			final Type type = extractType(key);
			return new CMActivityVariableToProcess(name, type);
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
