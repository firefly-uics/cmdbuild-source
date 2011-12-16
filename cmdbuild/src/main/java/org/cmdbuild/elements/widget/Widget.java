package org.cmdbuild.elements.widget;

import net.jcip.annotations.NotThreadSafe;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@NotThreadSafe
public abstract class Widget {

	private String id; // unique inside a class only
	private String label;
	private boolean active;

	public Widget() {
		label = StringUtils.EMPTY;
		setActive(true);
	}

	public void setId(final String id) {
		this.id = id;
	}

	public final String getId() {
		return id;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Widget) {
			final Widget other = (Widget) obj;
			return this.getId().equals(other.getId());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	/*
	 * HACK to serialize type information in lists
	 */

	public void setType(final String type) {
	}

	public String getType() {
		final String fullName = this.getClass().getName();
		return fullName.substring(fullName.lastIndexOf("."));
	}
}