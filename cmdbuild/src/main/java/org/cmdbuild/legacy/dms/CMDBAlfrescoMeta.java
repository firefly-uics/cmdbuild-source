package org.cmdbuild.legacy.dms;

import java.io.Serializable;

import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.ResultSetRow;


public class CMDBAlfrescoMeta implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String classname;
	private int objid;
	private String category;
	private String notes;
	private String description;
	private String code;
	
	public CMDBAlfrescoMeta(String classname, int objid, String category) {
		super();
		this.classname = classname;
		this.objid = objid;
		this.category = category;
	}
	
	public CMDBAlfrescoMeta(String classname, String code, String notes, String description, int objid, String category) {
		super();
		this.classname = classname;
		this.code = code;
		this.notes = notes;
		this.description = description;
		this.objid = objid;
		this.category = category;
	}
	
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
	public int getObjid() {
		return objid;
	}
	public void setObjid(int objid) {
		this.objid = objid;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	protected CMDBAlfrescoMeta() {
		//this("",-1,"");
		this("","","","",-1,"");
	}
	
	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CMDBAlfrescoMeta copy() {
		CMDBAlfrescoMeta out = new CMDBAlfrescoMeta();
		out.classname = classname;
		out.objid = objid;
		return out;
	}

	public static CMDBAlfrescoMeta fromResultSetRow(ResultSetRow row){
		CMDBAlfrescoMeta out = new CMDBAlfrescoMeta();
		NamedValue[] nvs = row.getColumns();
		for(NamedValue nv : nvs){
			CMDBAlfProp prop = CMDBAlfProp.fromNV(nv);
			if(prop != null)
				prop.setInMeta(out, nv);
		}
		return out;
	}

}
