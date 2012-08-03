/**
 * 
 */
package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.workflow.Constants;
import org.cmdbuild.workflow.utils.SimpleXMLNode;
import org.enhydra.jxpdl.XPDLConstants;

public enum XPDLAttributeType {
	BOOLEAN(DataType.BASIC, XPDLConstants.BASIC_TYPE_BOOLEAN),
	STRING(DataType.BASIC, XPDLConstants.BASIC_TYPE_STRING),
	DOUBLE(DataType.BASIC, XPDLConstants.BASIC_TYPE_FLOAT),
	INT(DataType.BASIC, XPDLConstants.BASIC_TYPE_INTEGER),
	DATETIME(DataType.BASIC, XPDLConstants.BASIC_TYPE_DATETIME),
	LOOKUP(DataType.DECLARED, Constants.XPDL_LOOKUP_DECLARED_TYPE),
	REFERENCE(DataType.DECLARED, Constants.XPDL_REFERENCE_DECLARED_TYPE),
	REFERENCES(DataType.DECLARED, Constants.XPDL_REFERENCE_ARRAY_DECLARED_TYPE),
	LOOKUPS(DataType.DECLARED, Constants.XPDL_LOOKUP_ARRAY_DECLARED_TYPE);
	
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
