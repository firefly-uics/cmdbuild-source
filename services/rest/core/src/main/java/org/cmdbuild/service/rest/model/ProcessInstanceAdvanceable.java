package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.PROCESS_INSTANCE_ADVANCE;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.model.adapter.ProcessInstanceAdvanceableAdapter;

@XmlRootElement(name = PROCESS_INSTANCE_ADVANCE)
@XmlJavaTypeAdapter(ProcessInstanceAdvanceableAdapter.class)
public class ProcessInstanceAdvanceable extends ProcessInstance {

	private Long activity;
	private boolean advance;

	ProcessInstanceAdvanceable() {
		// package visibility
	}

	public Long getActivity() {
		return activity;
	}

	void setActivity(final Long activityId) {
		this.activity = activityId;
	}

	public boolean isAdvance() {
		return advance;
	}

	void setAdvance(final boolean advance) {
		this.advance = advance;
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
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(super.doHashCode()) //
				.append(activity) //
				.append(advance) //
				.toHashCode();
	}

}
