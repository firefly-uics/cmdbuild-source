package org.cmdbuild.portlet.configuration;

public class ButtonBarConfiguration {

	private boolean displayNotes;
	private boolean displayAttachment;
	private boolean displayHelp;
	private boolean displayWorkflowWidgets;

	public ButtonBarConfiguration() {
	}

	public boolean isDisplayAttachment() {
		return displayAttachment;
	}

	public void setDisplayAttachment(final boolean displayAttachment) {
		this.displayAttachment = displayAttachment;
	}

	public boolean isDisplayHelp() {
		return displayHelp;
	}

	public void setDisplayHelp(final boolean displayHelp) {
		this.displayHelp = displayHelp;
	}

	public boolean isDisplayNotes() {
		return displayNotes;
	}

	public void setDisplayNotes(final boolean displayNotes) {
		this.displayNotes = displayNotes;
	}

	public boolean isDisplayWorkflowWidget() {
		return displayWorkflowWidgets;
	}

	public void setDisplayWorkflowWidget(final boolean displayWorkflowWidgets) {
		this.displayWorkflowWidgets = displayWorkflowWidgets;
	}

}
