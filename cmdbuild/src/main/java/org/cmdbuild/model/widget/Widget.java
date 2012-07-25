package org.cmdbuild.model.widget;

import java.util.Map;

import net.jcip.annotations.NotThreadSafe;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMActivity.CMActivityWidget;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@NotThreadSafe
public abstract class Widget implements CMActivityWidget {

	protected interface WidgetAction {
		Object execute() throws Exception;
	}

	private String id; // unique inside a class only
	private String label;
	private boolean active;
	private boolean alwaysenabled;

	public Widget() {
		label = StringUtils.EMPTY;
		setActive(true);
	}

	@Override
	public final Object executeAction(final String action, final Map<String, Object> params, final Map<String, Object> dsVars) throws Exception {
		final WidgetAction actionCommand = getActionCommand(action, params, dsVars);
		if (actionCommand != null) {
			return actionCommand.execute();
		}
		final String error = String.format("Action not defined for widget %s", getClass().getCanonicalName());
		throw new UnsupportedOperationException(error);
	}

	/**
	 * Returns the WidgetAction object for the action by that name. If no
	 * action matches, then it should return null.
	 * 
	 * @param action (can be null)
	 * @return a widget action or null
	 */
	protected WidgetAction getActionCommand(final String action, final Map<String, Object> params, final Map<String, Object> dsVars) {
		return null;
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output) throws Exception {
	}

	@Override
	public void advance(final CMActivityInstance activityInstance) {
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public final String getLabel() {
		return label;
	}

	public final void setActive(final boolean active) {
		this.active = active;
	}

	public final boolean isActive() {
		return active;
	}

	public final void setAlwaysenabled(final boolean alwaysenabled) {
		this.alwaysenabled = alwaysenabled;
	}

	@Override
	public final boolean isAlwaysenabled() {
		return alwaysenabled;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj != null && obj instanceof Widget) {
			final Widget other = (Widget) obj;
			return this.getId().equals(other.getId());
		} else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return getId().hashCode();
	}

	/*
	 * HACK to serialize type information in lists
	 */

	public final void setType(final String type) {
	}

	public final String getType() {
		final String fullName = this.getClass().getName();
		return fullName.substring(fullName.lastIndexOf("."));
	}
}