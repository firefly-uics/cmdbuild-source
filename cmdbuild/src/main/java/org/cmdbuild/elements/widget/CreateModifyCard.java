package org.cmdbuild.elements.widget;


public class CreateModifyCard extends Widget {

	private String cql;
	private String targetClass;
	private boolean readonly;

	public String getCql() {
		return cql;
	}

	public void setCql(String cql) {
		this.cql = cql;
	}

	public String getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
}