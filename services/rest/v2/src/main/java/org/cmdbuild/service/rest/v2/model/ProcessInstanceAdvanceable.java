package org.cmdbuild.service.rest.v2.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.v2.model.adapter.ProcessInstanceAdvanceableAdapter;

@XmlRootElement
@XmlJavaTypeAdapter(ProcessInstanceAdvanceableAdapter.class)
public class ProcessInstanceAdvanceable extends ProcessInstance {

	private String activity;
	private boolean advance;
	private Map<String, Object> widgets;

	ProcessInstanceAdvanceable() {
		// package visibility
	}

	public String getActivity() {
		return activity;
	}

	void setActivity(final String activityId) {
		this.activity = activityId;
	}

	public boolean isAdvance() {
		return advance;
	}

	void setAdvance(final boolean advance) {
		this.advance = advance;
	}

	public Map<String, Object> getWidgets() {
		return widgets;
	}

	void setWidgets(final Map<String, Object> widgets) {
		this.widgets = widgets;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessInstanceAdvanceable)) {
			return false;
		}

		final ProcessInstanceAdvanceable other = ProcessInstanceAdvanceable.class.cast(obj);
		return super.doEquals(obj) && new EqualsBuilder() //
				.append(this.activity, other.activity) //
				.append(this.advance, other.advance) //
				.append(this.widgets, other.widgets) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(super.doHashCode()) //
				.append(activity) //
				.append(advance) //
				.append(widgets) //
				.toHashCode();
	}

}
