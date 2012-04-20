package org.cmdbuild.workflow.type;

import java.io.Serializable;

import org.cmdbuild.common.annotations.Legacy;

@Legacy("Kept for backward compatibility")
public class ReferenceType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int id;
	int idClass;
	String description;
	
	public ReferenceType(){
		id = -1;
		idClass = -1;
		description = "";
	}
	public ReferenceType(int id, int idClass, String description) {
		super();
		this.id = id;
		this.idClass = idClass;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdClass() {
		return idClass;
	}

	public void setIdClass(int idClass) {
		this.idClass = idClass;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean checkValidity() {
		return id > 0;
	}

	@Override
	public String toString(){
			return "ReferenceType[idclass: "+this.idClass+" id: "+this.id+" description: "+this.description+"]";
	}
}
