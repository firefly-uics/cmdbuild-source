/**
 * 
 */
package org.cmdbuild.shark.eventaudit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.cmdbuild.workflow.utils.SimpleXMLNode;

public class CmdbAttr {
	String name;
	Object value;
	public CmdbAttr(String name, Object value){
		this.name = name;
		this.value = value;
	}
	
	public boolean isEmpty() {
		if(value == null) {return true;}
		boolean out = false;
//		if(value instanceof String) { out = (value == null); } also empty string are accepted...
//		if(value instanceof Integer) { out = this.<Integer>cast()==0; }
//		if(value instanceof Long) { out = this.<Long>cast()==0l; }
//		if(value instanceof Float) { out = this.<Float>cast()==0f; }
//		if(value instanceof Double) { out = this.<Double>cast()==0d; }
//		if(value instanceof LookupType) { out = (this.<LookupType>cast()).getId() <= 0; }
//		if(value instanceof ReferenceType) { out = (this.<ReferenceType>cast()).getId() <= 0; }
		return out;
	}
//	@SuppressWarnings("unchecked")
//	private <T> T cast() { return (T)value; }
	
	public void putNode(SimpleXMLNode parent) {
		if( isEmpty() ) {return;}
		SimpleXMLNode node = parent.createChild("Attribute");
		node.put("Name", name);
		
		if(value instanceof Boolean ||
			value instanceof Integer ||
			value instanceof Long ||
			value instanceof Float ||
			value instanceof Double ||
			value instanceof String) {
			node.set(value);
		} else if(value instanceof Calendar || value instanceof Date) {
			Date date = (value instanceof Calendar) ? ((Calendar)value).getTime() : (Date)value;
			node.set(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(date));
		} else if(value instanceof LookupType) {
			LookupType lkp = (LookupType)value;
			node.put("class", "org.cmdbuild.workflow.type.LookupType")
			.createChild("Id").set(lkp.getId()).parent()
			.createChild("Description").set(lkp.getDescription()).parent()
			.createChild("Type").set(lkp.getType());
		} else if(value instanceof ReferenceType) {
			ReferenceType ref = (ReferenceType)value;
			node.put("class", "org.cmdbuild.workflow.type.ReferenceType")
			.createChild("Id").set(ref.getId()).parent()
			.createChild("Description").set(ref.getDescription()).parent()
			.createChild("IdClass").set(ref.getIdClass());
		}
	}
}