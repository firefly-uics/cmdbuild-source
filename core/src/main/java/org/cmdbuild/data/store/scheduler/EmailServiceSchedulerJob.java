package org.cmdbuild.data.store.scheduler;

public class EmailServiceSchedulerJob extends SchedulerJob {

	private String emailAccount;
	private boolean notificationActive;
	private String regexFromFilter;
	private String regexSubjectFilter;
	private boolean attachmentsActive;
	private boolean workflowActive;
	private String workflowClassName;
	private String workflowFieldsMapping;
	private boolean workflowAdvanceable;
	private boolean attachmentsStorableToWorkflow;

	public EmailServiceSchedulerJob(final Long id) {
		super(id);
	}

	@Override
	public void accept(final SchedulerJobVisitor visitor) {
		visitor.visit(this);
	}

	public String getEmailAccount() {
		return emailAccount;
	}

	public void setEmailAccount(final String emailAccount) {
		this.emailAccount = emailAccount;
	}

	public boolean isNotificationActive() {
		return notificationActive;
	}

	public void setNotificationActive(final boolean notificationActive) {
		this.notificationActive = notificationActive;
	}

	public String getRegexFromFilter() {
		return regexFromFilter;
	}

	public void setRegexFromFilter(final String regexFromFilter) {
		this.regexFromFilter = regexFromFilter;
	}

	public String getRegexSubjectFilter() {
		return regexSubjectFilter;
	}

	public void setRegexSubjectFilter(final String regexSubjectFilter) {
		this.regexSubjectFilter = regexSubjectFilter;
	}

	public boolean isAttachmentsActive() {
		return attachmentsActive;
	}

	public void setAttachmentsActive(final boolean attachmentsActive) {
		this.attachmentsActive = attachmentsActive;
	}

	public boolean isWorkflowActive() {
		return workflowActive;
	}

	public void setWorkflowActive(final boolean workflowActive) {
		this.workflowActive = workflowActive;
	}

	public String getWorkflowClassName() {
		return workflowClassName;
	}

	public void setWorkflowClassName(final String workflowClassName) {
		this.workflowClassName = workflowClassName;
	}

	public String getWorkflowFieldsMapping() {
		return workflowFieldsMapping;
	}

	public void setWorkflowFieldsMapping(final String workflowFieldsMapping) {
		this.workflowFieldsMapping = workflowFieldsMapping;
	}

	public boolean isWorkflowAdvanceable() {
		return workflowAdvanceable;
	}

	public void setWorkflowAdvanceable(final boolean workflowAdvanceable) {
		this.workflowAdvanceable = workflowAdvanceable;
	}

	// TODO change name please
	public boolean isAttachmentsStorableToWorkflow() {
		return attachmentsStorableToWorkflow;
	}

	public void setAttachmentsStorableToWorkflow(final boolean attachmentsStorableToWorkflow) {
		this.attachmentsStorableToWorkflow = attachmentsStorableToWorkflow;
	}

}
