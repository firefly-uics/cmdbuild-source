package org.cmdbuild.elements.widget;

import java.util.Map;

import net.jcip.annotations.NotThreadSafe;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@NotThreadSafe
public abstract class Widget {

	protected interface WidgetAction {
		Object execute() throws Exception;
	}

	private String id; // unique inside a class only
	private String label;
	private boolean active;

	public Widget() {
		label = StringUtils.EMPTY;
		setActive(true);
	}

	public final Object executeAction(final String action, final Map<String, Object> dsVars) throws Exception {
		final WidgetAction actionCommand = getActionCommand(action, dsVars);
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
	protected WidgetAction getActionCommand(final String action, final Map<String, Object> dsVars) {
		return null;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final String getId() {
		return id;
	}

	public final void setLabel(final String label) {
		this.label = label;
	}

	public final String getLabel() {
		return label;
	}

	public final void setActive(final boolean active) {
		this.active = active;
	}

	public final boolean isActive() {
		return active;
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