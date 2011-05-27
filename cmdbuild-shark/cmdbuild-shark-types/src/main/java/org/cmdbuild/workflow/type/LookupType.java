package org.cmdbuild.workflow.type;

import java.io.Serializable;

public class LookupType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int id;
	String type;
	String description;
	String code;
	
	public LookupType() {
		id = -1;
		type = "";
		description = "";
		code="";
	}
	public LookupType(int id, String type, String description, String code) {
		super();
		this.id = id;
		this.type = type;
		this.description = description;
		this.code=code;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public boolean checkValidity() {
		return id > 0;
	}
	
	@Override
	public String toString(){
			return "LookupType[id: "+this.id+" code: "+this.code+" description: "+this.description+"]";
	}
}
