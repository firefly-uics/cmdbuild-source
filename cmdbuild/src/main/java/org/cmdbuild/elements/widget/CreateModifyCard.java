package org.cmdbuild.elements.widget;


public class CreateModifyCard extends Widget {

	private String idcardcqlselector;
	private String targetClass;
	private boolean readonly;

	public String getIdcardcqlselector() {
		return idcardcqlselector;
	}

	public void setIdcardcqlselector(String idcardcqlselector) {
		this.idcardcqlselector = idcardcqlselector;
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