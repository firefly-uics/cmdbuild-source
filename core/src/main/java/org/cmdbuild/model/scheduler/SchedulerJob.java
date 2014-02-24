package org.cmdbuild.model.scheduler;

import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;

public class SchedulerJob implements Storable {

	public static enum Type {
		workflow, emailService
	}

	private final Long id;

	private String code;
	private String description;
	private Map<String, String> parameters;
	private String cronExpression;
	private String detail;
	private Type type;
	private boolean running;

	public SchedulerJob() {
		this(null);
	}

	public SchedulerJob(final Long id) {
		this.id = id;
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * @deprecated use new parameters
	 */
	@Deprecated
	public Map<String, String> getLegacyParameters() {
		return parameters;
	}

	/**
	 * @deprecated use new parameters
	 */
	@Deprecated
	public void setLegacyParameters(final Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(final String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(final String detail) {
		this.detail = detail;
	}

	public Type getType() {
		return type;
	}

	public void setType(final Type type) {
		this.type = type;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(final Boolean running) {
		this.running = (running == null) ? false : running;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
