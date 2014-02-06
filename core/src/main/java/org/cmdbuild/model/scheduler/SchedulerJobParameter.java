package org.cmdbuild.model.scheduler;

import org.cmdbuild.data.store.Storable;

public class SchedulerJobParameter implements Storable {

	private final Long id;

	private String key;
	private String value;

	public SchedulerJobParameter() {
		this(null);
	}

	public SchedulerJobParameter(final Long id) {
		this.id = id;
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	};

}
