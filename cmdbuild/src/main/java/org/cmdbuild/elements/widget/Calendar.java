package org.cmdbuild.elements.widget;


public class Calendar extends Widget {

	private String targetClass;
	private String startDate;
	private String endDate;
	private String eventTitle;
	private String filter;

	public String getTargetClass() {
		return this.targetClass;
	}

	public void setTargetClass(final String targetClass) {
		this.targetClass = targetClass;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(final String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(final String endDate) {
		this.endDate = endDate;
	}

	public String getEventTitle() {
		return eventTitle;
	}

	public void setEventTitle(final String eventTitle) {
		this.eventTitle = eventTitle;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(final String filter) {
		this.filter = filter;
	}
}
