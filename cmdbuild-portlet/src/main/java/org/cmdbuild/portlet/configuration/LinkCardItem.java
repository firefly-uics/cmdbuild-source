package org.cmdbuild.portlet.configuration;

public class LinkCardItem {

	private String classname;
	private int singleSelect;
	private int noSelect;
	private int required;
	private String filter;
	private String identifier;
	private String label;

	public LinkCardItem() {
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(final String classname) {
		this.classname = classname;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(final String filter) {
		this.filter = filter;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public int getNoSelect() {
		return noSelect;
	}

	public void setNoSelect(final int noSelect) {
		this.noSelect = noSelect;
	}

	public int getRequired() {
		return required;
	}

	public void setRequired(final int required) {
		this.required = required;
	}

	public int getSingleSelect() {
		return singleSelect;
	}

	public void setSingleSelect(final int singleSelect) {
		this.singleSelect = singleSelect;
	}
}
