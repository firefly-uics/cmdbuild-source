/**
 * 
 */
package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.workflow.utils.SimpleXMLNode;

public enum XPDLAttributeType {
	BOOLEAN(DataType.BASIC,"BOOLEAN"),
	STRING(DataType.BASIC,"STRING"),
	DOUBLE(DataType.BASIC,"FLOAT"),
	INT(DataType.BASIC,"INTEGER"),
	DATETIME(DataType.BASIC,"DATETIME"),
	LOOKUP(DataType.DECLARED,"Lookup"),
	REFERENCE(DataType.DECLARED,"Reference"),
	REFERENCES(DataType.DECLARED, "References"),
	LOOKUPS(DataType.DECLARED, "Lookups");
	
	DataType type;
	String xpdl;
	private XPDLAttributeType(DataType type, String xpdl){
		this.type = type;
		this.xpdl = xpdl;
	}
	
	public void putSimpleXML( SimpleXMLNode parent ) {
		parent.createChild("DataType")
		.createChild(type.xpdlType)
		.put(type.param, xpdl);
	}
	
	public static XPDLAttributeType fromCmdbuildType( AttributeType type ) {
		if (type == AttributeType.BOOLEAN) {
			return BOOLEAN;
		} else if (type == AttributeType.CHAR
				|| type == AttributeType.STRING
				|| type == AttributeType.TEXT
				|| type == AttributeType.INET) {
			return STRING;
		} else if (type == AttributeType.DATE
				|| type == AttributeType.TIMESTAMP
				|| type == AttributeType.TIME) {
			return DATETIME;
		} else if (type == AttributeType.DECIMAL
				|| type == AttributeType.DOUBLE) {
			return DOUBLE;
		} else if (type == AttributeType.INTEGER
				|| type == AttributeType.REGCLASS) {
			return INT;
		} else if (type == AttributeType.LOOKUP) {
			return LOOKUP;
		} else if (type == AttributeType.REFERENCE) {
			return REFERENCE;
		}
		return null;
	}
	
	public enum DataType {
		BASIC("BasicType","Type"),
		DECLARED("DeclaredType","Id");
		
		String xpdlType;
		String param;
		private DataType(String xpdlType,String param){
			this.xpdlType = xpdlType;
			this.param = param;
		}
	}
}
